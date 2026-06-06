package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.model.Transaction;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.UserRepository;
import com.hackathon.financebanking.service.FraudDetectionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(fraudDetectionService.getTransactionsByUser(user));
    }

    @PostMapping("/execute")
    public ResponseEntity<Transaction> executeTransaction(Principal principal, @RequestBody Transaction transaction) {
        User user = getUserFromPrincipal(principal);
        Transaction result = fraudDetectionService.executeTransaction(user, transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveTransaction(
            Principal principal,
            @PathVariable Long id,
            @RequestBody ResolutionRequest request) {
        User user = getUserFromPrincipal(principal);
        try {
            Transaction resolved = fraudDetectionService.resolveTransaction(user, id, request.getDecision());
            return ResponseEntity.ok(resolved);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Data
    public static class ResolutionRequest {
        private String decision; // APPROVE or BLOCK
    }
}
