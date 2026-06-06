package com.hackathon.financebanking.repository;

import com.hackathon.financebanking.model.LoanApplication;
import com.hackathon.financebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByUserOrderByApplicationDateDesc(User user);
}
