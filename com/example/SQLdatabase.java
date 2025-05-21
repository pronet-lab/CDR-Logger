package com.example;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.log4j.Logger;

public class SQLdatabase {
  public int port = 0;
  
  public Path path;
  
  public static final Logger log = Logger.getLogger(SQLdatabase.class);
  
  static final String connectionUrl = "jdbc:sqlserver://localhost:1433";
  
  static final String myDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  
  //static final String USER = "hbl_user";
  static final String USER = "sa";

  //static final String PASS = "hbl4455&";
  static final String PASS = "Sa$#@!";
  
  static final String DatabaseName = "CDR";
  
  public void getfrom() {
    try {
      // String url = "jdbc:sqlserver://localhost:1433;databaseName=CDR;user=hbl_user;password=hbl4455&";
      String url = "jdbc:sqlserver://localhost:1433;databaseName=CDR;user=sa;password=Sa$#@!";
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      Connection conn = DriverManager.getConnection(url);
      String query = "SELECT TOP 1 * FROM Settings ORDER BY SerialNumber DESC";
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      System.out.println(rs);
      if (rs != null) {
        while (rs.next()) {
          this.port = Integer.parseInt(rs.getString("PortNumber"));
          this.path = Paths.get(rs.getString("Path"), new String[0]);
        } 
      } else {
        System.out.println("RS IS NULL");
      } 
      st.close();
    } catch (Exception e) {
      log.error(e + ": DB Connection error while trying to get previous values");
      System.err.println("Got an exception! ");
      System.err.println(e.getMessage());
    } 
  }
}
