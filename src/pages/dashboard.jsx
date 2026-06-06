import { useState, useEffect } from "react";
import Sidebar from "../components/sidebar";
import api from "../services/api";

function Dashboard() {
  const [profile, setProfile] = useState(null);
  const [insights, setInsights] = useState([]);
  const [stats, setStats] = useState({
    activeGoals: 0,
    flaggedTxns: 0
  });

  const loadDashboardData = async () => {
    try {
      // 1. Load Profile
      const profileRes = await api.get("/api/auth/me");
      setProfile(profileRes.data);

      // 2. Load Smart Insights
      const insightsRes = await api.get("/api/insights");
      setInsights(insightsRes.data);

      // 3. Load Stats (Goals & Transactions)
      const goalsRes = await api.get("/api/goals");
      const txnsRes = await api.get("/api/transactions");

      const flaggedCount = txnsRes.data.filter(t => t.status === "FLAGGED").length;

      setStats({
        activeGoals: goalsRes.data.length,
        flaggedTxns: flaggedCount
      });
    } catch (err) {
      console.error("Failed to load dashboard metrics:", err);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  if (!profile) {
    return (
      <div className="dashboard-layout">
        <Sidebar />
        <div className="dashboard-content">
          <h2>Loading banking dashboard...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Dashboard</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
          Welcome back, <strong style={{ color: "#3b82f6" }}>{profile.username}</strong>! Here is your financial snapshot.
        </p>

        {/* Financial Stat Cards */}
        <div className="cards-container">
          <div className="card" style={{ borderLeft: "5px solid #3b82f6" }}>
            <h3>Account Cash Balance</h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", marginTop: "10px", color: "#60a5fa" }}>
              ${profile.balance.toFixed(2)}
            </p>
          </div>

          <div className="card" style={{ borderLeft: "5px solid #10b981" }}>
            <h3>Portfolio Investments</h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", marginTop: "10px", color: "#10b981" }}>
              ${profile.portfolioValue.toFixed(2)}
            </p>
          </div>

          <div className="card" style={{ borderLeft: "5px solid #8b5cf6" }}>
            <h3>Active Savings Goals</h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", marginTop: "10px", color: "#c084fc" }}>
              {stats.activeGoals} Active Goals
            </p>
          </div>

          <div className="card" style={{ 
            borderLeft: stats.flaggedTxns > 0 ? "5px solid #ef4444" : "5px solid #64748b" 
          }}>
            <h3>Flagged Fraud Alerts</h3>
            <p style={{ 
              fontSize: "28px", 
              fontWeight: "bold", 
              marginTop: "10px", 
              color: stats.flaggedTxns > 0 ? "#ef4444" : "#cbd5e1" 
            }}>
              {stats.flaggedTxns} Action Needed
            </p>
          </div>
        </div>

        {/* Smart Insights Panel */}
        <div className="card" style={{ marginTop: "30px" }}>
          <h3>Smart AI Banking Insights</h3>
          <p style={{ fontSize: "13px", color: "#94a3b8", marginBottom: "15px" }}>
            Algorithmic recommendations generated based on your spending, credit score, and balance.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {insights.map((insight, index) => {
              let typeColor = "#64748b"; // info
              let bg = "rgba(100, 116, 139, 0.1)";
              if (insight.type === "SUCCESS") {
                typeColor = "#10b981";
                bg = "rgba(16, 185, 129, 0.1)";
              } else if (insight.type === "DANGER") {
                typeColor = "#ef4444";
                bg = "rgba(239, 68, 68, 0.1)";
              } else if (insight.type === "WARNING") {
                typeColor = "#f59e0b";
                bg = "rgba(245, 158, 11, 0.1)";
              }

              return (
                <div 
                  key={index} 
                  style={{
                    padding: "15px",
                    borderRadius: "8px",
                    background: bg,
                    border: `1px solid ${typeColor}`,
                  }}
                >
                  <strong style={{ color: typeColor, display: "block", marginBottom: "3px" }}>
                    {insight.title}
                  </strong>
                  <span style={{ fontSize: "14px", color: "#cbd5e1" }}>
                    {insight.message}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;