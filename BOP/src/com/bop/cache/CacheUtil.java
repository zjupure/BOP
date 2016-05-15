package com.bop.cache;

import com.bop.graph.GraphNode;

import java.security.MessageDigest;
import java.util.HashMap;

/**
 * Created by liuchun on 2016/5/14.
 */
public class CacheUtil {
    /** LruCache instance */
    private static LruCache<String, String> mMemoryCache;
    /** Database Cache instance */
    //private static DbCache mDbCache = new DbCache();
    /** Cache for those have massive references CiteNode */
    private static HashMap<Long, GraphNode> mNodeCache = new HashMap<Long, GraphNode>();
    /**
     * create a LruCache in the memory, the size depends on the system max memory
     */
    private static void createLruCache(){
        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory/64;
        // 7G/64 = 100M cache
        mMemoryCache = new LruCache<String, String>(mCacheSize){
            @Override
            protected int sizeOf(String key, String value) {
                return value.length();
            }
        };
    }

    /**
     * get cache
     * @return
     */
    private static LruCache<String,String> getLruCache(){
        if(mMemoryCache == null){
            createLruCache();
        }

        return mMemoryCache;
    }
    /**
     * put the results to the cache
     * @param id1
     * @param id2
     * @param results
     */
    public static void put(long id1, long id2, String results){
        String key = getMD5Hash(id1, id2);
        getLruCache().put(key, results);
        //mDbCache.put(id1, id2, results);
    }

    /**
     * get the cache from LruCache
     * @param id1
     * @param id2
     * @return
     */
    public static String get(long id1, long id2){
        String key = getMD5Hash(id1, id2);
        String result;

        /** first get data from LruCache */
        result = getLruCache().get(key);
        if(result != null){
            return result;  // hit the LruCache
        }

        /** then get data from Database Cache */
        //result = mDbCache.get(id1, id2);

        return result;
    }

    /**
     * cache for the massive cite papers
     * @param id
     * @param graphNode
     */
    public static void putGraphNode(long id, GraphNode graphNode){
        mNodeCache.put(id, graphNode);
    }

    /**
     * get those cached GraphNodes
     * @param id
     * @return
     */
    public static GraphNode getGraphNode(long id){
        GraphNode graphNode = null;
        if(mNodeCache.containsKey(id)){
            graphNode = mNodeCache.get(id);
        }
        return graphNode;
    }

    /**
     * get MD5 hash code from id1 and id2
     * @param id1
     * @param id2
     * @return
     */
    public static String getMD5Hash(long id1, long id2){
        String key = id1 + "_" + id2;
        byte[] source = key.getBytes();
        String s = null;
        char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();          // MD5 的计算结果是一个 128 位的长整数，
            // 用字节表示就是 16 个字节
            char str[] = new char[16 * 2];   // 每个字节用 16 进制表示的话，使用两个字符，
            // 所以表示成 16 进制需要 32 个字符
            int k = 0;                                // 表示转换结果中对应的字符位置
            for (int i = 0; i < 16; i++) {    // 从第一个字节开始，对 MD5 的每一个字节
                // 转换成 16 进制字符的转换
                byte byte0 = tmp[i];  // 取第 i 个字节
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];  // 取字节中高 4 位的数字转换,
                // >>> 为逻辑右移，将符号位一起右移
                str[k++] = hexDigits[byte0 & 0xf];   // 取字节中低 4 位的数字转换
            }
            s = new String(str);  // 换后的结果转换为字符串

        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
