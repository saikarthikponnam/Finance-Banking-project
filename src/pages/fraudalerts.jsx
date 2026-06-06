import Sidebar from "../components/Sidebar";

function FraudAlerts() {
  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Fraud Alerts</h1>

        <div className="card">
          <h3>Flagged Transactions</h3>

          <table className="expense-table">
            <thead>
              <tr>
                <th>Transaction ID</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Risk Score</th>
              </tr>
            </thead>

            <tbody>
              <tr>
                <td>TXN101</td>
                <td>₹25,000</td>
                <td>FLAGGED</td>
                <td>85%</td>
              </tr>

              <tr>
                <td>TXN102</td>
                <td>₹10,000</td>
                <td>FLAGGED</td>
                <td>70%</td>
              </tr>
            </tbody>
          </table>

          <button className="expense-btn">
            Approve Transaction
          </button>

          <button className="expense-btn">
            Block Transaction
          </button>
        </div>
      </div>
    </div>
  );
}

export default FraudAlerts;