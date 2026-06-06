package com.hackathon.financebanking.controller;

import com.hackathon.financebanking.config.JwtTokenProvider;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.UserRepository;
import com.hackathon.financebanking.service.InvestmentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final InvestmentService investmentService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // Create new user profile
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .monthlyIncome(registerRequest.getMonthlyIncome() != null ? registerRequest.getMonthlyIncome() : new BigDecimal("2000"))
                .creditScore(registerRequest.getCreditScore() != null ? registerRequest.getCreditScore() : 650)
                .balance(new BigDecimal("10000.00")) // Starts with seed bank balance for demonstration
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully! Please log in.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found after authorization"));

        BigDecimal portfolioVal = investmentService.getPortfolioValue(user);

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getMonthlyIncome(),
                user.getCreditScore(),
                user.getBalance(),
                portfolioVal
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        BigDecimal portfolioVal = investmentService.getPortfolioValue(user);

        return ResponseEntity.ok(new UserProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getMonthlyIncome(),
                user.getCreditScore(),
                user.getBalance(),
                portfolioVal
        ));
    }

    // --- Inner Request/Response Classes (DTOs) ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private BigDecimal monthlyIncome;
        private Integer creditScore;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String email;
        private BigDecimal monthlyIncome;
        private Integer creditScore;
        private BigDecimal balance;
        private BigDecimal portfolioValue;
    }

    @Data
    @AllArgsConstructor
    public static class UserProfileResponse {
        private String username;
        private String email;
        private BigDecimal monthlyIncome;
        private Integer creditScore;
        private BigDecimal balance;
        private BigDecimal portfolioValue;
    }
}
