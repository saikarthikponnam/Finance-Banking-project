package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.model.SavingsGoal;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.UserRepository;
import com.hackathon.financebanking.service.SavingsGoalService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getGoals(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(savingsGoalService.getGoalsByUser(user));
    }

    @PostMapping
    public ResponseEntity<SavingsGoal> createGoal(Principal principal, @RequestBody SavingsGoal goal) {
        User user = getUserFromPrincipal(principal);
        SavingsGoal saved = savingsGoalService.createGoal(user, goal);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<?> depositToGoal(
            Principal principal,
            @PathVariable Long id,
            @RequestBody DepositRequest request) {
        User user = getUserFromPrincipal(principal);
        try {
            SavingsGoal updated = savingsGoalService.depositToGoal(user, id, request.getAmount());
            return ResponseEntity.ok(updated);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Data
    public static class DepositRequest {
        private BigDecimal amount;
    }
}
