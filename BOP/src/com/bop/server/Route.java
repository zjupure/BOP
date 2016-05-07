package com.bop.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/route")
public class Route {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayHello() {
		return "Hello World!";
	}

	@GET
	@Path("/id1={id1}&id2={id2}")
	@Produces("text/plain;charset=UTF-8")
	public String handleGetRequest(@PathParam("id1") String id1, @PathParam("id2") String id2) {
		return "id1 = " + id1 + " id2 = " + id2;
	}
}