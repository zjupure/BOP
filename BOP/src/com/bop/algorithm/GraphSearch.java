package com.bop.algorithm;

import com.bop.graph.GraphNode;
import com.bop.graph.GraphPath;
import com.bop.net.AcademyClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by liuchun on 2016/5/8.
 */
public class GraphSearch {
    GraphNode startNode;
    GraphNode endNode;
    GraphNode citeNode = null;
    //
    ExecutorService threadpool = Executors.newCachedThreadPool();
    Future<GraphNode> rsp = null;

    public String search(long id1, long id2) throws InterruptedException, ExecutionException{

        Future<GraphNode> rsp1 = threadpool.submit(new RequestCall(id1));
        Future<GraphNode> rsp2 = threadpool.submit(new RequestCall(id2));
        // block in get method
        startNode = rsp1.get();
        endNode = rsp2.get();
        if(endNode.getNodeType() == GraphNode.PAPER_NODE){
            rsp = threadpool.submit(new RequestCall(id2, RequestCall.GET_CITE));
        }
        //
        Future<List<GraphPath>> h1 = threadpool.submit(new RouteCall(RouteCall.ONE_HOP));
        Future<List<GraphPath>> h2 = threadpool.submit(new RouteCall(RouteCall.TWO_HOP));
        //Future<List<GraphPath>> h3 = threadpool.submit(new RouteCall(RouteCall.THREE_HOP));

        List<GraphPath> one_hop = h1.get();
        List<GraphPath> two_hop = h2.get();
        //List<GraphPath> three_hop = h3.get();

        one_hop.addAll(two_hop);
        //one_hop.addAll(three_hop);

        return GraphPath.getPathString(one_hop);
    }

    /**
     * get one hop path between startNode and endNode
     * @param startNode
     * @param endNode
     * @return
     */
    public List<GraphPath> getOneHop(GraphNode startNode, GraphNode endNode){
        List<GraphPath> paths = new ArrayList<GraphPath>();

        if(startNode.isAdjacent(endNode)){
            GraphPath path = new GraphPath(startNode.getNodeId(), endNode.getNodeId());
            paths.add(path);
        }

        return paths;
    }

    /**
     * get two hop paths between startNode and endNode
     * @param startNode
     * @param endNode
     * @return
     */
    public List<GraphPath> getTwoHop(GraphNode startNode, GraphNode endNode){
        List<GraphPath> paths = new ArrayList<GraphPath>();

        List<Long> hops = startNode.getMiddleNode(endNode);
        for(Long id : hops){
            GraphPath path = new GraphPath(startNode.getNodeId(), id, endNode.getNodeId());
            paths.add(path);
        }

        // endNode is paperNode, then get all papers that reference to it
        if(endNode.getNodeType() != GraphNode.PAPER_NODE){
            return paths;
        }

        if(citeNode == null){
            try{
                citeNode = rsp.get();
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (ExecutionException e){
                e.printStackTrace();
            }
        }

        if(citeNode != null){
            List<Long> refs = startNode.getMiddleNode(citeNode);
            for(long id : refs){
                GraphPath path = new GraphPath(startNode.getNodeId(), id, endNode.getNodeId());
                paths.add(path);
            }
        }

        return paths;
    }

    /**
     * get three hop paths between startNode and endNode
     * @param startNode
     * @param endNode
     * @return
     */
    public List<GraphPath> getThreeHop(GraphNode startNode, GraphNode endNode){
        List<GraphPath> paths = new ArrayList<GraphPath>();

        /** TODO write three hop algorithm here */

        return paths;
    }

    /**
     * Route algorithm in the background
     */
    public class RouteCall implements Callable<List<GraphPath>>{
        public static final int ONE_HOP = 0;
        public static final int TWO_HOP = 1;
        public static final int THREE_HOP = 2;

        private int routeType;

        public RouteCall(int routeType){
            this.routeType = routeType;
        }

        public List<GraphPath> call() throws Exception {
            List<GraphPath> paths = null;
            switch (routeType){
                case ONE_HOP:
                    paths = getOneHop(startNode, endNode);
                    break;
                case TWO_HOP:
                    paths = getTwoHop(startNode, endNode);
                    break;
                case THREE_HOP:
                    paths = getThreeHop(startNode, endNode);
                    break;
                default:
                    break;
            }

            //System.out.println("route call is over");

            return paths;
        }
    }


    /**
     * fetch data from Academy API, and wrapper them into graphNode
     * run in the background
     */
    public static class RequestCall implements Callable<GraphNode>{
        public static final int UNSPECIFIC = -1;
        public static final int GET_PAPER = 0;
        public static final int GET_AUTHOR = 1;
        public static final int GET_CITE = 2;

        private long id;
        private int type;

        public RequestCall(long id){
            this(id, UNSPECIFIC);
        }

        public RequestCall(long id, int type){
            this.id = id;
            this.type = type;
        }

        public GraphNode call() throws Exception {
            GraphNode graphNode = null;
            switch (type){
                case GET_PAPER:
                    graphNode = AcademyClient.getPaperInfo(id);
                    break;
                case GET_AUTHOR:
                    graphNode = AcademyClient.getAuthorInfo(id);
                    break;
                case GET_CITE:
                    graphNode = AcademyClient.getCiteInfo(id);
                    break;
                default:
                    graphNode = AcademyClient.getIdInfo(id);
                    break;
            }

            //System.out.println("request call is over");

            return graphNode;
        }
    }
}
