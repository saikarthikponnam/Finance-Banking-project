package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.model.InvestmentPortfolio;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.UserRepository;
import com.hackathon.financebanking.service.InvestmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;
    private final UserRepository userRepository;

    @GetMapping("/market")
    public ResponseEntity<Map<String, BigDecimal>> getMarketPrices() {
        return ResponseEntity.ok(investmentService.getMarketPrices());
    }

    @GetMapping("/portfolio")
    public ResponseEntity<List<InvestmentPortfolio>> getPortfolio(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(investmentService.getPortfolio(user));
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(Principal principal, @RequestBody TradeRequest request) {
        User user = getUserFromPrincipal(principal);
        try {
            InvestmentPortfolio portfolio = investmentService.buyStock(user, request.getTicker(), request.getShares());
            return ResponseEntity.ok(portfolio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(Principal principal, @RequestBody TradeRequest request) {
        User user = getUserFromPrincipal(principal);
        try {
            investmentService.sellStock(user, request.getTicker(), request.getShares());
            return ResponseEntity.ok("Stock sold successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Data
    public static class TradeRequest {
        private String ticker;
        private Double shares;
    }
}
