package com.bop.server;
import java.util.*;
import com.bop.graph.*;


public class Cache {
	List<Pair> cachePool=new ArrayList<Pair>();
	final long cacheSize=3768;
	HashMap<Pair, List<GraphPath> >  exist=new HashMap<Pair, List<GraphPath> >();

	public class Pair {
		long a,b;
		public Pair(long a, long b)
		{
			this.a=a;this.b=b;
		}
	}
	public Cache (){
	}
	public void push(long id1, long id2, List<GraphPath> path){
		//Query queryNow= new Query(id1, id2, path);
		Pair idPair=new Pair(id1,id2);

		if(exist.get(idPair)==null){
			cachePool.add(0,idPair);
			exist.put(idPair,path);
		}//if idPair doesn't exist, add to the pool;

		if(cachePool.size()>cacheSize){
			exist.remove(cachePool.get(cachePool.size()-1));
			cachePool.remove(cachePool.size()-1);
		}//removing the last element in the list;
	}
	public List<GraphPath> get(long id1, long id2){
		Pair idPair=new Pair(id1,id2);
		List<GraphPath> ans = new ArrayList<GraphPath>();

		if(exist.get(idPair)==null)
			return null;
		ans=exist.get(idPair);
		cachePool.remove(idPair);
		cachePool.add(0,idPair);
		return ans;
	}
}
