package com.bop.json;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/7.
 */
public class PaperEntity {
    // Entity ID
    @JSONField(name = "Id")
    private long id;
    // Authors List
    @JSONField(name = "AA")
    private List<AuthorEntity> authors = new ArrayList<AuthorEntity>();
    // Field of Study List
    @JSONField(name = "F")
    private List<FieldEntity> fields = new ArrayList<FieldEntity>();
    // Journal
    @JSONField(name = "J")
    private JournalEntity journal;
    // Conference
    @JSONField(name = "C")
    private ConferenceEntity conference;
    // RIds
    @JSONField(name = "RId")
    private List<Long> rids = new ArrayList<Long>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<AuthorEntity> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorEntity> authors) {
        this.authors = authors;
    }

    public List<FieldEntity> getFields() {
        return fields;
    }

    public void setFields(List<FieldEntity> fields) {
        this.fields = fields;
    }

    public JournalEntity getJournal() {
        return journal;
    }

    public void setJournal(JournalEntity journal) {
        this.journal = journal;
    }

    public ConferenceEntity getConference() {
        return conference;
    }

    public void setConference(ConferenceEntity conference) {
        this.conference = conference;
    }

    public List<Long> getRids() {
        return rids;
    }

    public void setRids(List<Long> rids) {
        this.rids = rids;
    }
}
