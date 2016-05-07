package msra.bop.graph;

import msra.bop.json.PaperEntity;

import java.util.ArrayList;
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
            isAdj = checkPaper((PaperNode)graphNode);
        }else if(graphNode.getNodeId() == GraphNode.AUTHOR_AFFILIATION_NODE){
            isAdj = checkAffiliation(graphNode);
        }

        return isAdj;
    }

    /** check the paper ownership */
    private boolean checkPaper(PaperNode paperNode){
        // check the author list
        for(AuthorNode author : paperNode.authors){
            if(nodeId == author.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /** check the author's affiliation */
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
     * get the middle node between this node and paperNode(wrapped with citeNode)
     * @param citeNode
     * @return
     */
    public List<Long> getMiddleNodes(CiteNode citeNode){
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
     *  get the middle node between this node and authorNode
     * @param authorNode
     * @return
     */
    public List<Long> getMiddleNodes(AuthorNode authorNode){
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
}
