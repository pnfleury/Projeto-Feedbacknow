import React from "react";
import { Link, useLocation } from "react-router-dom";
import { 
  LayoutDashboard, 
  MessageSquare, 
  BarChart2, 
  FileText, 
  LogOut,
  ChevronRight
} from "lucide-react";

const Sidebar = () => {
  const location = useLocation();

  // --- ESTILOS ---
  const sidebarStyle = {
    width: "260px",
    backgroundColor: "#1e293b",
    height: "100vh",
    padding: "24px 16px",
    display: "flex",
    flexDirection: "column",
    borderRight: "1px solid #334155",
    boxShadow: "4px 0 10px rgba(0,0,0,0.1)"
  };

  const logoStyle = {
    color: "#3b82f6",
    fontSize: "1.5rem",
    fontWeight: "bold",
    marginBottom: "40px",
    paddingLeft: "12px",
    display: "flex",
    alignItems: "center",
    gap: "10px",
    textDecoration: "none"
  };

  // Função para aplicar estilo dinâmico nos itens do menu
  const getMenuItemStyle = (path) => {
    const isActive = location.pathname === path;
    return {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      padding: "12px 16px",
      borderRadius: "10px",
      textDecoration: "none",
      marginBottom: "8px",
      transition: "all 0.3s ease",
      backgroundColor: isActive ? "#3b82f6" : "transparent",
      color: isActive ? "white" : "#94a3b8",
    };
  };

  return (
    <div style={sidebarStyle}>
      {/* LOGO */}
      <Link to="/dashboard" style={logoStyle}>
        <div style={{ width: "8px", height: "24px", backgroundColor: "#3b82f6", borderRadius: "2px" }}></div>
        FeedbackNow
      </Link>
      
      {/* NAVEGAÇÃO PRINCIPAL */}
      <nav style={{ flex: 1 }}>
        <Link to="/dashboard" style={getMenuItemStyle("/dashboard")}>
          <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
            <LayoutDashboard size={20} />
            <span>Visão Geral</span>
          </div>
          {location.pathname === "/dashboard" && <ChevronRight size={16} />}
        </Link>

        <Link to="/todoscomentarios" style={getMenuItemStyle("/todoscomentarios")}>
          <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
            <MessageSquare size={20} />
            <span>Comentários</span>
          </div>
          {location.pathname === "/todoscomentarios" && <ChevronRight size={16} />}
        </Link>

        <Link to="/analise" style={getMenuItemStyle("/analise")}>
          <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
            <BarChart2 size={20} />
            <span>Análise em Lote</span>
          </div>
          {location.pathname === "/analise" && <ChevronRight size={16} />}
        </Link>

        <Link to="/reports" style={getMenuItemStyle("/reports")}>
          <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
            <FileText size={20} />
            <span>Relatórios</span>
          </div>
          {location.pathname === "/reports" && <ChevronRight size={16} />}
        </Link>
      </nav>

      {/* RODAPÉ / SAIR */}
      <div style={{ borderTop: "1px solid #334155", paddingTop: "20px" }}>
        <Link 
          to="/login" 
          style={{ 
            ...getMenuItemStyle("/login"), 
            color: "#ef4444",
            backgroundColor: "transparent" 
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
            <LogOut size={20} />
            <span>Sair da Conta</span>
          </div>
        </Link>
      </div>
    </div>
  );
};

// ESTA LINHA É A MAIS IMPORTANTE PARA RESOLVER O SEU ERRO
export default Sidebar;