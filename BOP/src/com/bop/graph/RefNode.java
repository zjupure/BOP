package com.bop.graph;

import com.bop.json.PaperEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/8.
 * Forward references
 * the paper will cite all the paper in List<PaperNode>
 */
public class RefNode extends GraphNode{
    List<PaperNode> refPapers;

    public RefNode(long id){
        super(id, GraphNode.REF_NODE);
        refPapers = new ArrayList<PaperNode>();
    }

    public void addEntities(List<PaperEntity> entities){
        // set cited paper
        for(PaperEntity entity : entities){
            PaperNode paperNode = new PaperNode(entity.getId());
            paperNode.setPaperEntity(entity);
            refPapers.add(paperNode);
        }
    }

    /**
     * get the middle paper has chain reference relation ship
     * paper A ---> paper C ---> paper B (paper A has been transform to RefNode)
     * @param graphNode
     * @return
     * @throws IllegalArgumentException
     */
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
