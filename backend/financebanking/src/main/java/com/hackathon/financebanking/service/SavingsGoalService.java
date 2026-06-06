package com.hackathon.financebanking.service;

import com.hackathon.financebanking.model.SavingsGoal;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.SavingsGoalRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;

    public List<SavingsGoal> getGoalsByUser(User user) {
        return savingsGoalRepository.findByUser(user);
    }

    @Transactional
    public SavingsGoal createGoal(User user, SavingsGoal goal) {
        goal.setUser(user);
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        return savingsGoalRepository.save(goal);
    }

    @Transactional
    public SavingsGoal depositToGoal(User user, Long goalId, BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Savings goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to allocate funds to this goal");
        }

        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance to complete transfer");
        }

        // Deduct from wallet, credit to goal vault
        user.setBalance(user.getBalance().subtract(amount));
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        userRepository.save(user);
        return savingsGoalRepository.save(goal);
    }
}
