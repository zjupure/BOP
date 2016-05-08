package com.bop.graph;

import com.bop.json.PaperEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/7.
 * Reverse references
 * all the paper in List<PaperNode> will cite the current paper
 */
public class CiteNode extends GraphNode {
    List<PaperNode> citePapers;

    public CiteNode(long id){
        super(id, GraphNode.CITE_NODE);
        citePapers = new ArrayList<PaperNode>();
    }

    public void setEntities(List<PaperEntity> entities){
        // set cited paper
        for(PaperEntity entity : entities){
            PaperNode paperNode = new PaperNode(entity.getId());
            paperNode.setPaperEntity(entity);
            citePapers.add(paperNode);
        }
    }

    @Override
    public List<Long> getMiddleNode(GraphNode graphNode) throws IllegalArgumentException {
        throw new IllegalArgumentException("citeNode can not be a start graphNode");
    }

	public List<PaperNode> getCitePapers() {
		return citePapers;
	}
}
