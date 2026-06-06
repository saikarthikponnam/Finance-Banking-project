import Sidebar from "../components/Sidebar";

function SavingsGoals() {
  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Savings Goals</h1>

        <div className="card">
          <h3>Create New Goal</h3>

          <input
            type="text"
            placeholder="Goal Name"
            className="expense-input"
          />

          <input
            type="number"
            placeholder="Target Amount"
            className="expense-input"
          />

          <button className="expense-btn">
            Create Goal
          </button>
        </div>

        <div className="card">
          <h3>My Goals</h3>

          <p>🚗 Car Fund</p>
          <p>Saved: ₹50,000 / ₹2,00,000</p>

          <br />

          <p>🏠 House Fund</p>
          <p>Saved: ₹1,50,000 / ₹10,00,000</p>
        </div>
      </div>
    </div>
  );
}

export default SavingsGoals;