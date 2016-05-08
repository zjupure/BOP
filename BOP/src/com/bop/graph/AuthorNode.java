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
        List<Long> middles = new ArrayList<Long>();

        if(graphNode instanceof CiteNode){
            middles = getBridgeNodes((CiteNode)graphNode);
        }else if(graphNode instanceof AuthorNode){
            middles = getBridgeNodes((AuthorNode)graphNode);
        }else if(graphNode instanceof PaperNode){
            // return empty list to avod exception
            //throw new IllegalArgumentException("paperNode should be transform to citeNode manually");
        }else{
            throw new IllegalArgumentException("invalid graphNode arguments");
        }

        return middles;
    }

    /**
     * get the paper with this author that also has reference to citeNode
     * author Id <----> paper C ---->  paper B
     * @param citeNode
     * @return
     */
    private List<Long> getBridgeNodes(CiteNode citeNode){
        List<Long> mRefs = new ArrayList<Long>();
        for(PaperNode paperNode : papers){
            mRefs.add(paperNode.getNodeId());
        }

        List<Long> nRefs = new ArrayList<Long>();
        for(PaperNode paperNode : citeNode.citePapers){
            nRefs.add(paperNode.getNodeId());
        }
        mRefs.retainAll(nRefs);

        return mRefs;
    }

    /**
     *  get the common paper or affiliation between this author and authorNode
     *  author A <----> paper C <----> author B
     *  author A <----> affiliation Id <----> author B
     * @param authorNode
     * @return
     */
    private List<Long> getBridgeNodes(AuthorNode authorNode){
        List<Long> mNodes = new ArrayList<Long>();
        for(PaperNode paperNode : papers){
            mNodes.add(paperNode.getNodeId());
        }
        for(GraphNode affiNode : affiliations){
            mNodes.add(affiNode.getNodeId());
        }

        List<Long> nNodes = new ArrayList<Long>();
        for(PaperNode paperNode : authorNode.papers){
            nNodes.add(paperNode.getNodeId());
        }
        for(GraphNode affiNode : authorNode.affiliations){
            nNodes.add(affiNode.getNodeId());
        }
        mNodes.retainAll(nNodes);

        return mNodes;
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
        }else{
            // check the paper list with this author
            long id = paperNode.getNodeId();
            for(PaperNode paper : papers){
                if(id == paper.getNodeId()){
                    return true;
                }
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
}
