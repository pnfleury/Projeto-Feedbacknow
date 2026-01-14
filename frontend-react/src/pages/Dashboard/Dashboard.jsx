import React, { useState, useEffect } from "react";
import api from "../../services/api";
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement,
  ArcElement, Title, Tooltip, Legend, Filler
} from "chart.js";
import { Bar, Doughnut } from "react-chartjs-2";
import { TrendingUp, PieChart, Instagram, Facebook, Home, RefreshCw, X } from "lucide-react";

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend, Filler);

const Dashboard = () => {
  const [dadosBanco, setDadosBanco] = useState([]);
  const nomeUsuario = localStorage.getItem("username");
  const [carregando, setCarregando] = useState(false);
  const [modalAberto, setModalAberto] = useState(false);
  const [filtroTipo, setFiltroTipo] = useState(""); 
  const [valorFiltro, setValorFiltro] = useState(""); 
  const [novasInsta, setNovasInsta] = useState(0);
  const [novasFace, setNovasFace] = useState(0);

  // --- PADRONIZAÇÃO DE DADOS
  const padronizarDados = (lista) => {
    return lista.map(item => ({
      sentimento: item.sentimento || item.sentimentoText || "NEUTRO",
      comentario: item.comentario || item.mensagem || "",
      origem: item.origem || item.rede || "",
      criadoEm: item.criadoEm || item.data || new Date().toISOString()
    }));
  };

  const buscarDadosDoBanco = async () => {
    setCarregando(true);
    try {
      const token = localStorage.getItem("token");
      const response = await api.get("http://localhost:8080/sentiments?size=2000", {
        headers: { 'Authorization': `Bearer ${token}`}
      });
      const lista = response.data.conteudo || response.data.content || response.data || [];
      setDadosBanco(padronizarDados(lista));
    } catch (error) {
      console.error("Erro ao buscar dados:", error);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    buscarDadosDoBanco();

    // Inicia a conexão SSE
    const eventSource = new EventSource("http://localhost:8080/webhook/alerts", { withCredentials: true }); 

    // ✅ NOVO: Escuta o evento de confirmação que o AlertService envia agora
    eventSource.addEventListener("connected", (event) => {
        console.log("✅ Conexão SSE confirmada pelo Java:", event.data);
    });

    eventSource.onopen = () => {
        console.log("🔌 Canal de eventos aberto. Aguardando notificações...");
    };

    eventSource.onmessage = (event) => {
        try {
            const novoAlerta = JSON.parse(event.data);
            const alertaFormatado = padronizarDados([novoAlerta])[0];
            
            // Adiciona o novo alerta em tempo real ao topo da lista
            setDadosBanco(prev => [alertaFormatado, ...prev]);

            // Atualiza os contadores de notificação (badges)
            const origem = (alertaFormatado.origem || "").toUpperCase();
            if (origem === "INSTAGRAM") setNovasInsta(p => p + 1);
            if (origem === "FACEBOOK" || origem === "PAGE") setNovasFace(p => p + 1);
            
            console.log("📩 Novo feedback recebido em tempo real!");
        } catch (err) {
            console.error("Erro ao processar mensagem do servidor:", err);
        }
    };

    eventSource.onerror = (err) => {
        console.error("❌ Conexão SSE falhou. O backend pode estar fora ou a rota bloqueada.");
        // Não fechamos aqui para permitir que o navegador tente reconectar sozinho
    };

    // ✅ LIMPEZA: Fecha a conexão ao sair do Dashboard ou recarregar
    return () => {
        console.log("🔌 Desconectando do canal de alertas...");
        eventSource.close();
    };
}, []);

  const abrirModal = (tipo, valor) => {
    setFiltroTipo(tipo);
    setValorFiltro(valor);
    setModalAberto(true);
  };

  const fecharModal = () => {
    if (valorFiltro === "Instagram") setNovasInsta(0);
    if (valorFiltro === "Facebook") setNovasFace(0);
    setModalAberto(false);
  };

  const mensagensFiltradas = dadosBanco.filter(f => {
    if (filtroTipo === "SENTIMENTO") {
      if (valorFiltro === "TOTAL") return true;
      return f.sentimento === valorFiltro;
    }
    if (filtroTipo === "REDE") {
      const origem = (f.origem || "").toUpperCase();
      if (valorFiltro === "Instagram") return origem === "INSTAGRAM";
      if (valorFiltro === "Facebook") return origem === "FACEBOOK" || origem === "PAGE";
    }
    return true;
  });

  const ultimosRegistros = [...dadosBanco].slice(0, 10).reverse();
  const barData = {
    labels: ultimosRegistros.map(f => {
      const data = f.criadoEm ? new Date(f.criadoEm) : new Date();
      return `${data.getHours()}:${String(data.getMinutes()).padStart(2, '0')}`;
    }),
    datasets: [
      { label: "Positivos", data: ultimosRegistros.map(f => f.sentimento === "POSITIVO" ? 1 : 0), backgroundColor: "#10b981", borderRadius: 4 },
      { label: "Negativos", data: ultimosRegistros.map(f => f.sentimento === "NEGATIVO" ? 1 : 0), backgroundColor: "#ef4444", borderRadius: 4 }
    ],
  };

  return (
    <div className="dashboard-container" style={{ backgroundColor: "#0f172a", minHeight: "100vh" }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', padding: '20px', color: 'white' }}>
        <h2 style={{ margin: 0 }}>Dashboard de Sentimentos</h2>
        <span className="user-badge" style={{ backgroundColor: '#1e293b', padding: '5px 15px', borderRadius: '20px' }}>
            Olá, <strong>{nomeUsuario}</strong>!
        </span>
      </header>
      
      <div style={{ padding: "30px", color: "white", position: "relative", overflowX: "hidden" }}>
        
        {/* HEADER BOTOES */}
        <div style={{ position: "absolute", top: "0px", right: "30px", display: "flex", gap: "15px", zIndex: 100 }}>
          <button onClick={buscarDadosDoBanco} style={btnIconStyle}>
            <RefreshCw size={22} color="#94a3b8" className={carregando ? "animate-spin" : ""} />
          </button>
          <button onClick={() => abrirModal("REDE", "Instagram")} style={btnIconStyle}>
            <Instagram color="#E4405F" size={24} />
            {novasInsta > 0 && <span style={badgeStyle}>{novasInsta}</span>}
          </button>
          <button onClick={() => abrirModal("REDE", "Facebook")} style={btnIconStyle}>
            <Facebook color="#1877F2" size={24} />
            {novasFace > 0 && <span style={badgeStyle}>{novasFace}</span>}
          </button>
        </div>

        <button onClick={() => window.location.href = "/"} style={btnHomeStyle}><Home size={18} /> Home</button>

        {/* CARDS RESUMO */}
        <div style={{ display: "flex", gap: "20px", marginBottom: "30px" }}>
          <div onClick={() => abrirModal("SENTIMENTO", "TOTAL")} style={{ ...cardResumo, borderLeft: "6px solid #3b82f6", cursor: "pointer" }}>
            <span style={labelCard}>Total</span>
            <div style={{ fontSize: "2rem", fontWeight: "bold" }}>{dadosBanco.length}</div>
          </div>
          <div onClick={() => abrirModal("SENTIMENTO", "POSITIVO")} style={{ ...cardResumo, borderLeft: "6px solid #10b981", cursor: "pointer" }}>
            <span style={labelCard}>Positivos</span>
            <div style={{ fontSize: "2rem", fontWeight: "bold", color: "#10b981" }}>{dadosBanco.filter(f => f.sentimento === "POSITIVO").length}</div>
          </div>
          <div onClick={() => abrirModal("SENTIMENTO", "NEGATIVO")} style={{ ...cardResumo, borderLeft: "6px solid #ef4444", cursor: "pointer" }}>
            <span style={labelCard}>Negativos</span>
            <div style={{ fontSize: "2rem", fontWeight: "bold", color: "#ef4444" }}>{dadosBanco.filter(f => f.sentimento === "NEGATIVO").length}</div>
          </div>
        </div>

        {/* ÁREA DE GRÁFICOS */}
        <div style={{ display: "flex", gap: "20px", width: "100%", alignItems: "flex-start" }}>
          <div style={{ ...cardGrafico, flex: "0 0 58%", height: "350px", minWidth: "0" }}>
            <div style={tituloGrafico}><TrendingUp size={18} /> Histórico Temporal</div>
            <div style={{ height: "250px" }}>
              <Bar data={barData} options={{ maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { display: false }, x: { ticks: { color: "#94a3b8" } } } }} />
            </div>
          </div>

          <div style={{ ...cardGrafico, flex: "0 0 38%", height: "350px", minWidth: "0" }}>
            <div style={tituloGrafico}><PieChart size={18} /> Proporção</div>
            <div style={{ height: "250px" }}>
              <Doughnut 
                data={{
                  labels: ["Positivos", "Negativos"],
                  datasets: [{ data: [dadosBanco.filter(f => f.sentimento === "POSITIVO").length, dadosBanco.filter(f => f.sentimento === "NEGATIVO").length], backgroundColor: ["#10b981", "#ef4444"], borderWidth: 0 }]
                }} 
                options={{ maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: "#94a3b8" } } } }}
              />
            </div>
          </div>
        </div>

        {/* MODAL */}
        {modalAberto && (
          <div style={modalOverlay}>
            <div style={modalContent}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "20px" }}>
                <h3 style={{ margin: 0 }}>Visualizando: {valorFiltro}</h3>
                <X size={28} style={{ cursor: "pointer" }} onClick={fecharModal} />
              </div>
              <div style={{ maxHeight: "500px", overflowY: "auto" }}>
                {mensagensFiltradas.length === 0 ? <p>Nenhum registro encontrado.</p> : 
                  mensagensFiltradas.map((m, i) => (
                    <div key={i} style={mensagemItem}>
                      <p style={{ margin: "0 0 8px 0" }}>{m.comentario}</p>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: m.sentimento === "POSITIVO" ? "#10b981" : "#ef4444", fontWeight: "bold", fontSize: "12px" }}>{m.sentimento}</span>
                        <span style={{ fontSize: '10px', color: '#64748b' }}>{new Date(m.criadoEm).toLocaleString()}</span>
                      </div>
                    </div>
                  ))
                }
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// Estilos mantidos conforme o original
const btnIconStyle = { width: "50px", height: "50px", borderRadius: "12px", backgroundColor: "#1e293b", border: "1px solid #334155", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", position: "relative" };
const badgeStyle = { position: "absolute", top: "-5px", right: "-5px", backgroundColor: "#ef4444", color: "white", borderRadius: "50%", minWidth: "20px", height: "20px", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "10px", fontWeight: "bold" };
const btnHomeStyle = { background: "#1e293b", border: "1px solid #334155", color: "#94a3b8", padding: "8px 16px", borderRadius: "6px", cursor: "pointer", display: "flex", alignItems: "center", gap: "8px", marginBottom: "20px" };
const cardResumo = { flex: "1", background: "#1e293b", padding: "20px", borderRadius: "12px", border: "1px solid #334155" };
const cardGrafico = { background: "#1e293b", padding: "25px", borderRadius: "15px", border: "1px solid #334155" };
const labelCard = { fontSize: "0.75rem", color: "#94a3b8", textTransform: "uppercase" };
const tituloGrafico = { display: "flex", alignItems: "center", gap: "10px", marginBottom: "15px", color: "#cbd5e1", fontSize: "14px" };
const modalOverlay = { position: "fixed", top: 0, left: 0, width: "100%", height: "100%", backgroundColor: "rgba(0,0,0,0.85)", display: "flex", justifyContent: "center", alignItems: "center", zIndex: 1000 };
const modalContent = { backgroundColor: "#1e293b", padding: "30px", borderRadius: "20px", width: "650px", border: "1px solid #334155" };
const mensagemItem = { padding: "15px", borderBottom: "1px solid #334155", backgroundColor: "#0f172a", marginBottom: "10px", borderRadius: "8px" };

export default Dashboard;