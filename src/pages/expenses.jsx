import Sidebar from "../components/Sidebar";

function Expenses() {
  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Expense Tracker</h1>

        <div className="card">
          <h3>Add Expense</h3>

          <input
            type="number"
            placeholder="Amount"
            className="expense-input"
          />

          <input
            type="text"
            placeholder="Category"
            className="expense-input"
          />

          <input
            type="text"
            placeholder="Description"
            className="expense-input"
          />

          <button className="expense-btn">
            Add Expense
          </button>
        </div>

        <div className="card">
          <h3>Recent Expenses</h3>

          <table className="expense-table">
            <thead>
              <tr>
                <th>Amount</th>
                <th>Category</th>
                <th>Description</th>
              </tr>
            </thead>

            <tbody>
              <tr>
                <td>₹500</td>
                <td>Food</td>
                <td>Lunch</td>
              </tr>

              <tr>
                <td>₹1000</td>
                <td>Travel</td>
                <td>Bus Pass</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Expenses;