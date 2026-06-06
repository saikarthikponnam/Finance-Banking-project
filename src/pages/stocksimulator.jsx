import Sidebar from "../components/Sidebar";

function StockSimulator() {
  return (
    <div className="dashboard-layout">
      <Sidebar />

      <div className="dashboard-content">
        <h1>Stock Market Simulator</h1>

        <div className="card">
          <h3>Available Stocks</h3>

          <table className="expense-table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Price</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              <tr>
                <td>Apple</td>
                <td>₹15,000</td>
                <td>
                  <button className="expense-btn">Buy</button>
                </td>
              </tr>

              <tr>
                <td>Google</td>
                <td>₹25,000</td>
                <td>
                  <button className="expense-btn">Buy</button>
                </td>
              </tr>

              <tr>
                <td>Tesla</td>
                <td>₹20,000</td>
                <td>
                  <button className="expense-btn">Buy</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div className="card">
          <h3>My Portfolio</h3>

          <p>Apple - 5 Shares</p>
          <p>Google - 2 Shares</p>
          <p>Tesla - 3 Shares</p>
        </div>
      </div>
    </div>
  );
}

export default StockSimulator;