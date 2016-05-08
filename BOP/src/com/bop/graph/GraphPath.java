package com.bop.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/2.
 */
public class GraphPath {
    // node ids
    private List<Long> nodeIds = new ArrayList<Long>();

    public GraphPath(){
        nodeIds = new ArrayList<Long>();
    }

    public GraphPath(Long... ids){
        for(Long id : ids){
            nodeIds.add(id);
        }
    }

    public void push(Long nextId){
        nodeIds.add(nextId);
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
}
