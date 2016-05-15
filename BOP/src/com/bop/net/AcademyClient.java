package com.bop.net;


import com.bop.graph.*;
import com.bop.json.JParser;
import com.bop.json.PaperEntity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by liuchun on 2016/5/2.
 */
public class AcademyClient {
    //
	private static CloseableHttpClient httpClient = null;
    private static HttpClientContext context = null;

    /**
     * initial the httpClient instance
     */
    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)  // 30s
                .setSocketTimeout(30000)   // 30s
                .build();
        context = HttpClientContext.create();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * sync network request
     * @param url: must be build with the UrlBuilder
     * @return json string
     */
    public static String getAcademyResp(URI url){
        //
        //System.out.println(url);
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet, context);
            // get the response json
            String json = "";
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = response.getEntity();
                if(entity != null){
                    /*
                    InputStream is = entity.getContent();
                    int i = (int)entity.getContentLength();
                    if (i < 0) {
                        i = 4096;
                    }
                    String charset = "UTF-8";
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
                    CharArrayBuffer buffer = new CharArrayBuffer(i);
                    char[] tmp = new char[4096];
                    int len;
                    while((len = br.read(tmp)) != -1){
                        buffer.append(tmp, 0, len);
                    }
                    //
                    is.close();
                    json = buffer.toString();*/
                    //System.out.println("content-length: " + entity.getContentLength());
                	json = EntityUtils.toString(entity);
                }
            }
            response.close();

            return json;
        }catch (ClientProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return "";
    }

    /**
     * get info from id: paperId or authorId
     * @param id
     * @return
     */
    public static GraphNode getIdInfo(long id){
        String format = "Or(Id=%d,Composite(AA.AuId=%d))";
        String expr = String.format(format, id, id);
        String attrs = "Id,AA.AuId,AA.AfId,F.FId,J.JId,C.CId,RId";
        URI url = new AcademyUrlBuilder()
                .setExpr(expr)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = null;
        if(!json.equals("")){
            entities = JParser.getPaperEntity(json);
        }

        if(entities == null || entities.size() <= 0){
            return null;
        }

        GraphNode graphNode = null;
        if(entities.size() == 1){
            // might be paper id, have to check the entity id
            PaperEntity entity = entities.get(0);
            if(entity.getId() == id){
                // id equal, there could be paper node, check the author list
                if(entity.getAuthors().size() > 0) {
                    PaperNode paperNode = new PaperNode(id);
                    paperNode.setPaperEntity(entity);
                    graphNode = paperNode;
                }
            }else{
                // could be author node
                AuthorNode authorNode = new AuthorNode(id);
                authorNode.setEntities(entities);
                graphNode = authorNode;
            }
        }else{
            // paper id in only, so it must be author node
            AuthorNode authorNode = new AuthorNode(id);
            authorNode.setEntities(entities);
            graphNode = authorNode;
        }

        return graphNode;
    }

    /**
     * get information for paperId
     * @param paperId
     * @return
     */
    public static PaperNode getPaperInfo(long paperId){
        String attrs = "Id,AA.AuId,AA.AfId,F.FId,J.JId,C.CId,RId";
        URI url = new AcademyUrlBuilder()
                .setExpr("Id=" + paperId)
                .setCount(10)    // a fix paperId only return one entity
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = JParser.getPaperEntity(json);

        PaperNode paperNode = null;
        if(entities != null && entities.size() > 0){
            PaperEntity entity = entities.get(0);
            if(entity.getAuthors().size() > 0){
                // a paper must have authors, to fix the bug
                // when give a wrong id, the Academy API still return a entity with nothing
                paperNode = new PaperNode(paperId);
                paperNode.setPaperEntity(entities.get(0));
            }
        }
        return paperNode;
    }

    /**
     * get information for authorId
     * @param authorId
     * @return
     */
    public static AuthorNode getAuthorInfo(long authorId){
        String attrs = "Id,AA.AuId,AA.AfId,F.FId,J.JId,C.CId,RId";
        String expr = String.format("Composite(AA.AuId=%d)", authorId);
        URI url = new AcademyUrlBuilder()
                .setExpr(expr)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = JParser.getPaperEntity(json);
        AuthorNode authorNode = null;
        if(entities != null && entities.size() > 0){
            authorNode = new AuthorNode(authorId);
            authorNode.setEntities(entities);
        }
        return authorNode;
    }

    /**
     * get the affiliation info for the authorId
     * @param authorId
     * @return
     */
    public static AuthorNode getAffiInfo(long authorId){
        String attrs = "Id,AA.AuId,AA.AfId";
        String expr = String.format("Composite(AA.AuId=%d)", authorId);
        URI url = new AcademyUrlBuilder()
                .setExpr(expr)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        AuthorNode authorNode = null;
        List<PaperEntity> entities = JParser.getPaperEntity(json);
        if(entities != null && entities.size() > 0){
            authorNode = new AuthorNode(authorId);
            authorNode.setEntities(entities);
        }
        return authorNode;
    }

    /**
     * get paperId reference paper info
     * @param paperId
     * @param rids
     * @return
     */
    public static RefNode getPaperRefInfo(long paperId, List<Long> rids){
        String attrs = "Id,AA.AuId,AA.AfId,F.FId,J.JId,C.CId,RId";
        String format = "Or(%s,Id=%d)";
        String expr = "", json;
        URI url;
        AcademyUrlBuilder builder = new AcademyUrlBuilder().setAttributes(attrs);
        List<PaperEntity> entities;
        RefNode refNode = new RefNode(paperId);

        int index = 0;
        for(int i = 0; i < rids.size(); i++){
            if(index == 0){
                expr = "Id=" + rids.get(i);
                index++;
                continue;
            }else if(index == 1){
                expr = String.format(format, expr, rids.get(i));
                index++;
                continue;
            }
            index++;
            // build expr
            expr = String.format(format, expr, rids.get(i));
            //System.out.println(expr);
            // start network request
            if(index >= 50){
                url = builder.setExpr(expr)
                        .setCount(50)
                        .build();
                json = getAcademyResp(url);
                entities = JParser.getPaperEntity(json);
                if(entities != null && entities.size() > 0){
                    refNode.addEntities(entities);
                }
                index = 0;
            }
        }
        // the last request
        url = builder.setExpr(expr)
                .setCount(50)
                .build();
        json = getAcademyResp(url);
        entities = JParser.getPaperEntity(json);
        if(entities != null && entities.size() > 0){
            refNode.addEntities(entities);
        }
        //System.out.println(expr);

        return refNode;
    }

    /**
     * get all the papers that cite the paperId
     * @param paperId
     * @return
     */
    public static CiteNode getCiteInfo(long paperId){
        String attrs = "Id,AA.AuId,AA.AfId,F.FId,J.JId,C.CId";
        String expr = "RId=" + paperId;
        String json;
        int count = 3000;  // proper value to avoid response overflow
        int offset = 0;
        AcademyUrlBuilder builder = new AcademyUrlBuilder()
                .setExpr(expr)
                .setAttributes(attrs);

        List<PaperEntity> entities;
        CiteNode citeNode = new CiteNode(paperId);

        while(true){
            URI url = builder.setCount(count)
                    .setOffset(offset)
                    .build();
            json = getAcademyResp(url);
            entities = JParser.getPaperEntity(json);
            if(entities == null || entities.size() == 0){
                break;
            }

            if(entities.size() > 0){
                citeNode.addEntities(entities);
            }

            if(entities.size() < count){
                break;
            }

            offset += count;
        }

        return citeNode;
    }

    /**
     * query the middle paper that cite the right paper
     * paper A ---> paper C ---> paper B
     * @param ids
     * @param paperId
     * @return
     */
    public static List<Long> getMiddlePapers(List<Long> ids, long paperId){
        List<Long> middles = new ArrayList<Long>();
        String or_format = "Or(%s,Id=%d)";
        String and_format = "And(%s,RId=%d)";
        String expr = "", json;
        URI url;
        AcademyUrlBuilder builder = new AcademyUrlBuilder().setAttributes("Id");
        List<PaperEntity> entities;

        int index = 0;
        for(int i = 0; i < ids.size(); i++){
            if(index == 0){
                expr = String.format("Id=%d", ids.get(0));
                index++;
                continue;
            }else if(index == 1){
                expr = String.format(or_format, expr, ids.get(i));
                index++;
                continue;
            }
            index++;
            // build expr
            expr = String.format(or_format, expr, ids.get(i));
            //System.out.println(expr);
            // start network request
            if(index >= 100){
                // build final expr
                expr = String.format(and_format, expr, paperId);
                url = builder.setExpr(expr)
                        .setCount(100)
                        .build();
                json = getAcademyResp(url);
                entities = JParser.getPaperEntity(json);
                if(entities != null && entities.size() > 0){
                    for(PaperEntity entity : entities){
                        middles.add(entity.getId());
                    }
                }
                index = 0;
            }
        }
        // the last request
        expr = String.format(and_format, expr, paperId);
        url = builder.setExpr(expr)
                .setCount(100)
                .build();
        json = getAcademyResp(url);
        entities = JParser.getPaperEntity(json);
        if(entities != null && entities.size() > 0){
            for(PaperEntity entity : entities){
                middles.add(entity.getId());
            }
        }
        System.out.println(expr);

        return middles;
    }

    /**
     * query the middle paper that cite the right paper with common info Id, such as authors, journal...
     * @param Id
     * @param paperId
     * @return
     */
    public static List<Long> getMiddlePapers(long Id, long paperId, int type){
        List<Long> middles = new ArrayList<Long>();
        String and_format = "And(%s,RId=%d)";
        String[] type_format = {"Id=%d", "Composite(AA.AuId=%d)", "Composite(F.FId=%d)", "Composite(J.JId=%d)",
                "Composite(C.CId=%d)"};
        String expr, json;
        URI url;
        AcademyUrlBuilder builder = new AcademyUrlBuilder().setAttributes("Id");
        List<PaperEntity> entities;

        expr = String.format(type_format[type], Id);
        expr = String.format(and_format, expr, paperId);
        url = builder.setExpr(expr).build();
        json = getAcademyResp(url);
        entities = JParser.getPaperEntity(json);
        if(entities != null && entities.size() > 0){
            for(PaperEntity entity : entities){
                middles.add(entity.getId());
            }
        }
        System.out.println(expr);

        return middles;
    }


    /** Builder the Academy URL */
    public static class AcademyUrlBuilder{
        public static final String BASE_URI = "https://oxfordhk.azure-api.net/academic/v1.0/evaluate";
        public static final String SUB_KEY = "f7cc29509a8443c5b3a5e56b0e38b5a6";
        public static final int DEFAULT_COUNT = 2000; // default count param
        HashMap<String, String> params = new HashMap<String, String>();


        public AcademyUrlBuilder setExpr(String expr){
            params.put("expr", expr);
            return this;
        }

        public AcademyUrlBuilder setModel(String model){
            params.put("model", model);
            return this;
        }

        public AcademyUrlBuilder setAttributes(String attrs){
            params.put("attributes", attrs);
            return this;
        }

        public AcademyUrlBuilder setCount(int count){
            params.put("count", Integer.toString(count));
            return this;
        }

        public AcademyUrlBuilder setOffset(int offset){
            params.put("offset", Integer.toString(offset));
            return this;
        }

        public AcademyUrlBuilder setSubscriptionKey(String key){
            params.put("subscription-key", key);
            return this;
        }

        private List<NameValuePair> generatePaparms(){
            String[] paramKeys = {"expr", "model", "attributes", "count", "offset", "subscription-key"};
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            for(String key : paramKeys){
                if(params.containsKey(key)){
                    nvps.add(new BasicNameValuePair(key, params.get(key)));
                }else if(key.equals("subscription-key")){
                    nvps.add(new BasicNameValuePair("subscription-key", SUB_KEY));
                }else if(key.equals("count")){
                    nvps.add(new BasicNameValuePair("count",Integer.toString(DEFAULT_COUNT)));
                }
            }

            return nvps;
        }

        /** builder the url */
        public URI build(){
            try{
                URI uri = new URIBuilder(BASE_URI)
                        .setParameters(generatePaparms())
                        .build();
                //
                return uri;
            }catch (URISyntaxException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
