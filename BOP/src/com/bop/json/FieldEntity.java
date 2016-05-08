package com.bop.json;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liuchun on 2016/5/7.
 */
public class FieldEntity {
    // Field of study ID
    @JSONField(name = "FId")
    private long fid;

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }
}
