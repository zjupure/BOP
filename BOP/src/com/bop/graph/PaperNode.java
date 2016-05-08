package com.bop.graph;

import com.bop.json.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/7.
 */
public class PaperNode extends GraphNode {
    List<AuthorNode> authors;
    List<GraphNode> fields;
    GraphNode journal;
    GraphNode conference;
    List<GraphNode> refs;

    public PaperNode(long id){
        super(id, GraphNode.PAPER_NODE);
        authors = new ArrayList<AuthorNode>();
        fields = new ArrayList<GraphNode>();
        refs = new ArrayList<GraphNode>();
    }

    public void setPaperEntity(PaperEntity paperEntity){
        // adjacent authors
        List<AuthorEntity> authorEntities = paperEntity.getAuthors();
        for(AuthorEntity entity : authorEntities){
            AuthorNode authorNode = new AuthorNode(entity.getAuId());
            if(entity.getAfId() > 0){
                // affiliation == 0, means the author do not have author affiliation info
                authorNode.addAdjAff(new GraphNode(entity.getAfId(), GraphNode.AUTHOR_AFFILIATION_NODE));
            }
            //authorNode.addAdjPaper(this);
            authors.add(authorNode);
        }
        // adjacent field of study
        List<FieldEntity> fieldEntities = paperEntity.getFields();
        for(FieldEntity entity : fieldEntities){
            GraphNode fieldNode = new GraphNode(entity.getFid(), GraphNode.FIELD_NODE);
            fields.add(fieldNode);
        }
        // adjacent journal
        JournalEntity journalEntity = paperEntity.getJournal();
        if(journalEntity != null){
            journal = new GraphNode(journalEntity.getJid(), GraphNode.JOURNAL_NODE);
        }
        // adjacent conference
        ConferenceEntity conferenceEntity = paperEntity.getConference();
        if(conferenceEntity != null){
            conference = new GraphNode(conferenceEntity.getCid(), GraphNode.CONFERENCE_NODE);
        }
        // adjacent references
        List<Long> references = paperEntity.getRids();
        for(Long ref : references){
            GraphNode refNode = new GraphNode(ref, GraphNode.PAPER_NODE);
            refs.add(refNode);
        }
    }

    /**
     * judge the adjacent relationship between this node and graphNode
     * @param graphNode next graphNode
     * @return
     */
    @Override
    public boolean isAdjacent(GraphNode graphNode){
        boolean isAdj = false;
        if(graphNode.getNodeType() == GraphNode.PAPER_NODE){
            isAdj =  checkRef(graphNode);
        }else if(graphNode.getNodeType() == GraphNode.AUTHOR_NODE){
            isAdj = checkAuthor(graphNode);
        }else if(graphNode.getNodeType() == GraphNode.FIELD_NODE){
            isAdj = checkField(graphNode);
        }else if(graphNode.getNodeType() == GraphNode.JOURNAL_NODE){
            isAdj = checkJournal(graphNode);
        }else if(graphNode.getNodeType() == GraphNode.CONFERENCE_NODE){
            isAdj = checkConference(graphNode);
        }

        return isAdj;
    }

    /**
     * get one node that connect this node and graphNode, then graphNode may be PaperNode, CiteNode, or AuthorNode
     * @param graphNode
     * @return
     */
    @Override
    public List<Long> getMiddleNode(GraphNode graphNode) throws IllegalArgumentException{
        List<Long> middles;

        if(graphNode instanceof PaperNode){
            /** can only get the common authors, journal, ... */
            middles = getBridgeNodes((PaperNode)graphNode);
        }else if(graphNode instanceof CiteNode){
            /** can get the middle reference paper, for example paper A--->paper C--->paper B,
             * then get the paper C */
            middles = getBridgeNodes((CiteNode)graphNode);
        }else if(graphNode instanceof AuthorNode){
            /** get the reference papers with author id == authorNode.id */
            middles = getBridgeNodes((AuthorNode)graphNode);
        }else {
            throw new IllegalArgumentException("invalid graphNode arguments");
        }

        return middles;
    }

    /**
     * get common nodes that connect this node and paperNode, for example, with common author, journal...
     * paper A <----> author id <----> paper B
     * paper A <----> journal id <----> paper B
     * ...
     * the middle reference will be deal separately,
     * @see #getBridgeNodes(CiteNode)
     * @param paperNode
     * @return
     */
    private List<Long> getBridgeNodes(PaperNode paperNode){
        List<Long> bridges = new ArrayList<Long>();
        // check the common author nodes
        bridges.addAll(getCommonAuthors(paperNode));
        // check the common field nodes
        bridges.addAll(getCommonField(paperNode));
        // check the common journal
        bridges.addAll(getCommonJournal(paperNode));
        // check the common conference
        bridges.addAll(getCommonConference(paperNode));

        return bridges;
    }

    /**
     * get the middle reference between this paper and paperNode(wrapped with citeNode)
     * paper A -----> paper C -----> paper B (node B should transform to citeNode)
     * @param citeNode
     * @return
     */
    private List<Long> getBridgeNodes(CiteNode citeNode){
        List<Long> mRefs = new ArrayList<Long>();
        for(GraphNode refNode : refs){
            mRefs.add(refNode.getNodeId());
        }

        List<Long> nRefs = new ArrayList<Long>();
        for(PaperNode refNode : citeNode.citePapers){
            nRefs.add(refNode.getNodeId());
        }
        mRefs.retainAll(nRefs);

        return mRefs;
    }

    /**
     * get the middle reference paper with author id == authorNode.id
     * paper A ----> paper C <----> author Id
     * @param authorNode
     * @return
     */
    private List<Long> getBridgeNodes(AuthorNode authorNode){
        List<Long> mRefs = new ArrayList<Long>();
        for(GraphNode refNode : refs){
            mRefs.add(refNode.getNodeId());
        }

        List<Long> nRefs = new ArrayList<Long>();
        for(PaperNode paperNode : authorNode.papers){
            nRefs.add(paperNode.getNodeId());
        }
        mRefs.retainAll(nRefs);

        return mRefs;
    }

    /**
     * check the reference relationship
     * paper A ----> paper B
     */
    private boolean checkRef(GraphNode paperNode){
        // check the reference list
        long id = paperNode.getNodeId();
        for(GraphNode ref : refs){
            if(id ==  ref.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /**
     * check the author relationship
     * paper A <-----> author Id
     */
    private boolean checkAuthor(GraphNode authorNode){
        // check the author list
        long id = authorNode.getNodeId();
        for(AuthorNode author : authors){
            if(id == author.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /**
     * check the field relation ship
     * paper A <-----> field Id
     */
    private boolean checkField(GraphNode fieldNode){
        // check the field list
        long id = fieldNode.getNodeId();
        for(GraphNode field : fields){
            if(id == field.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /**
     * check the journal relationship
     * paper A <-----> journal Id
     */
    private boolean checkJournal(GraphNode journalNode){
        // check the journal
        long id = journalNode.getNodeId();
        if(journal != null && id == journal.getNodeId()){
            return true;
        }
        return false;
    }

    /**
     * check the conference relationship
     * paper A <-----> conference Id
     */
    private boolean checkConference(GraphNode conferenceNode){
        // check the conference
        long id = conferenceNode.getNodeId();
        if(conference != null && id == conference.getNodeId()){
            return true;
        }
        return false;
    }

    /**
     * get the common authors
     * paper A <----> author Id <---->  paper B
     */
    private List<Long> getCommonAuthors(PaperNode paperNode){
        List<Long> mAuthors = new ArrayList<Long>();
        for(AuthorNode authorNode : authors){
            mAuthors.add(authorNode.getNodeId());
        }

        List<Long> nAuthors = new ArrayList<Long>();
        for(AuthorNode authorNode : paperNode.authors){
            nAuthors.add(authorNode.getNodeId());
        }
        mAuthors.retainAll(nAuthors);

        return mAuthors;
    }

    /**
     * get the common fields
     * paper A <----> field Id <----> paper B
     */
    private List<Long> getCommonField(PaperNode paperNode){
        List<Long> mFields = new ArrayList<Long>();
        for(GraphNode field : fields){
            mFields.add(field.getNodeId());
        }

        List<Long> nFields = new ArrayList<Long>();
        for(GraphNode field : paperNode.fields){
            nFields.add(field.getNodeId());
        }
        mFields.retainAll(nFields);

        return mFields;
    }

    /**
     * get the common journal
     * paper A <----> journal Id <---->  paper B
     */
    private List<Long> getCommonJournal(PaperNode paperNode){
        List<Long> mJournals = new ArrayList<Long>();
        if(journal != null && paperNode.journal != null
                && journal.getNodeId() == paperNode.journal.getNodeId()){
            mJournals.add(journal.getNodeId());
        }
        return mJournals;
    }

    /**
     * get the common conference
     * paper A <----> conference Id <----> paper B
     */
    private List<Long> getCommonConference(PaperNode paperNode){
        List<Long> mConferences = new ArrayList<Long>();
        if(conference != null && paperNode.conference != null
                && conference.getNodeId() == paperNode.conference.getNodeId()){
            mConferences.add(conference.getNodeId());
        }
        return mConferences;
    }

    public List<AuthorNode> getAuthors() {
        return authors;
    }

    public List<GraphNode> getRefs() {
		return refs;
	}
    
    
}
