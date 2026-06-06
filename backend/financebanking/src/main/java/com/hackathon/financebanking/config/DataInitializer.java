package com.hackathon.financebanking.config;

import com.hackathon.financebanking.model.Expense;
import com.hackathon.financebanking.model.SavingsGoal;
import com.hackathon.financebanking.model.Transaction;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.ExpenseRepository;
import com.hackathon.financebanking.repository.SavingsGoalRepository;
import com.hackathon.financebanking.repository.TransactionRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed data if database is empty
        if (userRepository.existsByUsername("demo")) {
            return;
        }

        // 1. Create Default Mock User
        User user = User.builder()
                .username("demo")
                .email("demo@bank.com")
                .passwordHash(passwordEncoder.encode("password"))
                .monthlyIncome(new BigDecimal("5000.00"))
                .creditScore(740)
                .balance(new BigDecimal("12500.00"))
                .build();

        userRepository.save(user);

        // 2. Seed Expenses for spending breakdown charts
        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(new BigDecimal("1200.00"))
                .category("Rent")
                .description("Monthly Apartment Lease")
                .date(LocalDate.now().minusDays(5))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(new BigDecimal("150.00"))
                .category("Food")
                .description("Groceries & Dinners")
                .date(LocalDate.now().minusDays(3))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(new BigDecimal("85.50"))
                .category("Utilities")
                .description("Electric & Wifi bill")
                .date(LocalDate.now().minusDays(2))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(new BigDecimal("210.00"))
                .category("Travel")
                .description("Weekly fuel & tolls")
                .date(LocalDate.now().minusDays(1))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(new BigDecimal("60.00"))
                .category("Entertainment")
                .description("Movie tickets")
                .date(LocalDate.now())
                .build());

        // 3. Seed Transactions for Fraud detection audits
        transactionRepository.save(Transaction.builder()
                .user(user)
                .amount(new BigDecimal("50.00"))
                .category("Food")
                .merchant("Starbucks Cafe")
                .timestamp(LocalDateTime.now().minusDays(4))
                .location("New York")
                .status("APPROVED")
                .riskScore(5)
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .amount(new BigDecimal("1200.00"))
                .category("Rent")
                .merchant("Metropolitan Living")
                .timestamp(LocalDateTime.now().minusDays(3))
                .location("New York")
                .status("APPROVED")
                .riskScore(8)
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .amount(new BigDecimal("45.00"))
                .category("Travel")
                .merchant("Uber Ride")
                .timestamp(LocalDateTime.now().minusDays(2))
                .location("New York")
                .status("APPROVED")
                .riskScore(10)
                .build());

        // Suspicious transaction triggered in Dubai, NY cardholder location mismatch (FLAGGED)
        transactionRepository.save(Transaction.builder()
                .user(user)
                .amount(new BigDecimal("6200.00"))
                .category("Electronics")
                .merchant("Dubai Gold Palace")
                .timestamp(LocalDateTime.now().minusHours(4))
                .location("Dubai")
                .status("FLAGGED")
                .riskScore(85)
                .failureReason("Location anomaly: Sudden change in transaction location from New York to Dubai. Amount exceeds normal spending threshold.")
                .build());

        // 4. Seed Savings Goals
        savingsGoalRepository.save(SavingsGoal.builder()
                .user(user)
                .goalName("Tesla Model 3")
                .targetAmount(new BigDecimal("45000.00"))
                .currentAmount(new BigDecimal("8500.00"))
                .targetDate(LocalDate.now().plusYears(2))
                .build());

        savingsGoalRepository.save(SavingsGoal.builder()
                .user(user)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .currentAmount(new BigDecimal("4000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .build());
    }
}
