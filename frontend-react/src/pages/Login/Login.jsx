import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [pass, setPass] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // funcao login via jwt
  async function handleLogin() {
    setError("");
    if (!username.trim() || !pass.trim()) {
      setError("Por favor, preencha usuário e senha.");
      return;
    }

    setLoading(true);

    try {//chama o backend para autenticar
      const response = await axios.post("http://localhost:8080/auth/login", {
        username,
        password: pass
    });

    const token = response.data.accessToken;
    const { refreshToken, type } = response.data;

    //salva o token no localstorage
    localStorage.setItem("token", response.data.token);
    localStorage.setItem("username", username);

    //redireciona para a rota 
    navigate("/dashboard");

  } catch (err) {
    setError("Usuario ou senha incorretos.")
  }finally {
    setLoading(false);
  }
}

  // --- ADICIONE ESTES ESTILOS AQUI ---
  const styles = {
    pageContainer: {
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      width: "100vw",
      height: "100vh",
      backgroundColor: "#0f172a"
    },
    modalPanel: {
      width: "100%",
      maxWidth: "400px", // Limita a largura da box
      padding: "2rem",
      backgroundColor: "rgba(30, 41, 59, 0.7)", // Efeito Glass
      borderRadius: "1rem",
      display: "flex",
      flexDirection: "column",
      gap: "1rem" // Espaço igual entre os inputs
    },
    input: {
      width: "100%", // Ocupa todo o espaço da box
      padding: "12px",
      borderRadius: "8px",
      border: "1px solid #334155",
      backgroundColor: "#0f172a",
      color: "white",
      boxSizing: "border-box" // Garante que o padding não aumente o tamanho
    }
  };

  return (
    <div style={styles.pageContainer}>
      <div style={styles.modalPanel} className="glass">
        <h2 style={{ textAlign: "center", color: "white", marginBottom: "1rem" }}>
          Acesso Empresa
        </h2>

        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          style={styles.input} // Aplica o estilo centralizado
          placeholder="Usuário"
        />

        <input
          value={pass}
          onChange={(e) => setPass(e.target.value)}
          type="password"
          style={styles.input} // Aplica o estilo centralizado
          placeholder="Senha"
        />

        {error && <div style={{ color: "#fecaca", fontSize: "0.8rem" }}>{error}</div>}

        <div className="form-actions" style={{ display: "flex", gap: "10px", marginTop: "1rem" }}>
          <button onClick={handleLogin} disabled={loading} className="home-card-btn primary" style={{ flex: 1, padding: "10px", cursor: "pointer" }}>
            {loading ? "Entrando..." : "Entrar"}
          </button>

          <button onClick={() => navigate("/")} disabled={loading} className="home-card-btn secondary" style={{ flex: 1, padding: "10px", cursor: "pointer" }}>
            Voltar
          </button>
        </div>
      </div>
    </div>
  );
}