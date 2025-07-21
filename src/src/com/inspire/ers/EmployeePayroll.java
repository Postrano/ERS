package src.com.inspire.ers;

public class EmployeePayroll {
    private int employeeId;
    private String employeeName;
    private int lateDeduction;
    private String remarks;
    private boolean halfDay;
    private int overtimeMinutes;

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public int getLateDeduction() { return lateDeduction; }
    public void setLateDeduction(int lateDeduction) { this.lateDeduction = lateDeduction; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isHalfDay() { return halfDay; }
    public void setHalfDay(boolean halfDay) { this.halfDay = halfDay; }

    public boolean getHalfDay() {
    return halfDay;
}

    public int getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(int overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }
    
    
}
