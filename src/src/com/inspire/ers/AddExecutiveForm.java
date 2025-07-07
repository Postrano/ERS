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
    private JTextField basicPayField, allowanceField, execAllowanceField, marketingAllowanceField;

    public AddExecutiveForm(ExecutivePage parent) {
        super(parent, "Add Executive", true);
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(9, 2, 10, 5)); // 8 fields + Save button

        idField = new JTextField();
        nameField = new JTextField();
        deptField = new JTextField();
        bankField = new JTextField();
        basicPayField = new JTextField();
        allowanceField = new JTextField();
        execAllowanceField = new JTextField();
        marketingAllowanceField = new JTextField();

        add(new JLabel("ID No:")); add(idField);
        add(new JLabel("Name:")); add(nameField);
        add(new JLabel("Department/Position:")); add(deptField);
        add(new JLabel("Bank:")); add(bankField);
        add(new JLabel("Basic Pay:")); add(basicPayField);
        add(new JLabel("Allowance:")); add(allowanceField);
       

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                saveExecutiveToDatabase();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        add(new JLabel()); // empty cell
        add(saveBtn);
    }

    private void saveExecutiveToDatabase() throws SQLException {
        String execId = idField.getText();
        String name = nameField.getText();
        String dept = deptField.getText();
        String bank = bankField.getText();

        double basicPay = Double.parseDouble(basicPayField.getText());
        double allowance = Double.parseDouble(allowanceField.getText());
        double execAllowance = Double.parseDouble(execAllowanceField.getText());
        double marketing = Double.parseDouble(marketingAllowanceField.getText());

        LocalDate today = LocalDate.now();

        try (Connection conn = DBUtil.getConnection()) {
            // Insert into executive_info
            String insertExecSQL = """
                INSERT INTO executive_info (exec_id, name, department_or_position, bank,
                basic_pay, allowance, executive_allowance, marketing_allowance)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            PreparedStatement execStmt = conn.prepareStatement(insertExecSQL);
            execStmt.setString(1, execId);
            execStmt.setString(2, name);
            execStmt.setString(3, dept);
            execStmt.setString(4, bank);
            execStmt.setDouble(5, basicPay);
            execStmt.setDouble(6, allowance);
            execStmt.setDouble(7, execAllowance);
            execStmt.setDouble(8, marketing);
            execStmt.executeUpdate();

            // Insert initial attendance row (only exec_id and date)
            String insertAttendanceSQL = """
                INSERT INTO executive_attendance (exec_id, attendance_date)
                VALUES (?, ?)
            """;
            PreparedStatement attStmt = conn.prepareStatement(insertAttendanceSQL);
            attStmt.setString(1, execId);
            attStmt.setDate(2, java.sql.Date.valueOf(today));
            attStmt.executeUpdate();
        }
    }
}
