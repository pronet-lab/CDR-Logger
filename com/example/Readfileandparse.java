package com.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;
import org.apache.log4j.Logger;

public class Readfileandparse {
  public static Path filepath;
  
  public static String filename;
  
  public static final Logger log = Logger.getLogger(Readfileandparse.class);
  
  static final String connectionUrl = "jdbc:sqlserver://localhost:1433";
  
  static final String myDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

  // static final String USER = "hbl_user";
  static final String USER = "sa";
  
  // static final String PASS = "hbl4455&";
  static final String PASS = "Sa$#@!";
  
  static final String DatabaseName = "CDR";
  
  Readfileandparse(Path path, String name) {
    filepath = path;
    filename = name;
  }
  
  Readfileandparse() {}
  
  public void settings(int port, Path p) throws IOException, SQLException {
    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      // String url = "jdbc:sqlserver://localhost:1433;databaseName=CDR;user=hbl_user;password=hbl4455&";
      String url = "jdbc:sqlserver://localhost:1433;databaseName=CDR;user=sa;password=Sa$#@!";
      System.out.print("Connecting to SQL Server ... ");
      Connection con1 = DriverManager.getConnection(url);
      log.trace("Connection with Database established");
      String sql = "INSERT INTO Settings (PortNumber, Path) values (?,?)";
      PreparedStatement preparedStatement = con1.prepareStatement(sql);
      preparedStatement.setString(1, String.valueOf(port));
      preparedStatement.setString(2, String.valueOf(p));
      preparedStatement.executeUpdate();
      log.info("Settings data inserted in database");
    } catch (ClassNotFoundException e) {
      log.error(e + ": DB Connection error");
    } 
  }
  
  public void readfile() throws IOException, SQLException {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat("MMddyy");
      SimpleDateFormat tformatter = new SimpleDateFormat("hhmmss");
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      // String url = "jdbc:sqlserver://localhost:1433;databaseName=CDR;user=hbl_user;password=hbl4455&";
      String url = "jdbc:sqlserver://localhost:1433;databaseName=CDR;user=sa;password=Sa$#@!";
      System.out.print("Connecting to SQL Server ... ");
      final Connection con1 = DriverManager.getConnection(url);
      log.trace("Connection with Database established");
      Path path = Paths.get(filename, new String[0]);
      List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      lines.forEach(new Consumer<String>() {
            public void accept(String line) {
              if (line.length() >= 2)
                if (!"\\n".equals(String.valueOf(line.charAt(0)))) {
                  String in_trk_code = "", frl = "", rDate = "", date = "", starttime = "", secdur = "", callingnum = "", dialednum = "", codedial = "", codeused = "";
                  String outcrtid = "", authcode = "", acctcode = "", condcode = "", clgnum = "", incrtid = "", clgptycat = "", ucid = "";
                  Time tDate = null;
                  Timestamp timestamp = null;
                  date = line.substring(0, 6);
                  date = date.substring(0, 2) + "-" + date.substring(2, 4) + "-" + date.substring(4);
                  starttime = line.substring(7, 13);
                  starttime = starttime.substring(0, 2) + ":" + starttime.substring(2, 4) + ":" + starttime.substring(4);
                  String jDate = date + " " + starttime;
                  secdur = line.substring(14, 19);
                  int sec = Integer.parseInt(secdur);
                  callingnum = line.substring(20, 35);
                  dialednum = line.substring(36, 59);
                  codedial = line.substring(60, 64);
                  codeused = line.substring(65, 69);
                  outcrtid = line.substring(70, 73);
                  authcode = line.substring(74, 87);
                  acctcode = line.substring(88, 103);
                  condcode = line.substring(104, 105);
                  clgnum = line.substring(106, 121);
                  incrtid = line.substring(122, 125);
                  clgptycat = line.substring(126, 128);
                  ucid = line.substring(129, 149);
                  frl = line.substring(150, 151);
                  frl = frl.trim();
                  in_trk_code = line.substring(153, 156);
                  in_trk_code = in_trk_code.trim();
                  try {
                    String sql = "INSERT INTO CDR_Reports (start_date, start_time, sec_dur, calling_num, dialed_num, code_dial,code_used, out_crt_id,in_crt_id, auth_code, acct_code, cond_code, clg_num_in_tac,  clg_pty_cat, ucid, frl, in_trk_code, creationdate ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement preparedStatement = con1.prepareStatement(sql);
                    preparedStatement.setString(1, jDate);
                    preparedStatement.setString(2, starttime);
                    preparedStatement.setInt(3, sec);
                    preparedStatement.setString(4, callingnum);
                    preparedStatement.setString(5, dialednum);
                    preparedStatement.setString(6, codedial);
                    preparedStatement.setString(7, codeused);
                    preparedStatement.setString(8, outcrtid);
                    preparedStatement.setString(9, incrtid);
                    preparedStatement.setString(10, authcode);
                    preparedStatement.setString(11, acctcode);
                    preparedStatement.setString(12, condcode);
                    preparedStatement.setString(13, clgnum);
                    preparedStatement.setString(14, clgptycat);
                    preparedStatement.setString(15, ucid);
                    preparedStatement.setString(16, frl);
                    preparedStatement.setString(17, in_trk_code);
                    preparedStatement.setTimestamp(18, timestamp);
                    preparedStatement.executeUpdate();
                  } catch (NumberFormatException|SQLException e) {
                    System.out.println("Error : " + e);
                    Readfileandparse.log.error("Exception: Insertion of Data in Database: " + e);
                  } 
                }  
            }
          });
    } catch (ClassNotFoundException e) {
      log.error(e);
    } 
  }
}
