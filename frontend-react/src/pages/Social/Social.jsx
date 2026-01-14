import React, { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft, Facebook, Instagram, ThumbsUp, ThumbsDown } from "lucide-react";

const Social = ({ sampleFeedbacks = [], setNotificacoes }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const redeAlvo = location.state?.rede; // "Instagram" ou "Facebook"
  const filtroSentimento = location.state?.filtroSentimento; // "pos" ou "neg"

  useEffect(() => {
    if (setNotificacoes && redeAlvo) {
      setNotificacoes(prev => ({ ...prev, [redeAlvo.toLowerCase()]: 0 }));
    }
  }, [redeAlvo, setNotificacoes]);

  // --- FILTRAGEM ---
  let mensagens = sampleFeedbacks.flatMap(f => f.mensagens || []);

  if (redeAlvo) {
    mensagens = mensagens.filter(m => m.origem?.toLowerCase() === redeAlvo.toLowerCase());
  } else if (filtroSentimento) {
    mensagens = mensagens.filter(m => m.sentiment === filtroSentimento);
  }

  // Pegar as últimas 10 mensagens
  const ultimas10 = mensagens.slice(-10).reverse();

  return (
    <div style={{ padding: "40px", color: "white", minHeight: "100vh", backgroundColor: "#0f172a" }}>
      
      <button onClick={() => navigate("/dashboard")} style={btnVoltar}>
        <ArrowLeft size={20} /> Voltar ao Painel
      </button>

      <div style={{ display: "flex", alignItems: "center", gap: "15px", marginBottom: "30px" }}>
        {redeAlvo === "Instagram" && <Instagram color="#E4405F" size={35} />}
        {redeAlvo === "Facebook" && <Facebook color="#1877F2" size={35} />}
        <h1 style={{ margin: 0 }}>
          {redeAlvo ? `Mensagens: ${redeAlvo}` : `Últimos Feedbacks ${filtroSentimento === 'pos' ? 'Positivos' : 'Negativos'}`}
        </h1>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "15px", maxWidth: "800px" }}>
        {ultimas10.map((m, i) => (
          <div key={i} style={cardMsg}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
              <span style={m.sentiment === 'pos' ? labelPos : labelNeg}>
                {m.sentiment === 'pos' ? <ThumbsUp size={12} /> : <ThumbsDown size={12} />}
                {m.sentiment === 'pos' ? 'Positivo' : 'Negativo'}
              </span>
              <span style={{ fontSize: "0.7rem", color: "#64748b", textTransform: "uppercase" }}>
                Via {m.origem}
              </span>
            </div>
            <p style={{ margin: 0, lineHeight: "1.5", color: "#e2e8f0" }}>{m.text}</p>
          </div>
        ))}

        {ultimas10.length === 0 && (
          <p style={{ color: "#64748b", textAlign: "center", padding: "40px" }}>Nenhuma mensagem encontrada.</p>
        )}
      </div>
    </div>
  );
};

// --- ESTILOS SOCIAL ---
const btnVoltar = { background: "none", border: "none", color: "#94a3b8", cursor: "pointer", display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" };
const cardMsg = { background: "#1e293b", padding: "20px", borderRadius: "12px", border: "1px solid #334155" };
const labelPos = { display: "flex", alignItems: "center", gap: "5px", color: "#10b981", fontSize: "0.75rem", fontWeight: "bold" };
const labelNeg = { display: "flex", alignItems: "center", gap: "5px", color: "#ef4444", fontSize: "0.75rem", fontWeight: "bold" };

export default Social;