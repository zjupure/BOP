package com.bop.algorithm;

import com.bop.graph.*;
import com.bop.net.AcademyClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by liuchun on 2016/5/8.
 */
public class GraphSearch {
    GraphNode startNode;  // <startNode, endNode> is a pair
    GraphNode endNode;    // <endNode, citeNode> is a pair
    GraphNode refNode = null;
    GraphNode citeNode = null;
    //
    static ExecutorService threadpool = Executors.newCachedThreadPool();

    Future<GraphNode> rspStart = null;
    Future<GraphNode> rspEnd = null;

    static {
        // preload academyClient
        AcademyClient.getClient();
    }

    public String search(long id1, long id2) throws InterruptedException, ExecutionException{

        refNode = null;
        citeNode = null;
        Future<GraphNode> rsp1 = threadpool.submit(new RequestCall(id1));
        Future<GraphNode> rsp2 = threadpool.submit(new RequestCall(id2));
        // block in get method
        startNode = rsp1.get();
        if(startNode instanceof PaperNode){
            //
            rspStart = threadpool.submit(new Callable<GraphNode>() {
                public GraphNode call() throws Exception {
                    List<GraphNode> refs = ((PaperNode)startNode).getRefs();
                    if(refs.size() <= 0){
                        return new RefNode(startNode.getNodeId());
                    }
                    List<Long> rids = new ArrayList<Long>();
                    for(GraphNode ref : refs){
                        rids.add(ref.getNodeId());
                    }

                    return AcademyClient.getPaperRefInfo(startNode.getNodeId(), rids);
                }
            });
        }

        endNode = rsp2.get();
        if(endNode instanceof PaperNode){
            //
            rspEnd = threadpool.submit(new RequestCall(id2, RequestCall.GET_CITE));
        }
        //
        Future<List<GraphPath>> h1 = threadpool.submit(new RouteCall(RouteCall.ONE_HOP));
        Future<List<GraphPath>> h2 = threadpool.submit(new RouteCall(RouteCall.TWO_HOP));
        Future<List<GraphPath>> h3 = threadpool.submit(new RouteCall(RouteCall.THREE_HOP));

        List<GraphPath> one_hop = h1.get();
        List<GraphPath> two_hop = h2.get();
        List<GraphPath> three_hop = h3.get();

        one_hop.addAll(two_hop);
        one_hop.addAll(three_hop);

        //List<GraphPath> paths = one_hop;
        List<GraphPath> paths = GraphPath.filterPaths(one_hop);
        System.out.println("valid path number: " + paths.size());

        return GraphPath.getPathString(paths);
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

        CiteNode citeNode = getCiteNode();

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
        List<GraphPath> paths = null;
        /** TODO write three hop algorithm here */
        if(startNode instanceof PaperNode){
            if(endNode instanceof PaperNode){
                paths = getThreeHopInternal((PaperNode)startNode,(PaperNode)endNode);
            }else if(endNode instanceof AuthorNode){
                paths = getThreeHopInternal((PaperNode)startNode, (AuthorNode)endNode);
            }
        }else if(startNode instanceof AuthorNode){
            if(endNode instanceof PaperNode){
                paths = getThreeHopInternal((AuthorNode)startNode, (PaperNode)endNode);
            }else if(endNode instanceof AuthorNode){
                paths = getThreeHopInternal((AuthorNode)startNode, (AuthorNode)endNode);
            }
        }

        return paths;
    }

    /**
     * PaperNode connect to PaperNode with 3 hops
     * 1. PaperNode ----> PaperNode ----> SomeNode  ----> PaperNode
     * 2. PaperNode ----> SomeNode  ----> PaperNode ----> PaperNode
     * @param startNode
     * @param endNode
     * @return
     */
    private List<GraphPath> getThreeHopInternal(PaperNode startNode, PaperNode endNode){
    	List<GraphPath> paths = new ArrayList<GraphPath>();

    	/**
    	 * Condition one: the next hop is paper, so get the paper ref info first
    	 */
    	RefNode refNode = getRefNode();
        CiteNode citeNode = getCiteNode();
        for(PaperNode paper : refNode.getRefPapers()){
            List<Long> middles = paper.getMiddleNode(endNode);

            middles.addAll(paper.getMiddleNode(citeNode));
            // generate the path
            for(Long id : middles){
                GraphPath path = new GraphPath(startNode.getNodeId(), paper.getNodeId(),
                        id, endNode.getNodeId());
                paths.add(path);
            }
        }
    	/**
    	 * Condition two, the next is not paper, so can get common info for two paperNode
    	 */
    	for(PaperNode paper : citeNode.getCitePapers()){
    		List<Long> middles = startNode.getMiddleNode(paper);
    		// generate the path
    		for(long id : middles){
    			GraphPath path = new GraphPath(startNode.getNodeId(), id, paper.getNodeId(),
    					endNode.getNodeId());
    			paths.add(path);
    		}
    	}
    	
    	return paths;
    }
    
    /**
     * PaperNode connect to AuthorNode with 3 hops
     * 1. PaperNode ---> SomeNode ---> PaperNode ---> AuthorNode
     * 2. PaperNode ---> AuthorNode ---> AffiliationNode ---> AuthorNode
     * @param startNode
     * @param endNode
     * @return
     */
    private List<GraphPath> getThreeHopInternal(PaperNode startNode, AuthorNode endNode){
    	List<GraphPath> paths = new ArrayList<GraphPath>();
    	
    	/**
    	 * Condition one, the second hop is PaperNode
    	 */
        RefNode refNode = getRefNode();
    	for(PaperNode paper : endNode.getPapers()){
    		/** transform to two hop [paper, paper] */
    		List<Long> middles = startNode.getMiddleNode(paper);

    		middles.addAll(refNode.getMiddleNode(paper));
    		
    		// generate the path
    		for(Long id : middles){
    			GraphPath path = new GraphPath(startNode.getNodeId(), id, paper.getNodeId(),
    					endNode.getNodeId());
    			paths.add(path);
    		}
    	}
        /**
         * Condition two, the second hop is affiliationNode
         */
        long authorId, affiId;
        for(AuthorNode author : startNode.getAuthors()){

            authorId = author.getNodeId();
            /** attention!!! /
            /** TODO the middle author id cannot be endNode author?? */
            if(authorId == endNode.getNodeId()){
                continue; // pass
            }

            if(author.getAffiliations().size() > 0){
                GraphNode affiNode = author.getAffiliations().get(0);
                affiId = affiNode.getNodeId();
                if(endNode.containAffiliation(affiId)){
                    // exist a path
                    GraphPath path = new GraphPath(startNode.getNodeId(), authorId, affiId, endNode.getNodeId());
                    paths.add(path);
                }
            }
        }
    	
    	return paths;
    }

    /**
     * AuthorNode connect to PaperNode with 3 hops
     * 1. AuthorNode ---> PaperNode ---> SomeNode ---> PaperNode
     * 2. AuthorNode ---> AffiliationNode ---> AuthorNode ---> PaperNode
     * @param startNode
     * @param endNode
     * @return
     */
    private List<GraphPath> getThreeHopInternal(AuthorNode startNode, PaperNode endNode){
        List<GraphPath> paths = new ArrayList<GraphPath>();

        /**
         * Condition one, the next hop is PaperNode
         */
        CiteNode citeNode = getCiteNode();
        for(PaperNode paper : startNode.getPapers()){
            List<Long> middles = paper.getMiddleNode(endNode);

            middles.addAll(paper.getMiddleNode(citeNode));
            // generate the path
            for(Long id : middles){
                GraphPath path = new GraphPath(startNode.getNodeId(), paper.getNodeId(), id,
                        endNode.getNodeId());
                paths.add(path);
            }
        }
        /**
         * Condition two, the next hop is affiliationNode
         */
        long authorId, affiId;
        for(AuthorNode author : endNode.getAuthors()){

            authorId = author.getNodeId();
            /** attention!!! */
            /** TODO the middle author id cannot be startNode author?? */
            if(authorId == startNode.getNodeId()){
                continue; // pass
            }

            if(author.getAffiliations().size() > 0){
                GraphNode affiNode = author.getAffiliations().get(0);
                affiId = affiNode.getNodeId();
                if(startNode.containAffiliation(affiId)){
                    // exist a path
                    GraphPath path = new GraphPath(startNode.getNodeId(), affiId, authorId, endNode.getNodeId());
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    /**
     * AuthorNode connect to AuthorNode with 3 hops
     * AuthorNode ---> PaperNode ---> PaperNode ---> AuthorNode
     * @param startNode
     * @param endNode
     * @return
     */
    private List<GraphPath> getThreeHopInternal(AuthorNode startNode, AuthorNode endNode){
        List<GraphPath> paths = new ArrayList<GraphPath>();

        /**
         * Only one condition: the middle hops must be paper and paper
         */
        for(PaperNode startPaper : startNode.getPapers()){
            for(PaperNode endPaper : endNode.getPapers()){
                if(startPaper.isAdjacent(endPaper)){
                    // exist a path
                    GraphPath path = new GraphPath(startNode.getNodeId(), startPaper.getNodeId(),
                            endPaper.getNodeId(), endNode.getNodeId());
                    paths.add(path);
                }
            }
        }

        return paths;
    }

    /**
     * try to get RefNode, must be lock
     * @return
     */
    private RefNode getRefNode(){
        // must lock to assure thread safe
        if(refNode == null){
            synchronized (this){
                if(refNode == null){
                    try{
                        refNode = rspStart.get();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }catch (ExecutionException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        return (RefNode)refNode;
    }

    /**
     * try to get the citeNode
     */
    private CiteNode getCiteNode(){
        // must lock to assure thread safe
        if(citeNode == null){
            synchronized (this){
                if(citeNode == null){
                    try{

                        if(rspEnd == null){
                            System.out.println("this is null point rspEnd");
                        }

                        citeNode = rspEnd.get();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }catch (ExecutionException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        return (CiteNode)citeNode;
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
        public static final int GET_REF = 2;
        public static final int GET_CITE = 3;

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
