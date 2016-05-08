package com.bop.graph;

import com.bop.json.PaperEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/8.
 * Reverse references
 * the paper will cite all the paper in List<PaperNode>
 */
public class RefNode extends GraphNode{
    List<PaperNode> refPapers;

    public RefNode(long id){
        super(id, GraphNode.REF_NODE);
        refPapers = new ArrayList<PaperNode>();
    }

    public void setEntities(List<PaperEntity> entities){
        // set cited paper
        for(PaperEntity entity : entities){
            PaperNode paperNode = new PaperNode(entity.getId());
            paperNode.setPaperEntity(entity);
            refPapers.add(paperNode);
        }
    }

    @Override
    public List<Long> getMiddleNode(GraphNode graphNode) throws IllegalArgumentException {
        List<Long> middles = new ArrayList<Long>();

        for(PaperNode paperNode : refPapers){
            if(paperNode.isAdjacent(graphNode)){
                middles.add(paperNode.getNodeId());
            }
        }

        return middles;
    }

    public List<PaperNode> getRefPapers() {
        return refPapers;
    }
}
