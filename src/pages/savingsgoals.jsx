import { useState, useEffect } from "react";
import Sidebar from "../components/sidebar";
import api from "../services/api";

function SavingsGoals() {
  const [goals, setGoals] = useState([]);
  const [goalName, setGoalName] = useState("");
  const [targetAmount, setTargetAmount] = useState("");
  const [targetDate, setTargetDate] = useState("");
  const [depositAmount, setDepositAmount] = useState({}); // Stores deposit inputs for each goal
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchGoals = async () => {
    try {
      const response = await api.get("/api/goals");
      setGoals(response.data);
    } catch (err) {
      console.error("Failed to load goals:", err);
    }
  };

  useEffect(() => {
    fetchGoals();
  }, []);

  const handleCreateGoal = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!goalName || !targetAmount || !targetDate) {
      setError("Please fill in all fields.");
      setLoading(false);
      return;
    }

    try {
      await api.post("/api/goals", {
        goalName,
        targetAmount: parseFloat(targetAmount),
        targetDate
      });

      // Clear fields
      setGoalName("");
      setTargetAmount("");
      setTargetDate("");

      // Refresh
      await fetchGoals();
      alert("Savings goal created successfully!");
    } catch (err) {
      console.error(err);
      setError("Failed to create savings goal.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeposit = async (goalId) => {
    const amount = depositAmount[goalId];
    if (!amount || parseFloat(amount) <= 0) {
      alert("Please enter a valid deposit amount.");
      return;
    }

    try {
      await api.post(`/api/goals/${goalId}/deposit`, {
        amount: parseFloat(amount)
      });

      // Clear input
      setDepositAmount(prev => ({ ...prev, [goalId]: "" }));

      // Refresh
      await fetchGoals();
      alert("Funds deposited successfully!");
    } catch (err) {
      console.error(err);
      alert(err.response?.data || "Deposit failed. Ensure balance is sufficient.");
    }
  };

  const handleDepositChange = (goalId, value) => {
    setDepositAmount(prev => ({ ...prev, [goalId]: value }));
  };

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Savings Goals</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
          Define savings targets and allocate funds directly from your checking account balance.
        </p>

        {error && <p style={{ color: "#ef4444", marginBottom: "15px" }}>{error}</p>}

        <form onSubmit={handleCreateGoal} className="card">
          <h3>Create New Goal</h3>

          <input
            type="text"
            placeholder="Goal Name (e.g. Car Fund, House Downpayment)"
            className="expense-input"
            value={goalName}
            onChange={(e) => setGoalName(e.target.value)}
            required
          />

          <input
            type="number"
            placeholder="Target Amount ($)"
            className="expense-input"
            value={targetAmount}
            onChange={(e) => setTargetAmount(e.target.value)}
            required
          />

          <input
            type="date"
            className="expense-input"
            value={targetDate}
            onChange={(e) => setTargetDate(e.target.value)}
            required
          />

          <button type="submit" className="expense-btn" disabled={loading}>
            {loading ? "Creating..." : "Create Goal"}
          </button>
        </form>

        <div className="card">
          <h3>My Goals</h3>

          {goals.length === 0 ? (
            <p style={{ marginTop: "15px", color: "#94a3b8" }}>No active savings goals found.</p>
          ) : (
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))", gap: "20px", marginTop: "15px" }}>
              {goals.map((goal) => {
                const completionPercentage = Math.min(
                  ((goal.currentAmount / goal.targetAmount) * 100),
                  100
                ).toFixed(1);

                return (
                  <div key={goal.id} className="card" style={{ margin: 0, border: "1px solid #334155", background: "#1e293b" }}>
                    <h4>🎯 {goal.goalName}</h4>
                    <p style={{ fontSize: "14px", color: "#cbd5e1", margin: "10px 0" }}>
                      Target Date: {goal.targetDate}
                    </p>
                    
                    {/* Progress Bar */}
                    <div style={{ background: "#475569", borderRadius: "10px", height: "10px", width: "100%", margin: "15px 0 5px 0" }}>
                      <div style={{
                        background: "#10b981",
                        height: "100%",
                        width: `${completionPercentage}%`,
                        borderRadius: "10px",
                        transition: "width 0.5s ease-in-out"
                      }}></div>
                    </div>
                    
                    <p style={{ display: "flex", justifyContent: "space-between", fontSize: "14px", fontWeight: "bold", color: "#10b981" }}>
                      <span>Saved: ${goal.currentAmount.toFixed(2)} / ${goal.targetAmount.toFixed(2)}</span>
                      <span>{completionPercentage}%</span>
                    </p>

                    {/* Deposit Actions */}
                    <div style={{ display: "flex", gap: "10px", marginTop: "15px" }}>
                      <input
                        type="number"
                        placeholder="Deposit amount"
                        className="expense-input"
                        style={{ margin: 0, flex: 1, padding: "8px" }}
                        value={depositAmount[goal.id] || ""}
                        onChange={(e) => handleDepositChange(goal.id, e.target.value)}
                      />
                      <button 
                        onClick={() => handleDeposit(goal.id)}
                        className="expense-btn"
                        style={{ margin: 0, width: "auto", padding: "8px 15px" }}
                      >
                        Deposit
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default SavingsGoals;