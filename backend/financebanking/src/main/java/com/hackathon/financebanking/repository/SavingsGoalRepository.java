package com.hackathon.financebanking.repository;

import com.hackathon.financebanking.model.SavingsGoal;
import com.hackathon.financebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(User user);
}
