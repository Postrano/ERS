package com.inspire.ers;

import java.util.Date;

public class Employee {
    private int id;
    private String firstName, lastName, middleName, idNumber;
    private Date dateHired;
    private String emailAddress, currentAddress, cellphoneNo;
    private String position;
    private double basicPay, executiveAllowance, marketingTranspoAllowance, monthlySalary;
    private String sssNumber, philHealthNumber, pagIbigNumber, tinNumber, bankAccount;
    private byte[] photo;
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public Date getDateHired() { return dateHired; }
    public void setDateHired(Date dateHired) { this.dateHired = dateHired; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    public String getCellphoneNo() { return cellphoneNo; }
    public void setCellphoneNo(String cellphoneNo) { this.cellphoneNo = cellphoneNo; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public double getBasicPay() { return basicPay; }
    public void setBasicPay(double basicPay) { this.basicPay = basicPay; }
    public double getExecutiveAllowance() { return executiveAllowance; }
    public void setExecutiveAllowance(double executiveAllowance) { this.executiveAllowance = executiveAllowance; }
    public double getMarketingTranspoAllowance() { return marketingTranspoAllowance; }
    public void setMarketingTranspoAllowance(double marketingTranspoAllowance) { this.marketingTranspoAllowance = marketingTranspoAllowance; }
    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double monthlySalary) { this.monthlySalary = monthlySalary; }
    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    public String getPhilHealthNumber() { return philHealthNumber; }
    public void setPhilHealthNumber(String philHealthNumber) { this.philHealthNumber = philHealthNumber; }
    public String getPagIbigNumber() { return pagIbigNumber; }
    public void setPagIbigNumber(String pagIbigNumber) { this.pagIbigNumber = pagIbigNumber; }
    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    
public byte[] getPhoto() {
    return photo;
}

public void setPhoto(byte[] photo) {
    this.photo = photo;
}
}
