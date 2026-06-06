import { useState, useEffect } from "react";
import Sidebar from "../components/sidebar";
import api from "../services/api";

function StockSimulator() {
  const [market, setMarket] = useState({});
  const [portfolio, setPortfolio] = useState([]);
  const [sharesInput, setSharesInput] = useState({}); // Ticker -> quantity input
  const [loading, setLoading] = useState(false);

  const fetchMarketAndPortfolio = async () => {
    try {
      const marketRes = await api.get("/api/investments/market");
      setMarket(marketRes.data);

      const portfolioRes = await api.get("/api/investments/portfolio");
      setPortfolio(portfolioRes.data);
    } catch (err) {
      console.error("Failed to load stock data:", err);
    }
  };

  useEffect(() => {
    fetchMarketAndPortfolio();
    
    // Auto-update prices every 15 seconds to simulate market ticking
    const interval = setInterval(fetchMarketAndPortfolio, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleBuy = async (ticker) => {
    const qty = sharesInput[ticker];
    if (!qty || parseFloat(qty) <= 0) {
      alert("Please enter a valid number of shares.");
      return;
    }

    setLoading(true);
    try {
      await api.post("/api/investments/buy", {
        ticker,
        shares: parseFloat(qty)
      });
      
      // Clear input
      setSharesInput(prev => ({ ...prev, [ticker]: "" }));
      
      await fetchMarketAndPortfolio();
      alert(`Bought ${qty} shares of ${ticker} successfully!`);
    } catch (err) {
      console.error(err);
      alert(err.response?.data || "Purchase failed. Check balance.");
    } finally {
      setLoading(false);
    }
  };

  const handleSell = async (ticker) => {
    const qty = sharesInput[ticker];
    if (!qty || parseFloat(qty) <= 0) {
      alert("Please enter a valid number of shares.");
      return;
    }

    setLoading(true);
    try {
      await api.post("/api/investments/sell", {
        ticker,
        shares: parseFloat(qty)
      });
      
      // Clear input
      setSharesInput(prev => ({ ...prev, [ticker]: "" }));
      
      await fetchMarketAndPortfolio();
      alert(`Sold ${qty} shares of ${ticker} successfully!`);
    } catch (err) {
      console.error(err);
      alert(err.response?.data || "Sell failed. Check holdings.");
    } finally {
      setLoading(false);
    }
  };

  const handleQtyChange = (ticker, value) => {
    setSharesInput(prev => ({ ...prev, [ticker]: value }));
  };

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Stock Market Simulator</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
          Simulated Stock Exchange. Prices fluctuate slightly in real-time. Buy/sell using your bank balance.
        </p>

        <div className="card">
          <h3>Available Stocks</h3>
          
          <table className="expense-table">
            <thead>
              <tr>
                <th>Ticker</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {Object.keys(market).map((ticker) => (
                <tr key={ticker}>
                  <td style={{ fontWeight: "bold" }}>{ticker}</td>
                  <td style={{ color: "#10b981", fontWeight: "bold" }}>
                    ${market[ticker].toFixed(2)}
                  </td>
                  <td>
                    <input
                      type="number"
                      placeholder="Shares"
                      className="expense-input"
                      style={{ margin: 0, width: "100px", padding: "5px" }}
                      value={sharesInput[ticker] || ""}
                      onChange={(e) => handleQtyChange(ticker, e.target.value)}
                    />
                  </td>
                  <td>
                    <div style={{ display: "flex", gap: "5px" }}>
                      <button 
                        onClick={() => handleBuy(ticker)}
                        className="expense-btn"
                        style={{ margin: 0, width: "auto", padding: "5px 12px", background: "#10b981" }}
                        disabled={loading}
                      >
                        Buy
                      </button>
                      <button 
                        onClick={() => handleSell(ticker)}
                        className="expense-btn"
                        style={{ margin: 0, width: "auto", padding: "5px 12px", background: "#ef4444" }}
                        disabled={loading}
                      >
                        Sell
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h3>My Portfolio Holdings</h3>
          {portfolio.length === 0 ? (
            <p style={{ marginTop: "15px", color: "#94a3b8" }}>No active investments owned.</p>
          ) : (
            <table className="expense-table">
              <thead>
                <tr>
                  <th>Ticker</th>
                  <th>Shares Owned</th>
                  <th>Avg Buy Price</th>
                  <th>Current Market Value</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.map((holding) => {
                  const currentPrice = market[holding.ticker] || holding.averageBuyPrice;
                  const totalValue = currentPrice * holding.sharesOwned;
                  return (
                    <tr key={holding.id}>
                      <td style={{ fontWeight: "bold" }}>{holding.ticker}</td>
                      <td>{holding.sharesOwned.toFixed(2)}</td>
                      <td>${holding.averageBuyPrice.toFixed(2)}</td>
                      <td style={{ color: "#10b981", fontWeight: "bold" }}>
                        ${totalValue.toFixed(2)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default StockSimulator;