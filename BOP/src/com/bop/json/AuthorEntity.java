package com.bop.json;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liuchun on 2016/5/7.
 */
public class AuthorEntity {
    // Author ID
    @JSONField(name = "AuId")
    private long auId;
    // Author affiliation ID
    @JSONField(name = "AfId")
    private long afId;

    public long getAuId() {
        return auId;
    }

    public void setAuId(long auId) {
        this.auId = auId;
    }

    public long getAfId() {
        return afId;
    }

    public void setAfId(long afId) {
        this.afId = afId;
    }
}
