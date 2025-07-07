/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src.com.inspire.ers;

/**
 *
 * @author Romel Postrano
 */
public record Executive(
    String execId,
    String name,
    String dept,
    String bank,
    double basicPay,
    double allowance
//    double execAllowance,
//    double marketingAllowance
) {}
