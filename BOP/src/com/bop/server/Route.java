package com.bop.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.bop.algorithm.GraphSearch;
import com.bop.graph.*;

@Path("/route")
public class Route {

	

	static Cache_sql cache=new Cache_sql();

	@GET
	@Produces("application/json;charset=UTF-8")
	public String handleGet(@QueryParam("id1") long id1, @QueryParam("id2") long id2) {
		System.out.println("id1 = " + id1 + " id2 = " + id2);
		GraphSearch search = new GraphSearch();
		List<GraphPath> ans= new ArrayList<GraphPath>();
		String result = null;

		try {
			result = cache.get(id1,id2);
			
			if(result == ""){
				ans=new ArrayList<GraphPath>();
				ans=search.search(id1, id2);
				Cache_sql.push(id1,id2, ans);
				result =GraphPath.getPathString(ans);
			}
			
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