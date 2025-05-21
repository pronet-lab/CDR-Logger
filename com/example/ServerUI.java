package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.border.Border;
//import org.apache.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUI extends JFrame {
  private static volatile int r = 0;
  
  private static int portNumber = 0;
  
  private static boolean stop = false;
  
  private static Socket clientSocket;
  
  private static ServerSocket serverSocket;
  
  public static Path movefrom = Paths.get(System.getProperty("user.dir"), new String[0]);
  
  public static Path target;
  
  // public static final Logger log = Logger.getLogger(ServerUI.class);
  public static final Logger log = LoggerFactory.getLogger(ServerUI.class); // SLF4J
  
  public static Date currfiledate;
  
  public static Date date;
  
  public static String dat;
  
  public static String filename;
  
  public static File f;
  
  public static SimpleDateFormat formatter;
  
  private JLabel browsepathbutton;
  
  private JLabel changebutton;
  
  private JLabel getprevious;
  
  public static JFileChooser jFileChooser1;
  
  private JLabel jLabel1;
  
  private static JLabel jLabel12;
  
  private static JLabel jLabel13;
  
  private JLabel jLabel2;
  
  private JLabel jLabel4;
  
  private static JLabel jLabel5;
  
  private static JLabel jLabel6;
  
  private static JLabel jLabel7;
  
  private JPanel jPanel1;
  
  private JScrollPane jScrollPane1;
  
  private static JTextArea jTextArea1;
  
  private static JTextField pathfield;
  
  private static JTextField portfield;
  
  private JLabel setbutton;
  
  private JLabel startstop;
  
  public ServerUI() {
    initComponents();
  }
  
//  public static void closeconnections() throws IOException {
//    log.info("Closing Connections");
//    try {
//      clientSocket.close();
//      serverSocket.close();
//    } catch (IOException e) {
//      System.out.println("Potential issue: Program was closed before first connection was made");
//      log.error(e + ": Potential issue: Program was closed before first connection was made");
//    }
//  }

  public static void closeconnections() throws IOException {
    log.info("Closing Connections");
    try {
      if (clientSocket != null && !clientSocket.isClosed()) {
        clientSocket.close();
      }
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
      System.out.println("Potential issue: Program was closed before first connection was made");
      log.error(e + ": Potential issue: Program was closed before first connection was made");
    }
  }

  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException|NullPointerException e) {
      return false;
    } 
    return true;
  }
  
  private static boolean available(int port) {
    Socket s = null;
    try {
      s = new Socket("localhost", port);
      return false;
    } catch (IOException e) {
      return true;
    } finally {
      if (s != null)
        try {
          s.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }  
    } 
  }
  
  public static void appendStrToFile(String fileName, String str) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
      out.write("\n");
      out.write(str);
      out.close();
    } catch (IOException e) {
      log.error(e + ": Writing data in File.");
    } 
  }
  
  public static void createandrunserver() throws SocketException, IOException, SQLException {
    stop = true;
    while (stop == true) {
      stop = false;
      while (r == 0);
      jTextArea1.append("Server started. Listening on Port " + portNumber);
      log.trace("Server started");
      if (!available(portNumber))
        serverSocket.close();
      serverSocket = new ServerSocket(portNumber);
      try {
        clientSocket = serverSocket.accept();
        if (clientSocket == null) {
          log.warn("clientSocket is null. Skipping...");
          continue;
        }
      } catch (SocketException e) {
        log.info("Server socket was closed while waiting for client — stopping server");
        closeconnections();
        break;
      } catch (IOException e) {
        log.error("Unexpected IO error in accept()", e);
        closeconnections();
      }
//      catch (IOException e) {
//        log.error("ClientSocket issue", e);
//        closeconnections();
//        continue;
//      }

//      serverSocket = new ServerSocket(portNumber);
//      try {
//        clientSocket = serverSocket.accept();
//      } catch (IOException e) {
//        System.out.println("ClientSocket issue");
//        log.error(e + "ClientSocket issue");
//        closeconnections();
//      }

      currfiledate = new Date();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
        jTextArea1.append("\nClient connected");
        jTextArea1.setCaretPosition(jTextArea1.getText().length());
        log.trace("Client connected");
        formatter = new SimpleDateFormat("ddMMyyyy HHmmss");
        date = currfiledate;
        dat = formatter.format(date);
        filename = "Log_" + dat;
        f = new File(filename + ".txt");
        if (!f.exists()) {
          f.createNewFile();
          log.info("Log file created");
        } else {
          log.trace("File already exists");
        } 
        TimerTask task = new TimerTask() {
            public synchronized void run() {
              try {
                Path target1 = Paths.get(String.valueOf(ServerUI.target) + "\\" + ServerUI.filename + ".txt", new String[0]);
                Path movefrom1 = Paths.get(String.valueOf(ServerUI.movefrom) + "\\" + ServerUI.filename + ".txt", new String[0]);
                Readfileandparse rp = new Readfileandparse(movefrom1, ServerUI.filename + ".txt");
                rp.readfile();
                ServerUI.log.info("Data entered in Database");
                Files.move(movefrom1, target1, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
                ServerUI.log.info("File moved to archive");
                ServerUI.currfiledate = new Date();
                ServerUI.dat = ServerUI.formatter.format(ServerUI.currfiledate);
                ServerUI.filename = "Log_" + ServerUI.dat;
                ServerUI.f = new File(ServerUI.filename + ".txt");
                ServerUI.f.createNewFile();
              } catch (IOException|SQLException ex) {
              // Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, (String)null, ex);
                LoggerFactory.getLogger(ServerUI.class).error("Exception caught", ex);
              }
            }
          };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 300000L, 300000L);
        Readfileandparse sp = new Readfileandparse();
        sp.settings(portNumber, target);
        while (!stop) {
          String inputLine;
          if (((!stop ? 1 : 0) | (((inputLine = in.readLine()) != null) ? 1 : 0)) != 0) {
            if (inputLine.length() > 15) {
              if (inputLine.length() > 158)
                inputLine = inputLine.substring(3); 
              appendStrToFile(filename + ".txt", inputLine);
              if (jTextArea1.getText().length() > 6000)
                jTextArea1.setText(""); 
              jTextArea1.append("\n" + inputLine);
              jTextArea1.setCaretPosition(jTextArea1.getText().length());
            } 
            continue;
          } 
          log.info("Connection was closed");
        } 
        jLabel13.setVisible(false);
        jLabel7.setVisible(false);
      } catch (IOException e) {
        log.warn(e + " (Stopped by user or LAN disconnected)");
        formatter = new SimpleDateFormat("ddMMyyyy HHmmss");
        dat = formatter.format(currfiledate);
        filename = "Log_" + dat;
        Path target1 = Paths.get(String.valueOf(target) + "\\" + filename + ".txt", new String[0]);
        Path movefrom1 = Paths.get(String.valueOf(movefrom) + "\\" + filename + ".txt", new String[0]);
        try {
          Readfileandparse rp = new Readfileandparse(movefrom1, filename + ".txt");
          rp.readfile();
          Files.move(movefrom1, target1, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
          log.info("File moved to archive");
        } catch (IOException ea) {
          log.error("An I/O error occurred", ea);
          // log.error(ea);
        } 
        log.info("Half written file data entered in Database");
        closeconnections();
        jTextArea1.append("\nDisconnected\n\n");
        jTextArea1.setCaretPosition(jTextArea1.getText().length());
      } 
      stop = true;
    } 
  }
  
  private void initComponents() {
    jFileChooser1 = new JFileChooser();
    this.jPanel1 = new JPanel();
    portfield = new JTextField();
    this.jLabel1 = new JLabel();
    this.jLabel2 = new JLabel();
    pathfield = new JTextField();
    this.jScrollPane1 = new JScrollPane();
    jTextArea1 = new JTextArea();
    jLabel12 = new JLabel();
    jLabel13 = new JLabel();
    this.jLabel4 = new JLabel();
    jLabel5 = new JLabel();
    jLabel6 = new JLabel();
    jLabel7 = new JLabel();
    this.setbutton = new JLabel();
    this.changebutton = new JLabel();
    this.getprevious = new JLabel();
    this.browsepathbutton = new JLabel();
    this.startstop = new JLabel();
    jFileChooser1.setCurrentDirectory(new File("."));
    jFileChooser1.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            ServerUI.this.jFileChooser1ActionPerformed(evt);
          }
        });
    setDefaultCloseOperation(0);
    setTitle("PRONET CDR logger");
    setIconImage((new ImageIcon(getClass().getResource("pronetlogo.png"))).getImage());
    setLocation(new Point(200, 100));
    addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent evt) {
            ServerUI.this.formWindowClosing(evt);
          }
        });
    this.jPanel1.setBackground(new Color(220, 220, 220));
    this.jPanel1.setMaximumSize(new Dimension(2000, 2000));
    this.jPanel1.setPreferredSize(new Dimension(750, 360));
    this.jPanel1.setVerifyInputWhenFocusTarget(false);
    portfield.setBackground(new Color(240, 240, 240));
    this.jLabel1.setText("Enter Port");
    this.jLabel2.setText("Log File Path");
    pathfield.setDisabledTextColor(new Color(102, 102, 102));
    pathfield.setEnabled(false);
    pathfield.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            ServerUI.this.pathfieldActionPerformed(evt);
          }
        });
    jTextArea1.setEditable(false);
    jTextArea1.setBackground(new Color(245, 245, 235));
    jTextArea1.setColumns(20);
    jTextArea1.setRows(5);
    jTextArea1.setBorder((Border)null);
    this.jScrollPane1.setViewportView(jTextArea1);
    jLabel12.setFont(new Font("Tahoma", 0, 14));
    jLabel12.setForeground(new Color(255, 0, 0));
    jLabel12.setText("*");
    jLabel13.setIcon(new ImageIcon(getClass().getResource("/com/example/RunningRed.png")));
    jLabel13.setText("");
    jLabel13.setMaximumSize(new Dimension(20, 20));
    jLabel13.setMinimumSize(new Dimension(20, 20));
    jLabel13.setPreferredSize(new Dimension(20, 20));
    this.jLabel4.setFont(new Font("Tahoma", 0, 14));
    this.jLabel4.setForeground(new Color(255, 0, 0));
    this.jLabel4.setText("*");
    jLabel5.setText("Please enter only numeric values in Port field");
    jLabel6.setText("No previous record found");
    jLabel7.setForeground(new Color(229, 26, 39));
    jLabel7.setText("Running...");
    this.setbutton.setBackground(new Color(226, 29, 36));
    this.setbutton.setForeground(new Color(255, 255, 255));
    this.setbutton.setHorizontalAlignment(0);
    this.setbutton.setText("Set");
    this.setbutton.setBorder(BorderFactory.createLineBorder(new Color(153, 0, 0)));
    this.setbutton.setMaximumSize(new Dimension(93, 24));
    this.setbutton.setOpaque(true);
    this.setbutton.setPreferredSize(new Dimension(93, 22));
    this.setbutton.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            ServerUI.this.setbuttonMouseClicked(evt);
          }
        });
    this.changebutton.setBackground(new Color(226, 29, 36));
    this.changebutton.setForeground(new Color(255, 255, 255));
    this.changebutton.setHorizontalAlignment(0);
    this.changebutton.setText("Change");
    this.changebutton.setBorder(BorderFactory.createLineBorder(new Color(153, 0, 0)));
    this.changebutton.setEnabled(false);
    this.changebutton.setOpaque(true);
    this.changebutton.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            ServerUI.this.changebuttonMouseClicked(evt);
          }
        });
    this.getprevious.setBackground(new Color(226, 29, 39));
    this.getprevious.setForeground(new Color(255, 255, 255));
    this.getprevious.setHorizontalAlignment(0);
    this.getprevious.setText("Get Previous config Values");
    this.getprevious.setBorder(BorderFactory.createLineBorder(new Color(153, 0, 0)));
    this.getprevious.setOpaque(true);
    this.getprevious.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            ServerUI.this.getpreviousMouseClicked(evt);
          }
        });
    this.browsepathbutton.setBackground(new Color(229, 26, 39));
    this.browsepathbutton.setForeground(new Color(255, 255, 255));
    this.browsepathbutton.setHorizontalAlignment(0);
    this.browsepathbutton.setText("Browse");
    this.browsepathbutton.setBorder(BorderFactory.createLineBorder(new Color(153, 0, 0)));
    this.browsepathbutton.setHorizontalTextPosition(0);
    this.browsepathbutton.setOpaque(true);
    this.browsepathbutton.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            ServerUI.this.browsepathbuttonMouseClicked(evt);
          }
        });
    this.startstop.setBackground(new Color(226, 29, 39));
    this.startstop.setForeground(new Color(255, 255, 255));
    this.startstop.setHorizontalAlignment(0);
    this.startstop.setText("Start");
    this.startstop.setBorder(BorderFactory.createLineBorder(new Color(153, 0, 0)));
    this.startstop.setEnabled(false);
    this.startstop.setOpaque(true);
    this.startstop.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            ServerUI.this.startstopMouseClicked(evt);
          }
        });
    GroupLayout jPanel1Layout = new GroupLayout(this.jPanel1);
    this.jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(jPanel1Layout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
          .addGap(23, 23, 23)
          .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addComponent(this.jLabel2, -2, 85, -2)
              .addGap(18, 18, 18)
              .addComponent(pathfield, -2, 448, -2)
              .addGap(12, 12, 12)
              .addComponent(this.jLabel4)
              .addGap(18, 18, 18)
              .addComponent(this.browsepathbutton, -2, 99, -2))
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addComponent(this.jLabel1, -2, 85, -2)
              .addGap(18, 18, 18)
              .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                  .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                      .addComponent(this.setbutton, -2, -1, -2)
                      .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                      .addComponent(this.changebutton, -1, -1, 32767))
                    .addComponent(this.getprevious, -2, 185, -2))
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel6))
                .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(portfield, -2, 185, -2)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel12, -2, 16, -2)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(jLabel5)))))
          .addContainerGap(118, 32767))
        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
          .addGap(0, 0, 32767)
          .addComponent(jLabel7)
          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
          .addComponent(jLabel13, -2, 28, -2)
          .addGap(28, 28, 28)
          .addComponent(this.startstop, -2, 100, -2)
          .addGap(365, 365, 365))
        .addGroup(jPanel1Layout.createSequentialGroup()
          .addContainerGap()
          .addComponent(this.jScrollPane1)
          .addContainerGap()));
    jPanel1Layout.setVerticalGroup(jPanel1Layout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
          .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addGap(15, 15, 15)
              .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(this.jLabel2)
                .addComponent(this.jLabel4)
                .addComponent(this.browsepathbutton, -2, 22, -2)))
            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
              .addContainerGap()
              .addComponent(pathfield, -2, -1, -2)))
          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
          .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
              .addComponent(portfield, -2, -1, -2)
              .addComponent(this.jLabel1, -1, -1, 32767))
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel12)
                .addComponent(jLabel5))
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, 32767)))
          .addGap(18, 18, 18)
          .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addGap(34, 34, 34)
              .addComponent(jLabel6))
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.changebutton, -2, 22, -2)
                .addComponent(this.setbutton, -2, -1, -2))
              .addGap(7, 7, 7)
              .addComponent(this.getprevious, -2, 22, -2)))
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 51, 32767)
          .addComponent(this.jScrollPane1, -2, 212, -2)
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(jLabel13, -2, 28, -2)
            .addComponent(jLabel7)
            .addComponent(this.startstop, -2, 28, -2))
          .addGap(9, 9, 9)));
    jLabel12.setVisible(false);
    jLabel13.setVisible(false);
    this.jLabel4.setVisible(false);
    jLabel5.setVisible(false);
    jLabel6.setVisible(false);
    jLabel7.setVisible(false);
    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(this.jPanel1, -1, 829, 32767));
    layout.setVerticalGroup(layout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(this.jPanel1, -1, 443, 32767));
    pack();
  }
  
  private void jFileChooser1ActionPerformed(ActionEvent evt) {}
  
  private void startstopMouseClicked(MouseEvent evt) {
    if (this.startstop.isEnabled())
      if ("Start".equals(this.startstop.getText())) {
        this.changebutton.setEnabled(false);
        jLabel12.setVisible(false);
        jLabel13.setIcon(new ImageIcon(getClass().getResource("/com/example/RunningRed.png")));
        jLabel13.setVisible(true);
        jLabel7.setVisible(true);
        this.startstop.setText("Stop");
        this.changebutton.setEnabled(false);
        r = 1;
        stop = false;
        portfield.setEnabled(false);
        pathfield.setEnabled(false);
      } else {
        this.changebutton.setEnabled(true);
        jLabel13.setVisible(false);
        jLabel7.setVisible(false);
        stop = true;
        try {
          closeconnections();
          log.info("Stopped by User");
        } catch (IOException ex) {
          log.error(ex + " when stopped by user");
        } 
        r = 0;
        this.startstop.setText("Start");
      }  
  }
  
  private void browsepathbuttonMouseClicked(MouseEvent evt) {
    if (this.browsepathbutton.isEnabled()) {
      jFileChooser1.setFileSelectionMode(1);
      int returnVal = jFileChooser1.showOpenDialog(this.jPanel1);
      if (returnVal == 0)
        pathfield.setText(jFileChooser1.getSelectedFile().getAbsolutePath()); 
    } 
  }
  
  private void getpreviousMouseClicked(MouseEvent evt) {
    SQLdatabase values = new SQLdatabase();
    values.getfrom();
    if (values.port != 0) {
      log.trace("Getting previous values from database");
      setfields(String.valueOf(values.path), String.valueOf(values.port));
    } else {
      jLabel6.setVisible(true);
    } 
  }
  
  private void changebuttonMouseClicked(MouseEvent evt) {
    stop = true;
    r = 0;
    portfield.setEnabled(true);
    this.changebutton.setEnabled(false);
    this.setbutton.setEnabled(true);
    this.startstop.setEnabled(false);
    this.browsepathbutton.setEnabled(true);
  }
  
  private void setbuttonMouseClicked(MouseEvent evt) {
    jLabel6.setVisible(false);
    if ("".equals(portfield.getText())) {
      jLabel12.setVisible(true);
    } else {
      jLabel12.setVisible(false);
    } 
    if ("".equals(pathfield.getText())) {
      this.jLabel4.setVisible(true);
    } else {
      this.jLabel4.setVisible(false);
    }
    // if ((jLabel12.isVisible() | this.jLabel4.isVisible()) == 0)
    if (!jLabel12.isVisible() && !this.jLabel4.isVisible())
        if (isInteger(portfield.getText())) {
        stop = false;
        jLabel5.setVisible(false);
        jLabel12.setVisible(false);
        this.jLabel4.setVisible(false);
        target = Paths.get(String.valueOf(pathfield.getText()), new String[0]);
        portNumber = Integer.parseInt(portfield.getText());
        portfield.setEnabled(false);
        pathfield.setEnabled(false);
        this.setbutton.setEnabled(false);
        this.changebutton.setEnabled(true);
        this.startstop.setEnabled(true);
        this.browsepathbutton.setEnabled(false);
      } else {
        jLabel5.setVisible(true);
      }  
  }
  
  private void pathfieldActionPerformed(ActionEvent evt) {}
  
  private void formWindowClosing(WindowEvent evt) {
    if (JOptionPane.showConfirmDialog(null, "Are you sure to close the program?", "Really Closing?", 0, 3) == 0)
      if ("Start".equals(this.startstop.getText())) {
        System.exit(0);
      } else {
        JOptionPane.showMessageDialog(null, "Please stop the connection first", "Warning", 1);
      }  
  }
  
  public static void setfields(String path, String port) {
    portNumber = Integer.parseInt(port);
    target = Paths.get(path, new String[0]);
    portfield.setText(port);
    pathfield.setText(path);
  }
  
  public static void main(String[] args) throws SQLException, IOException {
    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Metal".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        } 
      } 
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
      // Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, (String)null, ex);
      log.error("UI initialization failed", ex);
    }
    EventQueue.invokeLater(() -> (new ServerUI()).setVisible(true));
    log.trace("Calling createandrunserver");
    createandrunserver();
  }
}
