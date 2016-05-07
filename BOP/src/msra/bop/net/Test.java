package msra.bop.net;

import msra.bop.graph.GraphNode;
import msra.bop.graph.GraphPath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by liuchun on 2016/5/7.
 */
public class Test {
	
    public static void main(String[] args) {
        long id1 = 2027532957;
        long id2 = 2014935324;
        ExecutorService threadPool = Executors.newCachedThreadPool();
        Future<GraphNode> resp1 = threadPool.submit(new LoadCall(id1));
        Future<GraphNode> resp2 = threadPool.submit(new LoadCall(id2));
        /** TODO do some other things, then get the future results */
        List<GraphPath>  paths = new ArrayList<GraphPath>();
        try{
            GraphNode startNode = resp1.get();
            GraphNode endNode = resp2.get();
            // deal with graph node
            if(startNode.isAdjacent(endNode)){
                GraphPath path = new GraphPath(startNode.getNodeId(), endNode.getNodeId());
                paths.add(path);
            }
            // go on the next
            System.out.println(GraphPath.getPathString(paths));

        }catch (ExecutionException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("Test Over");
    }

    

    /** load data in the background */
    public static class LoadCall implements Callable<GraphNode>{
        public static final int UNSPECIFIC = -1;
        public static final int GET_PAPER = 0;
        public static final int GET_AUTHOR = 1;
        public static final int GET_CITE = 2;

        private long id;
        private int type;

        public LoadCall(long id){
            this.id = id;
            type = UNSPECIFIC;
        }

        public LoadCall(long id, int type){
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

            return graphNode;
        }
    }
}
