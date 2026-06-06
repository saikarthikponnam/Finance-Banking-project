package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.model.Expense;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.ExpenseRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @GetMapping
    public ResponseEntity<List<Insight>> getInsights(Principal principal) {
        User user = getUserFromPrincipal(principal);
        List<Insight> insights = new ArrayList<>();

        // 1. Credit score insights
        if (user.getCreditScore() >= 720) {
            insights.add(new Insight(
                    "SUCCESS",
                    "Prime Rate Pre-Approval",
                    "Your excellent credit score of " + user.getCreditScore() + " qualifies you for our best prime interest rates (4.5% APR) on personal loans."
            ));
        } else if (user.getCreditScore() < 600) {
            insights.add(new Insight(
                    "DANGER",
                    "Credit Score Improvement Needed",
                    "Your credit score is " + user.getCreditScore() + " (subprime). Rebuilding your credit requires timely bill payments and reducing outstanding debts."
            ));
        } else {
            insights.add(new Insight(
                    "WARNING",
                    "Credit Score Warning",
                    "Your credit score is " + user.getCreditScore() + ". Making on-time payments can push you above 700 to unlock lower loan interest rates."
            ));
        }

        // 2. Expense and budget insights
        List<Expense> expenses = expenseRepository.findByUserOrderByDateDesc(user);
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (Expense e : expenses) {
            totalExpenses = totalExpenses.add(e.getAmount());
        }

        if (user.getMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal expenseRatio = totalExpenses.multiply(BigDecimal.valueOf(100))
                    .divide(user.getMonthlyIncome(), 2, RoundingMode.HALF_UP);

            if (expenseRatio.compareTo(BigDecimal.valueOf(60)) > 0) {
                insights.add(new Insight(
                        "DANGER",
                        "High Spending Alert",
                        "You have spent $" + totalExpenses + " this month, which represents " + expenseRatio + "% of your monthly income. Consider reducing dining and shopping categories."
                ));
            } else if (expenseRatio.compareTo(BigDecimal.valueOf(30)) > 0) {
                insights.add(new Insight(
                        "WARNING",
                        "Moderate Budget Threshold",
                        "Your spending represents " + expenseRatio + "% of your income. You are within safe bounds, but monitor discretionary purchases."
                ));
            } else {
                insights.add(new Insight(
                        "SUCCESS",
                        "Excellent Budget Control",
                        "Outstanding budgeting! Your monthly expenses represent only " + expenseRatio + "% of your income. You have plenty of room for savings."
                ));
            }
        }

        // 3. Balance allocation advice
        if (user.getBalance().compareTo(new BigDecimal("5000")) > 0) {
            insights.add(new Insight(
                    "INFO",
                    "Wealth Allocation Advice",
                    "You have a healthy account balance of $" + user.getBalance() + ". Consider setting up a new Savings Goal or investing in our Stock Simulator to compound your assets."
            ));
        } else if (user.getBalance().compareTo(new BigDecimal("1000")) < 0) {
            insights.add(new Insight(
                    "WARNING",
                    "Low Account Balance",
                    "Your checking account balance is low ($" + user.getBalance() + "). Avoid making high-value purchases to protect against overdrafts."
            ));
        }

        // 4. Tech tip
        insights.add(new Insight(
                "INFO",
                "Market Diversification",
                "Market update: Blue-chip stocks like MSFT and AAPL are showing strong volume today. Diversify your checking balance under the Investment Simulator."
        ));

        return ResponseEntity.ok(insights);
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Data
    @AllArgsConstructor
    public static class Insight {
        private String type; // SUCCESS, WARNING, DANGER, INFO
        private String title;
        private String message;
    }
}
