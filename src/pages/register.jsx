function Register() {
  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Create Account</h1>

        <input type="text" placeholder="Username" />

        <input type="email" placeholder="Email" />

        <input type="password" placeholder="Password" />

        <input type="number" placeholder="Monthly Income" />

        <input type="number" placeholder="Credit Score" />

        <button>Register</button>
      </div>
    </div>
  );
}

export default Register;