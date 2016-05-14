package com.bop.server;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.sql.*;

import com.bop.algorithm.GraphSearch;
import com.bop.graph.*;


public class Cache_sql {
	static List<Pair> cachePool=new ArrayList<Pair>();
	final static long cacheSize=100000;
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/Cachepool";

	//  Database credentials
	static final String USER = "root";
	static final String PASS = "tangyiqi-123";

	public static class Pair {
		long a,b;
		public Pair(long a, long b)
		{
			this.a=a;this.b=b;
		}
	}
	public Cache_sql (){
	}
	public static void push(long id1, long id2, List<GraphPath> path){
		//Query queryNow= new Query(id1, id2, path);
		Pair idPair=new Pair(id1,id2);
			cachePool.add(0,idPair);
			Connection conn = null;
			Statement stmt = null;
			try{
			    //STEP 2: Register JDBC driver
			    Class.forName("com.mysql.jdbc.Driver");

			    //STEP 3: Open a connection
			    System.out.println("Connecting to database...");
			    conn = DriverManager.getConnection(DB_URL,USER,PASS);
			  //STEP 4: Execute a query
			      System.out.println("Creating statement...");
			      stmt = conn.createStatement();
			      String sql;
			     // sql = " delete from Query where id1="+id1+" and id2="+id2;
			      //sql = " insert into Query values("+id1+","+id2+",'fasdifas');";
			      sql = " insert into Query values("+id1+","+id2+",'"+GraphPath.getPathString(path)+"');";
			      int rs = stmt.executeUpdate(sql);
			      

			      //STEP 6: Clean-up environment
			      stmt.close();
			      conn.close();
			      //while(true);
			   }catch(SQLException se){
			      //Handle errors for JDBC
			      se.printStackTrace();
			   }catch(Exception e){
			      //Handle errors for Class.forName
			      e.printStackTrace();
			   }finally{
			      //finally block used to close resources
			      try{
			         if(stmt!=null)
			            stmt.close();
			      }catch(SQLException se2){
			      }// nothing we can do
			      try{
			         if(conn!=null)
			            conn.close();
			      }catch(SQLException se){
			         se.printStackTrace();
			      }//end finally try
			   }//end try
		//if idPair doesn't exist, add to the pool;

		if(cachePool.size()>cacheSize){
			conn = null;
			stmt = null;
			try{
			    //STEP 2: Register JDBC driver
			    Class.forName("com.mysql.jdbc.Driver");

			    //STEP 3: Open a connection
			    System.out.println("Connecting to database...");
			    conn = DriverManager.getConnection(DB_URL,USER,PASS);
			  //STEP 4: Execute a query
			      System.out.println("Creating statement...");
			      stmt = conn.createStatement();
			      String sql;
			      sql = "delete from Query where id1="+cachePool.get(cachePool.size()-1).a+" and id2="+cachePool.get(cachePool.size()-1).b;
			      int rs = stmt.executeUpdate(sql);

			      //STEP 6: Clean-up environment
			      stmt.close();
			      conn.close();
			   }catch(SQLException se){
			      //Handle errors for JDBC
			      se.printStackTrace();
			   }catch(Exception e){
			      //Handle errors for Class.forName
			      e.printStackTrace();
			   }finally{
			      //finally block used to close resources
			      try{
			         if(stmt!=null)
			            stmt.close();
			      }catch(SQLException se2){
			      }// nothing we can do
			      try{
			         if(conn!=null)
			            conn.close();
			      }catch(SQLException se){
			         se.printStackTrace();
			      }//end finally try
			   }//end try
			cachePool.remove(cachePool.size()-1);
		}//removing the last element in the list;
	}
	public String get(long id1, long id2){
		Pair idPair=new Pair(id1,id2);
		String ans = new String();
		//sql = " select path from Query where id1="+id1+" and id2="+id2;
		ans= "";

		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			//STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = " select path from Query where id1="+id1+" and id2="+id2;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
				ans=rs.getString("path");
			//STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
		//finally block used to close resources
		try{
			if(stmt!=null)
			stmt.close();
		}catch(SQLException se2){
			}// nothing we can do
		try{
			if(conn!=null)
				conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		cachePool.remove(idPair);
		cachePool.add(0,idPair);
		return ans;
	}
	/*
	public static void main(String args[]) throws InterruptedException, ExecutionException{
		
		GraphSearch search = new GraphSearch();
		List<GraphPath> ans= new ArrayList<GraphPath>();
		long  id1=2126125555L;
		long id2=2153635508L;
		ans=search.search(id1, id2);
		push(1,2,ans);
	}*/
}
