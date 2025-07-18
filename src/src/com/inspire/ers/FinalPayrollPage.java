package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

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

    JLabel birTaxLabel = new JLabel("0.00");
    JLabel netPayLabel = new JLabel("0.00");
    JLabel totalEmployeeContributionLabel = new JLabel("0.00");
    JLabel totalContributionLabel = new JLabel("0.00");

    Runnable compute = () -> {
        // Update selection states
        sssSelected = sssCheckbox.isSelected();
        philHealthSelected = philHealthCheckbox.isSelected();
        pagibigSelected = pagibigCheckbox.isSelected();

        // Declare deductions
        double sssTotal = 0, sssEmp = 0, sssEe = 0;
        double philTotal = 0, philEmp = 0, philEe = 0;
        double pagibigTotal = 0, pagibigEmp = 0, pagibigEe = 0;
        double birTax = 0;

        // === SSS Calculation ===
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

        // === PhilHealth Calculation ===
        if (philHealthSelected) {
    philTotal = basic * 0.05;
    philEmp = philTotal / 2;
    philEe = philTotal / 2;
}

        // === Pag-IBIG Calculation ===
        if (pagibigSelected) {
            double pagibigBase = Math.min(basic, 10000);
            pagibigEmp = pagibigBase * 0.02;
            pagibigEe = pagibigBase * 0.02;
            pagibigTotal = pagibigEmp + pagibigEe;
        }

        // === Total Employee Deductions Only ===
        double totalDeductions = sssEe + philEe + pagibigEe;
        
        double totalEmployeeContribution = sssEe + philEe + pagibigEe;
totalEmployeeContributionLabel.setText(String.format("%.2f", totalEmployeeContribution));


        // === BIR Tax ===
        // === BIR Tax ===
double taxableIncome = basic - totalDeductions;
if (taxableIncome <= 20833) {
    birTax = 0;
} else if (taxableIncome <= 33332) {
    birTax = (taxableIncome - 20833) * 0.15;
} else if (taxableIncome <= 66666) {
    birTax = 2500 + (taxableIncome - 33333) * 0.20;
} else if (taxableIncome <= 166666) {
    birTax = 10833.33 + (taxableIncome - 66667) * 0.25;
} else if (taxableIncome <= 666666) {
    birTax = 40833.33 + (taxableIncome - 166667) * 0.30;
} else {
    birTax = 200833.33 + (taxableIncome - 666667) * 0.35;
}


        // === Net Pay ===
        double netPay = basic - totalDeductions - birTax;

        // === Set Labels ===
        sssEmpLabel.setText(String.format("%.2f", sssEmp));
        sssEeLabel.setText(String.format("%.2f", sssEe));
        sssTotalLabel.setText(String.format("%.2f", sssTotal));

        philEmpLabel.setText(String.format("%.2f", philEmp));
        philEeLabel.setText(String.format("%.2f", philEe));
        philTotalLabel.setText(String.format("%.2f", philTotal));

        pagibigEmpLabel.setText(String.format("%.2f", pagibigEmp));
        pagibigEeLabel.setText(String.format("%.2f", pagibigEe));
        pagibigTotalLabel.setText(String.format("%.2f", pagibigTotal));

        if (birTax == 0) {
    birTaxLabel.setText("0 - Tax Exempted");
} else {
    birTaxLabel.setText(String.format("%.2f", birTax));
}

        netPayLabel.setText(String.format("%.2f", netPay));

        double total = sssTotal + philTotal + pagibigTotal;
        totalContributionLabel.setText(String.format("%.2f", total));
    };

    // Add listeners
    sssCheckbox.addItemListener(e -> compute.run());
    philHealthCheckbox.addItemListener(e -> compute.run());
    pagibigCheckbox.addItemListener(e -> compute.run());

    // Initial compute
    compute.run();

    // Layout panel
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setPreferredSize(new Dimension(500, 300));

    JPanel headerRow = new JPanel(new GridLayout(1, 5, 5, 5));
    headerRow.add(new JLabel("Include"));
    headerRow.add(new JLabel("Benefit"));
    headerRow.add(new JLabel("Employer"));
    headerRow.add(new JLabel("Employee"));
    headerRow.add(new JLabel("Total"));
    panel.add(headerRow);

    // SSS
    JPanel sssRow = new JPanel(new GridLayout(1, 5, 5, 5));
    sssRow.add(sssCheckbox);
    sssRow.add(new JLabel("SSS"));
    sssRow.add(sssEmpLabel);
    sssRow.add(sssEeLabel);
    sssRow.add(sssTotalLabel);
    panel.add(sssRow);

    // PhilHealth
    JPanel philRow = new JPanel(new GridLayout(1, 5, 5, 5));
    philRow.add(philHealthCheckbox);
    philRow.add(new JLabel("PhilHealth"));
    philRow.add(philEmpLabel);
    philRow.add(philEeLabel);
    philRow.add(philTotalLabel);
    panel.add(philRow);

    // Pag-IBIG
    JPanel pagibigRow = new JPanel(new GridLayout(1, 5, 5, 5));
    pagibigRow.add(pagibigCheckbox);
    pagibigRow.add(new JLabel("Pag-IBIG"));
    pagibigRow.add(pagibigEmpLabel);
    pagibigRow.add(pagibigEeLabel);
    pagibigRow.add(pagibigTotalLabel);
    panel.add(pagibigRow);
    
     // Total Contribution
    panel.add(Box.createRigidArea(new Dimension(0, 20)));
    // Total Employee Contribution
    JPanel employeeTotalRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    employeeTotalRow.add(new JLabel("Total Employee Contribution: "));
    employeeTotalRow.add(totalEmployeeContributionLabel);
    panel.add(employeeTotalRow);


    // Total Contribution
    JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    totalRow.add(new JLabel("Total Monthly Contribution: "));
    totalRow.add(totalContributionLabel);
    panel.add(totalRow);

    // BIR and Net Pay
    JPanel birRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    birRow.add(new JLabel("BIR Tax: "));
    birRow.add(birTaxLabel);
    panel.add(birRow);

    JPanel netPayRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    netPayRow.add(new JLabel("Net Pay: "));
    netPayRow.add(netPayLabel);
    panel.add(netPayRow);

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


 private String encodeImageToBase64(String imagePath) {
    try {
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    } catch (IOException e) {
        e.printStackTrace();
        return "";
    }
}
 
 private double sssValue = 0.0;
private double pagibigValue = 0.0;
private double philhealthValue = 0.0;


private void handleDownloadPayslip() {
    int selectedRow = table.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row.");
        return;
    }

    // === Get data from table ===
    String execId = model.getValueAt(selectedRow, 0).toString(); // ID NUMBER
    String name = model.getValueAt(selectedRow, 1).toString();   // EMPLOYEE NAME
    double basicPay = Double.parseDouble(model.getValueAt(selectedRow, 4).toString());
    double allowance = Double.parseDouble(model.getValueAt(selectedRow, 5).toString());
    double execAllow = Double.parseDouble(model.getValueAt(selectedRow, 6).toString());
    double mins = Double.parseDouble(model.getValueAt(selectedRow, 7).toString());
    int absentDays = Integer.parseInt(model.getValueAt(selectedRow, 9).toString());
    double halfDay = Double.parseDouble(model.getValueAt(selectedRow, 11).toString());
    double otHours = Double.parseDouble(model.getValueAt(selectedRow, 14).toString());
    double otPay = Double.parseDouble(model.getValueAt(selectedRow, 15).toString());
    int workedDays = Integer.parseInt(model.getValueAt(selectedRow, 17).toString());
    double dailyRate = Double.parseDouble(model.getValueAt(selectedRow, 18).toString());
    String payDate = model.getValueAt(selectedRow, 21).toString();
    String cutoffStart = model.getValueAt(selectedRow, 22).toString();
    String cutoffEnd = model.getValueAt(selectedRow, 23).toString();
    String payPeriod = cutoffStart + " - " + cutoffEnd;

    // === Get fixed deductions ===
    double sss = sssValue;
    double pagibig = pagibigValue;
    double philhealth = philhealthValue;

    // ✅ Get Total Absent Deduction from column 13
    double absentDeduction = Double.parseDouble(model.getValueAt(selectedRow, 13).toString());

    // ✅ Recalculate totals
    double totalEarnings = basicPay + allowance + execAllow + otPay;
    double totalDeductions = sss + pagibig + philhealth + absentDeduction;
    double netPay = totalEarnings - totalDeductions;

    // ✅ Update the Net Pay cell in the table (optional)
    model.setValueAt(String.format("%.2f", netPay), selectedRow, 16); // Column 16 = NET PAY

    // === Company Info ===
    String tin, location;
   if ("Inspire Next Global Inc.".equalsIgnoreCase(selectedCompany)) {
    tin = "010-824-345-0000";
    location = "PSE Tower One Bonifacio High Street 5th Ave. cor. 28th Street BGC, Taguig, Metro Manila";
} else if ("Inspire Alliance Fund Group Inc.".equalsIgnoreCase(selectedCompany)) {
    tin = "010-911-458-000";
    location = "MAIN OFFICE: 6F Alliance Global Tower,\n11th Avenue\ncorner 36th St, Taguig,\nMetro Manila";
} else if ("Inspire Holdings Incorporated".equalsIgnoreCase(selectedCompany)) {
    tin = "660-605-053-00000";
    location = "PSE Tower One Bonifacio High Street 5th Ave. cor. 28th Street BGC, Taguig, Metro Manila";
} else {
    tin = "123-456-789-000";
    location = "123 Main St., Makati City, Philippines";
}

String logoUrl;

if ("Inspire Next Global Inc.".equalsIgnoreCase(selectedCompany)) {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/inspirenextglobal.png");
} else if ("Inspire Alliance Fund Group Inc.".equalsIgnoreCase(selectedCompany)) {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/inspirealliance.png");
} else if ("Inspire Holdings Incorporated".equalsIgnoreCase(selectedCompany)) {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/inpireholding.png");
} else {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/deepocean5.jpg");
}
    // Format HTML
    String html = String.format("""
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Payslip</title>
<style>
    @page { margin: 2cm 3.18cm; }
    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; color: #222; }
    .company-branding {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 15px;
        margin-bottom: 5px;
    }
    
    .company-logo {
        max-width: 50px;
        height: auto;
    }
    
    .company-name {
        font-size: 18px;
        font-weight: bold;
    }
    .company-info { text-align: center; font-size: 8px; color: #555; margin-bottom: 25px; }
    .info-section { background-color: #f9f9f9; border: 1px solid #ccc; padding: 5px; border-radius: 6px; margin-bottom: 20px; }
    .info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px 10px; font-size: 10px;}
    .info-row { display: flex; gap: 5px; font-size: 9px;}
    .label { font-weight: 600; min-width: 100px; color: #333; }
    .value { color: #444; }
    table { width: 100%%; border-collapse: collapse; margin-bottom: 10px; font-size: 10px; }
    th, td { border: 1px solid #999; padding: 8px 12px; text-align: left;font-size: 10px; }
    th { background-color: #e9e9e9; }
    .summary-and-signatures { display: flex; justify-content: space-between; margin-top: 15px; gap: 15px;font-size: 10px; }
    .net-summary { flex: 1; }
    .signature-boxes { flex: 1; display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; text-align: center;font-size: 10px; }
    .sig-box { margin-top: 30px; color: black;font-size: 10px; }
    .sig-line { border-top: 1px solid #000; margin-top: 30px; padding-top: 5px; font-size: 13px; font-size: 10px;}
</style>
</head>
<body>
<div class="company-branding">
    <img src="%s" alt="Company Logo" class="company-logo">
    <div class="company-name">%s</div>
</div>
                                
                                

<div class="company-info">TIN: %s &nbsp; | &nbsp; Location: <span>%s</span></div>

<div class="info-section">
    <div class="info-grid">
        <div class="info-row"><span class="label">Employee Name:</span><span class="value">%s</span></div>
        <div class="info-row"><span class="label">Pay Date:</span><span class="value">%s</span></div>
        <div class="info-row"><span class="label">Worked Days:</span><span class="value">%d</span></div>
        <div class="info-row"><span class="label">Employee ID:</span><span class="value">%s</span></div>
        <div class="info-row"><span class="label">Pay Period:</span><span class="value">%s</span></div>
    </div>
</div>

<table>
    <thead><tr><th colspan="2">Earnings</th></tr></thead>
    <tr><td>Basic Pay</td><td>Php %.2f</td></tr>
    <tr><td>Allowance</td><td>Php %.2f</td></tr>
    <tr><td>Executive Allowance</td><td>Php %.2f</td></tr>
    <tr><td>Overtime Pay</td><td>Php %.2f</td></tr>
    <tr><th>Total Earnings</th><th>Php %.2f</th></tr>
</table>

<table>
    <thead><tr><th>Deduction</th><th>Amount</th></tr></thead>
    <tr><td>SSS</td><td>Php %.2f</td></tr>
    <tr><td>Pag-IBIG</td><td>Php %.2f</td></tr>
    <tr><td>PhilHealth</td><td>Php %.2f</td></tr>

    <tr><td>Absent Deduction (%d Day%s)</td><td>Php %.2f</td></tr>
    <tr><th>Total Deductions</th><th>Php %.2f</th></tr>
</table>

<div class="summary-and-signatures">
    <div class="signature-boxes">
        <div class="sig-box"><div class="sig-line">CEO Signature</div></div>
        <div class="sig-box"><div class="sig-line">President Signature</div></div>
        <div class="sig-box"><div class="sig-line">Accounting Signature</div></div>
        <div class="sig-box"><div class="sig-line">Employee Signature</div></div>
    </div>

    <table class="net-summary">
        <thead><tr><th colspan="2">Net Pay Summary</th></tr></thead>
        <tr><td>Gross Earnings</td><td>Php %.2f</td></tr>
        <tr><td>Total Deductions</td><td>Php %.2f</td></tr>
        <tr><th>Net Pay</th><th>Php %.2f</th></tr>
    </table>
</div>

</body>
</html>
""",
        logoUrl, selectedCompany, tin, location,
        name, payDate, workedDays,
        execId, payPeriod,
        basicPay, allowance, execAllow, otPay, totalEarnings,
        sss, pagibig, philhealth, absentDays, absentDays == 1 ? "" : "s", absentDeduction, totalDeductions,
        totalEarnings, totalDeductions, netPay
    );

    // Convert to PDF
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

    // Auto-fill the blank columns (Absent/half-day computation)
    for (int i = 0; i < model.getRowCount(); i++) {
        try {
            BigDecimal dailyRate = new BigDecimal(model.getValueAt(i, 18).toString()); // daily
            int absent = Integer.parseInt(model.getValueAt(i, 9).toString());           // absent days
            int halfDay = Integer.parseInt(model.getValueAt(i, 11).toString());         // half-day count

            BigDecimal absentAmount = dailyRate.multiply(BigDecimal.valueOf(absent)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal halfDayAmount = dailyRate.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(halfDay))
                                                .setScale(2, RoundingMode.HALF_UP);

            model.setValueAt(absentAmount.toString(), i, 10);  // Computed absent amount
            model.setValueAt(halfDayAmount.toString(), i, 12); // Computed half-day amount

        } catch (Exception e) {
            e.printStackTrace(); // silent fail per row
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
