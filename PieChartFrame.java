
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PieChartFrame extends javax.swing.JFrame {

    private Connection connection;
    private String currentAmountType;

    public PieChartFrame() {
        initComponents();
        connectToDatabase();
        loadData();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/pie_chart_db?useSSL=false";
            connection = DriverManager.getConnection(url, "root", "root");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Clear existing data

        try ( PreparedStatement stmt = connection.prepareStatement("SELECT item_name, amount FROM items");  ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String item = rs.getString("item_name");
                String amount = rs.getString("amount");
                model.addRow(new Object[]{item, amount});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidItemName(String itemName) {
        // Check if the item name is not empty and starts with an alphabet character
        return !itemName.isEmpty() && Character.isLetter(itemName.charAt(0));
    }

    private void addItem() {
        String itemName = jTextField1.getText();
        String amountType = (String) jComboBox1.getSelectedItem();
        String amountText = jTextField2.getText();

        if (itemName.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name and Amount are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidItemName(itemName)) {
            JOptionPane.showMessageDialog(this, "Item Name must start with an alphabet character.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentAmountType != null && !currentAmountType.equals(amountType)) {
            JOptionPane.showMessageDialog(this, "Amount type must be constant until reset.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validateAmount(amount, amountType)) {
            return;
        }

        currentAmountType = amountType;
        String formattedAmount = formatAmount(amount, amountType);

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.addRow(new Object[]{itemName, formattedAmount});

        try ( Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pie_chart_db?useSSL=false", "root", "root")) {
            String query = "INSERT INTO items (item_name, amount) VALUES (?, ?)";
            try ( PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, itemName);
                stmt.setString(2, formattedAmount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateAmount(double amount, String amountType) {
        switch (amountType) {
            case "CGPA":
                if (amount < 0 || amount > 10) {
                    JOptionPane.showMessageDialog(this, "CGPA must be between 0 and 10.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case "Percentage":
                if (amount < 0 || amount > 100) {
                    JOptionPane.showMessageDialog(this, "Percentage must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case "INR":
            case "USD":
            case "EUR":
                if (amount < 0) {
                    JOptionPane.showMessageDialog(this, "Currency amount must be non-negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case "Kilograms":
            case "Grams":
            case "Millimeters":
            case "Centimeters":
            case "Meters":
                if (amount < 0) {
                    JOptionPane.showMessageDialog(this, "Measurement amount must be non-negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case "Hours":
                if (amount < 0 || amount > 24) {
                    JOptionPane.showMessageDialog(this, "Hours must be between 0 and 24.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case "Minutes":
                if (amount < 0 || amount > 60) {
                    JOptionPane.showMessageDialog(this, "Minutes must be between 0 and 60.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case "Seconds":
                if (amount < 0 || amount > 60) {
                    JOptionPane.showMessageDialog(this, "Seconds must be between 0 and 60.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            default:
                JOptionPane.showMessageDialog(this, "Invalid amount type.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
        }
        return true;
    }

    private String formatAmount(double amount, String amountType) {
        switch (amountType) {
            case "CGPA":
                return String.format("%.2f CGPA", amount);
            case "Percentage":
                return String.format("%.2f Percentage", amount);
            case "INR":
                return String.format("%.2f Rs", amount);
            case "USD":
                return String.format("%.2f USD", amount);
            case "EUR":
                return String.format("%.2f EUR", amount);
            case "Kilograms":
                return String.format("%.2f Kilograms", amount);
            case "Grams":
                return String.format("%.2f Grams", amount);
            case "Millimeters":
                return String.format("%.2f Millimeters", amount);
            case "Centimeters":
                return String.format("%.2f Centimeters", amount);
            case "Meters":
                return String.format("%.2f Meters", amount);
            case "Hours":
                return String.format("%.2f Hours", amount);
            case "Minutes":
                return String.format("%.2f Minutes", amount);
            case "Seconds":
                return String.format("%.2f Seconds", amount);
            default:
                return String.format("%.2f", amount);
        }
    }

    private void showPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            String item = (String) model.getValueAt(i, 0);
            String amountStr = (String) model.getValueAt(i, 1);

            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr.split(" ")[0]);
                    dataset.setValue(item, amount);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Invalid amount format in table. Please check the data.", "Data Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        JFreeChart chart = ChartFactory.createPieChart("Item Distribution", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("CGPA", new Color(255, 100, 100));
        plot.setSectionPaint("Percentage", new Color(100, 255, 100));
        plot.setSectionPaint("INR", new Color(100, 100, 255));
        plot.setSectionPaint("USD", new Color(255, 255, 100));
        plot.setSectionPaint("EUR", new Color(100, 255, 255));
        plot.setSectionPaint("Kilograms", new Color(255, 100, 255));
        plot.setSectionPaint("Grams", new Color(200, 200, 200));
        plot.setSectionPaint("Millimeters", new Color(100, 100, 100));
        plot.setSectionPaint("Centimeters", new Color(200, 100, 100));
        plot.setSectionPaint("Meters", new Color(100, 200, 100));
        plot.setSectionPaint("Hours", new Color(100, 100, 200));
        plot.setSectionPaint("Minutes", new Color(200, 200, 100));
        plot.setSectionPaint("Seconds", new Color(100, 200, 200));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(460, 440));
        this.chartPanel.removeAll();
        this.chartPanel.add(chartPanel, BorderLayout.CENTER);
        this.chartPanel.validate();
    }

private void resetData() {
        // Clear the table data in the application
        // This is optional and depends on whether you want to clear the visible table data or not
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Clears all rows from the table (optional)
        jTextField1.setText("");
        jTextField2.setText("");

        // Clear the chart panel
        chartPanel.removeAll();
        chartPanel.repaint();

        // Add a blank row to the database
        try ( Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pie_chart_db?useSSL=false", "root", "root")) {
            String query = "INSERT INTO items (item_name, amount) VALUES (?, ?)";
            try ( PreparedStatement stmt = conn.prepareStatement(query)) {
                // Insert a row with empty values
                stmt.setString(1, "");  // Blank item name
                stmt.setString(2, "");  // Blank amount
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding blank row to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Reset current amount type
        currentAmountType = null;
    } 

    private void updateData() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.", "Update Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String itemName = jTextField1.getText();
        String amountText = jTextField2.getText();
        String amountType = (String) jComboBox1.getSelectedItem();

        if (itemName.isEmpty() || !isValidItemName(itemName)) {
            JOptionPane.showMessageDialog(this, "Item Name must start with an alphabet character.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validateAmount(amount, amountType)) {
            return;
        }

        currentAmountType = amountType;
        String formattedAmount = formatAmount(amount, amountType);
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setValueAt(itemName, selectedRow, 0);
        model.setValueAt(formattedAmount, selectedRow, 1);

        try ( Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pie_chart_db?useSSL=false", "root", "root")) {
            String query = "UPDATE items SET item_name = ?, amount = ? WHERE item_name = ? AND amount = ?";
            try ( PreparedStatement stmt = conn.prepareStatement(query)) {
                String oldItemName = (String) model.getValueAt(selectedRow, 0);
                String oldAmount = (String) model.getValueAt(selectedRow, 1);
                stmt.setString(1, itemName);
                stmt.setString(2, formattedAmount);
                stmt.setString(3, oldItemName);
                stmt.setString(4, oldAmount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteData() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        String itemName = (String) model.getValueAt(selectedRow, 0);
        String amount = (String) model.getValueAt(selectedRow, 1);

        model.removeRow(selectedRow);

        try ( Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pie_chart_db?useSSL=false", "root", "root")) {
            String query = "DELETE FROM items WHERE item_name = ? AND amount = ?";
            try ( PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, itemName);
                stmt.setString(2, amount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        chartPanel = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 255, 204));
        jLabel1.setText("DATA VISUALIZATION");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 13, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Enter Item Name:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 38, 120, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Enter Amount Type:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 66, -1, -1));
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, 190, -1));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CGPA", "Percentage", "INR", "USD", "EUR", "Kilograms", "Grams", "Millimeters", "Centimeters", "Meters", "Hours", "Minutes", "Seconds" }));
        jPanel1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, 110, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Enter Amount:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 94, -1, -1));
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 100, 110, -1));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setText("ADD DATA");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 170, 50));

        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ITEMS", "AMOUNT"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 343, 290));

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setText("PIE CHART");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 510, 343, 50));

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setText("RESET");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 510, 180, 50));

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setText("Exit");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 10, -1, -1));

        chartPanel.setBackground(new java.awt.Color(255, 255, 255));
        chartPanel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(chartPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(394, 48, 460, 440));

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setText("UPDATE");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 510, 160, 50));

        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setText("DELETE");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 510, 130, 50));
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 880, 570));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        resetData();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addItem();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        showPieChart();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        updateData();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        deleteData();
    }//GEN-LAST:event_jButton6ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PieChartFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables

}
