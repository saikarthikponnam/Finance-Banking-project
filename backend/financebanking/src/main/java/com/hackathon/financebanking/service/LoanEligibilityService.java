package com.hackathon.financebanking.service;

import com.hackathon.financebanking.model.LoanApplication;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.LoanApplicationRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanEligibilityService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;

    public List<LoanApplication> getLoansByUser(User user) {
        return loanApplicationRepository.findByUserOrderByApplicationDateDesc(user);
    }

    @Transactional
    public LoanApplication applyForLoan(User user, LoanApplication loanApp, BigDecimal monthlyDebt) {
        loanApp.setUser(user);
        loanApp.setApplicationDate(LocalDateTime.now());

        BigDecimal income = user.getMonthlyIncome();
        Integer creditScore = user.getCreditScore();

        // 1. Income requirement check
        if (income.compareTo(new BigDecimal("1500")) < 0) {
            loanApp.setStatus("REJECTED");
            loanApp.setDtiRatio(BigDecimal.ZERO);
            loanApp.setAnalysisReport("REJECTED: Minimum monthly income requirement of $1,500 is not met. Your current monthly income is $" + income + ".");
            return loanApplicationRepository.save(loanApp);
        }

        // 2. DTI Calculation
        BigDecimal term = BigDecimal.valueOf(loanApp.getTermMonths());
        BigDecimal monthlyInstallment = loanApp.getLoanAmount().divide(term, 2, RoundingMode.HALF_UP);
        BigDecimal totalMonthlyOutgo = monthlyInstallment.add(monthlyDebt);
        BigDecimal dti = totalMonthlyOutgo.multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP);

        loanApp.setDtiRatio(dti);

        // Check if DTI exceeds 40%
        if (dti.compareTo(BigDecimal.valueOf(40)) > 0) {
            loanApp.setStatus("REJECTED");
            loanApp.setAnalysisReport("REJECTED: Debt-to-Income (DTI) ratio is too high (" + dti + "%). Total monthly obligations (including new loan installment of $" + monthlyInstallment + ") exceed the safe limit of 40% of your income.");
            return loanApplicationRepository.save(loanApp);
        }

        // 3. Credit Score Check
        if (creditScore < 600) {
            loanApp.setStatus("REJECTED");
            loanApp.setAnalysisReport("REJECTED: Credit score (" + creditScore + ") is below the minimum threshold of 600 required for loan underwriting.");
            return loanApplicationRepository.save(loanApp);
        }

        // 4. Max Loan Limit Check
        BigDecimal eligibleMultiple;
        double interestRate;
        if (creditScore >= 700) {
            eligibleMultiple = BigDecimal.valueOf(8); // Qualifying up to 8x monthly income
            if (creditScore >= 750) {
                interestRate = 4.5;
            } else {
                interestRate = 6.5;
            }
        } else {
            eligibleMultiple = BigDecimal.valueOf(3); // Qualifying up to 3x monthly income
            if (creditScore >= 650) {
                interestRate = 9.0;
            } else {
                interestRate = 12.0;
            }
        }

        BigDecimal maxLoanAmount = income.multiply(eligibleMultiple);
        if (loanApp.getLoanAmount().compareTo(maxLoanAmount) > 0) {
            loanApp.setStatus("REJECTED");
            loanApp.setAnalysisReport("REJECTED: Requested loan amount ($" + loanApp.getLoanAmount() + ") exceeds your credit-limit cap of $" + maxLoanAmount + ". With your credit score of " + creditScore + ", you qualify for a maximum loan multiple of " + eligibleMultiple + "x your monthly income.");
            return loanApplicationRepository.save(loanApp);
        }

        // Approval!
        loanApp.setStatus("APPROVED");
        loanApp.setAnalysisReport("APPROVED: Loan request approved successfully! Interest Rate: " + interestRate + "%. Your DTI ratio is healthy at " + dti + "%. The approved capital of $" + loanApp.getLoanAmount() + " has been disbursed directly into your checking account.");

        // Disburse loan to user balance
        user.setBalance(user.getBalance().add(loanApp.getLoanAmount()));
        userRepository.save(user);

        return loanApplicationRepository.save(loanApp);
    }
}
