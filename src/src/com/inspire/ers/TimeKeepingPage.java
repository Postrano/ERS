package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import src.com.inspire.ers.EmployeePayroll;
import src.com.inspire.ers.EmployeePayrollDAO;


public class TimeKeepingPage extends JFrame {

    private JTable timeTable;

    public TimeKeepingPage(String selectedCompany) {
        setTitle("Time Keeping - " + selectedCompany);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columnNames = {
            "ID No", "Employee Name", "Department/Position",
            "Worked Days", "Minutes Late", "Absent", "Half Day", "OT Hours"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only "Worked Days" editable
                return column == 3; // Index for Worked Days
            }
        };

        timeTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(timeTable);
        add(scrollPane, BorderLayout.CENTER);

        loadTimeKeepingData(selectedCompany, model);
    }

   private void loadTimeKeepingData(String selectedCompany, DefaultTableModel model) {
    List<Employee> employees = EmployeeDAO.fetchEmployeesByCompany(selectedCompany);

    for (Employee emp : employees) {
        List<EmployeePayroll> payrolls = EmployeePayrollDAO.fetchAllPayrollsByEmployeeId(emp.getId());

        int totalLate = 0;
        int absentCount = 0;
        int halfDayCount = 0;
        int totalOvertime = 0;

        for (EmployeePayroll p : payrolls) {
            totalLate += p.getLateDeduction();
            if ("absent".equalsIgnoreCase(p.getRemarks())) {
                absentCount++;
            }
            if (p.isHalfDay()) {
                halfDayCount++;
            }
            totalOvertime += p.getOvertimeMinutes();
        }

        model.addRow(new Object[]{
            emp.getIdNumber(),
            emp.getFirstName() + " " + emp.getLastName(),
            emp.getPosition(),
            "", // Worked Days input
            totalLate,
            absentCount,
            halfDayCount,
            totalOvertime
        });
    }
}


}
