import { Link } from "react-router-dom";

function Sidebar() {
  return (
    <div className="sidebar">
      <h2>Finance Bank</h2>

      <ul>
        <li>
          <Link to="/dashboard">Dashboard</Link>
        </li>

        <li>
          <Link to="/expenses">Expenses</Link>
        </li>

        <li>
          <Link to="/fraud-alerts">Fraud Alerts</Link>
        </li>

        <li>
          <Link to="/loan-predictor">Loan Predictor</Link>
        </li>

        <li>
          <Link to="/savings-goals">Savings Goals</Link>
        </li>

        <li>
          <Link to="/stocks">Stock Simulator</Link>
        </li>

        <li>
          <Link to="/">Logout</Link>
        </li>
      </ul>
    </div>
  );
}

export default Sidebar;