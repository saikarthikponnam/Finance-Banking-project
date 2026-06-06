import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../services/api";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!username || !password) {
      setError("Please enter username and password.");
      setLoading(false);
      return;
    }

    try {
      const response = await api.post("/api/auth/login", {
        username,
        password
      });

      // Save token and profile details to localStorage for backend mapping
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("username", response.data.username);
      localStorage.setItem("email", response.data.email);
      localStorage.setItem("monthlyIncome", response.data.monthlyIncome);
      localStorage.setItem("creditScore", response.data.creditScore);
      
      navigate("/dashboard");
    } catch (err) {
      console.error(err);
      setError(err.response?.data || "Authentication failed. Check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleLogin} className="login-box">
        <h1>Finance Bank</h1>
        <p style={{ fontSize: "14px", color: "#94a3b8", textAlign: "center" }}>
          Secure Digital Banking Platform
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
          type="password" 
          placeholder="Password" 
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? "Authenticating..." : "Login"}
        </button>

        <p style={{ textAlign: "center", fontSize: "14px", marginTop: "10px" }}>
          Don't have an account? <Link to="/" style={{ color: "#3b82f6", textDecoration: "none" }}>Register here</Link>
        </p>
      </form>
    </div>
  );
}

export default Login;