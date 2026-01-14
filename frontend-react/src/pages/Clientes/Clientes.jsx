import React, { useState } from 'react';
import axios from 'axios';
import { Send, CheckCircle, MessageSquare, Home, AlertCircle, Loader2 } from 'lucide-react';

export default function Clientes() {
  const [comentario, setComentario] = useState("");
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState({ type: '', msg: '' });

  // Configurações de Autenticação extraídas do seu código funcional
  //const authHeader = btoa("admin:123456");

  const handleEnviarFeedback = async () => {
    if (!comentario.trim()) {
  setStatus({
    type: 'error',
    msg: 'O feedback não pode estar vazio.'
  });
  return;
}

if (comentario.trim().length < 4) {
  setStatus({
    type: 'error',
    msg: 'Quantidade de caracteres inválida. O feedback deve conter no mínimo 4 caracteres.'
  });
  return;
}

    setLoading(true);
    setStatus({ type: '', msg: '' });

    try {
      // URL no singular conforme verificado no seu log e código antigo
      const response = await axios.post(
        "http://localhost:8080/sentiment", 
        { comentario: comentario },
        //{
          //headers: {
          //  'Authorization': `Basic ${authHeader}`,
          //  'Content-Type': 'application/json'
          //}
        //}
      );

      // Se chegamos aqui, o log do Hibernate que você mandou acabou de ser gerado!
      setStatus({ 
        type: 'success', 
        msg: `Sucesso! Sentimento identificado: ${response.data.sentimento}` 
      });
      setComentario(""); 
    } catch (error) {
      console.error("Erro no envio:", error);
      setStatus({ 
        type: 'error', 
        msg: error.response?.status === 401 
          ? 'Erro de Autenticação (Basic Auth).' 
          : 'Erro ao conectar com o servidor.' 
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.pageContainer}>
      {/* Botão de retorno posicionado no topo */}
      <button onClick={() => window.location.href = "/"} style={styles.btnHome}>
        <Home size={18} /> Voltar para Home
      </button>

      <div style={styles.feedbackBox}>
        <div style={styles.header}>
          <div style={styles.iconCircle}>
            <MessageSquare size={28} color="#10b981" />
          </div>
          <h2 style={styles.title}>Novo Feedback</h2>
          <p style={styles.subtitle}>Registre comentários manuais para análise de IA</p>
        </div>

        <div style={styles.form}>
          <textarea
            style={styles.textArea}
            placeholder="Ex: O atendimento foi excelente, parabéns à equipe!"
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            disabled={loading}
          />

          <button
            onClick={handleEnviarFeedback}
            disabled={loading || !comentario.trim()}
            style={{
              ...styles.button,
              backgroundColor: loading ? "#1e293b" : "#10b981",
              cursor: loading ? "not-allowed" : "pointer"
            }}
          >
            {loading ? (
              <Loader2 size={20} className="animate-spin" />
            ) : (
              <>Enviar Comentário <Send size={18} style={{ marginLeft: '10px' }} /></>
            )}
          </button>

          {status.type === 'success' && (
            <div style={styles.successMessage}><CheckCircle size={18} /> {status.msg}</div>
          )}
          {status.type === 'error' && (
            <div style={styles.errorMessage}><AlertCircle size={18} /> {status.msg}</div>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  pageContainer: {
    display: "flex", justifyContent: "center", alignItems: "center",
    width: "100vw", height: "100vh", backgroundColor: "#0f172a", position: "relative"
  },
  btnHome: {
    position: "absolute", top: "30px", left: "30px", background: "#1e293b",
    border: "1px solid #334155", color: "#94a3b8", padding: "10px 16px",
    borderRadius: "8px", cursor: "pointer", display: "flex", alignItems: "center", gap: "8px"
  },
  feedbackBox: {
    width: "90%", maxWidth: "450px", backgroundColor: "#1e293b", padding: "40px",
    borderRadius: "24px", border: "1px solid #334155", boxShadow: "0 20px 50px rgba(0,0,0,0.5)"
  },
  header: { textAlign: "center", marginBottom: "30px" },
  iconCircle: {
    width: "60px", height: "60px", backgroundColor: "rgba(16, 185, 129, 0.1)",
    borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center",
    margin: "0 auto 15px"
  },
  title: { color: "white", margin: "0 0 10px 0", fontSize: "1.8rem" },
  subtitle: { color: "#94a3b8", fontSize: "0.9rem" },
  form: { display: "flex", flexDirection: "column", gap: "20px" },
  textArea: {
    width: "100%", height: "160px", backgroundColor: "#0f172a", color: "white",
    border: "1px solid #334155", borderRadius: "12px", padding: "15px",
    fontSize: "1rem", outline: "none", resize: "none", boxSizing: "border-box"
  },
  button: {
    color: "white", border: "none", padding: "16px", borderRadius: "12px",
    fontSize: "1rem", fontWeight: "bold", display: "flex", justifyContent: "center", alignItems: "center"
  },
  successMessage: {
    display: "flex", alignItems: "center", justifyContent: "center", gap: "8px",
    color: "#10b981", backgroundColor: "rgba(16, 185, 129, 0.1)", padding: "12px", borderRadius: "8px"
  },
  errorMessage: {
    display: "flex", alignItems: "center", justifyContent: "center", gap: "8px",
    color: "#ef4444", backgroundColor: "rgba(239, 68, 68, 0.1)", padding: "12px", borderRadius: "8px"
  }
};
