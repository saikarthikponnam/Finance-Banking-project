package com.hackathon.financebanking.service;

import com.hackathon.financebanking.model.Transaction;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.TransactionRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUserOrderByTimestampDesc(user);
    }

    @Transactional
    public Transaction executeTransaction(User user, Transaction transaction) {
        transaction.setUser(user);
        transaction.setTimestamp(LocalDateTime.now());

        // Default evaluations
        int riskScore = 5;
        String status = "APPROVED";
        String reason = "";

        // 1. Velocity Rule: Check count of transactions in the last 60 seconds
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long recentCount = transactionRepository.countRecentTransactions(user, oneMinuteAgo);
        if (recentCount >= 3) {
            status = "BLOCKED";
            riskScore = 95;
            reason = "Velocity limit exceeded: More than 3 transactions in 1 minute.";
        }

        // 2. Geolocation Anomaly Rule: Check distance/location deviation relative to time
        if (!"BLOCKED".equals(status)) {
            Optional<Transaction> lastTxOpt = transactionRepository.findFirstByUserOrderByTimestampDesc(user);
            if (lastTxOpt.isPresent()) {
                Transaction lastTx = lastTxOpt.get();
                if (!lastTx.getLocation().equalsIgnoreCase(transaction.getLocation())) {
                    // If locations are different and time difference is less than 30 minutes
                    if (lastTx.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(30))) {
                        status = "FLAGGED";
                        riskScore = 75;
                        reason = "Location anomaly: Sudden change in transaction location from " 
                                + lastTx.getLocation() + " to " + transaction.getLocation() + ".";
                    }
                }
            }
        }

        // 3. High Value Transaction Check: Any transaction > $5000 requires review
        if ("APPROVED".equals(status) && transaction.getAmount().compareTo(new BigDecimal("5000")) > 0) {
            status = "FLAGGED";
            riskScore = 65;
            reason = "High-value transaction: Amount exceeds $5,000 limit.";
        }

        // 4. Deviation from Average Transaction: If transaction is 3x larger than average
        if ("APPROVED".equals(status)) {
            List<Transaction> userTxs = transactionRepository.findByUserOrderByTimestampDesc(user);
            if (userTxs.size() >= 3) {
                BigDecimal sum = BigDecimal.ZERO;
                for (Transaction tx : userTxs) {
                    sum = sum.add(tx.getAmount());
                }
                BigDecimal avg = sum.divide(BigDecimal.valueOf(userTxs.size()), RoundingMode.HALF_UP);
                if (transaction.getAmount().compareTo(avg.multiply(BigDecimal.valueOf(3))) > 0) {
                    status = "FLAGGED";
                    riskScore = 60;
                    reason = "Unusual amount deviation: Transaction is 3x larger than your average.";
                }
            }
        }

        transaction.setStatus(status);
        transaction.setRiskScore(riskScore);
        transaction.setFailureReason(reason);

        // Deduct from balance if transaction is fully APPROVED
        if ("APPROVED".equals(status)) {
            if (user.getBalance().compareTo(transaction.getAmount()) < 0) {
                transaction.setStatus("BLOCKED");
                transaction.setRiskScore(90);
                transaction.setFailureReason("Insufficient balance.");
            } else {
                user.setBalance(user.getBalance().subtract(transaction.getAmount()));
                userRepository.save(user);
            }
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction resolveTransaction(User user, Long transactionId, String decision) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to resolve this transaction");
        }

        if (!"FLAGGED".equals(tx.getStatus())) {
            throw new IllegalStateException("Transaction is not flagged for review");
        }

        if ("APPROVE".equalsIgnoreCase(decision)) {
            if (user.getBalance().compareTo(tx.getAmount()) < 0) {
                tx.setStatus("BLOCKED");
                tx.setFailureReason("Insufficient balance at resolution time.");
            } else {
                tx.setStatus("APPROVED");
                user.setBalance(user.getBalance().subtract(tx.getAmount()));
                userRepository.save(user);
            }
        } else if ("BLOCK".equalsIgnoreCase(decision)) {
            tx.setStatus("BLOCKED");
            tx.setFailureReason("Blocked manually by cardholder.");
        } else {
            throw new IllegalArgumentException("Invalid resolution decision");
        }

        return transactionRepository.save(tx);
    }
}
