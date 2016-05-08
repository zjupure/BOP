package com.bop.net;


import com.bop.algorithm.GraphSearch;

import java.util.concurrent.ExecutionException;

/**
 * Created by liuchun on 2016/5/7.
 */
public class Test {
    public static void main(String[] args) {

        long id1 = 2179036997L;
        long id2 = 2152770371L;
        long t = 0;
        // test [Id, Id]
        t += test(id1, id2);

        id1 = 2179036997L;
        id2 = 2131087226L;
        // test [Id, AuId]
        t += test(id1, id2);

        id1 = 2131087226L;
        id2 = 2129379104L;
        // test [AuId, Id]
        t += test(id1, id2);

        id1 = 2268927867L;
        id2 = 2179036997L;
        // test [AuId, AuId]
        t += test(id1, id2);

        long avr = t/4;
        System.out.println("average time: " + avr);
    }

    public static long test(long id1, long id2){
        long start_time, end_time, elapse_time;

        start_time = System.currentTimeMillis();
        System.out.println("start time: " + start_time);
        GraphSearch search = new GraphSearch();
        try{
            String json = search.search(id1, id2);

            end_time = System.currentTimeMillis();
            System.out.println("end time: " + end_time);
            System.out.println(json);

            elapse_time = end_time - start_time;
            System.out.println(elapse_time);

            return elapse_time;
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }

        System.out.println("test over");
        return 0;
    }
}
