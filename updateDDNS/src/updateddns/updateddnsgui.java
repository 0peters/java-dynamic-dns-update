/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updateddns;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import sun.rmi.log.ReliableLog;

/**
 *
 * @author petersson
 */
public class updateddnsgui extends javax.swing.JFrame {

    /**
     * Creates new form updateddnsgui
     */
    
    final String PREF_USER = "username";
    final String PREF_PASS = "userpass";
    final String PREF_USERNOIP = "usernamenoip";
    final String PREF_PASSNOIP = "userpassnoip";
    final String PREF_TOKEN = "token";
    final String PREF_IPMANUALMODE = "ipmanualmode";
    final String PREF_IPMANUALVALUE = "ipmanualvalue";
    final String PREF_OBTERIPMODE = "obteripmode";
    final String PREF_OBTERIPURL = "obteripurl";
    final String PREF_OBTERIPREGEX = "obteripregex";
    
    String erroForceUpdate = "";
    ScheduledExecutorService executor = null;
    
    static JFrame updateddnsgui;
    
    public updateddnsgui() {
        initComponents();
        Preferences prefs = Preferences.userNodeForPackage(updateddnsgui.class);
        switch(jcbService.getSelectedItem().toString()){
            case "no-ip":
                jtUser.setText(prefs.get(PREF_USERNOIP, ""));
                jtPass.setText(prefs.get(PREF_PASSNOIP, ""));
                jtToken.setText("");
            break;
            case "dynv6":
                jtUser.setText("");
                jtPass.setText("");
                jtToken.setText(prefs.get(PREF_TOKEN, ""));
            break;
            case "myq-see":
                jtUser.setText(prefs.get(PREF_USER, ""));
                jtPass.setText(prefs.get(PREF_PASS, ""));
                jtToken.setText("");
            break;
        }
        jcbService.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                switch(jcbService.getSelectedItem().toString()){
                    case "no-ip":
                        jtUser.setText(prefs.get(PREF_USERNOIP, ""));
                        jtPass.setText(prefs.get(PREF_PASSNOIP, ""));
                        jtToken.setText("");
                        jtUser.setEnabled(true);
                        jtPass.setEnabled(true);
                        jtToken.setEnabled(false);
                        btLogin.setEnabled(false);
                        jcbHost.removeAllItems();
                        jcbHost.addItem("Não desponível!");
                        jcbHost.setEnabled(false);
                        jtHost.setEnabled(true);
                        btnForceUpdate.setEnabled(true);
                        jcbTime.setEnabled(true);
                        btStart.setEnabled(true);
                    break;
                    case "dynv6":
                        jtUser.setText("");
                        jtPass.setText("");
                        jtToken.setText(prefs.get(PREF_TOKEN, ""));
                        jtUser.setEnabled(false);
                        jtPass.setEnabled(false);
                        jtToken.setEnabled(true);
                        btLogin.setEnabled(false);
                        jtHost.setEnabled(true);
                        jcbHost.removeAllItems();
                        jcbHost.addItem("Não desponível!");
                        jcbHost.setEnabled(false);
                        jtPublicIP.setText("");
                        btnForceUpdate.setEnabled(true);
                        jcbTime.setEnabled(true);
                        btStart.setText("Start");
                        btStart.setEnabled(true);
                    break;
                    case "myq-see":
                        jtUser.setText(prefs.get(PREF_USER, ""));
                        jtPass.setText(prefs.get(PREF_PASS, ""));
                        jtToken.setText("");
                        jtUser.setEnabled(true);
                        jtPass.setEnabled(true);
                        jtToken.setEnabled(false);
                        btLogin.setEnabled(true);
                        jtHost.setEnabled(false);
                        jcbHost.removeAllItems();
                        jcbHost.addItem("Faça login!");
                        jcbHost.setEnabled(false);
                        btnForceUpdate.setEnabled(false);
                        jcbTime.setEnabled(false);
                        btStart.setEnabled(false);
                    break;
                }
            }
        });
        ActionListener rbActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
              AbstractButton rButton = (AbstractButton) actionEvent.getSource();
              //System.out.println("Selected: " + rButton.getText());
              switch(rButton.getText()){
                  case "Padrão":
                      jtUrlIP.setEnabled(false);
                      jtRegexIP.setEnabled(false);
                  break;
                  case "Manual":
                      jtUrlIP.setEnabled(true);
                      jtRegexIP.setEnabled(true);
                  break;
              }
            }
        };
        jrbPadrao.addActionListener(rbActionListener);
        jrbManual.addActionListener(rbActionListener);
        
        if(prefs.getBoolean(PREF_IPMANUALMODE,false)){
            jcbManualIP.setSelected(true);
            jtManualIP.setEnabled(true);
        }
        jtManualIP.setText(prefs.get(PREF_IPMANUALVALUE,""));
        setSelectedButtonText(rbgObterIP,prefs.get(PREF_OBTERIPMODE,"Padrão"));
        switch(prefs.get(PREF_OBTERIPMODE,"Padrão")){
            case "Padrão":
                jtUrlIP.setEnabled(false);
                jtRegexIP.setEnabled(false);
            break;
            case "Manual":
                jtUrlIP.setEnabled(true);
                jtRegexIP.setEnabled(true);
            break;
        }
        jtUrlIP.setText(prefs.get(PREF_OBTERIPURL,"http://checkip.dyndns.com"));
        jtRegexIP.setText(prefs.get(PREF_OBTERIPREGEX,"\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"));
        // "\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b"
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rbgObterIP = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jtPass = new javax.swing.JPasswordField();
        jPanel2 = new javax.swing.JPanel();
        jtUser = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jcbHost = new javax.swing.JComboBox<>();
        jtHost = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btLogin = new javax.swing.JButton();
        btnForceUpdate = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btStart = new javax.swing.JButton();
        jcbTime = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jtPublicIP = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jcbService = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jtToken = new javax.swing.JPasswordField();
        btStorePass = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jtManualIP = new javax.swing.JTextField();
        jcbManualIP = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jrbPadrao = new javax.swing.JRadioButton();
        jrbManual = new javax.swing.JRadioButton();
        jtRegexIP = new javax.swing.JTextField();
        jtUrlIP = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnSaveConfigs = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DDNS Update Client");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Password"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jtPass)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("User"));

        jtUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtUserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jtUser, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("host"));

        jcbHost.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Faça login!" }));
        jcbHost.setEnabled(false);
        jcbHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbHostActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jcbHost, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 3, Short.MAX_VALUE))
            .addComponent(jtHost)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jcbHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel1.setText("DDNS Update Client");

        btLogin.setText("Login");
        btLogin.setEnabled(false);
        btLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLoginActionPerformed(evt);
            }
        });

        btnForceUpdate.setText("Force Update");
        btnForceUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnForceUpdateActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Auto Update"));

        btStart.setText("Start");
        btStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btStartActionPerformed(evt);
            }
        });

        jcbTime.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1 min", "2 min", "5 min", "15 min", "30 min", "1 hora" }));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jcbTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btStart, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(21, 21, 21))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jcbTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btStart))
        );

        jLabel2.setText("Last public IP:");

        jtPublicIP.setEditable(false);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Serviço"));

        jcbService.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "no-ip", "dynv6", "myq-see" }));
        jcbService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbServiceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jcbService, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jcbService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("TOKEN"));

        jtToken.setEnabled(false);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jtToken, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jtToken, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btStorePass.setText("Salvar usuário");
        btStorePass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btStorePassActionPerformed(evt);
            }
        });

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Definir IP manualmente"));

        jtManualIP.setEnabled(false);

        jcbManualIP.setText("manual");
        jcbManualIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbManualIPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jcbManualIP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jtManualIP, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jtManualIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jcbManualIP))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Regex para obter IP"));

        rbgObterIP.add(jrbPadrao);
        jrbPadrao.setSelected(true);
        jrbPadrao.setText("Padrão");

        rbgObterIP.add(jrbManual);
        jrbManual.setText("Manual");

        jtRegexIP.setEnabled(false);
        jtRegexIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtRegexIPActionPerformed(evt);
            }
        });

        jtUrlIP.setEnabled(false);
        jtUrlIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtUrlIPActionPerformed(evt);
            }
        });

        jLabel3.setText("URL IP:");

        jLabel4.setText("REGEX:");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jrbPadrao)
                        .addGap(10, 10, 10)
                        .addComponent(jrbManual)
                        .addContainerGap())
                    .addComponent(jtUrlIP)
                    .addComponent(jtRegexIP)))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jrbPadrao)
                    .addComponent(jrbManual))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtUrlIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtRegexIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)))
        );

        btnSaveConfigs.setText("Save Configs");
        btnSaveConfigs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveConfigsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btStorePass)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btLogin))
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtPublicIP, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnForceUpdate)
                            .addComponent(btnSaveConfigs))
                        .addGap(39, 39, 39))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btStorePass, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jtPublicIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(btnSaveConfigs)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnForceUpdate))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jtUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtUserActionPerformed

    private void btLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoginActionPerformed
        // TODO add your handling code here:
        if (btLogin.getText().equals("Login")){
            Preferences prefs = Preferences.userNodeForPackage(updateddnsgui.class);
            prefs.put(PREF_USER, jtUser.getText() );
            prefs.put(PREF_PASS, new String(jtPass.getPassword()));
            testLogin();
        } else {
            btLogin.setText("Login");
            jcbHost.removeAllItems();
            jcbHost.addItem("Faça login!");
            jcbHost.setEnabled(false);
            jtPublicIP.setText("");
            btnForceUpdate.setEnabled(false);
            jcbTime.setEnabled(false);
            btStart.setText("Start");
            btStart.setEnabled(false);
        }
        //getHostList();
    }//GEN-LAST:event_btLoginActionPerformed

    private void jcbServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbServiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jcbServiceActionPerformed

    private void btStorePassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btStorePassActionPerformed
        // TODO add your handling code here:
        Preferences prefs = Preferences.userNodeForPackage(updateddnsgui.class);
        switch(jcbService.getSelectedItem().toString()){
            case "no-ip":
                prefs.put(PREF_USERNOIP, jtUser.getText() );
                prefs.put(PREF_PASSNOIP, new String(jtPass.getPassword()));
            break;
            case "dynv6":
               prefs.put(PREF_TOKEN, new String(jtToken.getPassword()));
            break;
            case "myq-see":
                prefs.put(PREF_USER, jtUser.getText() );
                prefs.put(PREF_PASS, new String(jtPass.getPassword()));
            break;
        }
        
    }//GEN-LAST:event_btStorePassActionPerformed

    private void jcbHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbHostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jcbHostActionPerformed

    private void jtUrlIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtUrlIPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtUrlIPActionPerformed

    private void jtRegexIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtRegexIPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtRegexIPActionPerformed

    private void btStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btStartActionPerformed
        // TODO add your handling code here:

        int time = 1;
        switch(jcbTime.getSelectedItem().toString()){
            case("1 min"):
            time = 1;
            break;
            case("2 min"):
            time = 2;
            break;
            case("5 min"):
            time = 5;
            break;
            case("15 min"):
            time = 15;
            break;
            case("30 min"):
            time = 30;
            break;
            case("1 hora"):
            time = 60;
            break;
        }
        Runnable runnable = new Runnable() {
            public void run() {
                if(btStart.getText().equals("Start")){
                    System.out.println("STOP");
                    executor.shutdown();
                } else {
                    atualizaIP();
                }
            }
        };
        if(btStart.getText().equals("Start")){
            btStart.setText("Stop");
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(runnable, 0, time, TimeUnit.MINUTES);
        } else {
            btStart.setText("Start");
            executor.shutdown();
        }
    }//GEN-LAST:event_btStartActionPerformed

    private void btnForceUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForceUpdateActionPerformed
        // TODO add your handling code here:
        atualizaIP();
        if(!erroForceUpdate.equals("")){
            JOptionPane.showMessageDialog(null, erroForceUpdate);
            erroForceUpdate = "";
        }
    }//GEN-LAST:event_btnForceUpdateActionPerformed

    private void jcbManualIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbManualIPActionPerformed
        // TODO add your handling code here:
        if(jcbManualIP.isSelected()){
            jtManualIP.setEnabled(true);
        } else {
            jtManualIP.setEnabled(false);
        }
    }//GEN-LAST:event_jcbManualIPActionPerformed

    private void btnSaveConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveConfigsActionPerformed
        // TODO add your handling code here:
        Preferences prefs = Preferences.userNodeForPackage(updateddnsgui.class);
        prefs.putBoolean(PREF_IPMANUALMODE, jcbManualIP.isSelected());
        prefs.put(PREF_IPMANUALVALUE, jtManualIP.getText());
        prefs.put(PREF_OBTERIPMODE, getSelectedButtonText());
        prefs.put(PREF_OBTERIPURL, jtUrlIP.getText());
        prefs.put(PREF_OBTERIPREGEX, jtRegexIP.getText());
    }//GEN-LAST:event_btnSaveConfigsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(updateddnsgui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(updateddnsgui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(updateddnsgui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(updateddnsgui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                updateddnsgui = new updateddnsgui();
                updateddnsgui.setVisible(true);
                updateddnsgui.setResizable(false);
            }
        });
    }
    
    public String getSelectedButtonText() {
        ButtonGroup buttonGroup = rbgObterIP;
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }
    
    public void setSelectedButtonText(ButtonGroup buttonGroup, String text) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.getText().equals(text)) {
                button.setSelected(true);
                return;
            }
        }
    }
       
    public void testLogin(){
        String user = jtUser.getText();
        String pwd = new String(jtPass.getPassword());
        try {
                logFile("testLogin init");
                String urlStr = "http://myq-see.com/client/usercheck.aspx";
//                switch(jcbService.getSelectedItem().toString()){
//                    case "no-ip":
//                        urlStr = "http://dynupdate.no-ip.com/nic/update";
//                    break;
//                    case "dynv6":
//                        urlStr = "http://ipv4.dynv6.com/api/update";
//                    break;
//                    case "myq-see":
//                        urlStr = "http://myq-see.com/client/usercheck.aspx";
//                    break;
//                }
                URL url = new URL(urlStr);
                String encoding = Base64.getEncoder().encodeToString((user+":"+pwd).getBytes("UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty  ("Authorization", "Basic " + encoding);
                InputStream content = (InputStream)connection.getInputStream();
                BufferedReader in   = 
                    new BufferedReader (new InputStreamReader (content));
                String line;
                String result = "";
                line = in.readLine();
                result = line;
//                while ((line = in.readLine()) != null) {
//                    result += "\n" + line;
//                }
                logFile("testLogin result: " + result);
                if(result.equals("okuser")){
                    btLogin.setText("Logout");
                    getHostList();
                } else if(result.equals("nouser")){
                    JOptionPane.showMessageDialog(null,"Verifique o usuário e senha!");
                } else {
                    JOptionPane.showMessageDialog(null,"Não foi possível conectar ao serviço");
                }
                //JOptionPane.showMessageDialog(null, result);
            } catch(Exception e) {
                e.printStackTrace();
            }
    }
    
    public String getHostList(){
        String list = "";
        String user = jtUser.getText();
        String pwd = new String(jtPass.getPassword());
        try {
            logFile("getHostList init");
            URL url = new URL ("http://myq-see.com/client/domlook.aspx");
            String encoding = Base64.getEncoder().encodeToString((user+":"+pwd).getBytes("UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   = 
                new BufferedReader (new InputStreamReader (content));
            String line;
            String result = "";
            while ((line = in.readLine()) != null) {
                result += line + "\n";
            }
            Pattern PATTERN = Pattern.compile("<url>.*<\\/url>", Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES);
            Matcher matcher = PATTERN.matcher(result);
            if(matcher.find()){
                logFile("getHostList result: List ok");
                jcbHost.setEnabled(true);
                jcbHost.removeAllItems();
                btnForceUpdate.setEnabled(true);
                jcbTime.setEnabled(true);
                btStart.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(null,"Não foi possível obter a lista de hosts");
            }
            while(matcher.find()) {
                jcbHost.addItem(result.substring(matcher.start()+5, matcher.end()-6));
            }
            //JOptionPane.showMessageDialog(null, result);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private void atualizaIP(){
//        settings settings = new settings();
//        String user = settings.getUser();
//        String pwd = settings.getUser();
//        String host = "ftpserverext.myq-see.com";

        String user = jtUser.getText();
        String pwd = new String(jtPass.getPassword());
        String token = new String(jtToken.getPassword());
        String host = "";
        switch(jcbService.getSelectedItem().toString()){
                    case "no-ip":
                        host = jtHost.getText();
                    break;
                    case "dynv6":
                        host = jtHost.getText();
                    break;
                    case "myq-see":
                        host = jcbHost.getSelectedItem().toString();
                    break;
                }
        String currentIP = "";
        erroForceUpdate = "";
        
        System.out.println("Iniciando Atualização");
        
        Boolean manualIPmode = jcbManualIP.isSelected();
        String manualIP = jtManualIP.getText();
        String obterIPmode =  getSelectedButtonText();
        String obterIPurl =  jtUrlIP.getText();
        String obterIPregex =  jtRegexIP.getText();
        
        if(manualIPmode){
            currentIP = manualIP;
        } else {
            currentIP = getPublicIP(obterIPmode, obterIPurl, obterIPregex);
        }
        jtPublicIP.setText(currentIP);
        
        if (!currentIP.equals("") && currentIP != null){
            try {
                logFile("atualizaIP init");
                String urlStr = "http://myq-see.com/client/usercheck.aspx";
                switch(jcbService.getSelectedItem().toString()){
                    case "no-ip":
                        urlStr = "http://dynupdate.no-ip.com/nic/update?hostname="+host+"&myip="+currentIP;
                    break;
                    case "dynv6":
                        urlStr = "http://ipv4.dynv6.com/api/update?hostname="+host+"&ipv4="+currentIP+"&token="+token;
                    break;
                    case "myq-see":
                        urlStr = "http://myq-see.com/nic/update/default.aspx?hostname="+host+"&myip="+currentIP;
                    break;
                }
                URL url = new URL(urlStr);
//                URL url = new URL ("http://myq-see.com/nic/update/default.aspx?hostname="+host+"&myip="+currentIP);
                String encoding = Base64.getEncoder().encodeToString((user+":"+pwd).getBytes("UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                
                if (jcbService.getSelectedItem().toString().equals("no-ip") || jcbService.getSelectedItem().toString().equals("myq-see")){
                    connection.setRequestProperty("Authorization", "Basic " + encoding);
                }
                
                InputStream content = (InputStream)connection.getInputStream();
                BufferedReader in   = 
                    new BufferedReader (new InputStreamReader (content));
                String line;
                String result = "";
                while ((line = in.readLine()) != null) {
                    result += line;
                    System.out.println(line);
                }
                logFile("atualizaIP host: "+host+" resultt: "+result);
                //JOptionPane.showMessageDialog(null, result);
            } catch(Exception e) {
                e.printStackTrace();
                try{
                logFile("Erro ao atualizar IP");
                } catch(Exception f){
                    f.printStackTrace();
                }
            }
        } else {
            erroForceUpdate = "Não foi possível obter o ip público";
        }
        System.out.println("public IP: " + currentIP);
    }
    
    public String getPublicIP(String mode, String urlIP, String regex){
        String IPpublic = "";
        try {
            logFile("getPublicIP init");
        } catch (IOException ex) {
            Logger.getLogger(updateddnsgui.class.getName()).log(Level.SEVERE, null, ex);
        }
        String check = getPageStatus(mode,urlIP);
        String IPregex = "\\\"IPAddress\\\"\\s\\:\\s\\\"\\d\\d?\\d?\\.\\d\\d?\\d?\\.\\d\\d?\\d?\\.\\d\\d?\\d?\\\"";
        if (!mode.equals("Padrão")){
            IPregex = regex;
        }
        Pattern PATTERN = Pattern.compile(IPregex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = PATTERN.matcher(check);
        //matcher.find();
        if (!check.equals("") && matcher.find()){
            
            String publicIP = check.substring(matcher.start(0), matcher.end(0));
            if (!mode.equals("Padrão")){
                IPpublic = publicIP;
            } else{
                IPpublic = publicIP.substring(15,publicIP.length()-1);
            }
            //JOptionPane.showMessageDialog(null, IPpublic);
        }
        try {
            logFile("getPublicIP result: "+IPpublic);
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                logFile(ex.getMessage());
            } catch (IOException ex1) {
                Logger.getLogger(updateddnsgui.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(updateddnsgui.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IPpublic;
    }
    public static String getPageStatus(String mode, String urlIP){
        final int SOCKET_TIMEOUT_MS = 10000;
        HttpURLConnection urlConnection = null;
        String url = "http://192.168.25.1/pt_BR/admin/services_status.htm";
        if(!mode.equals("Padrão"))
            url = urlIP;
        try {
            URL urlModem = new URL(url);
            urlConnection = (HttpURLConnection) urlModem.openConnection();
            urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            //return String.valueOf(urlConnection.getResponseCode());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuffer html = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }
            in.close();

            return html.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    String dateTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    public void logFile(String log) throws IOException{
        Writer output;
        output = new BufferedWriter(new FileWriter("ddnsclient.log", true));  //clears file every time
        output.append(dateTime() + " > "+log+"\n");
        output.close();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btLogin;
    private javax.swing.JButton btStart;
    private javax.swing.JButton btStart1;
    private javax.swing.JButton btStorePass;
    private javax.swing.JButton btnForceUpdate;
    private javax.swing.JButton btnSaveConfigs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JComboBox<String> jcbHost;
    private javax.swing.JCheckBox jcbManualIP;
    private javax.swing.JComboBox<String> jcbService;
    private javax.swing.JComboBox<String> jcbTime;
    private javax.swing.JComboBox<String> jcbTime1;
    private javax.swing.JRadioButton jrbManual;
    private javax.swing.JRadioButton jrbPadrao;
    private javax.swing.JTextField jtHost;
    private javax.swing.JTextField jtManualIP;
    private javax.swing.JPasswordField jtPass;
    private javax.swing.JTextField jtPublicIP;
    private javax.swing.JTextField jtRegexIP;
    private javax.swing.JPasswordField jtToken;
    private javax.swing.JTextField jtUrlIP;
    private javax.swing.JTextField jtUser;
    private javax.swing.ButtonGroup rbgObterIP;
    // End of variables declaration//GEN-END:variables
}
