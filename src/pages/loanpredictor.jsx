import Sidebar from "../components/Sidebar";

function LoanPredictor() {
  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Loan Eligibility Predictor</h1>

        <div className="card">
          <h3>Apply for Loan</h3>

          <input
            type="number"
            placeholder="Loan Amount"
            className="expense-input"
          />

          <input
            type="number"
            placeholder="Loan Duration (Months)"
            className="expense-input"
          />

          <input
            type="number"
            placeholder="Monthly Income"
            className="expense-input"
          />

          <input
            type="number"
            placeholder="Credit Score"
            className="expense-input"
          />

          <button className="expense-btn">
            Check Eligibility
          </button>
        </div>

        <div className="card">
          <h3>Prediction Result</h3>

          <p>Status: APPROVED</p>
          <p>Credit Score: 750</p>
          <p>Loan Amount Eligible: ₹5,00,000</p>
        </div>
      </div>
    </div>
  );
}

export default LoanPredictor;