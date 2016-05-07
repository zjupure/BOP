package msra.bop.json;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liuchun on 2016/5/7.
 */
public class JournalEntity {
    // Journal ID
    @JSONField(name = "JId")
    private long jid;

    public long getJid() {
        return jid;
    }

    public void setJid(long jid) {
        this.jid = jid;
    }
}
