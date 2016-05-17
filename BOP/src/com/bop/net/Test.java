package com.bop.net;


import com.bop.algorithm.GraphSearch;

import java.util.concurrent.ExecutionException;

/**
 * Created by liuchun on 2016/5/7.
 */
public class Test {

    public static void main(String[] args) {
    	
        pressureTest();
    	//simpleTest();
        //officialTest();
        //long id1 = 2027775552L;
        //long id2 = 2085680244L;
        //test(id1, id2);
        //officialTest();
        //System.out.println("all test over");
    }

    public static void pressureTest(){
        long id1 = 2126125555L;
        long id2 = 2153635508L;  // massive paper has cited this paper
        // paperId, paperId
        long t = test(id1, id2);

        id1 = 2292217923L;
        id2 = 2100837269L;  // massive paper around 12w has cited this paper

        //id1 = 2175015405L;
        //id2 = 2121939561L;
        // authorId, authorId
        test(id1, id2);

        System.out.println("pressure test over\n");
    }

    /**
     * official 5 group test case
     */
    public static void officialTest(){
        long id1 = 2251253715L;
        long id2 = 2180737804L;
        long t = test(id1, id2);

        id1 = 2147152072L;
        id2 = 189831743L;
        t += test(id1, id2);

        id1 = 2332023333L;
        id2 = 2310280492L;
        t += test(id1,id2);

        id1 = 2332023333L;
        id2 = 57898110L;
        t += test(id1, id2);

        id1 = 57898110L;
        id2 = 2014261844L;
        t += test(id1, id2);

        double avr = t/5.0;
        System.out.println("average time: " + avr);

        System.out.println("official test over\n");
    }
    

    public static void simpleTest(){
    	long id1 = 2179036997L;
        long id2 = 2152770371L;
        //long id2 = 2125838338L;
        long t = 0;
        // test [Id, Id]
        t += test(id1, id2);


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
        System.out.println("average time: " + avr);
        
        System.out.println("simple test over\n");
    }

    public static long test(long id1, long id2){
        long start_time, end_time, elapse_time;

        GraphSearch search = new GraphSearch();
        
        start_time = System.currentTimeMillis();
        //System.out.println("start time: " + start_time);     
        try{
            String json = search.search(id1,id2);

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
