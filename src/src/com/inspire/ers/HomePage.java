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

    public HomePage(String company) {
        this.selectedCompany = company;
        setTitle("INSPIRE EMPLOYEE RECORDS SYSTEM - " + company);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen
        setLocationRelativeTo(null);

        // Set icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/inspirelogo2.jpg"));
        setIconImage(icon.getImage());

        // === Main Panel ===
        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 245, 250)); // light gray background

        // === Top Bar ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

       
        // Welcome Message Panel (Centered at Top)
        JLabel welcomeLabel = new JLabel("Welcome to " + company.toUpperCase() + " Employees");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(33, 37, 41));

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        welcomePanel.setBackground(topPanel.getBackground());
        welcomePanel.add(welcomeLabel);

        // Search Field
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(200, 30));

        // Buttons
        JButton executiveBtn = new JButton("EXECUTIVE");
        JButton addEmployeeBtn = new JButton("ADD EMPLOYEE");
        JButton finalPayrollBtn = new JButton("PAYROLL");
        executiveBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addEmployeeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        finalPayrollBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Employee Count
        employeeCountLabel = new JLabel("#Employee: 0");
        employeeCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeCountLabel.setForeground(new Color(80, 80, 80));

        // Button Container
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(topPanel.getBackground());
        leftPanel.add(executiveBtn);
        leftPanel.add(addEmployeeBtn);
        leftPanel.add(finalPayrollBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(topPanel.getBackground());
        rightPanel.add(employeeCountLabel);
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
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(mainPanel.getBackground());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

        // === Button Actions ===
        executiveBtn.addActionListener(e -> {
            ExecutivePage executivePage = new ExecutivePage(selectedCompany);
            executivePage.setVisible(true);
        });


        addEmployeeBtn.addActionListener(e -> {
            EmployeeForm employeeForm = EmployeeForm.createForNewEmployee(this, selectedCompany);
            employeeForm.setVisible(true);
        });
        
        finalPayrollBtn.addActionListener(e -> {
            FinalPayrollPage finalPayrollPage = new FinalPayrollPage();
            finalPayrollPage.setVisible(true);
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText().toLowerCase();
                filterEmployeeList(searchText);
            }
        });

        // Load from database
        loadEmployeesFromDB();
    }

    private void loadEmployeesFromDB() {
        List<Employee> dbEmployees = EmployeeDAO.fetchEmployeesByCompany(selectedCompany);
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
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel nameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton editBtn = new JButton("Edit");
        JButton timeBtn = new JButton("Time");
        JButton removeBtn = new JButton("Remove");

        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        buttonPanel.add(timeBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);

        card.add(nameLabel, BorderLayout.WEST);
        card.add(buttonPanel, BorderLayout.EAST);

        // === Button Actions ===
        editBtn.addActionListener(e -> {
          EmployeeForm editForm = new EmployeeForm(HomePage.this, employee, getSelectedCompany());

            editForm.setVisible(true);
        });

        timeBtn.addActionListener(e -> {
            String middleName = employee.getMiddleName() != null ? employee.getMiddleName() : "";
            String fullName = employee.getFirstName() + " " + middleName + " " + employee.getLastName();
            String idNumber = String.valueOf(employee.getId());
            PayrollPage payrollPage = new PayrollPage(fullName.trim(), idNumber);
            payrollPage.setVisible(true);
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

        return card;
    }

    private void filterEmployeeList(String searchText) {
        employeeListPanel.removeAll();
        for (Employee employee : employees) {
            String fullName = (employee.getFirstName() + " " + employee.getLastName()).toLowerCase();
            if (fullName.contains(searchText)) {
                employeeListPanel.add(createEmployeeCard(employee));
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

        SwingUtilities.invokeLater(() -> {
            new HomePage("IHI").setVisible(true);
        });
    }
}
