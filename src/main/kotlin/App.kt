
import io.dropwizard.Application as DropWizardApplication

import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

import io.dropwizard.assets.AssetsBundle

import io.dropwizard.migrations.MigrationsBundle

import io.dropwizard.views.ViewBundle

import io.dropwizard.client.JerseyClientBuilder

import io.dropwizard.db.DataSourceFactory

import io.dropwizard.jersey.sessions.SessionFactoryProvider

import org.eclipse.jetty.server.session.SessionHandler

import io.dropwizard.jdbi.DBIFactory

import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature

public class App : DropWizardApplication<utils.Config>() {

    companion object {
        @JvmStatic public fun main(args: Array<String>) {
            App().run(*args)
        }
    }

    override fun run(configuration: utils.Config, environment: Environment) {
        val client = JerseyClientBuilder(environment)
            .using(configuration.jerseyClient)
            .build(getName())

        val mainController = web.MainController()

        val factory = DBIFactory()
        val dbi = factory.build(environment, configuration.database, "h2")

        val authorizedOrcidDao = dbi.onDemand(db.AuthorizedOrcidDao::class.java)
        val tokenRecordDao = dbi.onDemand(db.TokenRecordDao::class.java)

        val objectMapperFactory = environment.getObjectMapper()
            .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)

        mainController.client = client
        mainController.dbi = dbi
        mainController.localConfig = configuration.local
        mainController.tokenRecordDao = tokenRecordDao

        environment.jersey().register(mainController)
        environment.jersey().register(web.AdminController.build(mainController))

        environment.jersey().register(SessionFactoryProvider::class.java)
        environment.jersey().register(web.NotAuthorizedWrapper())

        environment.servlets().setSessionHandler(SessionHandler())

        environment.jersey().register(AuthDynamicFeature(utils.jwt.newFilter(configuration.local, authorizedOrcidDao)))

        environment.jersey().register(AuthValueFactoryProvider.Binder(utils.User::class.java))
        environment.jersey().register(RolesAllowedDynamicFeature::class.java)

    }

    override fun initialize(bootstrap: Bootstrap<utils.Config>) {

        bootstrap.addBundle(AssetsBundle("/assets/", "/assets/"))

        bootstrap.addBundle(object : ViewBundle<utils.Config>() {
            override fun getViewConfiguration(config: utils.Config): Map<String, Map<String, String>> {
                return config.viewRendererConfiguration ?: emptyMap()
            }
        })

        bootstrap.addBundle(object : MigrationsBundle<utils.Config>() {
            override fun getDataSourceFactory(config: utils.Config): DataSourceFactory {
                return config.database
            }

            override fun getMigrationsFileName(): String = "migrations.yml"
        })

        bootstrap.addCommand(utils.AddAuthorizedCommand())

    }

    override fun getName(): String {
        return "ORCED"
    }

}

