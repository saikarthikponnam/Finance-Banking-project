import { useState, useEffect } from "react";
import Sidebar from "../components/sidebar";
import api from "../services/api";

function LoanPredictor() {
  const [loanAmount, setLoanAmount] = useState("");
  const [termMonths, setTermMonths] = useState("");
  const [purpose, setPurpose] = useState("");
  const [monthlyDebt, setMonthlyDebt] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState("");

  const fetchHistory = async () => {
    try {
      const response = await api.get("/api/loans/history");
      setHistory(response.data);
    } catch (err) {
      console.error("Failed to load history:", err);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleCheckEligibility = async (e) => {
    e.preventDefault();
    setError("");
    setResult(null);
    setLoading(true);

    if (!loanAmount || !termMonths || !purpose) {
      setError("Please fill in all required fields.");
      setLoading(false);
      return;
    }

    try {
      const response = await api.post("/api/loans/apply", {
        loanAmount: parseFloat(loanAmount),
        termMonths: parseInt(termMonths),
        purpose,
        monthlyDebt: monthlyDebt ? parseFloat(monthlyDebt) : 0
      });

      setResult(response.data);
      
      // Clear inputs on success
      setLoanAmount("");
      setTermMonths("");
      setPurpose("");
      setMonthlyDebt("");

      // Refresh list
      await fetchHistory();
    } catch (err) {
      console.error(err);
      setError("Eligibility check failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Loan Eligibility Predictor</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
          Check your loan eligibility instantly. Approved loans will disburse cash immediately into your bank balance.
        </p>

        {error && <p style={{ color: "#ef4444", marginBottom: "15px" }}>{error}</p>}

        <form onSubmit={handleCheckEligibility} className="card">
          <h3>Apply for Loan</h3>

          <input
            type="number"
            placeholder="Requested Loan Amount ($)"
            className="expense-input"
            value={loanAmount}
            onChange={(e) => setLoanAmount(e.target.value)}
            required
          />

          <input
            type="number"
            placeholder="Loan Duration (Months, e.g. 12, 24)"
            className="expense-input"
            value={termMonths}
            onChange={(e) => setTermMonths(e.target.value)}
            required
          />

          <input
            type="text"
            placeholder="Purpose (e.g. Education, Renovation)"
            className="expense-input"
            value={purpose}
            onChange={(e) => setPurpose(e.target.value)}
            required
          />

          <input
            type="number"
            placeholder="Current Monthly Debt Payments ($)"
            className="expense-input"
            value={monthlyDebt}
            onChange={(e) => setMonthlyDebt(e.target.value)}
          />

          <button type="submit" className="expense-btn" disabled={loading}>
            {loading ? "Evaluating..." : "Check Eligibility"}
          </button>
        </form>

        {result && (
          <div className={`card ${result.status === "APPROVED" ? "approved-border" : "rejected-border"}`}
               style={{
                 borderLeft: result.status === "APPROVED" ? "6px solid #10b981" : "6px solid #ef4444",
                 paddingLeft: "15px"
               }}>
            <h3>Evaluation Result</h3>
            <p style={{ fontSize: "18px", fontWeight: "bold", margin: "10px 0" }}>
              Status: <span style={{ color: result.status === "APPROVED" ? "#10b981" : "#ef4444" }}>{result.status}</span>
            </p>
            <p style={{ color: "#cbd5e1", lineHeight: "1.5" }}>{result.analysisReport}</p>
          </div>
        )}

        <div className="card">
          <h3>Application History</h3>
          {history.length === 0 ? (
            <p style={{ marginTop: "15px", color: "#94a3b8" }}>No applications found.</p>
          ) : (
            <table className="expense-table">
              <thead>
                <tr>
                  <th>Amount</th>
                  <th>Duration</th>
                  <th>Purpose</th>
                  <th>DTI Ratio</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {history.map((app) => (
                  <tr key={app.id}>
                    <td>${app.loanAmount.toFixed(2)}</td>
                    <td>{app.termMonths} Months</td>
                    <td>{app.purpose}</td>
                    <td>{app.dtiRatio.toFixed(1)}%</td>
                    <td style={{ 
                      color: app.status === "APPROVED" ? "#10b981" : "#ef4444",
                      fontWeight: "bold"
                    }}>{app.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default LoanPredictor;