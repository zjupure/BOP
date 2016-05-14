package com.bop.json;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Created by liuchun on 2016/5/7.
 */
public class JParser {

    public static List<PaperEntity> getPaperEntity(String json){
        // parser json with fastjson lib
        EntityWrapper wrapper = JSON.parseObject(json, EntityWrapper.class);

        return wrapper.getEntities();
    }
}
