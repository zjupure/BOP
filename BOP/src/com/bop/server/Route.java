package com.bop.server;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import com.bop.algorithm.GraphSearch;

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
		System.out.println("id1 = " + id1 + " id2 = " + id2);
		GraphSearch search = new GraphSearch();
		String result = null;
		try {
			result = search.search(Long.parseLong(id1), Long.parseLong(id2));
		} catch (NumberFormatException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}
}