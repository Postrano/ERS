package com.inspire.ers;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import src.com.inspire.ers.DBUtil;

public class AddExecutiveForm extends JDialog {
    private JTextField idField, nameField, deptField, bankField;
    private JTextField basicPayField, allowanceField;
    private final String selectedCompany;
    private final ExecutivePage executivePage;
    private boolean isEditMode = false;
    private JTextField sssNumberField, sssValueField;
    private JTextField pagibigNumberField, tinNumberField, philhealthNumberField;

    public AddExecutiveForm(ExecutivePage parent, String selectedCompany) {
        super(parent, "Add Executive", true);
        this.executivePage = parent;
        this.selectedCompany = selectedCompany;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Input Fields
        idField = new JTextField(20);
        nameField = new JTextField(20);
        deptField = new JTextField(20);
        bankField = new JTextField(20);
        basicPayField = new JTextField(20);
        allowanceField = new JTextField(20);
        sssNumberField = new JTextField(20);
//        sssValueField = new JTextField(20);
        pagibigNumberField = new JTextField(20);
        tinNumberField = new JTextField(20);
        philhealthNumberField = new JTextField(20);

        // Add Fields to formPanel
        addRow(formPanel, gbc, "ID No:", idField);
        addRow(formPanel, gbc, "Name:", nameField);
        addRow(formPanel, gbc, "Department/Position:", deptField);
        addRow(formPanel, gbc, "Bank:", bankField);
        addRow(formPanel, gbc, "Basic Pay:", basicPayField);
        addRow(formPanel, gbc, "Allowance:", allowanceField);
        addRow(formPanel, gbc, "SSS Number:", sssNumberField);
//        addRow(formPanel, gbc, "SSS Value:", sssValueField);
        addRow(formPanel, gbc, "Pag-IBIG Number:", pagibigNumberField);
        addRow(formPanel, gbc, "TIN Number:", tinNumberField);
        addRow(formPanel, gbc, "PhilHealth Number:", philhealthNumberField);

        // Save Button
        JButton saveBtn = new JButton("Save");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            try {
                if (validateInput()) {
                    if (isEditMode) {
                        updateExecutiveInDatabase();
                    } else {
                        saveExecutiveToDatabase();
                    }

                    executivePage.loadExecutives();
                    String entry = idField.getText() + " - " + nameField.getText();
                    executivePage.getExecutiveFilterCombo().setSelectedItem(entry);
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(textField, gbc);
        gbc.gridy++;
    }

    public AddExecutiveForm(ExecutivePage parent, String selectedCompany,
                          String execId, String name, String dept, String bank,
                          double basicPay, double allowance,
                          String sssNumber,
                          String pagibigNumber, String tinNumber, String philhealthNumber) {
        this(parent, selectedCompany);
        isEditMode = true;
        setTitle("Edit Executive");

        idField.setText(execId);
        idField.setEditable(false);
        nameField.setText(name);
        deptField.setText(dept);
        bankField.setText(bank);
        basicPayField.setText(String.valueOf(basicPay));
        allowanceField.setText(String.valueOf(allowance));
        sssNumberField.setText(sssNumber);
//        sssValueField.setText(String.valueOf(sssValue));
        pagibigNumberField.setText(pagibigNumber);
        tinNumberField.setText(tinNumber);
        philhealthNumberField.setText(philhealthNumber);
    }

    private boolean validateInput() {
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() ||
            deptField.getText().isEmpty() || bankField.getText().isEmpty() ||
            basicPayField.getText().isEmpty() || allowanceField.getText().isEmpty() ||
            sssNumberField.getText().isEmpty() || 
            pagibigNumberField.getText().isEmpty() ||
            tinNumberField.getText().isEmpty() || philhealthNumberField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return false;
        }

        try {
            Double.parseDouble(basicPayField.getText());
            Double.parseDouble(allowanceField.getText());
//            Double.parseDouble(sssValueField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Basic Pay, Allowance, and SSS Value must be numbers.");
            return false;
        }

        return true;
    }

    private void saveExecutiveToDatabase() throws SQLException {
        String execId = idField.getText().trim();
        String name = nameField.getText().trim();
        String dept = deptField.getText().trim();
        String bank = bankField.getText().trim();
        double basicPay = Double.parseDouble(basicPayField.getText().trim());
        double allowance = Double.parseDouble(allowanceField.getText().trim());
        LocalDate today = LocalDate.now();

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            String insertExecSql = """
                INSERT INTO executive_info (
                    exec_id, name, department_or_position, bank, 
                    basic_pay, allowance, company,
                    sss_number, 
                    pagibig_number, tin_number, philhealth_number
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement execStmt = conn.prepareStatement(insertExecSql);
            execStmt.setString(1, execId);
            execStmt.setString(2, name);
            execStmt.setString(3, dept);
            execStmt.setString(4, bank);
            execStmt.setDouble(5, basicPay);
            execStmt.setDouble(6, allowance);
            execStmt.setString(7, selectedCompany);
            execStmt.setString(8, sssNumberField.getText().trim());
//            execStmt.setDouble(9, Double.parseDouble(sssValueField.getText().trim()));
            execStmt.setString(9, pagibigNumberField.getText().trim());
            execStmt.setString(10, tinNumberField.getText().trim());
            execStmt.setString(11, philhealthNumberField.getText().trim());

            execStmt.executeUpdate();

            String insertAttendanceSQL = """
                INSERT INTO executive_attendance (exec_id, attendance_date)
                VALUES (?, ?)
            """;
            PreparedStatement attStmt = conn.prepareStatement(insertAttendanceSQL);
            attStmt.setString(1, execId);
            attStmt.setDate(2, java.sql.Date.valueOf(today));
            attStmt.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            throw ex;
        }
    }

    private void updateExecutiveInDatabase() throws SQLException {
        String execId = idField.getText().trim();
        String name = nameField.getText().trim();
        String dept = deptField.getText().trim();
        String bank = bankField.getText().trim();
        double basicPay = Double.parseDouble(basicPayField.getText().trim());
        double allowance = Double.parseDouble(allowanceField.getText().trim());
        String sssNumber = sssNumberField.getText().trim();
//        double sssValue = Double.parseDouble(sssValueField.getText().trim());
        String pagibigNumber = pagibigNumberField.getText().trim();
        String tinNumber = tinNumberField.getText().trim();
        String philhealthNumber = philhealthNumberField.getText().trim();

        try (Connection conn = DBUtil.getConnection()) {
            String updateSql = """
                UPDATE executive_info SET
                    name = ?, department_or_position = ?, bank = ?,
                    basic_pay = ?, allowance = ?,
                    sss_number = ?,  pagibig_number = ?,
                    tin_number = ?, philhealth_number = ?
                WHERE exec_id = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, name);
            stmt.setString(2, dept);
            stmt.setString(3, bank);
            stmt.setDouble(4, basicPay);
            stmt.setDouble(5, allowance);
            stmt.setString(6, sssNumber);
           
            stmt.setString(7, pagibigNumber);
            stmt.setString(8, tinNumber);
            stmt.setString(9, philhealthNumber);
            stmt.setString(10, execId);

            stmt.executeUpdate();
        }
    }
}
