import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../services/api";

function Register() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [monthlyIncome, setMonthlyIncome] = useState("");
  const [creditScore, setCreditScore] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!username || !email || !password || !monthlyIncome || !creditScore) {
      setError("Please fill in all fields.");
      setLoading(false);
      return;
    }

    try {
      await api.post("/api/auth/register", {
        username,
        email,
        password,
        monthlyIncome: parseFloat(monthlyIncome),
        creditScore: parseInt(creditScore)
      });
      
      alert("Registration successful! Redirecting to login page...");
      navigate("/login");
    } catch (err) {
      console.error(err);
      setError(err.response?.data || "Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleRegister} className="login-box">
        <h1>Create Account</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", textAlign: "center" }}>
          Secure Personal & Investment Banking Portal
        </p>

        {error && <p style={{ color: "#ef4444", fontSize: "14px", textAlign: "center" }}>{error}</p>}

        <input 
          type="text" 
          placeholder="Username" 
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />

        <input 
          type="email" 
          placeholder="Email Address" 
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <input 
          type="password" 
          placeholder="Password" 
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <input 
          type="number" 
          placeholder="Monthly Income (e.g. 5000)" 
          value={monthlyIncome}
          onChange={(e) => setMonthlyIncome(e.target.value)}
          required
        />

        <input 
          type="number" 
          placeholder="Credit Score (300 - 850)" 
          value={creditScore}
          onChange={(e) => setCreditScore(e.target.value)}
          min="300"
          max="850"
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>

        <p style={{ textAlign: "center", fontSize: "14px", marginTop: "10px" }}>
          Already have an account? <Link to="/login" style={{ color: "#3b82f6", textDecoration: "none" }}>Login here</Link>
        </p>
      </form>
    </div>
  );
}

export default Register;