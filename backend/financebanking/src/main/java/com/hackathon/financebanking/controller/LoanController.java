package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.model.LoanApplication;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.UserRepository;
import com.hackathon.financebanking.service.LoanEligibilityService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanEligibilityService loanEligibilityService;
    private final UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<List<LoanApplication>> getLoanHistory(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(loanEligibilityService.getLoansByUser(user));
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanApplication> applyForLoan(Principal principal, @RequestBody LoanRequest request) {
        User user = getUserFromPrincipal(principal);

        LoanApplication loanApp = LoanApplication.builder()
                .loanAmount(request.getLoanAmount())
                .termMonths(request.getTermMonths())
                .purpose(request.getPurpose())
                .build();

        BigDecimal monthlyDebt = request.getMonthlyDebt() != null ? request.getMonthlyDebt() : BigDecimal.ZERO;

        LoanApplication processed = loanEligibilityService.applyForLoan(user, loanApp, monthlyDebt);
        return ResponseEntity.ok(processed);
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Data
    public static class LoanRequest {
        private BigDecimal loanAmount;
        private Integer termMonths;
        private String purpose;
        private BigDecimal monthlyDebt; // Current monthly debt payments of user
    }
}
