import React, { useState } from "react";
import axios from "axios";
import { Send, CheckCircle, ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function EnviarFeedback() {
  const navigate = useNavigate();
  const [comentario, setComentario] = useState("");
  const [loading, setLoading] = useState(false);
  const [sucesso, setSucesso] = useState(false);

  const handleEnviar = async () => {
    if (!comentario.trim()) return;

    setLoading(true);
    try {
      // 🔗 Conexão com seu Backend Java (Spring Boot)
      const payload = {
        comentario: comentario, // Nome do campo igual ao seu banco
        origem: "CLIENTE_WEB"
      };

      await axios.post("http://localhost:8080/sentiments", payload);

      setSucesso(true);
      setComentario("");
      setTimeout(() => setSucesso(false), 3000);
    } catch (error) {
      alert("Erro ao enviar para análise. Verifique se o backend está ligado.");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.pageContainer}>
      <div style={styles.modalPanel} className="glass">
        {/* Botão simples para voltar se necessário */}
        <button onClick={() => navigate(-1)} style={styles.btnBack}>
          <ArrowLeft size={16} /> Voltar
        </button>

        <h2 style={styles.title}>Novo Feedback</h2>
        <p style={styles.subtitle}>O seu comentário será analisado automaticamente.</p>

        {/* Textarea centralizada e ocupando largura total */}
        <textarea
          value={comentario}
          onChange={(e) => setComentario(e.target.value)}
          placeholder="Digite aqui sua experiência..."
          style={styles.inputArea}
          disabled={loading}
        />

        <button 
          onClick={handleEnviar} 
          disabled={loading || !comentario.trim()} 
          style={{
            ...styles.btnSubmit,
            opacity: loading || !comentario.trim() ? 0.6 : 1
          }}
        >
          {loading ? "Enviando para análise..." : "Enviar Comentário"}
          {!loading && <Send size={18} style={{marginLeft: '8px'}} />}
        </button>

        {sucesso && (
          <div style={styles.successMsg}>
            <CheckCircle size={18} /> Enviado com sucesso!
          </div>
        )}
      </div>
    </div>
  );
}

// --- ESTILOS PARA CENTRALIZAÇÃO TOTAL ---
const styles = {
  pageContainer: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    width: "100vw",
    height: "100vh",
    backgroundColor: "#0f172a",
  },
  modalPanel: {
    width: "90%",
    maxWidth: "500px",
    backgroundColor: "#1e293b",
    padding: "30px",
    borderRadius: "16px",
    border: "1px solid #334155",
    display: "flex",
    flexDirection: "column",
    position: "relative"
  },
  btnBack: {
    position: "absolute", top: "20px", left: "20px",
    background: "none", border: "none", color: "#94a3b8",
    cursor: "pointer", display: "flex", alignItems: "center", gap: "5px"
  },
  title: { textAlign: "center", color: "white", marginTop: "20px", marginBottom: "5px" },
  subtitle: { textAlign: "center", color: "#94a3b8", fontSize: "0.9rem", marginBottom: "20px" },
  inputArea: {
    width: "100%",
    height: "150px",
    backgroundColor: "#0f172a",
    color: "white",
    border: "1px solid #334155",
    borderRadius: "10px",
    padding: "15px",
    fontSize: "1rem",
    outline: "none",
    resize: "none",
    boxSizing: "border-box" // Crucial para não quebrar a largura
  },
  btnSubmit: {
    marginTop: "20px",
    backgroundColor: "#10b981",
    color: "white",
    border: "none",
    padding: "14px",
    borderRadius: "8px",
    fontWeight: "bold",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center"
  },
  successMsg: {
    marginTop: "15px",
    color: "#10b981",
    textAlign: "center",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "8px"
  }
};