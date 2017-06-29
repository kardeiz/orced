package web

import io.dropwizard.auth.Auth

import io.dropwizard.jersey.sessions.Flash as DropwizardFlash
import io.dropwizard.jersey.sessions.Session

import io.dropwizard.views.View

import java.net.URI
import java.util.Optional

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.annotation.*
import javax.annotation.security.RolesAllowed

import javax.ws.rs.*
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.Form
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.NewCookie
import javax.ws.rs.core.UriBuilder
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

import org.hibernate.validator.constraints.NotEmpty

import org.skife.jdbi.v2.DBI

import utils.User

data class Flash(val key: String, val value: String)

data class Flashes(val inner: List<Flash>) {
    companion object {
        fun single(flash: Flash) = Flashes(listOf((flash)))
        fun single(key: String, value: String) = single(Flash(key, value))
    }
}

@Path("")
open class MainController() {

    @Context lateinit var request: HttpServletRequest
    @Context lateinit var response: HttpServletResponse

    lateinit var client: Client
    lateinit var dbi: DBI
    lateinit var localConfig: utils.LocalConfig
    lateinit var tokenRecordDao: db.TokenRecordDao

    @GET @Path("/")
    fun index(
        @Session flashes: DropwizardFlash<Flashes>,
        @Auth user: Optional<User>): View {
        return object : View("/templates/home.ftl") {
            val ctx = PageContext(localConfig).apply {
                this.flash = flashes.get().orElse(null)
                this.user = user.orElse(null)
            }
        }
    }

    @GET @Path("/auth/handle-orcid-code")
    fun handleOrcidCode(
        @NotEmpty @QueryParam("code") code: String,
        @QueryParam("state") state: String,
        @Session flashes: DropwizardFlash<Flashes>): Response {

        val url = "${localConfig.orcidBaseUrl}/oauth/token"

        var form = Form()
        form.param("client_id", localConfig.orcidClientId)
        form.param("client_secret", localConfig.orcidClientSecret)
        form.param("grant_type", "authorization_code")
        form.param("code", code)
        form.param("redirect_uri", localConfig.orcidOauthRedirectUri)

        val response = client.target(url)
            .request()
            .header(HttpHeaders.ACCEPT, "application/json")
            .buildPost(Entity.form(form))
            .invoke()

        val record = response.readEntity(db.TokenRecord::class.java)

        val out = if (state == "authorize") {
            tokenRecordDao.insertOrUpdate(record)
            flashes.set(Flashes.single("success", "Thank you for your response"))
            Response.seeOther(URI("/")).build()
        } else {
            val token = utils.jwt.tokenFor(localConfig, record.orcid)
            val cookie = NewCookie(localConfig.jwtCookieName, token, "/", null, null, NewCookie.DEFAULT_MAX_AGE, false)
            flashes.set(Flashes.single("success", "Signed in"))
            Response.seeOther(URI("/")).cookie(cookie).build()
        }

        return out
    }

    @GET @Path("/error-quick")
    fun errorQuick(
        @Session flashes: DropwizardFlash<Flashes>,
        @QueryParam("msg") msg: String,
        @DefaultValue("/") @QueryParam("to") to: String): Response {
        flashes.set(Flashes.single("danger", msg))
        return Response.seeOther(URI(to)).build()
    }

    @GET @Path("/sign-out")
    fun signOut(
        @Session flashes: DropwizardFlash<Flashes>): Response {
        val cookie = NewCookie(localConfig.jwtCookieName, null, "/", null, null, 0, false)
        flashes.set(Flashes.single("success", "Signed out"))
        return Response.seeOther(URI("/")).cookie(cookie).build()
    }

}

@RolesAllowed("ADMIN")
@Path("/admin")
class AdminController : MainController() {
    companion object {
        fun build(t: MainController): AdminController =
            AdminController().apply {
                this.client = t.client
                this.dbi = t.dbi
                this.localConfig = t.localConfig
                this.tokenRecordDao = t.tokenRecordDao
            }
    }

    @GET @Path("/token-records")
    fun tokenRecords(@Session flashes: DropwizardFlash<Flashes>, @Auth user: User): View {
        return object : View("/templates/token-records.ftl") {
            val ctx = PageContext(localConfig).apply {
                this.flash = flashes.get().orElse(null)
                this.user = user
            }
            val tokenRecords = tokenRecordDao.all()
        }
    }

    @GET @Path("/{orcid}/employment/new")
    fun employmentNew(
        @Session flashes: DropwizardFlash<Flashes>,
        @PathParam("orcid") orcid: String,
        @Auth user: User): View {
        return object : View("/templates/employment-new.ftl") {
            val ctx = PageContext(localConfig).apply {
                this.flash = flashes.get().orElse(null)
                this.user = user
            }
            val orcid = orcid
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{orcid}/employment/create")
    fun employmentCreate(
        params: MultivaluedMap<String, String>,
        @Session flashes: DropwizardFlash<Flashes>,
        @PathParam("orcid") orcid: String,
        @Auth user: User): Response {

        val out = try {
            val employment = forms.employment.buildEmployment(params)

            val tokenRecord = tokenRecordDao.findByOrcid(orcid) ?: throw Throwable("No token record for that ORCID")

            val response = client.target(localConfig.orcidActionUrlFor(orcid, "employment"))
                .request()
                .accept("application/vnd.orcid+xml")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenRecord.accessToken}")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.orcid+xml")
                .buildPost(Entity.entity(employment, "application/vnd.orcid+xml"))
                .invoke()

            if (response.getStatus() != 201) {
                throw Throwable("Could not POST employment")
            }

            flashes.set(Flashes.single("success", "Employment added"))
            Response.seeOther(URI("/admin/token-records")).build()
        } catch (e: Throwable) {
            // throw e
            val msg = e.getLocalizedMessage()
            val out = object : View("/templates/employment-new.ftl") {
                val ctx = PageContext(localConfig).apply {
                    this.flash = Flashes.single("danger", msg)
                    this.user = user
                }
                val orcid = orcid
                val params = params
            }
            Response.status(200).entity(out).build()
        }
        return out
    }

    @GET @Path("/{orcid}/work/new")
    fun workNew(
        @Session flashes: DropwizardFlash<Flashes>,
        @PathParam("orcid") orcid: String,
        @Auth user: User): View {

        return object : View("/templates/work-new.ftl") {
            val ctx = PageContext(localConfig).apply {
                this.flash = flashes.get().orElse(null)
                this.user = user
            }
            val orcid = orcid
            val workTypeValues = org.orcid.jaxb.model.record_v2.WorkType.values()
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{orcid}/work/create")
    fun workCreate(
        params: MultivaluedMap<String, String>,
        @Session flashes: DropwizardFlash<Flashes>,
        @PathParam("orcid") orcid: String,
        @Auth user: User): Response {

        val out = try {
            val work = forms.work.buildWork(params)

            val tokenRecord = tokenRecordDao.findByOrcid(orcid) ?: throw Throwable("No token record for that ORCID")

            val response = client.target(localConfig.orcidActionUrlFor(orcid, "work"))
                .request()
                .accept("application/vnd.orcid+xml")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenRecord.accessToken}")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.orcid+xml")
                .buildPost(Entity.entity(work, "application/vnd.orcid+xml"))
                .invoke()

            if (response.getStatus() != 201) {
                throw Throwable("Could not POST work")
            }

            flashes.set(Flashes.single("success", "Work added"))
            Response.seeOther(URI("/admin/token-records")).build()
        } catch (e: Throwable) {
            // throw e
            val msg = e.getLocalizedMessage()
            val out = object : View("/templates/work-new.ftl") {
                val ctx = PageContext(localConfig).apply {
                    this.flash = Flashes.single("danger", msg)
                    this.user = user
                }
                val orcid = orcid
                val workTypeValues = org.orcid.jaxb.model.record_v2.WorkType.values()
                val params = params
            }
            Response.status(200).entity(out).build()
        }
        return out
    }

}

class PageContext(config: utils.LocalConfig) {
    var user: User? = null
    var flash: Flashes? = null
    val orcidAuthUrlForAuthorize = config.orcidAuthUrlForAuthorize
    val orcidAuthUrlForSignIn = config.orcidAuthUrlForSignIn
    val thisYear by lazy { org.joda.time.DateTime.now().year().get().toString() }
}

@Provider
class NotAuthorizedWrapper : ExceptionMapper<ForbiddenException> {
    override fun toResponse(ex: ForbiddenException): Response {
        val uri = UriBuilder.fromPath("/error-quick")
            .queryParam("msg", ex.getLocalizedMessage())
            .build()

        return Response.seeOther(uri).build()
    }
}