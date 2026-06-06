package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.model.Expense;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.UserRepository;
import com.hackathon.financebanking.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(expenseService.getExpensesByUser(user));
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(Principal principal, @RequestBody Expense expense) {
        User user = getUserFromPrincipal(principal);
        Expense savedExpense = expenseService.addExpense(user, expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(Principal principal, @PathVariable Long id) {
        User user = getUserFromPrincipal(principal);
        try {
            expenseService.deleteExpense(user, id);
            return ResponseEntity.ok("Expense deleted successfully.");
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
}
