package com.inspire.ers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends JFrame {
    private ArrayList<Employee> employees = new ArrayList<>();
    private JLabel employeeCountLabel;
    private JPanel employeeListPanel;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private String selectedCompany;

    public HomePage(String company, String role) {
        this.selectedCompany = company;

        setTitle("INSPIRE EMPLOYEE RECORDS SYSTEM - " + company);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setIconImage(new ImageIcon(getClass().getResource("/images/inspirelogo2.jpg")).getImage());

        // === Main Panel ===
        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(13, 27, 42));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setContentPane(mainPanel);

        // === Top Panel ===
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(27, 38, 59));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("Welcome to " + company + " Employees");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        welcomeLabel.setForeground(Color.WHITE);

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        welcomePanel.setBackground(topPanel.getBackground());
        welcomePanel.add(welcomeLabel);

        // === Filters ===
        JComboBox<String> positionFilter = createStyledComboBox(new String[]{"All Departments", "System Developer", "Marketing", "Sales Associate"});
        JComboBox<String> companyFilter = createStyledComboBox(new String[]{"All Companies", "IHI", "INGI", "INSPIRE ALLIANCE"});
        JTextField searchField = createStyledTextField(20);

        // === Buttons ===
        JButton executiveBtn = createStyledButton("EXECUTIVE");
        JButton addEmployeeBtn = createStyledButton("ADD EMPLOYEE");
        JButton finalPayrollBtn = createStyledButton("PAYROLL");
        JButton timeKeepingBtn = new JButton("TIME KEEPING");

        employeeCountLabel = new JLabel("#Employee: 0");
        employeeCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeCountLabel.setForeground(Color.WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(topPanel.getBackground());
        leftPanel.add(executiveBtn);
        leftPanel.add(addEmployeeBtn);
        leftPanel.add(finalPayrollBtn);
        leftPanel.add(timeKeepingBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(topPanel.getBackground());
        rightPanel.add(employeeCountLabel);
        if ("ALL".equalsIgnoreCase(selectedCompany)) {
            rightPanel.add(companyFilter);
        }
        rightPanel.add(positionFilter);
        rightPanel.add(searchField);

        topPanel.add(welcomePanel, BorderLayout.NORTH);
        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // === Employee List Panel ===
        employeeListPanel = new JPanel();
        employeeListPanel.setLayout(new BoxLayout(employeeListPanel, BoxLayout.Y_AXIS));
        employeeListPanel.setBackground(mainPanel.getBackground());

        scrollPane = new JScrollPane(employeeListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(mainPanel.getBackground());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // === Actions ===
        executiveBtn.addActionListener(e -> new ExecutivePage(selectedCompany).setVisible(true));
        addEmployeeBtn.addActionListener(e -> EmployeeForm.createForNewEmployee(this, selectedCompany).setVisible(true));
        finalPayrollBtn.addActionListener(e -> new FinalPayrollPage("ALL".equalsIgnoreCase(selectedCompany) ? "ALL" : selectedCompany).setVisible(true));
                timeKeepingBtn.addActionListener(e -> {
            TimeKeepingPage tkPage = new TimeKeepingPage(selectedCompany);
            tkPage.setVisible(true);
        });
        
        KeyAdapter keyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updateFilter(searchField.getText(), positionFilter.getSelectedItem().toString(),
                        "ALL".equalsIgnoreCase(selectedCompany) ? companyFilter.getSelectedItem().toString() : selectedCompany);
            }
        };

        searchField.addKeyListener(keyListener);
        positionFilter.addActionListener(e -> updateFilter(searchField.getText(), positionFilter.getSelectedItem().toString(),
                "ALL".equalsIgnoreCase(selectedCompany) ? companyFilter.getSelectedItem().toString() : selectedCompany));
        if ("ALL".equalsIgnoreCase(selectedCompany)) {
            companyFilter.addActionListener(e -> updateFilter(searchField.getText(), positionFilter.getSelectedItem().toString(), companyFilter.getSelectedItem().toString()));
        }

        loadEmployeesFromDB();
    }

    private void loadEmployeesFromDB() {
        List<Employee> dbEmployees = "ALL".equalsIgnoreCase(selectedCompany)
                ? EmployeeDAO.fetchAllEmployees()
                : EmployeeDAO.fetchEmployeesByCompany(selectedCompany);

        employees.clear();
        employees.addAll(dbEmployees);
        updateEmployeeList();
        employeeCountLabel.setText("#Employee: " + employees.size());
    }

    private void updateEmployeeList() {
        employeeListPanel.removeAll();
        for (Employee employee : employees) {
            employeeListPanel.add(createEmployeeCard(employee));
            employeeListPanel.add(Box.createVerticalStrut(10));
        }
        employeeListPanel.revalidate();
        employeeListPanel.repaint();
    }

    private JPanel createEmployeeCard(Employee employee) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(new Color(33, 45, 65));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel nameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton editBtn = createStyledMiniButton("Edit");
        JButton timeBtn = createStyledMiniButton("Time");
        JButton removeBtn = createStyledMiniButton("Remove");

        editBtn.addActionListener(e -> new EmployeeForm(HomePage.this, employee, getSelectedCompany()).setVisible(true));
        timeBtn.addActionListener(e -> {
            String fullName = (employee.getFirstName() + " " +
                    (employee.getMiddleName() == null ? "" : employee.getMiddleName() + " ") +
                    employee.getLastName()).trim();
            new PayrollPage(fullName, String.valueOf(employee.getId())).setVisible(true);
        });
        removeBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(HomePage.this,
                    "Are you sure you want to remove this employee?",
                    "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                EmployeeDAO.softRemoveEmployee(employee.getId());
                refreshEmployeeList();
            }
        });

        buttonPanel.add(timeBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);

        card.add(nameLabel, BorderLayout.WEST);
        card.add(buttonPanel, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(40, 55, 80));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(33, 45, 65));
            }
        });

        return card;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(new Color(55, 71, 100));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createStyledMiniButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(new Color(55, 71, 100));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(160, 30));
        comboBox.setBackground(new Color(224, 224, 224));
        comboBox.setForeground(Color.BLACK);
        return comboBox;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    private void updateFilter(String searchText, String position, String company) {
        employeeListPanel.removeAll();

        boolean isAllCompanies = "All Companies".equalsIgnoreCase(company);
        boolean isAllDepartments = "All Departments".equalsIgnoreCase(position);

        for (Employee emp : employees) {
            boolean matchesSearch = (emp.getFirstName() + " " + emp.getLastName()).toLowerCase().contains(searchText.toLowerCase());
            boolean matchesCompany = isAllCompanies || (emp.getCompany() != null && emp.getCompany().equalsIgnoreCase(company));
            boolean matchesPosition = isAllDepartments || (emp.getPosition() != null && emp.getPosition().equalsIgnoreCase(position));

            if (matchesSearch && matchesCompany && matchesPosition) {
                employeeListPanel.add(createEmployeeCard(emp));
                employeeListPanel.add(Box.createVerticalStrut(10));
            }
        }

        employeeListPanel.revalidate();
        employeeListPanel.repaint();
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        updateEmployeeList();
        employeeCountLabel.setText("#Employee: " + employees.size());
    }

    public void refreshEmployeeList() {
        employees.clear();
        loadEmployeesFromDB();
    }

    public String getSelectedCompany() {
        return selectedCompany;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font uiFont = new Font("Segoe UI", Font.PLAIN, 13);
            UIManager.put("Label.font", uiFont);
            UIManager.put("Button.font", uiFont);
            UIManager.put("TextField.font", uiFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new HomePage("IHI", "Super Admin").setVisible(true));
    }
}