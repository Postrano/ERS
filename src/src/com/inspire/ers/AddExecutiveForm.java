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
    private boolean isEditMode = false; // Flag to distinguish between Add and Edit

    public AddExecutiveForm(ExecutivePage parent, String selectedCompany) {
        super(parent, "Add Executive", true);
        this.executivePage = parent;
        this.selectedCompany = selectedCompany;

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(8, 2, 10, 5)); // Fields + Save button

        // Input Fields
        idField = new JTextField();
        nameField = new JTextField();
        deptField = new JTextField();
        bankField = new JTextField();
        basicPayField = new JTextField();
        allowanceField = new JTextField();

        // Labels and Fields
        add(new JLabel("ID No:")); add(idField);
        add(new JLabel("Name:")); add(nameField);
        add(new JLabel("Department/Position:")); add(deptField);
        add(new JLabel("Bank:")); add(bankField);
        add(new JLabel("Basic Pay:")); add(basicPayField);
        add(new JLabel("Allowance:")); add(allowanceField);

        // Save Button
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                if (validateInput()) {
                    if (isEditMode) {
                        updateExecutiveInDatabase();
                    } else {
                        saveExecutiveToDatabase();
                    }

                    executivePage.loadExecutives(); // Refresh Combo & Table

                    // Auto-select the executive
                    String entry = idField.getText() + " - " + nameField.getText();
                    executivePage.getExecutiveFilterCombo().setSelectedItem(entry);

                    dispose(); // Close the dialog
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        add(new JLabel()); // Empty cell
        add(saveBtn);
    }

    // Constructor for editing an executive
    public AddExecutiveForm(ExecutivePage parent, String selectedCompany,
                            String execId, String name, String dept, String bank,
                            double basicPay, double allowance) {
        this(parent, selectedCompany); // Call main constructor
        isEditMode = true;
        setTitle("Edit Executive");

        idField.setText(execId);
        idField.setEditable(false); // Prevent editing ID during update
        nameField.setText(name);
        deptField.setText(dept);
        bankField.setText(bank);
        basicPayField.setText(String.valueOf(basicPay));
        allowanceField.setText(String.valueOf(allowance));
    }

    // Input Validation
    private boolean validateInput() {
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() ||
            deptField.getText().isEmpty() || bankField.getText().isEmpty() ||
            basicPayField.getText().isEmpty() || allowanceField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return false;
        }

        try {
            Double.parseDouble(basicPayField.getText());
            Double.parseDouble(allowanceField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Basic Pay and Allowance must be numbers.");
            return false;
        }

        return true;
    }

    // Save new executive
    private void saveExecutiveToDatabase() throws SQLException {
        String execId = idField.getText().trim();
        String name = nameField.getText().trim();
        String dept = deptField.getText().trim();
        String bank = bankField.getText().trim();
        double basicPay = Double.parseDouble(basicPayField.getText().trim());
        double allowance = Double.parseDouble(allowanceField.getText().trim());
        LocalDate today = LocalDate.now();

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            String insertExecSql = """
                INSERT INTO executive_info (
                    exec_id, name, department_or_position, bank,
                    basic_pay, allowance, company
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement execStmt = conn.prepareStatement(insertExecSql);
            execStmt.setString(1, execId);
            execStmt.setString(2, name);
            execStmt.setString(3, dept);
            execStmt.setString(4, bank);
            execStmt.setDouble(5, basicPay);
            execStmt.setDouble(6, allowance);
            execStmt.setString(7, selectedCompany);
            execStmt.executeUpdate();

            // Optional: Add today's attendance
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

    // Update existing executive
    private void updateExecutiveInDatabase() throws SQLException {
        String execId = idField.getText().trim();
        String name = nameField.getText().trim();
        String dept = deptField.getText().trim();
        String bank = bankField.getText().trim();
        double basicPay = Double.parseDouble(basicPayField.getText().trim());
        double allowance = Double.parseDouble(allowanceField.getText().trim());

        try (Connection conn = DBUtil.getConnection()) {
            String updateSql = """
                UPDATE executive_info SET
                    name = ?, department_or_position = ?, bank = ?,
                    basic_pay = ?, allowance = ?
                WHERE exec_id = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, name);
            stmt.setString(2, dept);
            stmt.setString(3, bank);
            stmt.setDouble(4, basicPay);
            stmt.setDouble(5, allowance);
            stmt.setString(6, execId);
            stmt.executeUpdate();
        }
    }
}
