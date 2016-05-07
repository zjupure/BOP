package msra.bop.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuchun on 2016/5/7.
 */
public class EntityWrapper {
    // expr
    private String expr;
    // entity Lists
    private List<PaperEntity> entities = new ArrayList<PaperEntity>();

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public List<PaperEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<PaperEntity> entities) {
        this.entities = entities;
    }
}
