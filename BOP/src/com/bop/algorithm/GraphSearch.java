package com.bop.algorithm;

import com.bop.cache.CacheUtil;
import com.bop.graph.*;
import com.bop.net.AcademyClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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


    /**
     * route search algorithm, public to outer
     * @param id1
     * @param id2
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public String search(long id1, long id2) throws InterruptedException, ExecutionException{
        /** first try get cache from LruCache */
        String result = CacheUtil.get(id1, id2);
        if(result != null){
            // hit the cache, return
            return result;
        }
        /** TODO second try get cache from local database */

    static {
        // preload academyClient
        AcademyClient.preLoad();
    }

    public String search(long id1, long id2) throws InterruptedException, ExecutionException{


        /** miss the cache, try to get data from Academy API */
        Future<GraphNode> rsp1 = threadpool.submit(new RequestCall(id1));
        Future<GraphNode> rsp2 = threadpool.submit(new RequestCall(id2));
        // block in get method
        startNode = rsp1.get();
        if(startNode == null){
            return "[]";
        }
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
        if(endNode == null){
            return "[]";
        }
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
        //
        List<GraphPath> paths = new ArrayList<GraphPath>();

        paths.addAll(one_hop);
        paths.addAll(two_hop);
        paths.addAll(three_hop);

        //System.out.println("valid path number: " + paths.size());
        result = GraphPath.getPathString(paths);
        
        //writeToFile(id1, id2, paths.size(), result);
        /** add the cache to LruCache */
        CacheUtil.put(id1, id2, result);
        /** add the cache to local database, run in the sub thread */

        return result;
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
        /** TODO write two hop algorithm here */
        List<Long> hops = startNode.getMiddleNode(endNode);
        for(Long id : hops){
            GraphPath path = new GraphPath(startNode.getNodeId(), id, endNode.getNodeId());
            paths.add(path);
        }

        /** [Id, Id] has to find the Id--->Id--->Id paths */
        if(startNode.getNodeType() != GraphNode.PAPER_NODE || endNode.getNodeType() != GraphNode.PAPER_NODE){
            return paths;
        }

        RefNode refNode = getRefNode();
        List<Long> middles = refNode.getMiddleNode(endNode);
        for(Long id : middles){
            GraphPath path = new GraphPath(startNode.getNodeId(), id, endNode.getNodeId());
            paths.add(path);
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
        /** filter the cycle paths */
        return GraphPath.filterPaths(paths);
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
     * try to get the RefNode, must be locked
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
     * try to get the citeNode, must be locked
     * @return
     */
    private CiteNode getCiteNode(){
        // must lock to assure thread safe
        if(citeNode == null){
            synchronized (this){
                if(citeNode == null){
                    try{
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
     * wirte results to log file
     * @param id1
     * @param id2
     * @param size
     * @param result
     */
    private void writeToFile(long id1, long id2, int size, String result){
        File dir = new File("results");
        if(!dir.exists()){
            dir.mkdir();
        }
    	String filename = id1 + "_" + id2 + ".txt";
    	File mFile = new File(dir, filename);
    	FileWriter writer = null;
    	try{
    		writer = new FileWriter(mFile);
    		writer.write("[" + id1 + "," + id2 + "]\n");
    		writer.write("path numbers: " + size + "\n");
    		writer.write(result + "\n");
    	}catch(IOException e){
    		e.printStackTrace();
    	}finally{
    		if(writer != null){
    			try{
    				writer.close();
    			}catch(IOException e){
    				e.printStackTrace();
    			}		
    		}
    	}
    	
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
