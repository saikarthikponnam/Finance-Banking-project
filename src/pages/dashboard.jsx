import Sidebar from "../components/Sidebar";

function Dashboard() {
  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Dashboard</h1>

        <div className="cards-container">
          <div className="card">
            <h3>Account Balance</h3>
            <p>₹50,000</p>
          </div>

          <div className="card">
            <h3>Monthly Expenses</h3>
            <p>₹10,000</p>
          </div>

          <div className="card">
            <h3>Savings Goals</h3>
            <p>3 Active Goals</p>
          </div>

          <div className="card">
            <h3>Fraud Alerts</h3>
            <p>2 Flagged Transactions</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;