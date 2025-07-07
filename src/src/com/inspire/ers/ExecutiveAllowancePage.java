package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import src.com.inspire.ers.DBUtil;

public class ExecutiveAllowancePage extends JFrame {
    private JComboBox<String> executiveCombo, allowanceTypeCombo, dayCombo, monthCombo, yearCombo;
    private JTextField amountField;
    private JTable allowanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthFilterCombo, nameFilterCombo;


    public ExecutiveAllowancePage() {
        setTitle("Executive Allowance Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Fonts
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Executive
        gbc.gridx = 0; gbc.gridy = row;
        inputPanel.add(new JLabel("Executive:"), gbc);
        executiveCombo = new JComboBox<>();
        executiveCombo.setPreferredSize(new Dimension(200, 25));
        loadExecutives();
        gbc.gridx = 1;
        inputPanel.add(executiveCombo, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = ++row;
        inputPanel.add(new JLabel("Date:"), gbc);
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        yearCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        dayCombo = new JComboBox<>();
        for (int y = 2020; y <= 2035; y++) yearCombo.addItem(String.valueOf(y));
        for (int m = 1; m <= 12; m++) monthCombo.addItem(String.format("%02d", m));
        for (int d = 1; d <= 31; d++) dayCombo.addItem(String.format("%02d", d));
        datePanel.add(yearCombo);
        datePanel.add(monthCombo);
        datePanel.add(dayCombo);
        gbc.gridx = 1;
        inputPanel.add(datePanel, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = ++row;
        inputPanel.add(new JLabel("Allowance Type:"), gbc);
        allowanceTypeCombo = new JComboBox<>(new String[]{"Executive", "Marketing"});
        allowanceTypeCombo.setPreferredSize(new Dimension(200, 25));
        gbc.gridx = 1;
        inputPanel.add(allowanceTypeCombo, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = ++row;
        inputPanel.add(new JLabel("Amount (₱):"), gbc);
        amountField = new JTextField(10);
        amountField.setPreferredSize(new Dimension(200, 25));
        gbc.gridx = 1;
        inputPanel.add(amountField, gbc);

        // Buttons
        gbc.gridx = 1; gbc.gridy = ++row;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveBtn = new JButton("💾 Save");
        JButton loadBtn = new JButton("🔄 Load Records");
        saveBtn.setFont(buttonFont);
        loadBtn.setFont(buttonFont);
        buttonPanel.add(saveBtn);
        buttonPanel.add(loadBtn);
        inputPanel.add(buttonPanel, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Executive Name", "Date", "Type", "Amount"}, 0);
        allowanceTable = new JTable(tableModel);
        allowanceTable.setRowHeight(25);
        allowanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        allowanceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        ((DefaultTableCellRenderer) allowanceTable.getDefaultRenderer(String.class)).setHorizontalAlignment(JLabel.LEFT);
        JScrollPane scrollPane = new JScrollPane(allowanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Allowance Records"));
       
        
                // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthFilterCombo = new JComboBox<>();
        monthFilterCombo.addItem("All Months");
        for (int i = 1; i <= 12; i++) {
            monthFilterCombo.addItem(String.format("%02d", i));
        }

        nameFilterCombo = new JComboBox<>();
        nameFilterCombo.addItem("All Names");
        loadExecutiveNamesForFilter();

//        filterPanel.add(new JLabel("Filter by Month:"));
//        filterPanel.add(monthFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Filter by Name:"));
        filterPanel.add(nameFilterCombo);

        monthFilterCombo.addActionListener(e -> loadRecords());
        nameFilterCombo.addActionListener(e -> loadRecords());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);


        // Actions
        saveBtn.addActionListener(e -> saveAllowance());
        loadBtn.addActionListener(e -> loadRecords());

        loadRecords(); // Initial load
    }

    private void loadExecutives() {
        try (Connection conn = DBUtil.getConnection()) {
            executiveCombo.removeAllItems();
            String sql = "SELECT exec_id, name FROM executive_info";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                executiveCombo.addItem(rs.getString("exec_id") + " - " + rs.getString("name"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading executives: " + ex.getMessage());
        }
    }

    private void saveAllowance() {
        String selected = (String) executiveCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an executive.");
            return;
        }

        String execId = selected.split(" - ")[0];
        String allowanceType = (String) allowanceTypeCombo.getSelectedItem();
        String dateStr = yearCombo.getSelectedItem() + "-" + monthCombo.getSelectedItem() + "-" + dayCombo.getSelectedItem();
        java.sql.Date date = java.sql.Date.valueOf(dateStr);

        String amountText = amountField.getText().trim();
        double amount;

        try {
            amount = Double.parseDouble(amountText);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO executive_allowances (exec_id, allowance_type, allowance_date, amount) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, execId);
            stmt.setString(2, allowanceType);
            stmt.setDate(3, date);
            stmt.setDouble(4, amount);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Allowance saved successfully.");
            amountField.setText("");
            loadRecords();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving allowance: " + ex.getMessage());
        }
    }

  private void loadRecords() {
    tableModel.setRowCount(0);

    String selectedMonth = (String) monthFilterCombo.getSelectedItem();
    String selectedName = (String) nameFilterCombo.getSelectedItem();

    StringBuilder sql = new StringBuilder("""
        SELECT ea.allowance_date, ea.allowance_type, ea.amount, ei.name
        FROM executive_allowances ea
        JOIN executive_info ei ON ei.exec_id = ea.exec_id
        WHERE 1=1
    """);

    if (!"All Months".equals(selectedMonth)) {
        sql.append(" AND MONTH(ea.allowance_date) = ").append(Integer.parseInt(selectedMonth));
    }
    if (!"All Names".equals(selectedName)) {
        sql.append(" AND ei.name = ?");
    }

    sql.append(" ORDER BY ea.allowance_date DESC");

    try (Connection conn = DBUtil.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement(sql.toString());

        int paramIndex = 1;
        if (!"All Names".equals(selectedName)) {
            stmt.setString(paramIndex++, selectedName);
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Vector<String> row = new Vector<>();
            row.add(rs.getString("name"));
            row.add(rs.getDate("allowance_date").toString());
            row.add(rs.getString("allowance_type"));
            row.add("₱" + String.format("%,.2f", rs.getDouble("amount")));
            tableModel.addRow(row);
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading filtered records: " + ex.getMessage());
    }
}


    
    private void loadExecutiveNamesForFilter() {
    try (Connection conn = DBUtil.getConnection()) {
        String sql = "SELECT DISTINCT name FROM executive_info ORDER BY name";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            nameFilterCombo.addItem(rs.getString("name"));
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading names: " + ex.getMessage());
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExecutiveAllowancePage().setVisible(true));
    }
}
