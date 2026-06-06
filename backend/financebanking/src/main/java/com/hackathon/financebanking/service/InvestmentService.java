package com.hackathon.financebanking.service;

import com.hackathon.financebanking.model.InvestmentPortfolio;
import com.hackathon.financebanking.model.User;
import com.hackathon.financebanking.repository.InvestmentRepository;
import com.hackathon.financebanking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    // In-memory registry for mock stock prices
    private static final Map<String, BigDecimal> STOCK_MARKET = new ConcurrentHashMap<>();

    static {
        STOCK_MARKET.put("AAPL", new BigDecimal("175.50"));
        STOCK_MARKET.put("MSFT", new BigDecimal("415.20"));
        STOCK_MARKET.put("GOOG", new BigDecimal("152.80"));
        STOCK_MARKET.put("TSLA", new BigDecimal("179.40"));
        STOCK_MARKET.put("AMZN", new BigDecimal("185.10"));
    }

    public Map<String, BigDecimal> getMarketPrices() {
        // Fluctuate prices slightly on load to make the dashboard look active
        Random rand = new Random();
        for (String ticker : STOCK_MARKET.keySet()) {
            BigDecimal currentPrice = STOCK_MARKET.get(ticker);
            // Fluctuate between -1.5% and +1.5%
            double changePercent = -1.5 + (3.0 * rand.nextDouble());
            BigDecimal changeMultiplier = BigDecimal.valueOf(1 + (changePercent / 100));
            BigDecimal newPrice = currentPrice.multiply(changeMultiplier).setScale(2, RoundingMode.HALF_UP);
            STOCK_MARKET.put(ticker, newPrice);
        }
        return STOCK_MARKET;
    }

    public List<InvestmentPortfolio> getPortfolio(User user) {
        return investmentRepository.findByUser(user);
    }

    @Transactional
    public InvestmentPortfolio buyStock(User user, String ticker, Double shares) {
        final String upperTicker = ticker.toUpperCase();
        Map<String, BigDecimal> market = getMarketPrices();
        if (!market.containsKey(upperTicker)) {
            throw new IllegalArgumentException("Ticker not found on mock exchange");
        }

        BigDecimal price = market.get(upperTicker);
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(shares)).setScale(2, RoundingMode.HALF_UP);

        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Total cost: $" + totalCost + ", available balance: $" + user.getBalance());
        }

        // Deduct from balance
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        Optional<InvestmentPortfolio> existingOpt = investmentRepository.findByUserAndTicker(user, upperTicker);
        InvestmentPortfolio portfolio;

        if (existingOpt.isPresent()) {
            portfolio = existingOpt.get();
            double oldShares = portfolio.getSharesOwned();
            double newShares = oldShares + shares;

            // Recalculate average buy price
            BigDecimal oldCost = portfolio.getAverageBuyPrice().multiply(BigDecimal.valueOf(oldShares));
            BigDecimal newCost = oldCost.add(totalCost);
            BigDecimal avgPrice = newCost.divide(BigDecimal.valueOf(newShares), 2, RoundingMode.HALF_UP);

            portfolio.setSharesOwned(newShares);
            portfolio.setAverageBuyPrice(avgPrice);
        } else {
            portfolio = InvestmentPortfolio.builder()
                    .user(user)
                    .ticker(upperTicker)
                    .sharesOwned(shares)
                    .averageBuyPrice(price)
                    .build();
        }

        return investmentRepository.save(portfolio);
    }

    @Transactional
    public void sellStock(User user, String ticker, Double shares) {
        final String upperTicker = ticker.toUpperCase();
        InvestmentPortfolio portfolio = investmentRepository.findByUserAndTicker(user, upperTicker)
                .orElseThrow(() -> new IllegalArgumentException("No shares owned for ticker: " + upperTicker));

        if (portfolio.getSharesOwned() < shares) {
            throw new IllegalArgumentException("Insufficient shares owned to sell. Owned: " + portfolio.getSharesOwned() + ", requested: " + shares);
        }

        Map<String, BigDecimal> market = getMarketPrices();
        BigDecimal price = market.get(upperTicker);
        BigDecimal proceeds = price.multiply(BigDecimal.valueOf(shares)).setScale(2, RoundingMode.HALF_UP);

        // Add proceeds to user balance
        user.setBalance(user.getBalance().add(proceeds));
        userRepository.save(user);

        double newShares = portfolio.getSharesOwned() - shares;
        if (newShares <= 0.0001) {
            investmentRepository.delete(portfolio);
        } else {
            portfolio.setSharesOwned(newShares);
            investmentRepository.save(portfolio);
        }
    }

    public BigDecimal getPortfolioValue(User user) {
        List<InvestmentPortfolio> holdings = investmentRepository.findByUser(user);
        Map<String, BigDecimal> prices = STOCK_MARKET; // Use baseline prices (or fluctuate)
        BigDecimal totalValue = BigDecimal.ZERO;

        for (InvestmentPortfolio holding : holdings) {
            BigDecimal currentPrice = prices.getOrDefault(holding.getTicker().toUpperCase(), holding.getAverageBuyPrice());
            BigDecimal assetValue = currentPrice.multiply(BigDecimal.valueOf(holding.getSharesOwned()));
            totalValue = totalValue.add(assetValue);
        }

        return totalValue.setScale(2, RoundingMode.HALF_UP);
    }
}
