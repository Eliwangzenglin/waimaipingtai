package com.wp.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;

public class WriteDBCPUtils {
    
	private static String url = "jdbc:mysql://127.0.0.1:3306/weixin?useUnicode=true&characterEncoding=UTF8";
	 private static String username = "123";  
	 private static String pwd = "123";  
	 private static String jdbc_driver="com.mysql.jdbc.Driver";
    
    
    private static BasicDataSource bs = null; //
    
    
    /**
     * 创建数据源
     * @return
     */
    public static BasicDataSource getDataSource() throws Exception{
        if(bs==null){
            bs = new BasicDataSource();
            bs.setDriverClassName(jdbc_driver);
            bs.setUrl(url);
            bs.setUsername(username);
            bs.setPassword(pwd);
            bs.setMaxIdle(50);//设置最大并发数
            bs.setInitialSize(3);//数据库初始化时，创建的连接个数
            bs.setMinIdle(5);//最小空闲连接数
            bs.setMaxIdle(100);//数据库最大连接数
            bs.setMaxWaitMillis(5000);
            bs.setMinEvictableIdleTimeMillis(1500);//空闲连接2秒中后释放
            bs.setTimeBetweenEvictionRunsMillis(1*60*1000);//5分钟检测一次是否有死掉的线程
            bs.setTestOnBorrow(false);
        }
        return bs;
    }
     
    
    /**
     * 释放数据源
     */
    public static void shutDownDataSource() throws Exception{
        if(bs!=null){
            bs.close();
        }
    }
     
    /**
     * 获取数据库连接
     * @return
     */
    public static Connection getConnection(){
        Connection con=null;
        try {
            if(bs!=null){
                con=bs.getConnection();
            }else{
                con=getDataSource().getConnection();
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return con;
    }
    
    
    
    
    /** 
     * 资源关闭 
     *  
     * @param rs 
     * @param stmt 
     * @param conn 
     */  
    public static void close(ResultSet rs, Statement stmt  
            , PreparedStatement pstat,Connection conn) {  
        if (rs != null) {  
            try {  
                rs.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
            }  
        }  
  
        if (stmt != null) {  
            try {  
                stmt.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
            }  
        }  
        if (pstat != null) {  
            try {  
            	pstat.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
            }  
        }  
  
        if (conn != null) {  
            try {  
                conn.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
            }finally{
            	
            }  
        }  
    }  
    
	
}
