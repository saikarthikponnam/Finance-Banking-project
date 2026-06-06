import { useState, useEffect } from "react";
import Sidebar from "../components/sidebar";
import api from "../services/api";

function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [amount, setAmount] = useState("");
  const [category, setCategory] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchExpenses = async () => {
    try {
      const response = await api.get("/api/expenses");
      setExpenses(response.data);
    } catch (err) {
      console.error("Failed to fetch expenses:", err);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, []);

  const handleAddExpense = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!amount || !category) {
      setError("Amount and Category are required.");
      setLoading(false);
      return;
    }

    try {
      const today = new Date().toISOString().split('T')[0];
      await api.post("/api/expenses", {
        amount: parseFloat(amount),
        category,
        description,
        date: today
      });

      // Clear form
      setAmount("");
      setCategory("");
      setDescription("");

      // Refresh list
      await fetchExpenses();
      alert("Expense added successfully!");
    } catch (err) {
      console.error(err);
      setError("Failed to add expense. Check balance or credentials.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteExpense = async (id) => {
    if (!window.confirm("Are you sure you want to delete this expense?")) return;
    
    try {
      await api.delete(`/api/expenses/${id}`);
      await fetchExpenses();
      alert("Expense deleted and balance refunded!");
    } catch (err) {
      console.error(err);
      alert("Failed to delete expense.");
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Expense Tracker</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", marginBottom: "20px" }}>
          Log your expenses. Adding an expense automatically deducts from your main wallet balance.
        </p>

        {error && <p style={{ color: "#ef4444", marginBottom: "15px" }}>{error}</p>}

        <form onSubmit={handleAddExpense} className="card">
          <h3>Add Expense</h3>

          <input
            type="number"
            placeholder="Amount (e.g. 150)"
            className="expense-input"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />

          <input
            type="text"
            placeholder="Category (e.g. Food, Travel, Rent)"
            className="expense-input"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            required
          />

          <input
            type="text"
            placeholder="Description (optional)"
            className="expense-input"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />

          <button type="submit" className="expense-btn" disabled={loading}>
            {loading ? "Processing..." : "Add Expense"}
          </button>
        </form>

        <div className="card">
          <h3>Recent Expenses</h3>

          {expenses.length === 0 ? (
            <p style={{ marginTop: "15px", color: "#94a3b8" }}>No expenses logged yet.</p>
          ) : (
            <table className="expense-table">
              <thead>
                <tr>
                  <th>Amount</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {expenses.map((exp) => (
                  <tr key={exp.id}>
                    <td>${exp.amount.toFixed(2)}</td>
                    <td>{exp.category}</td>
                    <td>{exp.description || "-"}</td>
                    <td>{exp.date}</td>
                    <td>
                      <button 
                        onClick={() => handleDeleteExpense(exp.id)}
                        style={{
                          background: "#ef4444",
                          color: "white",
                          border: "none",
                          padding: "5px 10px",
                          borderRadius: "3px",
                          cursor: "pointer"
                        }}
                      >
                        Delete
                      </button>
                    </td>
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

export default Expenses;