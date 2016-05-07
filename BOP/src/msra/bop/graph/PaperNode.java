package msra.bop.graph;

import msra.bop.json.*;

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
    List<PaperNode> refs;

    public PaperNode(long id){
        super(id, GraphNode.PAPER_NODE);
        authors = new ArrayList<AuthorNode>();
        fields = new ArrayList<GraphNode>();
        refs = new ArrayList<PaperNode>();
    }

    public void setPaperEntity(PaperEntity paperEntity){
        // adjacent authors
        List<AuthorEntity> authorEntitiys = paperEntity.getAuthors();
        for(AuthorEntity entity : authorEntitiys){
            AuthorNode authorNode = new AuthorNode(entity.getAuId());
            authorNode.addAdjAff(new GraphNode(entity.getAfId(), GraphNode.AUTHOR_AFFILIATION_NODE));
            authorNode.addAdjPaper(this);
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
            PaperNode paperNode = new PaperNode(ref);
            refs.add(paperNode);
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
            isAdj =  checkRef((PaperNode)graphNode);
        }else if(graphNode.getNodeType() == GraphNode.AUTHOR_NODE){
            isAdj = checkAuthor((AuthorNode)graphNode);
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
     * judge the common relation between this node and paperNode, such as common author, journal
     * @param paperNode
     * @return
     */
    public List<Long> getMiddleNodes(PaperNode paperNode){
        List<Long> bridges = new ArrayList<Long>();
        // check the common author nodes
        bridges.addAll(getCommonAuthors(paperNode));
        // check the common field nodes
        bridges.addAll(getCommonField(paperNode));
        // check the common journal
        bridges.addAll(getCommonJournal(paperNode));
        // check the common conference
        bridges.addAll(getCommonConference(paperNode));
        // check the middle reference, deal separate

        return bridges;
    }

    /**
     * get the middle reference between this paper and paperNode(wrapped with citeNode)
     * @param citeNode
     * @return
     */
    public List<Long> getMiddleNodes(CiteNode citeNode){
        List<Long> mRefs = new ArrayList<Long>();
        for(PaperNode paperNode : refs){
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
     * judge the common node between this node and authorNode
     * @param authorNode
     * @return
     */
    public List<Long> getMiddleNodes(AuthorNode authorNode){
        List<Long> mRefs = new ArrayList<Long>();
        for(PaperNode paperNode : refs){
            mRefs.add(paperNode.getNodeId());
        }

        List<Long> nRefs = new ArrayList<Long>();
        for(PaperNode paperNode : authorNode.papers){
            nRefs.add(paperNode.getNodeId());
        }
        mRefs.retainAll(nRefs);

        return mRefs;
    }

    /** check the reference relationship */
    private boolean checkRef(PaperNode paperNode){
        // check the reference list
        long id = paperNode.getNodeId();
        for(PaperNode ref : refs){
            if(id ==  ref.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /** check the author relationship */
    private boolean checkAuthor(AuthorNode authorNode){
        // check the author list
        long id = authorNode.getNodeId();
        for(AuthorNode author : authors){
            if(id == author.getNodeId()){
                return true;
            }
        }
        return false;
    }

    /** check the field relation ship */
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

    /** check the journal relationship */
    private boolean checkJournal(GraphNode journalNode){
        // check the journal
        long id = journalNode.getNodeId();
        if(journal != null && id == journal.getNodeId()){
            return true;
        }
        return false;
    }

    /** check the conference relationship */
    private boolean checkConference(GraphNode conferenceNode){
        // check the conference
        long id = conferenceNode.getNodeId();
        if(conference != null && id == conference.getNodeId()){
            return true;
        }
        return false;
    }

    /** get the common authors */
    public List<Long> getCommonAuthors(PaperNode paperNode){
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

    /** get the common fields */
    public List<Long> getCommonField(PaperNode paperNode){
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

    /** get the common journal */
    public List<Long> getCommonJournal(PaperNode paperNode){
        List<Long> mJournals = new ArrayList<Long>();
        if(journal != null && paperNode.journal != null
                && journal.getNodeId() == paperNode.journal.getNodeId()){
            mJournals.add(journal.getNodeId());
        }
        return mJournals;
    }

    /** get the common conference */
    public List<Long> getCommonConference(PaperNode paperNode){
        List<Long> mConferences = new ArrayList<Long>();
        if(conference != null && paperNode.conference != null
                && conference.getNodeId() == paperNode.conference.getNodeId()){
            mConferences.add(conference.getNodeId());
        }
        return mConferences;
    }
}
