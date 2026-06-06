package com.hackathon.financebanking.service;

import com.hackathon.financebanking.model.Expense;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.ExpenseRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByUserOrderByDateDesc(user);
    }

    @Transactional
    public Expense addExpense(User user, Expense expense) {
        expense.setUser(user);
        
        // Deduct expense from user balance to make the app interactive
        BigDecimal newBalance = user.getBalance().subtract(expense.getAmount());
        user.setBalance(newBalance);
        userRepository.save(user);

        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(User user, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to delete this expense");
        }

        // Refund expense amount back to user balance
        BigDecimal newBalance = user.getBalance().add(expense.getAmount());
        user.setBalance(newBalance);
        userRepository.save(user);

        expenseRepository.delete(expense);
    }
}
