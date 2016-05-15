package com.bop.cache;



import java.sql.*;

/**
 * Created by liuchun on 2016/5/14.
 */
public class DbCache {
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://localhost/Cachepool";

    //  Database credentials
    private static final String USERNAME = "root";
    private static final String PASSWORD = "tangyiqi-123";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS paths(id INTEGER NOT NULL AUTO_INCREMENT,"
                                + "id1 BIGINT NOT NULL,"
                                + "id2 BIGINT NOT NULL,"
                                + "pathStr MEDIUMTEXT,"
                                + "PRIMARY KEY(id));";
    private static final String QUERY_FORMAT = "SELECT pathStr from paths WHERE id1 = %d AND id2 = %d;";
    private static final String INSERT_FORMAT = "INSERT INTO paths values(%d, %d, \'%s\');";
    private static final String UPDATE_FORMAT = "UPDATE paths SET pathStr = \'%s\' WHERE id1 = %d AND id2 = %d;";

    private Connection conn = null;
    private PreparedStatement statement = null;

    /**
     * construction method, open database link
     * create table if need
     */
    public DbCache(){
        connSQL();
        updateSQL(CREATE_TABLE);
    }

    /**
     * get cache result from id1 and id2
     * @param id1
     * @param id2
     * @return
     */
    public String get(long id1, long id2){
        String sql = String.format(QUERY_FORMAT, id1, id2);
        String result = null;
        ResultSet rs = querySQL(sql);
        try {
            if(rs != null && rs.first()){
                result = rs.getString("pathStr");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return result;
    }

    /**
     * put result into cache
     * @param id1
     * @param id2
     * @param result
     */
    public void put(long id1, long id2, String result){
        String insert = String.format(INSERT_FORMAT, id1, id2, result);
        String update = String.format(UPDATE_FORMAT, id1, id2, result);
        String query = String.format(QUERY_FORMAT, id1, id2);
        ResultSet rs = querySQL(query);
        try{
            if(rs != null && rs.first()){
                updateSQL(update); // execute update sql
                return;
            }
            //
            updateSQL(insert);  // execute insert sql
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * connect to database
     */
    public void connSQL(){
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * close the database connection
     */
    public void disConnSQL(){
        if(conn != null){
            try{
                conn.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * query, SELECT
     * @param sql
     * @return
     */
    public ResultSet querySQL(String sql){
        ResultSet rs = null;
        synchronized (this){
            try{
                statement = conn.prepareStatement(sql);
                rs = statement.executeQuery();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return rs;
    }

    /**
     * INSERT, UPDATE, DELETE
     * @param sql
     * @return
     */
    public int updateSQL(String sql){
        int res = 0;
        synchronized (this){
            try{
                statement = conn.prepareStatement(sql);
                res = statement.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return res;
    }
}
