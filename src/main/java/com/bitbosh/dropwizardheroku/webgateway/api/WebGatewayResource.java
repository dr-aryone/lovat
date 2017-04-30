package com.bitbosh.dropwizardheroku.webgateway.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.skife.jdbi.v2.DBI;

import com.bitbosh.dropwizardheroku.webgateway.core.Microservice;
import com.bitbosh.dropwizardheroku.webgateway.repository.WebGatewayDao;

@Path("/")
public class WebGatewayResource {

	private final WebGatewayDao webGatewayDao;
	private final Client client;
	private React react;

	public WebGatewayResource(DBI jdbi, Client client, React react) {
		this.webGatewayDao = jdbi.onDemand(WebGatewayDao.class);
		this.client = client;
		this.react = react;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String index() {
		
		// Get events json data from Events microservice	
        ApiResponse events = getEventsJsonData();
        
        // Render the Events component and pass in props
        List <Object> eventsProps = (List<Object>) events.getList();
        String webApplicationHtml = this.react.renderComponent(Microservice.kEventsUiComponentRenderServerFunction, eventsProps);
        
        // Return the full application index.html after templating
        return webApplicationHtml;
	}

	private ApiResponse getEventsJsonData() {
		WebTarget webTarget = this.client.target(Microservice.kEventsApiEndpointEvents);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);		
        Response response = invocationBuilder.get();
        return response.readEntity(ApiResponse.class);		
	}

	@GET
	@Path("/events")
	@Produces(MediaType.APPLICATION_JSON)
	public ApiResponse getEvents() {		
		//WebTarget webTarget = client.target("https://dropwizardheroku-event-service.herokuapp.com/v1/api/events");
		String eventsMicroserviceURL = "http://localhost:8081/v1/api/events";
		WebTarget webTarget = this.client.target(eventsMicroserviceURL);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);		
        Response response = invocationBuilder.get();
        ApiResponse apiResponse = response.readEntity(ApiResponse.class);
        return apiResponse;
	}
}
