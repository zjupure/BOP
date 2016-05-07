package msra.bop.graph;

/**
 * Created by liuchun on 2016/5/7.
 */
public class GraphNode {
    public static final int PAPER_NODE = 0;
    public static final int AUTHOR_NODE = 1;
    public static final int FIELD_NODE = 2;
    public static final int JOURNAL_NODE = 3;
    public static final int CONFERENCE_NODE = 4;
    public static final int AUTHOR_AFFILIATION_NODE = 5;
    public static final int CITE_NODE = 6; // reverse cite relationship

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
}
