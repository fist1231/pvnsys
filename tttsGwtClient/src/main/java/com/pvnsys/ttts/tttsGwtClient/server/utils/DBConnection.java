package com.pvnsys.ttts.tttsGwtClient.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	public static Connection getConnection() throws Exception {

        Connection conn     = null;
        String url          = "jdbc:mysql://127.0.0.1:3306/";
        String db           = "***";
        String driver       = "com.mysql.jdbc.Driver";
        String user         = "***";
        String pass         = "*******";
            
    try {
            Class.forName(driver).newInstance();
    } catch (InstantiationException e) {
            e.printStackTrace();
            throw e;
    } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw e;
    } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
    }
    try {
                    
                    conn = DriverManager.getConnection(url+db, user, pass);
    } catch (SQLException e) {
            System.err.println("Mysql Connection Error: ");
            e.printStackTrace();
            throw e;
    }
            return conn;
}
}
