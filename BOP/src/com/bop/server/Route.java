package com.bop.server;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.bop.algorithm.GraphSearch;

@Path("/route")
public class Route {
	@GET
	@Produces("text/plain")
	public String handleGet(@QueryParam("id1") long id1, @QueryParam("id2") long id2) {
		System.out.println("id1 = " + id1 + " id2 = " + id2);
		GraphSearch search = new GraphSearch();
		String result = null;

		try {
			result = search.search(id1, id2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(result);
		return result;
	}
}