package com.hackathon.financebanking.repository;

import com.hackathon.financebanking.model.InvestmentPortfolio;
import com.hackathon.financebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<InvestmentPortfolio, Long> {
    List<InvestmentPortfolio> findByUser(User user);
    Optional<InvestmentPortfolio> findByUserAndTicker(User user, String ticker);
}
