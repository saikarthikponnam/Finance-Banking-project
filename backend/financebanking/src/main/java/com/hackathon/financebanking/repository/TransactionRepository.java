package com.hackathon.financebanking.repository;

import com.hackathon.financebanking.model.Transaction;
import com.hackathon.financebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTimestampDesc(User user);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.timestamp >= :timeThreshold")
    long countRecentTransactions(@Param("user") User user, @Param("timeThreshold") LocalDateTime timeThreshold);

    Optional<Transaction> findFirstByUserOrderByTimestampDesc(User user);
}
