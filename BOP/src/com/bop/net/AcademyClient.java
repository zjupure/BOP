package com.bop.net;


import com.bop.graph.AuthorNode;
import com.bop.graph.CiteNode;
import com.bop.graph.GraphNode;
import com.bop.graph.PaperNode;
import com.bop.json.JParser;
import com.bop.json.PaperEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharArrayBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by liuchun on 2016/5/2.
 */
public class AcademyClient {

    /**
     * sync network request
     * @param url: must be build with the UrlBuilder
     * @return json string
     */
    public static String getAcademyResp(String url){
        //
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // get the response json
            String json = "";
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = response.getEntity();
                if(entity != null){
                    InputStream is = entity.getContent();
                    String charset = "UTF-8";
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
                    CharArrayBuffer buffer = new CharArrayBuffer(4096);
                    char[] tmp = new char[4096];
                    int len;
                    while((len = br.read(tmp)) != -1){
                        buffer.append(tmp, 0, len);
                    }
                    //
                    is.close();
                    json = buffer.toString();
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
        String url = new AcademyUrlBuilder()
                .setExpr(expr)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = JParser.getPaperEntity(json);

        if(entities.size() <= 0){
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
        String url = new AcademyUrlBuilder()
                .setExpr("Id=" + paperId)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = JParser.getPaperEntity(json);
        PaperNode paperNode = null;
        if(entities.size() > 0){
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
        String expr = "Composite(AA.AuId=" + authorId + ")";
        String url = new AcademyUrlBuilder()
                .setExpr(expr)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = JParser.getPaperEntity(json);
        AuthorNode authorNode = null;
        if(entities.size() > 0){
            authorNode = new AuthorNode(authorId);
            authorNode.setEntities(entities);
        }
        return authorNode;
    }

    /**
     * get all the papers that cite the paperId
     * @param paperId
     * @return
     */
    public static CiteNode getCiteInfo(long paperId){
        String attrs = "Id,AA.AuId,AA.AfId,F.FId,J.JId,C.CId,RId";
        String url = new AcademyUrlBuilder()
                .setExpr("RId=" + paperId)
                .setAttributes(attrs)
                .build();
        String json = getAcademyResp(url);
        List<PaperEntity> entities = JParser.getPaperEntity(json);
        CiteNode citeNode = null;
        if(entities.size() > 0){
            citeNode = new CiteNode(paperId);
            citeNode.setEntities(entities);
        }
        return citeNode;
    }

    /** Builder the Academy URL */
    public static class AcademyUrlBuilder{
        public static final String BASE_URI = "https://oxfordhk.azure-api.net/academic/v1.0/evaluate";
        public static final String SUB_KEY = "f7cc29509a8443c5b3a5e56b0e38b5a6";
        HashMap<String, String> params = new HashMap<String, String>();

        public AcademyUrlBuilder addParam(String key, String value){
            params.put(key, value);
            return this;
        }

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
                }
            }

            return nvps;
        }

        /** builder the url */
        public String build(){
            try{
                URI uri = new URIBuilder(BASE_URI)
                        .setParameters(generatePaparms())
                        .build();
                //
                return uri.toString();
            }catch (URISyntaxException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
