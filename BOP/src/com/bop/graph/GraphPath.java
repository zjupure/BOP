package com.bop.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by liuchun on 2016/5/2.
 */
public class GraphPath {
    // node ids
    private List<Long> nodeIds = new ArrayList<Long>();

    public GraphPath(Long... ids){
        for(Long id : ids){
            nodeIds.add(id);
        }
    }

    /**
     * judge the nodes in path containing duplicated nodes or not
     * @return
     */
    public boolean isDistinct(){
        HashSet<Long> set = new HashSet<Long>(nodeIds);

        return set.size() == nodeIds.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for(int i = 0; i < nodeIds.size(); i++){
            if(i > 0){
                sb.append(",");
            }
            sb.append(nodeIds.get(i));
        }
        sb.append(']');

        return sb.toString();
    }

    /**
     * change a list of paths to json string
     * @param paths
     * @return
     */
    public static String getPathString(List<GraphPath> paths){
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for(int i = 0; i < paths.size(); i++){
            if(i > 0){
                sb.append(",");
            }
            GraphPath path = paths.get(i);
            sb.append(path.toString());
        }
        sb.append(']');

        return sb.toString();
    }

    /**
     * filter the paths that containg duplicated nodes
     * @param paths
     * @return
     */
    public static List<GraphPath> filterPaths(List<GraphPath> paths){
        List<GraphPath> valids = new ArrayList<GraphPath>();
        for(GraphPath path : paths){
            if(path.isDistinct()){
                valids.add(path);
            }
        }
        return valids;
    }
}
