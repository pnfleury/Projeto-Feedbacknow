import React from "react";
import { useNavigate } from "react-router-dom";

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="home-root">
      <h1 className="home-title">FeedbackNow</h1>
      <p className="home-subtitle">Escolha como deseja prosseguir</p>

      <div className="home-cards">
        <div className="home-card glass">
          <h2>Sou Cliente</h2>
          <p>Deseja enviar um feedback sobre um serviço?</p>
          <button
            onClick={() => navigate("/clientes")}
            className="home-card-btn primary"
          >
            Enviar feedback
          </button>
        </div>

        <div className="home-card glass">
          <h2>Menu Empresa</h2>
          <p>Entre para acessar o painel administrativo e relatórios.</p>
          <button
            onClick={() => navigate("/login")}
            className="home-card-btn secondary"
          >
            Entrar
          </button>
        </div>
      </div>
    </div>
  );
}
