package msra.bop.json;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liuchun on 2016/5/7.
 */
public class ConferenceEntity {
    // Conference series ID
    @JSONField(name = "CId")
    private long cid;

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }
}
