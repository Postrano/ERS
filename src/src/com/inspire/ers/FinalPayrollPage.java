package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FinalPayrollPage extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private final String selectedCompany;

    public FinalPayrollPage(String selectedCompany) {
        this.selectedCompany = selectedCompany;
        setTitle("Final Payroll Page");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Header Panel ===
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(Color.WHITE);

        JLabel companyLabel = new JLabel("Inspire Next Global Solutions Inc.", SwingConstants.CENTER);
        companyLabel.setFont(new Font("Serif", Font.BOLD, 20));

        JLabel titleLabel = new JLabel("EMPLOYEE PAYROLL SUMMARY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 16));

        headerPanel.add(companyLabel);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // === Table Setup ===
        String[] columns = {
            "ID NUMBER", "EMPLOYEE NAME", "DEPARTMENT/POSITION", "BANK",
            "BASIC PAY", "ALLOWANCE", "REFRESHMENT", "MINS",
            "TOTAL LATE", "ABSENT", " ", "HALF DAY",
            " ", "TOTAL ABSENT", "OT HOURS", "OT PAY",
            "NET PAY", "NUMBER OF DAYS", "DAILY",
            "PER HOUR", "PER MINUTE",
            "PAY DATE", "CUTOFF START", "CUTOFF END",
                "BENEFITS" // <- ADD THIS
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] columnWidths = {
            100, 150, 180, 100,
            100, 100, 100, 60,
            100, 80, 100, 80,
            100, 100, 80, 80,
            100, 130, 80,
            90, 100,
            100, 100, 100,
                100 // <- for "BENEFITS"
        };
        
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Create "Benefits" button renderer and editor
table.getColumn("BENEFITS").setCellRenderer(new ButtonRenderer());
table.getColumn("BENEFITS").setCellEditor(new ButtonEditor(new JCheckBox()));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        for (int i = 0; i < columnWidths.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidths[i]);
        }
        
         applyColumnColoring();
         
         // Listen to OT HOURS column changes and update OT PAY automatically
    model.addTableModelListener(e -> {
    int row = e.getFirstRow();
    int column = e.getColumn();

    // Check if the edited column is OT HOURS (column 14)
    if (column == 14) {
        try {
            String otHoursStr = model.getValueAt(row, 14).toString();
            double otHours = Double.parseDouble(otHoursStr);
            double otPay = otHours * 128.85;

            // Set the computed OT PAY at column 15
            model.setValueAt(String.format("%.2f", otPay), row, 15);
        } catch (Exception ex) {
            model.setValueAt("0.00", row, 15);
        }
    }
});

        JScrollPane scrollPane = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // === Month Filter Dropdown ===
        String[] months = {
            "Add New Payroll Entry", "01 - January", "02 - February", "03 - March", "04 - April",
            "05 - May", "06 - June", "07 - July", "08 - August", "09 - September",
            "10 - October", "11 - November", "12 - December"
        };

        JComboBox<String> monthCombo = new JComboBox<>(months);
        JPanel topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topFilterPanel.add(new JLabel("Filter by Month:"));
        topFilterPanel.add(monthCombo);
        add(topFilterPanel, BorderLayout.BEFORE_FIRST_LINE);

        monthCombo.addActionListener(e -> {
            int selectedIndex = monthCombo.getSelectedIndex();
            if (selectedIndex == 0) {
                openAddPayrollDialog();
            } else {
                String selectedMonth = String.format("%02d", selectedIndex);
                refreshTable(selectedMonth);
            }
        });

        refreshTable("");

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(this::handleSaveChanges);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(saveButton);
        
        JButton downloadPayslipButton = new JButton("Download Payslip");
        downloadPayslipButton.addActionListener(e -> handleDownloadPayslip());
        bottomPanel.add(downloadPayslipButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setText("Benefits");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String basicPay;
    private boolean clicked;
    private JTable table;

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton("Benefits");
        button.addActionListener(e -> showBenefitsDialog());
    }
    
    private boolean sssSelected = false;
private boolean philHealthSelected = false;
private boolean pagibigSelected = false;


    private void showBenefitsDialog() {
    int row = table.getSelectedRow();
    if (row == -1) return;

    String basicStr = table.getValueAt(row, 4).toString(); // BASIC PAY
    double basic = Double.parseDouble(basicStr);

    JCheckBox sssCheckbox = new JCheckBox();
    JCheckBox philHealthCheckbox = new JCheckBox();
    JCheckBox pagibigCheckbox = new JCheckBox();

    // Restore previous selections
    sssCheckbox.setSelected(sssSelected);
    philHealthCheckbox.setSelected(philHealthSelected);
    pagibigCheckbox.setSelected(pagibigSelected);

    // Labels
    JLabel sssEmpLabel = new JLabel("0.00");
    JLabel sssEeLabel = new JLabel("0.00");
    JLabel sssTotalLabel = new JLabel("0.00");

    JLabel philEmpLabel = new JLabel("0.00");
    JLabel philEeLabel = new JLabel("0.00");
    JLabel philTotalLabel = new JLabel("0.00");

    JLabel pagibigEmpLabel = new JLabel("0.00");
    JLabel pagibigEeLabel = new JLabel("0.00");
    JLabel pagibigTotalLabel = new JLabel("0.00");

    JLabel totalContributionLabel = new JLabel("0.00");

    // Compute function
    Runnable compute = () -> {
        // Save current states to fields
        sssSelected = sssCheckbox.isSelected();
        philHealthSelected = philHealthCheckbox.isSelected();
        pagibigSelected = pagibigCheckbox.isSelected();

        double sssEmp = 0, sssEe = 0, sssTotal = 0;
        double philEmp = 0, philEe = 0, philTotal = 0;
        double pagibigEmp = 0, pagibigEe = 0, pagibigTotal = 0;

        if (sssSelected) {
            sssTotal = basic * 0.15;
            sssEmp = basic * 0.10;
            sssEe = basic * 0.05;
            if (basic <= 14000) {
                sssEmp += 10;
                sssTotal += 10;
            } else if (basic >= 15000) {
                sssEmp += 30;
                sssTotal += 30;
            }
        }

        if (philHealthSelected) {
            philTotal = basic * 0.05;
            philEmp = philTotal / 2;
            philEe = philTotal / 2;
        }

        if (pagibigSelected) {
            if (basic <= 1500) {
                pagibigEmp = basic * 0.02;
                pagibigEe = basic * 0.01;
            } else if (basic >= 10000) {
                pagibigEmp = basic * 0.02;
                pagibigEe = basic * 0.02;
            }
            pagibigTotal = pagibigEmp + pagibigEe;
        }

        // Update labels
        sssEmpLabel.setText(String.format("%.2f", sssEmp));
        sssEeLabel.setText(String.format("%.2f", sssEe));
        sssTotalLabel.setText(String.format("%.2f", sssTotal));

        philEmpLabel.setText(String.format("%.2f", philEmp));
        philEeLabel.setText(String.format("%.2f", philEe));
        philTotalLabel.setText(String.format("%.2f", philTotal));

        pagibigEmpLabel.setText(String.format("%.2f", pagibigEmp));
        pagibigEeLabel.setText(String.format("%.2f", pagibigEe));
        pagibigTotalLabel.setText(String.format("%.2f", pagibigTotal));

        double total = sssTotal + philTotal + pagibigTotal;
        totalContributionLabel.setText(String.format("%.2f", total));
    };

    // Add listeners
    sssCheckbox.addItemListener(e -> compute.run());
    philHealthCheckbox.addItemListener(e -> compute.run());
    pagibigCheckbox.addItemListener(e -> compute.run());

    // Initial compute based on restored states
    compute.run();

    // Layout panel
    JPanel panel = new JPanel(new GridLayout(0, 5, 10, 5));
    panel.add(new JLabel("Include"));
    panel.add(new JLabel("Benefit"));
    panel.add(new JLabel("Employer"));
    panel.add(new JLabel("Employee"));
    panel.add(new JLabel("Total"));

    panel.add(sssCheckbox);
    panel.add(new JLabel("SSS"));
    panel.add(sssEmpLabel);
    panel.add(sssEeLabel);
    panel.add(sssTotalLabel);

    panel.add(philHealthCheckbox);
    panel.add(new JLabel("PhilHealth"));
    panel.add(philEmpLabel);
    panel.add(philEeLabel);
    panel.add(philTotalLabel);

    panel.add(pagibigCheckbox);
    panel.add(new JLabel("Pag-IBIG"));
    panel.add(pagibigEmpLabel);
    panel.add(pagibigEeLabel);
    panel.add(pagibigTotalLabel);

    panel.add(new JLabel());
    panel.add(new JLabel("TOTAL MONTHLY CONTRIBUTION:"));
    panel.add(totalContributionLabel);
    panel.add(new JLabel());
    panel.add(new JLabel());

    JOptionPane.showMessageDialog(null, panel, "Benefit Calculation", JOptionPane.PLAIN_MESSAGE);
}

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
        this.table = table;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Benefits";
    }

    @Override
    public boolean stopCellEditing() {
        clicked = false;
        return super.stopCellEditing();
    }
}
private void handleDownloadPayslip() {
    int selectedRow = table.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row.");
        return;
    }

    String execId = model.getValueAt(selectedRow, 0).toString(); // ID NUMBER
    String name = model.getValueAt(selectedRow, 1).toString();   // EMPLOYEE NAME
    String basicPay = model.getValueAt(selectedRow, 4).toString();
    String allowance = model.getValueAt(selectedRow, 5).toString();
    String refreshment = model.getValueAt(selectedRow, 6).toString();
    String mins = model.getValueAt(selectedRow, 7).toString();
    String absent = model.getValueAt(selectedRow, 9).toString();
    String halfDay = model.getValueAt(selectedRow, 11).toString();
    String otHours = model.getValueAt(selectedRow, 14).toString();
    String otPay = model.getValueAt(selectedRow, 15).toString();
    String netPay = model.getValueAt(selectedRow, 16).toString();
    String workedDays = model.getValueAt(selectedRow, 17).toString();
    String payDate = model.getValueAt(selectedRow, 21).toString();
    String cutoffStart = model.getValueAt(selectedRow, 22).toString();
    String cutoffEnd = model.getValueAt(selectedRow, 23).toString();
    String payPeriod = cutoffStart + " - " + cutoffEnd;

    String html = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Employee Payslip</title>
<style>
    body { font-family: Arial, sans-serif; margin: 40px auto; max-width: 800px; padding: 20px; }
    .payslip { border: 1px solid #000; padding: 20px; }
    .header { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
    .info-row { display: grid; grid-template-columns: 200px auto; margin-bottom: 10px; }
    .info-label { font-weight: bold; }
    .id-number { background-color: #e8f5e9; padding: 2px 5px; }
    .main-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
    .main-table th, .main-table td { border: 1px solid #000; padding: 8px; }
    .earnings-col { width: 40%%; }
    .amount-col { width: 10%%; text-align: right; }
    .deductions-col { width: 40%%; }
    .total-row { font-weight: bold; }
    .signature-section { display: flex; justify-content: space-between; margin-top: 50px; }
    .signature-line { border-top: 1px solid #000; width: 250px; text-align: center; padding-top: 5px; }
</style>
</head>
<body>
<div class="payslip">
<div class="header">
    <div>
        <div class="info-row"><span class="info-label">Employee Name</span><span>%s</span></div>
        <div class="info-row"><span class="info-label">ID No.</span><span class="id-number">%s</span></div>
    </div>
    <div>
        <div class="info-row"><span class="info-label">Pay Date</span><span>%s</span></div>
        <div class="info-row"><span class="info-label">Worked Days</span><span>%s</span></div>
        <div class="info-row"><span class="info-label">Pay Period</span><span>%s</span></div>
    </div>
</div>

<table class="main-table">
<tr>
    <th class="earnings-col">Earnings</th><th class="amount-col">Amount</th>
    <th class="deductions-col">Deductions</th><th class="amount-col">Amount</th>
</tr>
<tr><td>Basic Pay</td><td>%s</td><td>Absent</td><td>%s</td></tr>
<tr><td>Allowance</td><td>%s</td><td>PagIbig</td><td>%s</td></tr>
<tr><td>Refreshment</td><td>%s</td><td>PhilHealth</td><td>%s</td></tr>
<tr><td>OT Hours</td><td>%s</td><td></td><td></td></tr>
<tr><td>OT Pay</td><td>%s</td><td></td><td></td></tr>
<tr class="total-row">
    <td colspan="2">Net Pay</td><td colspan="2" style="text-align: right;">%s</td>
</tr>
</table>

<div class="signature-section">
    <div class="signature-line">Employer Signature</div>
    <div class="signature-line">Employee Signature</div>
</div>
</div>
</body>
</html>
""".formatted(
        name, execId,
        payDate, workedDays, payPeriod,
        basicPay, absent,
        allowance, halfDay,
        refreshment, mins,
        otHours, otPay,
        netPay
    );

    try {
        PdfShiftConverter converter = new PdfShiftConverter("sk_dc48b1f99bb971396765c80111f7d78e9e5fa723");
        converter.convertToPdfWithChooser(html);
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to generate payslip: " + e.getMessage());
    }
}


 
private int countWeekdaysBetween(String start, String end) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    LocalDate startDate = LocalDate.parse(start, formatter);

    LocalDate endDate = LocalDate.parse(end, formatter);
 
    int weekdays = 0;

    while (!startDate.isAfter(endDate)) {

        DayOfWeek day = startDate.getDayOfWeek();

        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {

            weekdays++;

        }

        startDate = startDate.plusDays(1);

    }

    return weekdays;

}

    private void openAddPayrollDialog() {
    JTextField payDateField = new JTextField("2025-08-31");
    JTextField cutoffStart = new JTextField();
    JTextField cutoffEnd = new JTextField();

    // Create working days options
    JRadioButton option21 = new JRadioButton("21 days");
    JRadioButton option22 = new JRadioButton("22 days");
    JRadioButton optionOther = new JRadioButton("Other:");
    JTextField otherField = new JTextField();
    otherField.setEnabled(false);

    // Group radio buttons
    ButtonGroup group = new ButtonGroup();
    group.add(option21);
    group.add(option22);
    group.add(optionOther);

    option22.setSelected(true); // Default selection

    // Enable/disable the custom text field
    optionOther.addActionListener(e -> otherField.setEnabled(true));
    option21.addActionListener(e -> otherField.setEnabled(false));
    option22.addActionListener(e -> otherField.setEnabled(false));

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Pay Date (YYYY-MM-DD):"));
    panel.add(payDateField);
    panel.add(new JLabel("Cutoff Start:"));
    panel.add(cutoffStart);
    panel.add(new JLabel("Cutoff End:"));
    panel.add(cutoffEnd);

    panel.add(new JLabel("Number of Working Days:"));
    panel.add(option21);
    panel.add(option22);
    JPanel otherPanel = new JPanel(new BorderLayout());
    otherPanel.add(optionOther, BorderLayout.WEST);
    otherPanel.add(otherField, BorderLayout.CENTER);
    panel.add(otherPanel);

    int result = JOptionPane.showConfirmDialog(this, panel, "Add Payroll for ALL Employees", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String payDate = payDateField.getText().trim();
        String cutoffStartStr = cutoffStart.getText().trim();
        String cutoffEndStr = cutoffEnd.getText().trim();

        if (payDate.isEmpty() || cutoffStartStr.isEmpty() || cutoffEndStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        int numberOfDays = 22; // default
        if (option21.isSelected()) {
            numberOfDays = 21;
        } else if (option22.isSelected()) {
            numberOfDays = 22;
        } else if (optionOther.isSelected()) {
            try {
                numberOfDays = Integer.parseInt(otherField.getText().trim());
                if (numberOfDays <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number of working days.");
                return;
            }
        }

        List<String[]> employees = EmployeeDataFetcher.fetchAllEmployeesOnly();

        for (String[] emp : employees) {
            String[] newRow = new String[24];
            newRow[0] = emp[0]; // ID
            newRow[1] = emp[1]; // Name
            newRow[2] = emp[2]; // Dept/Position
            newRow[3] = emp[3]; // Bank
            newRow[4] = emp[4]; // Basic Pay
            newRow[5] = emp[5]; // Allowance
            newRow[6] = "0";    // Refreshment
            newRow[7] = "0";    // Mins
            newRow[8] = "0";    // Total Late
            newRow[9] = "0";    // Absent
            newRow[10] = newRow[9];    // Spacer
            newRow[11] = "0";   // Half Day
            newRow[12] = newRow[11];    // Spacer
            newRow[13] = "0";   // Total Absent
            newRow[14] = "0";   // OT Hours
            newRow[15] = "0";   // OT Pay
            newRow[16] = emp[6]; // Monthly Salary
            newRow[17] = String.valueOf(numberOfDays); // ✅ Number of Days
            newRow[18] = "0";   // Daily
            newRow[19] = "0";   // Per Hour
            newRow[20] = "0";   // Per Minute
            newRow[21] = payDate;
            newRow[22] = cutoffStartStr;
            newRow[23] = cutoffEndStr;

            model.addRow(newRow);
            boolean inserted = EmployeeDataUpdater.insertPayrollOnly(newRow, selectedCompany);
            if (!inserted) {
                System.err.println("Payroll insert failed for: " + newRow[0]);
            }
        }

        JOptionPane.showMessageDialog(this, "Payroll entries for " + payDate + " added.");
    }
}

    private void refreshTable(String monthFilter) {
        model.setRowCount(0);
        List<String[]> employeeData = EmployeeDataFetcher.fetchEmployeeData(monthFilter, this.selectedCompany);
        for (String[] row : employeeData) {
            model.addRow(row);
        }
        // Auto-fill the blank columns
        for (int i = 0; i < model.getRowCount(); i++) {
    try {
        BigDecimal dailyRate = new BigDecimal(model.getValueAt(i, 18).toString()); // assuming col 18 = daily
        int absent = Integer.parseInt(model.getValueAt(i, 9).toString());
        int halfDay = Integer.parseInt(model.getValueAt(i, 11).toString());

        BigDecimal absentAmount = dailyRate.multiply(BigDecimal.valueOf(absent)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal halfDayAmount = dailyRate.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(halfDay))
                                            .setScale(2, RoundingMode.HALF_UP);

        model.setValueAt(absentAmount.toString(), i, 10);  // Show computed absent value
        model.setValueAt(halfDayAmount.toString(), i, 12); // Show computed half-day value

    } catch (Exception e) {
        e.printStackTrace(); // Fail silently for bad rows
    }
}
    }

   private void handleSaveChanges(ActionEvent e) {
    int selectedRow = table.getSelectedRow();
    System.out.println("Testing");
    
    System.out.println("SelectedRow index: " + selectedRow + ", PayDate in row: " + table.getValueAt(selectedRow, 21));
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to edit.");
        return;
    }

    if (table.isEditing()) {
        table.getCellEditor().stopCellEditing();
    }

    String[] updatedData = new String[table.getColumnCount()];
    for (int i = 0; i < updatedData.length; i++) {
        Object value = table.getValueAt(selectedRow, i);
        updatedData[i] = (value != null) ? value.toString().trim() : "";
    }

    boolean success = EmployeeDataUpdater.updateEmployeeData(updatedData, selectedCompany);
    if (success) {
        JOptionPane.showMessageDialog(this, "Update successful.");

        // === Recompute adjusted salary and update NET PAY column ===
        // === Fetch adjusted salary from database directly ===
        try {
            String idNumber = updatedData[0];
            String payDate = updatedData[21];
            
            System.out.println("Updating salary for ID " + idNumber + " on PAY DATE " + payDate);
            
            System.out.println("Row selected: " + selectedRow);
            System.out.println("ID: " + updatedData[0]);
            System.out.println("PAY DATE: " + updatedData[21]);


            BigDecimal adjustedSalary = EmployeeDataFetcher.fetchAdjustedSalary(idNumber, updatedData[21]);
            model.setValueAt(adjustedSalary.toPlainString(), selectedRow, 16); // NET PAY column
            model.fireTableRowsUpdated(selectedRow, selectedRow);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching adjusted salary: " + ex.getMessage());
        }


    } else {
        JOptionPane.showMessageDialog(this, "Update failed.");
    }
}
   
   private void applyColumnColoring() {
    // Column indexes (0-based)
    int[] greenBackgroundCols = {5, 6, 7, 9, 11, 14};
    int[] redTextCols = {7, 9, 11, 14};

    // Declare the renderer OUTSIDE the loop so it's accessible
    DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Reset styles if selected
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            } else {
                // Default colors
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);

                // Apply green background
                for (int greenCol : greenBackgroundCols) {
                    if (column == greenCol) {
                        c.setBackground(new Color(200, 255, 200)); // light green
                        break;
                    }
                }

                // Apply red text
                for (int redCol : redTextCols) {
                    if (column == redCol) {
                        c.setForeground(Color.RED);
                        break;
                    }
                }
            }

            return c;
        }
    };

    // Apply the renderer to all columns EXCEPT "BENEFITS"
    for (int i = 0; i < table.getColumnCount(); i++) {
        if (table.getColumnName(i).equals("BENEFITS")) continue; // Skip the button column
        table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
    }
}
}