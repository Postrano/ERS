package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import src.com.inspire.ers.DBUtil;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExecutivePayrollPage extends JFrame {
    private JTable payrollTable;
    private DefaultTableModel tableModel;
    private JSpinner payDateSpinner, startDateSpinner, endDateSpinner;

    public ExecutivePayrollPage() {
        setTitle("Executive Payroll Summary");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        payDateSpinner = createDateSpinner();
        startDateSpinner = createDateSpinner();
        endDateSpinner = createDateSpinner();

        JButton generateBtn = new JButton("Generate");

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Pay Date:"));
        inputPanel.add(payDateSpinner);
        inputPanel.add(new JLabel("Cutoff Start:"));
        inputPanel.add(startDateSpinner);
        inputPanel.add(new JLabel("Cutoff End:"));
        inputPanel.add(endDateSpinner);
        inputPanel.add(generateBtn);

        add(inputPanel, BorderLayout.NORTH);

        String[] cols = {
            "Exec ID", "Name", "Total Present", "Total Absent", "Basic Pay",
            "Allowance", "Marketing Allowance", "Executive Allowance", "Total Basic Pay"
        };

        tableModel = new DefaultTableModel(cols, 0);
        payrollTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make specific columns editable: Total Present, Total Absent, Allowances
                return column == 2 || column == 3 || column == 5 || column == 6 || column == 7;
            }
        };
        add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            Date payDate = (Date) payDateSpinner.getValue();
            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();

            java.sql.Date sqlStart = new java.sql.Date(startDate.getTime());
            java.sql.Date sqlEnd = new java.sql.Date(endDate.getTime());

            loadPayrollData(sqlStart, sqlEnd);
        });

        // Save Changes button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChangesToDatabase());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(saveBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        return dateSpinner;
    }

    private int countWeekdaysBetween(java.sql.Date start, java.sql.Date end) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(start);
        int count = 0;
        while (!cal.getTime().after(end)) {
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            if (dayOfWeek >= java.util.Calendar.MONDAY && dayOfWeek <= java.util.Calendar.FRIDAY) {
                count++;
            }
            cal.add(java.util.Calendar.DATE, 1);
        }
        return count;
    }

    private void loadPayrollData(java.sql.Date startDate, java.sql.Date endDate) {
        tableModel.setRowCount(0);
        int totalWorkingDays = countWeekdaysBetween(startDate, endDate);

        String sql = "SELECT ei.exec_id, ei.name, ei.basic_pay, ei.allowance, " +
                "ei.marketing_allowance, ei.executive_allowance, " +
                "SUM(CASE WHEN ea.present = 'Present' THEN 1 ELSE 0 END) AS total_present, " +
                "SUM(CASE WHEN ea.present = 'Absent' THEN 1 ELSE 0 END) AS total_absent " +
                "FROM executive_info ei " +
                "LEFT JOIN executive_attendance ea ON ei.exec_id = ea.exec_id " +
                "AND ea.attendance_date BETWEEN ? AND ? " +
                "GROUP BY ei.exec_id, ei.name, ei.basic_pay, ei.allowance, ei.marketing_allowance, ei.executive_allowance";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String execId = rs.getString("exec_id");
                String name = rs.getString("name");
                int totalPresent = rs.getInt("total_present");
                int totalAbsent = rs.getInt("total_absent");

                double basicPay = rs.getDouble("basic_pay");
                double allowance = rs.getDouble("allowance");
                double marketing = rs.getDouble("marketing_allowance");
                double executive = rs.getDouble("executive_allowance");

                double dailyRate = totalWorkingDays > 0 ? basicPay / totalWorkingDays : 0;
                double totalBasicPay = (dailyRate * totalPresent) + allowance;

                tableModel.addRow(new Object[]{
                        execId,
                        name,
                        totalPresent,
                        totalAbsent,
                        String.format("%.2f", basicPay),
                        String.format("%.2f", allowance),
                        String.format("%.2f", marketing),
                        String.format("%.2f", executive),
                        String.format("%.2f", totalBasicPay)
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading payroll: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void saveChangesToDatabase() {
        String sql = "UPDATE executive_info SET allowance = ?, marketing_allowance = ?, executive_allowance = ? WHERE exec_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String execId = tableModel.getValueAt(i, 0).toString();
                double allowance = parseCurrency(tableModel.getValueAt(i, 5));
                double marketing = parseCurrency(tableModel.getValueAt(i, 6));
                double executive = parseCurrency(tableModel.getValueAt(i, 7));

                stmt.setDouble(1, allowance);
                stmt.setDouble(2, marketing);
                stmt.setDouble(3, executive);
                stmt.setString(4, execId);

                stmt.addBatch();
            }

            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Changes saved successfully!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving changes: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private double parseCurrency(Object value) {
        try {
            if (value == null) return 0;
            String str = value.toString().replace("₱", "").replace(",", "").trim();
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExecutivePayrollPage().setVisible(true));
    }
}
