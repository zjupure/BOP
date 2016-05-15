package com.bop.graph;

import com.bop.json.PaperEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by liuchun on 2016/5/7.
 */
public class AuthorNode extends GraphNode{
    List<PaperNode> papers;
    List<GraphNode> affiliations;

    public AuthorNode(long id){
        super(id, GraphNode.AUTHOR_NODE);
        papers = new ArrayList<PaperNode>();
        affiliations = new ArrayList<GraphNode>();
    }

    /** @hide */
    public void addAdjPaper(PaperNode paper){
        papers.add(paper);
    }

    public void addAdjAff(GraphNode affi){
        affiliations.add(affi);
    }

    public void setEntities(List<PaperEntity> entities){
        // set related papers
        for(PaperEntity entity : entities){
            // remove those paper entity with id equal to author id
            // this would be empty results
            if(entity.getId() == nodeId){
                continue;
            }

            PaperNode paperNode = new PaperNode(entity.getId());
            paperNode.setPaperEntity(entity);
            papers.add(paperNode);
            // deal with the affiliation
            for(AuthorNode authorNode : paperNode.authors){
                if(authorNode.getNodeId() == nodeId){
                    affiliations.addAll(authorNode.affiliations);
                }
            }
        }
        // remove the duplicated elements in affiliations
        HashSet<GraphNode> set = new HashSet<GraphNode>(affiliations);
        affiliations.clear();
        affiliations.addAll(set);
    }

    /**
     * judge the adjacent relationship between this node and graphNode
     * @param graphNode next graphNode
     * @return
     */
    @Override
    public boolean isAdjacent(GraphNode graphNode) {
        boolean isAdj = false;

        if(graphNode.getNodeType() == GraphNode.PAPER_NODE){
            isAdj = checkPaper(graphNode);
        }else if(graphNode.getNodeId() == GraphNode.AUTHOR_AFFILIATION_NODE){
            isAdj = checkAffiliation(graphNode);
        }

        return isAdj;
    }

    /**
     * get one node that connect this node and graphNode, then graphNode may be CiteNode, or AuthorNode
     * if graphNode is paperNode, should be transform to CiteNode first
     * @param graphNode
     * @return
     */
    @Override
    public List<Long> getMiddleNode(GraphNode graphNode) throws IllegalArgumentException{
        List<Long> middles;

        if(graphNode instanceof PaperNode){
            /** get the paper with author that cite the right paperNode */
            middles = getBridgeNodes((PaperNode) graphNode);
        }else if(graphNode instanceof AuthorNode){
            /** get the common paper with two authors or common affiliations the authors belong to */
            middles = getBridgeNodes((AuthorNode)graphNode);
        }else if(graphNode instanceof CiteNode){
            /** deprecated */
            middles = getBridgeNodes((CiteNode) graphNode);
        }else{
            throw new IllegalArgumentException("invalid graphNode arguments");
        }

        return middles;
    }

    /**
     * get the paper that has reference to the paperNode
     * author Id <---> paper C ---> paper B
     * @param paperNode
     * @return
     */
    private List<Long> getBridgeNodes(PaperNode paperNode){
        List<Long> mPapers = new ArrayList<Long>();

        for(PaperNode paper : papers){
            if(paper.isAdjacent(paperNode)){
                mPapers.add(paper.getNodeId());
            }
        }

        return mPapers;
    }

    /**
     *  get the common paper or affiliation between this author and authorNode
     *  author A <----> paper C <----> author B
     *  author A <----> affiliation Id <----> author B
     * @param authorNode
     * @return
     */
    private List<Long> getBridgeNodes(AuthorNode authorNode){
        List<Long> mBridges = new ArrayList<Long>();
        /** get common papers*/
        mBridges.addAll(getCommonPapers(authorNode));
        /** get common affiliations */
        mBridges.addAll(getCommonAffi(authorNode));

        return mBridges;
    }

    /**
     * get the paper with this author that also has reference to citeNode
     * author Id <----> paper C ---->  paper B
     * @see #getBridgeNodes(PaperNode)
     * @param citeNode
     * @return
     */
    private List<Long> getBridgeNodes(CiteNode citeNode){
        List<Long> mPapers = new ArrayList<Long>();

        for(PaperNode paperNode : citeNode.citePapers){
            if(isAdjacent(paperNode)){
                mPapers.add(paperNode.getNodeId());
            }
        }

        return mPapers;
    }

    /**
     * check the paper ownership
     * author Id <-----> paper B
     */
    private boolean checkPaper(GraphNode paperNode){

        if(paperNode instanceof PaperNode){
            // check the author list in paper
            PaperNode paper = (PaperNode)paperNode;
            for(AuthorNode author : paper.authors){
                if(nodeId == author.getNodeId()){
                    return true;
                }
            }
        }
        // if cannot find, then check the paper list with this author
        long id = paperNode.getNodeId();
        for(PaperNode paper : papers){
            if(id == paper.getNodeId()){
                return true;
            }
        }

        return false;
    }

    /**
     * check the author's affiliation
     * author Id <----> affiliation Id
     */
    private boolean checkAffiliation(GraphNode affiNode){
        // check the author's affiliations
        long id = affiNode.getNodeId();
        for(GraphNode affi : affiliations){
            if(id == affi.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /**
     * get common papers between this node and authorNode
     * @param authorNode
     * @return
     */
    public List<Long> getCommonPapers(AuthorNode authorNode){
        List<Long> mPapers = new ArrayList<Long>();
        for(PaperNode paper : papers){
            mPapers.add(paper.getNodeId());
        }
        if(mPapers.size() == 0){
            return mPapers;
        }

        List<Long> nPapers = new ArrayList<Long>();
        for(PaperNode paper : authorNode.papers){
            nPapers.add(paper.getNodeId());
        }
        if(nPapers.size() == 0){
            return nPapers;
        }
        //
        mPapers.retainAll(nPapers);

        return mPapers;
    }

    /**
     * get common affiliation between this node and authorNode
     * @param authorNode
     * @return
     */
    public List<Long> getCommonAffi(AuthorNode authorNode){
        List<Long> mAffi = new ArrayList<Long>();
        for(GraphNode affi : affiliations){
            mAffi.add(affi.getNodeId());
        }
        if(mAffi.size() == 0){
            return mAffi;
        }

        List<Long> nAffi = new ArrayList<Long>();
        for(GraphNode affi : authorNode.affiliations){
            nAffi.add(affi.getNodeId());
        }
        if(nAffi.size() == 0){
            return nAffi;
        }
        //
        mAffi.retainAll(nAffi);

        return mAffi;
    }

    /**
     * the affId is connect with the author or not
     * @param affId
     * @return
     */
    public boolean containAffiliation(long affId){
        for(GraphNode affi : affiliations){
            if(affId == affi.getNodeId()){
                return true;
            }
        }
        return false;
    }

	public List<PaperNode> getPapers() {
		return papers;
	}

	public List<GraphNode> getAffiliations() {
		return affiliations;
	}
    
}
