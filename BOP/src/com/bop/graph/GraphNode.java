package com.bop.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/2.
 */
public class GraphNode {
    // paper node, hold all his neighbour node id
    public static final int PAPER_NODE = 0;
    // author node, hold all his neighbour paper node information and affiliation id
    public static final int AUTHOR_NODE = 1;
    // study field node
    public static final int FIELD_NODE = 2;
    // journal node
    public static final int JOURNAL_NODE = 3;
    // conference node
    public static final int CONFERENCE_NODE = 4;
    // author_affiliation node
    public static final int AUTHOR_AFFILIATION_NODE = 5;
    // forward ref relationship  paper A--->paper B, then paper B is a REF_NODE
    public static final int REF_NODE = 6;
    // reverse cite relationship paper A1--->paper B, paper A2--->paper B, then paper B
    // should be transform to a CITE_NODE, get all his previous node
    public static final int CITE_NODE = 7; // reverse cite relationship

    protected long nodeId;
    protected int nodeType;

    public GraphNode(long id, int type){
        nodeId = id;
        nodeType = type;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isAdjacent(GraphNode graphNode){
        return false;
    }

    public List<Long> getMiddleNode(GraphNode graphNode) throws IllegalArgumentException{
        return new ArrayList<Long>();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if(obj instanceof GraphNode){
            GraphNode other = (GraphNode)obj;
            if(nodeId == other.nodeId && nodeType == other.nodeType){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return (int) (nodeId & 0x7fffffff);
    }
}
