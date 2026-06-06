import { useState, useEffect } from "react";
import Sidebar from "../components/sidebar";
import api from "../services/api";

function FraudAlerts() {
  const [transactions, setTransactions] = useState([]);
  const [swipeAmount, setSwipeAmount] = useState("");
  const [swipeMerchant, setSwipeMerchant] = useState("");
  const [swipeLocation, setSwipeLocation] = useState("");
  const [loading, setLoading] = useState(false);
  const [swipeLoading, setSwipeLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState(""); // success, warning, error

  const fetchTransactions = async () => {
    try {
      const response = await api.get("/api/transactions");
      setTransactions(response.data);
    } catch (err) {
      console.error("Failed to load transactions:", err);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleSimulateSwipe = async (e) => {
    e.preventDefault();
    setMessage("");
    setSwipeLoading(true);

    if (!swipeAmount || !swipeMerchant || !swipeLocation) {
      alert("Please fill in all transaction simulation fields.");
      setSwipeLoading(false);
      return;
    }

    try {
      const response = await api.post("/api/transactions/execute", {
        amount: parseFloat(swipeAmount),
        category: "Shopping",
        merchant: swipeMerchant,
        location: swipeLocation
      });

      const tx = response.data;
      
      // Clear form
      setSwipeAmount("");
      setSwipeMerchant("");
      setSwipeLocation("");

      // Refresh list
      await fetchTransactions();

      // Show result message
      if (tx.status === "APPROVED") {
        setMessageType("success");
        setMessage(`Transaction APPROVED! $${tx.amount.toFixed(2)} charged at ${tx.merchant}.`);
      } else if (tx.status === "FLAGGED") {
        setMessageType("warning");
        setMessage(`SUSPICIOUS ACTIVITY DETECTED: Transaction of $${tx.amount.toFixed(2)} at ${tx.merchant} has been FLAGGED. Action required below.`);
      } else if (tx.status === "BLOCKED") {
        setMessageType("error");
        setMessage(`SECURITY ALERT: Transaction BLOCKED! Reason: ${tx.failureReason}`);
      }
    } catch (err) {
      console.error(err);
      setMessageType("error");
      setMessage("Transaction failed to execute.");
    } finally {
      setSwipeLoading(false);
    }
  };

  const handleResolve = async (id, decision) => {
    if (!window.confirm(`Are you sure you want to ${decision.toLowerCase()} this transaction?`)) {
      return;
    }

    setLoading(true);
    try {
      await api.post(`/api/transactions/${id}/resolve`, {
        decision: decision // "APPROVE" or "BLOCK"
      });
      
      alert(`Transaction has been resolved as: ${decision}`);
      setMessage("");
      await fetchTransactions(); // Refresh logs
    } catch (err) {
      console.error(err);
      alert(err.response?.data || "Failed to resolve transaction.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Fraud Alerts & Auditing</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
          Monitor your card swipes. Transactions flagged as suspicious require manual authorization.
        </p>

        {/* Card Swipe Simulator */}
        <form onSubmit={handleSimulateSwipe} className="card" style={{ borderLeft: "5px solid #2563eb" }}>
          <h3>Simulate a New Card Swipe</h3>
          <p style={{ fontSize: "12px", color: "#94a3b8", marginBottom: "15px" }}>
            Test the Fraud Engine live! Enter parameters to trigger velocity blocks, location alarms, or high-value audits.
          </p>

          {message && (
            <div style={{
              padding: "12px",
              borderRadius: "5px",
              marginBottom: "15px",
              background: messageType === "success" ? "rgba(16, 185, 129, 0.1)" : messageType === "warning" ? "rgba(245, 158, 11, 0.1)" : "rgba(239, 68, 68, 0.1)",
              border: `1px solid ${messageType === "success" ? "#10b981" : messageType === "warning" ? "#f59e0b" : "#ef4444"}`,
              color: messageType === "success" ? "#10b981" : messageType === "warning" ? "#f59e0b" : "#ef4444",
              fontSize: "14px"
            }}>
              {message}
            </div>
          )}

          <div style={{ display: "flex", gap: "10px" }}>
            <input
              type="number"
              placeholder="Amount ($)"
              className="expense-input"
              style={{ margin: 0, flex: 1 }}
              value={swipeAmount}
              onChange={(e) => setSwipeAmount(e.target.value)}
              required
            />
            <input
              type="text"
              placeholder="Merchant Name"
              className="expense-input"
              style={{ margin: 0, flex: 1 }}
              value={swipeMerchant}
              onChange={(e) => setSwipeMerchant(e.target.value)}
              required
            />
            <input
              type="text"
              placeholder="City Location"
              className="expense-input"
              style={{ margin: 0, flex: 1 }}
              value={swipeLocation}
              onChange={(e) => setSwipeLocation(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="expense-btn" style={{ marginTop: "15px" }} disabled={swipeLoading}>
            {swipeLoading ? "Swiping Card..." : "Swipe Card"}
          </button>
        </form>

        <div className="card">
          <h3>Transaction History & Risk Logs</h3>

          {transactions.length === 0 ? (
            <p style={{ marginTop: "15px", color: "#94a3b8" }}>No transactions executed yet. Use the card simulator above to swipe your card!</p>
          ) : (
            <table className="expense-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Amount</th>
                  <th>Merchant</th>
                  <th>Location</th>
                  <th>Risk Score</th>
                  <th>Status</th>
                  <th>Action Needed</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => {
                  let statusColor = "#cbd5e1"; // normal
                  if (tx.status === "APPROVED") statusColor = "#10b981";
                  if (tx.status === "BLOCKED") statusColor = "#ef4444";
                  if (tx.status === "FLAGGED") statusColor = "#f59e0b";

                  return (
                    <tr key={tx.id}>
                      <td>#{tx.id}</td>
                      <td>${tx.amount.toFixed(2)}</td>
                      <td>{tx.merchant}</td>
                      <td>{tx.location}</td>
                      <td style={{ 
                        color: tx.riskScore >= 70 ? "#ef4444" : tx.riskScore >= 50 ? "#f59e0b" : "#10b981",
                        fontWeight: "bold"
                      }}>
                        {tx.riskScore}%
                      </td>
                      <td style={{ color: statusColor, fontWeight: "bold" }}>
                        {tx.status}
                      </td>
                      <td>
                        {tx.status === "FLAGGED" ? (
                          <div style={{ display: "flex", gap: "5px" }}>
                            <button
                              onClick={() => handleResolve(tx.id, "APPROVE")}
                              className="expense-btn"
                              style={{ margin: 0, padding: "5px 10px", width: "auto", background: "#10b981" }}
                              disabled={loading}
                            >
                              Authorize
                            </button>
                            <button
                              onClick={() => handleResolve(tx.id, "BLOCK")}
                              className="expense-btn"
                              style={{ margin: 0, padding: "5px 10px", width: "auto", background: "#ef4444" }}
                              disabled={loading}
                            >
                              Block
                            </button>
                          </div>
                        ) : (
                          <span style={{ color: "#94a3b8", fontSize: "13px" }}>Resolved</span>
                        )}
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

export default FraudAlerts;