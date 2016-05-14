package com.bop.net;


import com.bop.algorithm.GraphSearch;
import com.bop.graph.*;

import java.util.concurrent.ExecutionException;

/**
 * Created by liuchun on 2016/5/7.
 */
public class Test {
    //static GraphSearch search = new GraphSearch();

    public static void main(String[] args) {
    	
        //pressureTest();
    	simpleTest();
        //long id1 = 2099495348L;
        //long id2 = 2094437628L;
        //test(id1, id2);
    }

    public static void pressureTest(){
        long id1 = 2126125555L;
        long id2 = 2153635508L;  // massive paper has cited this paper
        // paperId, paperId
        long t = test(id1, id2);

        id1 = 2175015405L;
        id2 = 2121939561L;
        // authorId, authorId
        t = test(id1, id2);
    }
    

    public static void simpleTest(){
    	long id1 = 2179036997L;
        long id2 = 2152770371L;
        //long id2 = 2125838338L;
        long t = 0;
        // test [Id, Id]
        t += test(id1, id2);

        /*
        id1 = 2179036997L;
        id2 = 2131087226L;
        // test [Id, AuId]
        t += test(id1, id2);

        id1 = 2131087226L;
        id2 = 2179036997L;
        // test [AuId, Id]
        t += test(id1, id2);

        id1 = 2268927867L;
        id2 = 2179036997L;
        // test [AuId, AuId]
        t += test(id1, id2);

        double avr = t/4.0;
        System.out.println("average time: " + avr);*/
        
        System.out.println("test over\n");
    }

    public static long test(long id1, long id2){
        long start_time, end_time, elapse_time;

        GraphSearch search = new GraphSearch();
        
        start_time = System.currentTimeMillis();
        //System.out.println("start time: " + start_time);     
        try{
            String json = GraphPath.getPathString(search.search(id1,id2));

            end_time = System.currentTimeMillis();
            //System.out.println("end time: " + end_time);
            //System.out.println(json);

            elapse_time = end_time - start_time;
            System.out.println("elapse time: " + elapse_time);

            return elapse_time;
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }

        return 0;
    }
    
}
