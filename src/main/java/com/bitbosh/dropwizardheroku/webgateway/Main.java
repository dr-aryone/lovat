package com.bitbosh.dropwizardheroku.webgateway;

import java.net.URISyntaxException;
import java.util.EnumSet;

import javax.script.ScriptEngineManager;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.client.Client;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;

import com.bitbosh.dropwizardheroku.webgateway.api.NashornController;
import com.bitbosh.dropwizardheroku.webgateway.api.WebGatewayResource;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import jdk.nashorn.api.scripting.NashornScriptEngine;

public class Main extends Application<ApplicationConfiguration> {

	public static void main(String[] args) throws Exception {
		new Main().run(args);
	}

	@Override
	public void run(ApplicationConfiguration configuration, Environment environment) throws URISyntaxException {

		// Create a DBIFactory to build instances of Dao classes for each
		// Resource
		// in the application.
		final DBIFactory factory = createDbiFactory();

		// The database configuration details are read from the DataSourcFactory
		// within the
		// MainConfiguration class.
		final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
		final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).using(environment).build("client");
		final NashornScriptEngine nashorn = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
		final NashornController react = new NashornController(nashorn);
		
		// Register each Resource with jersey and pass in the Dao so that it can
		// interact with the database.
		JerseyEnvironment jerseyEnvironment = environment.jersey();
		jerseyEnvironment.register(new WebGatewayResource(jdbi, client, react));
		
		// Enable CORS headers
	    final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

	    // Configure CORS parameters
	    cors.setInitParameter("allowedOrigins", "*");
	    cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
	    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

	    // Add URL mapping
	    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
	}

	private DBIFactory createDbiFactory() {
		return new DBIFactory();
	}

	@Override
	public void initialize(Bootstrap<ApplicationConfiguration> configuration) {
		configuration.addBundle(new AssetsBundle("/assets", "/assets"));
		configuration.addBundle(new ViewBundle());		
	}
}