package org.dean.toolkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * @description: jdbc工具类
 * @author: dean
 * @create: 2019/06/21 15:12
 */
public class JDBCUtils {

    /**
     * 获取数据库连接
     * @param url 数据库链接
     * @param userName 数据库账户
     * @param password 数据库密码
     * @return
     */
    public static Connection getConnection(String url,String userName,String password) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url,userName,password);
        } catch (Exception e) {
            System.out.println("-----------mysql get connection has exception , msg = "+ e.getMessage());
        }
        return con;
    }

    /**
     * 获取数据库连接
     * @param properties
     * @return
     */
    public static Connection getConnection(Properties properties){
        return getConnection(properties.getProperty("url"),properties.getProperty("userName"),properties.getProperty("password"));
    }

    /**
     * 关闭数据库资源
     * @param connection
     * @param ps
     */
    public static void close(Connection connection, PreparedStatement ps) {
        try {
            //关闭连接和释放资源
            if (connection != null) {
                connection.close();
            }
            if (ps != null) {
                ps.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取preparedstatement
     * @param connection
     * @param sql
     * @return
     */
    public static PreparedStatement getPreparedStatement(Connection connection, String sql) {
        try {
            return connection.prepareStatement(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
