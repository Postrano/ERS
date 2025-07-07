package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import src.com.inspire.ers.DBUtil;


import com.inspire.ers.Attendance;
import com.inspire.ers.Executive;
import com.inspire.ers.ExecutiveAllowancePage;
import com.inspire.ers.ExecutivePayrollPage;

public class ExecutivePage extends JFrame {
    private JTable executiveTable, attendanceTable;
    private DefaultTableModel executiveModel, attendanceModel;
    private JComboBox<String> executiveFilterCombo;
    private JComboBox<String> dayCombo, monthCombo, yearCombo;
    private JComboBox<String> filterMonthCombo, filterYearCombo;
    private JComboBox<String> statusCombo;

    public ExecutivePage() {
        setTitle("Executive Payroll and Attendance");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel for saving attendance and adding executives
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        executiveFilterCombo = new JComboBox<>();
        dayCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        yearCombo = new JComboBox<>();
        statusCombo = new JComboBox<>(new String[]{"Present", "Absent"});
        JButton saveStatusBtn = new JButton("Save Attendance");
        JButton addExecutiveBtn = new JButton("Add Executive");

        for (int i = 1; i <= 31; i++) dayCombo.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) monthCombo.addItem(String.format("%02d", i));
        for (int y = 2000; y <= 2035; y++) yearCombo.addItem(String.valueOf(y));

        topPanel.add(new JLabel("Executive:"));
        topPanel.add(executiveFilterCombo);
        topPanel.add(new JLabel("Date:"));
        topPanel.add(yearCombo);
        topPanel.add(monthCombo);
        topPanel.add(dayCombo);
        topPanel.add(new JLabel("Status:"));
        topPanel.add(statusCombo);
        topPanel.add(saveStatusBtn);
        topPanel.add(addExecutiveBtn);
        add(topPanel, BorderLayout.NORTH);

        // SplitPane for Executive Info and Attendance
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Executive Table
        String[] executiveCols = {
            "ID No", "Name", "Department/Position", "Bank", "Basic Pay",
            "Allowance"
        };
        executiveModel = new DefaultTableModel(executiveCols, 0);
        executiveTable = new JTable(executiveModel);
        JScrollPane execScroll = new JScrollPane(executiveTable);
        splitPane.setTopComponent(execScroll);

        // Bottom panel with attendance filter and table
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterMonthCombo = new JComboBox<>();
        filterYearCombo = new JComboBox<>();
        JButton loadAttendanceBtn = new JButton("Load Attendance");

        for (int i = 1; i <= 12; i++) filterMonthCombo.addItem(String.format("%02d", i));
        for (int y = 2000; y <= 2035; y++) filterYearCombo.addItem(String.valueOf(y));

        filterPanel.add(new JLabel("Filter by Month:"));
        filterPanel.add(filterYearCombo);
        filterPanel.add(filterMonthCombo);
        filterPanel.add(loadAttendanceBtn);
        bottomPanel.add(filterPanel, BorderLayout.NORTH);

        String[] attendanceCols = {"Date", "Status"};
                attendanceModel = new DefaultTableModel(attendanceCols, 0) {
               @Override
               public boolean isCellEditable(int row, int column) {
                   return column == 1; // Only allow editing "Status"
               }
           };

        attendanceTable = new JTable(attendanceModel);
        JScrollPane attendanceScroll = new JScrollPane(attendanceTable);
        bottomPanel.add(attendanceScroll, BorderLayout.CENTER);

        splitPane.setBottomComponent(bottomPanel);
        add(splitPane, BorderLayout.CENTER);

        // Button Listeners
        saveStatusBtn.addActionListener(e -> saveAttendanceStatus());
        loadAttendanceBtn.addActionListener(e -> loadAttendanceRecords());
        executiveFilterCombo.addActionListener(e -> loadExecutiveInfo());

        addExecutiveBtn.addActionListener(e -> {
            AddExecutiveForm addExec = new AddExecutiveForm(this); // opens your AddExecutive form
            addExec.setVisible(true);
        });
        
        JButton openAllowancePageBtn = new JButton("Open Allowance Page");
        topPanel.add(openAllowancePageBtn);
        openAllowancePageBtn.addActionListener(e -> {
            new ExecutiveAllowancePage().setVisible(true);
        });
        
        JButton openPayrollBtn = new JButton("Executive Payroll");
        topPanel.add(openPayrollBtn);

        openPayrollBtn.addActionListener(e -> {
            new ExecutivePayrollPage().setVisible(true);
        });
        
        
        JButton saveChangesBtn = new JButton("Save Changes");
        JButton deleteBtn = new JButton("Delete");

        filterPanel.add(saveChangesBtn);
        filterPanel.add(deleteBtn);

        saveChangesBtn.addActionListener(e -> saveAttendanceChanges());
       deleteBtn.addActionListener(e -> deleteAttendanceRecord());



        // Initial load
        loadExecutives();
    }
    
    
    private void saveAttendanceChanges() {
    String selected = (String) executiveFilterCombo.getSelectedItem();
    if (selected == null) return;
    String execId = selected.split(" - ")[0];

    try {
        AttendanceDAO dao = new AttendanceDAO();
        for (int i = 0; i < attendanceModel.getRowCount(); i++) {
            String dateStr = attendanceModel.getValueAt(i, 0).toString();
            String status = attendanceModel.getValueAt(i, 1).toString();

            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            dao.saveOrUpdateAttendance(execId, date, status);
        }
        JOptionPane.showMessageDialog(this, "Changes saved successfully.");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error saving changes: " + ex.getMessage());
        ex.printStackTrace();
    }
}

    private void deleteAttendanceRecord() {
    int selectedRow = attendanceTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to delete.");
        return;
    }

    String selected = (String) executiveFilterCombo.getSelectedItem();
    if (selected == null) return;
    String execId = selected.split(" - ")[0];

    String dateStr = attendanceModel.getValueAt(selectedRow, 0).toString();
    java.sql.Date date = java.sql.Date.valueOf(dateStr);

    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            AttendanceDAO dao = new AttendanceDAO();
            dao.deleteAttendance(execId, date);
            attendanceModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Record deleted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting record: " + ex.getMessage());
        }
    }
}


    private void loadExecutives() {
        try (Connection conn = DBUtil.getConnection()) {
            executiveFilterCombo.removeAllItems();
            String sql = "SELECT exec_id, name FROM executive_info";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                executiveFilterCombo.addItem(rs.getString("exec_id") + " - " + rs.getString("name"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading executives: " + ex.getMessage());
        }
    }

    private void loadExecutiveInfo() {
        executiveModel.setRowCount(0);
        String selected = (String) executiveFilterCombo.getSelectedItem();
        if (selected == null) return;

        String execId = selected.split(" - ")[0];
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM executive_info WHERE exec_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, execId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                executiveModel.addRow(new Object[]{
                    rs.getString("exec_id"),
                    rs.getString("name"),
                    rs.getString("department_or_position"),
                    rs.getString("bank"),
                    "₱" + String.format("%,.2f", rs.getDouble("basic_pay")),
                    "₱" + String.format("%,.2f", rs.getDouble("allowance"))
                    
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load executive info: " + ex.getMessage());
        }
    }

 private void saveAttendanceStatus() {
    String selectedExec = (String) executiveFilterCombo.getSelectedItem();
    if (selectedExec == null) {
        JOptionPane.showMessageDialog(this, "Please select an executive.");
        return;
    }

    String execId = selectedExec.split(" - ")[0];
    String dateStr = yearCombo.getSelectedItem() + "-" + monthCombo.getSelectedItem() + "-" + dayCombo.getSelectedItem();
    String status = (String) statusCombo.getSelectedItem();

    try {
        AttendanceDAO dao = new AttendanceDAO();
        dao.saveOrUpdateAttendance(execId, java.sql.Date.valueOf(dateStr), status);
        JOptionPane.showMessageDialog(this, "Attendance saved successfully.");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage());
    }
}


  private void loadAttendanceRecords() {
    attendanceModel.setRowCount(0);
    String selected = (String) executiveFilterCombo.getSelectedItem();
    if (selected == null) return;

    String execId = selected.split(" - ")[0];
    int month = Integer.parseInt((String) filterMonthCombo.getSelectedItem());
    int year = Integer.parseInt((String) filterYearCombo.getSelectedItem());

    try {
        AttendanceDAO dao = new AttendanceDAO();
        for (Attendance att : dao.getAttendance(execId, month, year)) {
            attendanceModel.addRow(new Object[]{
                att.getAttendanceDate(), att.getPresent()
            });
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading attendance: " + ex.getMessage());
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExecutivePage().setVisible(true));
    }
}
