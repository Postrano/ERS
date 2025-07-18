// Package and imports
package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import src.com.inspire.ers.DBUtil;

public class ExecutivePage extends JFrame {
    private JTable executiveTable, attendanceTable;
    private DefaultTableModel executiveModel, attendanceModel;
    private JComboBox<String> executiveFilterCombo;
    private JComboBox<String> dayCombo, monthCombo, yearCombo;
    private JComboBox<String> statusCombo;
    private final String selectedCompany;
    private JSpinner cutOffStartDateSpinner;
    private JSpinner cutOffEndDateSpinner;


    public ExecutivePage(String selectedCompany) {
        this.selectedCompany = selectedCompany;
        setTitle("Executive Payroll and Attendance");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        executiveFilterCombo = new JComboBox<>();
        dayCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        yearCombo = new JComboBox<>();
        statusCombo = new JComboBox<>(new String[]{"Present", "Absent"});
        JButton saveStatusBtn = new JButton("Save Attendance");
        JButton addExecutiveBtn = new JButton("Add Executive");
        JButton editExecutiveBtn = new JButton("Edit Executive");
        JButton deleteExecutiveBtn = new JButton("Delete Executive");

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
        topPanel.add(editExecutiveBtn);
        topPanel.add(deleteExecutiveBtn);
        add(topPanel, BorderLayout.NORTH);

        // Executive table
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

       String[] executiveCols = {
            "ID No", "Name", "Department/Position", "Bank",
            "Basic Pay", "Allowance", "SSS Number", 
            "Pag-IBIG Number", "TIN Number", "PhilHealth Number"
        };
        executiveModel = new DefaultTableModel(executiveCols, 0);
        executiveTable = new JTable(executiveModel);
        JScrollPane execScroll = new JScrollPane(executiveTable);
        splitPane.setTopComponent(execScroll);

        // Bottom Attendance Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Replace JDateChooser with JTextField
       cutOffStartDateSpinner = new JSpinner(new SpinnerDateModel());
        cutOffStartDateSpinner.setEditor(new JSpinner.DateEditor(cutOffStartDateSpinner, "yyyy-MM-dd"));

        cutOffEndDateSpinner = new JSpinner(new SpinnerDateModel());
        cutOffEndDateSpinner.setEditor(new JSpinner.DateEditor(cutOffEndDateSpinner, "yyyy-MM-dd"));

        JButton loadAttendanceBtn = new JButton("Load Attendance");

       filterPanel.add(new JLabel("Cut-off Start (YYYY-MM-DD):"));
        filterPanel.add(cutOffStartDateSpinner);
        filterPanel.add(new JLabel("Cut-off End (YYYY-MM-DD):"));
        filterPanel.add(cutOffEndDateSpinner);

        filterPanel.add(loadAttendanceBtn);
        bottomPanel.add(filterPanel, BorderLayout.NORTH);

        String[] attendanceCols = {"Date", "Status"};
        attendanceModel = new DefaultTableModel(attendanceCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        attendanceTable = new JTable(attendanceModel);
        JScrollPane attendanceScroll = new JScrollPane(attendanceTable);
        bottomPanel.add(attendanceScroll, BorderLayout.CENTER);
        splitPane.setBottomComponent(bottomPanel);
        add(splitPane, BorderLayout.CENTER);

        // Buttons
        JButton saveChangesBtn = new JButton("Save Changes");
        JButton deleteBtn = new JButton("Delete");
        filterPanel.add(saveChangesBtn);
        filterPanel.add(deleteBtn);

        JButton openAllowancePageBtn = new JButton("Open Allowance Page");
        topPanel.add(openAllowancePageBtn);
        JButton openPayrollBtn = new JButton("Exe Payroll");
        topPanel.add(openPayrollBtn);

        // Add Listeners
        saveStatusBtn.addActionListener(e -> saveAttendanceStatus());
        loadAttendanceBtn.addActionListener(e -> loadAttendanceRecords());
        executiveFilterCombo.addActionListener(e -> loadExecutiveInfo());
        addExecutiveBtn.addActionListener(e -> {
            AddExecutiveForm form = new AddExecutiveForm(this, selectedCompany);
            form.setVisible(true);
        });
        editExecutiveBtn.addActionListener(e -> editSelectedExecutive());
        deleteExecutiveBtn.addActionListener(e -> deleteSelectedExecutive());
        saveChangesBtn.addActionListener(e -> saveAttendanceChanges());
        deleteBtn.addActionListener(e -> deleteAttendanceRecord());
        openAllowancePageBtn.addActionListener(e -> new ExecutiveAllowancePage(selectedCompany).setVisible(true));
        openPayrollBtn.addActionListener(e -> new ExecutivePayrollPage(selectedCompany).setVisible(true));

        loadExecutives();
    }

   public void loadExecutives() {
    executiveModel.setRowCount(0);
    executiveFilterCombo.removeAllItems();

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "SELECT exec_id, name FROM executive_info WHERE company = ?")) {
        stmt.setString(1, selectedCompany);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String execId = rs.getString("exec_id");
            String name = rs.getString("name");
            executiveFilterCombo.addItem(execId + " - " + name);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading executives: " + e.getMessage());
    }
}


  private void loadExecutiveInfo() {
    executiveModel.setRowCount(0);
    String selected = (String) executiveFilterCombo.getSelectedItem();
    if (selected == null) return;

    String execId = selected.split(" - ")[0];

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM executive_info WHERE exec_id = ?")) {
        stmt.setString(1, execId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            executiveModel.addRow(new Object[]{
                rs.getString("exec_id"),
                rs.getString("name"),
                rs.getString("department_or_position"),
                rs.getString("bank"),
                "₱" + String.format("%,.2f", rs.getDouble("basic_pay")),
                "₱" + String.format("%,.2f", rs.getDouble("allowance")),
                rs.getString("sss_number"),
//                "₱" + String.format("%,.2f", rs.getDouble("sss_value")),
                rs.getString("pagibig_number"),
                rs.getString("tin_number"),
                rs.getString("philhealth_number")
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

    try {
        // Retrieve as java.util.Date
        java.util.Date startUtilDate = (java.util.Date) cutOffStartDateSpinner.getValue();
        java.util.Date endUtilDate = (java.util.Date) cutOffEndDateSpinner.getValue();

        // Validate range
        if (startUtilDate.after(endUtilDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.");
            return;
        }

        // Convert to java.sql.Date
        java.sql.Date sqlStart = new java.sql.Date(startUtilDate.getTime());
        java.sql.Date sqlEnd = new java.sql.Date(endUtilDate.getTime());

        // Load records from DAO
        AttendanceDAO dao = new AttendanceDAO();
        List<Attendance> records = dao.getAttendanceBetween(execId, sqlStart, sqlEnd);

        // Format and populate table
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Attendance att : records) {
            attendanceModel.addRow(new Object[]{
                sdf.format(att.getAttendanceDate()),
                att.getPresent()
            });
        }

    } catch (Exception ex) {
        ex.printStackTrace(); // For debugging
        JOptionPane.showMessageDialog(this, "Error loading attendance: " + ex.getMessage());
    }
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
    private void editSelectedExecutive() {
             String selected = (String) executiveFilterCombo.getSelectedItem();
             if (selected == null) {
                 JOptionPane.showMessageDialog(this, "Please select an executive.");
                 return;
             }

             String execId = selected.split(" - ")[0];
             try (Connection conn = DBUtil.getConnection();
                  PreparedStatement stmt = conn.prepareStatement("SELECT * FROM executive_info WHERE exec_id = ?")) {
                 stmt.setString(1, execId);
                 ResultSet rs = stmt.executeQuery();
                 if (rs.next()) {
                     AddExecutiveForm form = new AddExecutiveForm(
                         this, selectedCompany,
                         rs.getString("exec_id"),
                         rs.getString("name"),
                         rs.getString("department_or_position"),
                         rs.getString("bank"),
                         rs.getDouble("basic_pay"),
                         rs.getDouble("allowance"),
                         rs.getString("sss_number"),
//                         rs.getDouble("sss_value"),
                        
                         rs.getString("pagibig_number"),
                         rs.getString("tin_number"),
                         rs.getString("philhealth_number")
                     );
                     form.setVisible(true);
                 }
             } catch (SQLException e) {
                 JOptionPane.showMessageDialog(this, "Error loading executive: " + e.getMessage());
             }
         }


    private void deleteSelectedExecutive() {
        String selected = (String) executiveFilterCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an executive.");
            return;
        }

        String execId = selected.split(" - ")[0];
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this executive?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM executive_info WHERE exec_id = ?")) {
            stmt.setString(1, execId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Executive deleted successfully.");
                loadExecutives();
                executiveModel.setRowCount(0);
                attendanceModel.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(this, "Executive not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting executive: " + e.getMessage());
        }
    }

    public JComboBox<String> getExecutiveFilterCombo() {
        return executiveFilterCombo;
    }
}
