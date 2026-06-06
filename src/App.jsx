import { BrowserRouter, Routes, Route } from "react-router-dom";

import Login from "./pages/login";
import Register from "./pages/register";
import Dashboard from "./pages/dashboard";
import Expenses from "./pages/expenses";
import FraudAlerts from "./pages/fraudalerts";
import LoanPredictor from "./pages/loanpredictor";
import SavingsGoals from "./pages/savingsgoals";
import StockSimulator from "./pages/stocksimulator";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/expenses" element={<Expenses />} />
        <Route path="/fraud-alerts" element={<FraudAlerts />} />
        <Route path="/loan-predictor" element={<LoanPredictor />} />
        <Route path="/savings-goals" element={<SavingsGoals />} />
        <Route path="/stocks" element={<StockSimulator />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;