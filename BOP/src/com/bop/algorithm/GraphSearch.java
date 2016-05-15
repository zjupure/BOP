package com.bop.algorithm;

import com.bop.cache.CacheUtil;
import com.bop.graph.*;
import com.bop.net.AcademyClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    // save for author request
    HashMap<Long, Future<GraphNode>> rspMap = new HashMap<Long, Future<GraphNode>>();
    /**
     * route search algorithm, public to outer
     * @param id1
     * @param id2
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public String search(long id1, long id2) throws InterruptedException, ExecutionException{
    	/** TODO first try get cache from LruCache or DbCache, all operation should be implements in CacheUtil */
        /*
        String result = CacheUtil.get(id1, id2);
        if(result != null){
            // hit the cache, return
            return result;
        }*/

        /** miss the cache, try to get data from Academy API */
        Future<GraphNode> rsp1 = threadpool.submit(new RequestCall(id1));
        Future<GraphNode> rsp2 = threadpool.submit(new RequestCall(id2));
        // block in get method
        startNode = rsp1.get();
        if(startNode == null){
            return "[]";
        }
        /** get forward reference, out link */
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
        /** get reverse reference, in link */
        if(endNode instanceof PaperNode){
            //
            rspEnd = threadpool.submit(new RequestCall(id2, RequestCall.GET_CITE));
        }

        /** [Id, AuId] or [AuId, Id], must get the full author info for the paperId */
        if(startNode.getNodeType() != endNode.getNodeType()){
            if(startNode instanceof PaperNode){
                for(AuthorNode author : ((PaperNode) startNode).getAuthors()){
                    Future<GraphNode> rsp = threadpool.submit(new RequestCall(author.getNodeId(), RequestCall.GET_AFFI));
                    rspMap.put(author.getNodeId(), rsp);
                }
            }
            if(endNode instanceof PaperNode){
                for(AuthorNode author : ((PaperNode) endNode).getAuthors()){
                    Future<GraphNode> rsp = threadpool.submit(new RequestCall(author.getNodeId(), RequestCall.GET_AFFI));
                    rspMap.put(author.getNodeId(), rsp);
                }
            }
        }

        /** implement route algorithms */
        Future<List<GraphPath>> h1 = threadpool.submit(new RouteCall(RouteCall.ONE_HOP));
        Future<List<GraphPath>> h2 = threadpool.submit(new RouteCall(RouteCall.TWO_HOP));
        Future<List<GraphPath>> h3 = threadpool.submit(new RouteCall(RouteCall.THREE_HOP));

        List<GraphPath> one_hop = h1.get();
        List<GraphPath> two_hop = h2.get();
        List<GraphPath> three_hop = h3.get();
        //
        List<GraphPath> paths = new ArrayList<GraphPath>();
        /** collect the results */
        paths.addAll(one_hop);
        paths.addAll(two_hop);
        paths.addAll(three_hop);

        System.out.println("valid path number: " + paths.size());
        String result = GraphPath.getPathString(paths);
        
        //writeToFile(startNode, endNode, paths.size(), result);
        /** TODO keep the result to Cache */
        //CacheUtil.put(id1, id2, result);

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
        for(AuthorNode author : startNode.getAuthors()){
            Future<GraphNode> rsp = rspMap.get(author.getNodeId());
            try{
                AuthorNode authorNode = (AuthorNode)rsp.get();

                List<Long> middles = authorNode.getCommonAffi(endNode);
                for(Long id : middles){
                    GraphPath path = new GraphPath(startNode.getNodeId(), author.getNodeId(),
                            id, endNode.getNodeId());
                    paths.add(path);
                }

            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (ExecutionException e){
                e.printStackTrace();
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
            /** transform to two hop [paper, paper] */
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
        for(AuthorNode author : endNode.getAuthors()){
            Future<GraphNode> rsp = rspMap.get(author.getNodeId());
            try{
                AuthorNode authorNode = (AuthorNode)rsp.get();

                List<Long> middles = startNode.getCommonAffi(authorNode);
                for(Long id : middles){
                    GraphPath path = new GraphPath(startNode.getNodeId(), id,
                            author.getNodeId(), endNode.getNodeId());
                    paths.add(path);
                }

            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (ExecutionException e){
                e.printStackTrace();
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
     * @param startNode
     * @param endNode
     * @param size
     * @param result
     */
    private void writeToFile(GraphNode startNode, GraphNode endNode, int size, String result){
        File dir = new File("results");
        if(!dir.exists()){
            dir.mkdir();
        }
        long id1 = startNode.getNodeId(), id2 = endNode.getNodeId();
    	String filename = id1 + "_" + id2 + ".txt";
        String title = "[" + getTypeString(startNode) + "," + getTypeString(endNode) + "]\n";
    	File mFile = new File(dir, filename);
    	FileWriter writer = null;
    	try{
    		writer = new FileWriter(mFile);
            writer.write(title);
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
     * get String for NodeType
     * @param graphNode
     * @return
     */
    private String getTypeString(GraphNode graphNode){
        String type = "OtherId";
        if(graphNode.getNodeType() == GraphNode.PAPER_NODE){
            type = "paperId";
        }else if(graphNode.getNodeType() == GraphNode.AUTHOR_NODE){
            type = "AuId";
        }
        return type;
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
        public static final int GET_AFFI = 4;

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
                case GET_AFFI:
                    graphNode = AcademyClient.getAffiInfo(id);
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
