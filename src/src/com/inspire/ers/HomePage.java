package com.inspire.ers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import com.inspire.ers.EmployeeDAO;

public class HomePage extends JFrame {
    private ArrayList<Employee> employees = new ArrayList<>();
    private JLabel employeeCountLabel;
    private JPanel employeeListPanel;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private String selectedCompany;

    public HomePage(String company) {
        this.selectedCompany = company;
        setTitle("Home Page - " + company);
        

        setTitle("INSPIRE EMPLOYEE RECORDS SYSTEM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);

        // Set window icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/inspirelogo2.jpg"));
        setIconImage(icon.getImage());

        // Background image
        ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/images/deepocean1.jpg"));

        mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        topPanel.add(searchField, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);

        JButton executiveBtn = new JButton("EXECUTIVE");
        JButton addEmployeeBtn = new JButton("ADD EMPLOYEE");

        employeeCountLabel = new JLabel("#Employee: 0");
        employeeCountLabel.setForeground(Color.WHITE);

        buttonPanel.add(executiveBtn);
        buttonPanel.add(addEmployeeBtn);
        buttonPanel.add(Box.createHorizontalStrut(50));
        buttonPanel.add(employeeCountLabel);

        topPanel.add(buttonPanel, BorderLayout.WEST);

        // Employee list panel
        employeeListPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                int width = getWidth();
                int height = getHeight();

                float[] fractions = {0.0f, 0.5f, 1.0f};
                Color[] colors = {
                    new Color(255, 255, 255, (int)(0.41 * 255)),
                    new Color(226, 174, 245, (int)(0.41 * 255)),
                    new Color(240, 230, 144, (int)(0.41 * 255)),
                };

                LinearGradientPaint gradient = new LinearGradientPaint(
                        0, 0, width, 0, fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };
        employeeListPanel.setLayout(new BoxLayout(employeeListPanel, BoxLayout.Y_AXIS));
        employeeListPanel.setOpaque(false);

        scrollPane = new JScrollPane(employeeListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

        // Button Listeners
        executiveBtn.addActionListener(e -> {
            ExecutivePage executivePage = new ExecutivePage(selectedCompany);
            executivePage.setVisible(true);
        });


        addEmployeeBtn.addActionListener(e -> {
          EmployeeForm employeeForm = EmployeeForm.createForNewEmployee(this, selectedCompany);
          employeeForm.setVisible(true);
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText().toLowerCase();
                filterEmployeeList(searchText);
            }
        });

        // Load data
        loadEmployeesFromDB();
    }

   public String getSelectedCompany() {
        return selectedCompany;
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
            employeeListPanel.add(createEmployeePanel(employee));
            employeeListPanel.add(Box.createVerticalStrut(5));
        }

        employeeListPanel.revalidate();
        employeeListPanel.repaint();
    }

    private JPanel createEmployeePanel(Employee employee) {
        JPanel employeePanel = new JPanel(new BorderLayout());
        employeePanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel nameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        employeePanel.add(nameLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("Edit");
        JButton payrollBtn = new JButton("Payroll");
        JButton removeBtn = new JButton("Remove");

        buttonPanel.add(payrollBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.add(buttonPanel);
        employeePanel.add(rightWrapper, BorderLayout.EAST);

        editBtn.addActionListener(e -> {
            EmployeeForm editForm = new EmployeeForm(HomePage.this, employee);
            editForm.setVisible(true);
        });

        payrollBtn.addActionListener(e -> {
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

        return employeePanel;
    }
    
  

    private void filterEmployeeList(String searchText) {
        employeeListPanel.removeAll();

        for (Employee employee : employees) {
            String fullName = (employee.getFirstName() + " " + employee.getLastName()).toLowerCase();
            if (fullName.contains(searchText)) {
                employeeListPanel.add(createEmployeePanel(employee));
                employeeListPanel.add(Box.createVerticalStrut(5));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomePage("IHI").setVisible(true); // ✅ Provide default company for test
        });
    }

    private void initComponents() {
        // If you don’t have anything inside this, you can remove the method or leave it empty
    }
    
     
}
