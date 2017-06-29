package utils

import java.net.URI

import io.dropwizard.Configuration as DropWizardConfiguration

import javax.ws.rs.*
import javax.ws.rs.core.Response
import javax.annotation.*

import javax.ws.rs.core.UriBuilder

import org.hibernate.validator.constraints.NotEmpty

import javax.validation.constraints.NotNull

import io.dropwizard.client.JerseyClientConfiguration

import io.dropwizard.db.DataSourceFactory

import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.jwt.consumer.JwtContext

import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims

import org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256

import org.jose4j.keys.HmacKey

import io.dropwizard.auth.Authenticator
import io.dropwizard.auth.Authorizer
import io.dropwizard.auth.UnauthorizedHandler

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter

import java.util.Optional

import org.skife.jdbi.v2.DBI

import io.dropwizard.cli.ConfiguredCommand

import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser

import io.dropwizard.setup.Bootstrap

import com.codahale.metrics.MetricRegistry

import org.skife.jdbi.v2.util.*

class Config() : DropWizardConfiguration() {

    var viewRendererConfiguration: Map<String, Map<String, String>>? = null

    var jerseyClient = JerseyClientConfiguration()

    @NotNull
    lateinit var database: DataSourceFactory

    @NotNull
    lateinit var local: LocalConfig

}

class LocalConfig() {
    @NotEmpty lateinit var orcidBaseUrl: String
    @NotEmpty lateinit var orcidApiUrl: String
    @NotEmpty lateinit var orcidOauthRedirectUri: String
    @NotEmpty lateinit var orcidClientId: String
    @NotEmpty lateinit var orcidClientSecret: String

    fun orcidAuthUrlFor(scope: String, state: String = ""): URI =
        UriBuilder.fromPath("${orcidBaseUrl}/oauth/authorize")
            .queryParam("client_id", orcidClientId)
            .queryParam("response_type", "code")
            .queryParam("scope", scope)
            .queryParam("redirect_uri", orcidOauthRedirectUri)
            .queryParam("state", state)
            .build()

    val orcidAuthUrlForSignIn: URI by lazy { orcidAuthUrlFor("/authenticate", "sign-in") }

    val orcidAuthUrlForAuthorize: URI by lazy {
        orcidAuthUrlFor("/person/update /activities/update /read-limited", "authorize")
    }

    fun orcidActionUrlFor(orcid: String, action: String) =
        "${orcidApiUrl}/${orcid}/${action}"

    @NotEmpty lateinit var jwtTokenSecret: String
    @NotEmpty lateinit var jwtCookieName: String

}

data class User(val orcid: String) : java.security.Principal {
    override fun getName(): String = orcid
}

object jwt {

    fun tokenFor(config: LocalConfig, value: String): String {
        var claims = JwtClaims()
        claims.setSubject(value)
        var jws = JsonWebSignature()
        jws.setPayload(claims.toJson())
        jws.setAlgorithmHeaderValue(HMAC_SHA256)
        jws.setKey(HmacKey(config.jwtTokenSecret.toByteArray()))
        return jws.getCompactSerialization()
    }

    class LocalAuthenticator : Authenticator<JwtContext, User> {
        override fun authenticate(context: JwtContext): Optional<User> {
            val subject: String? = context.getJwtClaims().getSubject()
            return subject?.let { Optional.of(User(it)) } ?: Optional.empty()
        }
    }

    class LocalAuthorizer(val dao: db.AuthorizedOrcidDao) : Authorizer<User> {
        override fun authorize(user: User, role: String): Boolean {
            if (role != "ADMIN") { return false }
            return dao.countForId(user.orcid) == 1
        }
    }

    class LocalUnauthorizedHandler : UnauthorizedHandler {
        override fun buildResponse(prefix: String, realm: String): Response {
            val uri = UriBuilder.fromPath("/error-quick")
                .queryParam("msg", "Not authorized to view that page")
                .build()
            return Response
                .seeOther(uri)
                .build()
        }
    }

    fun newFilter(config: LocalConfig, dao: db.AuthorizedOrcidDao): JwtAuthFilter<User> {
        val secret = config.jwtTokenSecret.toByteArray()

        val consumer = JwtConsumerBuilder()
            .setAllowedClockSkewInSeconds(30)
            .setRequireSubject()
            .setVerificationKey(HmacKey(secret))
            .setRelaxVerificationKeyValidation()
            .build()

        return JwtAuthFilter.Builder<User>()
            .setJwtConsumer(consumer)
            .setCookieName(config.jwtCookieName)
            .setRealm("realm")
            .setPrefix("Bearer")
            .setAuthenticator(LocalAuthenticator())
            .setAuthorizer(LocalAuthorizer(dao))
            .setUnauthorizedHandler(LocalUnauthorizedHandler())
            .buildAuthFilter()
    }

}

class AddAuthorizedCommand : ConfiguredCommand<Config>("addOrcid", "Add authorized ORCID") {

    override fun configure(subparser: Subparser) {
        super.configure(subparser)
        subparser.addArgument("-o", "--orcid")
            .dest("orcid")
            .type(String::class.java)
            .required(true)
            .help("The ORCID to add")
    }

    override fun run(bootstrap: Bootstrap<Config>, namespace: Namespace, config: Config) {
        val dbi = DBI(config.database.build(MetricRegistry(), "h2"))
        val h = dbi.open()
        h.execute("INSERT INTO authorized_orcids (id) values (?)", namespace.getString("orcid"))
        h.close()

    }
}