import React, { useState, useEffect } from "react";
import { Routes, Route, Navigate } from "react-router-dom";

// --- SERVIÇOS E DADOS ---
import api, { getFeedbacks } from "./services/api";
//import { getFeedbacks } from "./services/api"; 
import { dadosExemplo } from "./data/mockData"; 

// --- COMPONENTES ---
import Sidebar from "./components/Sidebar";

// --- PÁGINAS ---
import Home from "./pages/Home/Home";
import Login from "./pages/Login/Login";
import Clientes from "./pages/Clientes/Clientes";
import Dashboard from "./pages/Dashboard/Dashboard";
import TodosComentarios from "./pages/FeedbackPages/TodosComentarios";
import Reports from "./pages/Reports/Reports";
import Social from "./pages/Social/Social"; 
import AnaliseEmLote from "./pages/AnaliseEmLote/AnaliseEmLote";


function App() {
  const [notificacoes, setNotificacoes] = useState({ 
    instagram: 8, 
    facebook: 3 
  });

  // Começamos com os dados de exemplo para o layout ter conteúdo visual imediato
  const [feedbacks, setFeedbacks] = useState(dadosExemplo);

  useEffect(() => {
    getFeedbacks()
      .then((response) => {
        // O log do Hibernate mostrou que o Spring está a fazer paginação.
        // Pegamos nos dados de 'conteudo' (conforme definido no seu DTO/Controller)
        const dadosVindoDoJava = response.data.conteudo || response.data.content;

        if (dadosVindoDoJava) {
          // Mantendo a compatibilidade: se o seu layout espera um array de objetos 
          // com o formato antigo, podemos mapear aqui, ou ajustar nos componentes.
          setFeedbacks(dadosVindoDoJava);
          console.log("Dados carregados do Backend com sucesso!");
        }
      })
      .catch((error) => {
        console.error("Erro ao conectar ao Backend. Mantendo MockData.", error);
      });
  }, []);

  return (
    <Routes>
      {/* --- ROTAS SEM SIDEBAR --- */}
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/clientes" element={<Clientes />} />

      {/* --- ROTAS COM SIDEBAR (Layout Protegido) --- */}
      <Route path="/*" element={
        <div style={{ display: "flex", width: "100vw", height: "100vh", backgroundColor: "#0f172a", overflow: "hidden" }}>
          <Sidebar />
          <main style={{ flex: 1, overflowY: "auto" }}>
            <Routes>
              <Route 
                path="/dashboard" 
                element={<Dashboard sampleFeedbacks={feedbacks} notificacoes={notificacoes} />} 
              />
              <Route 
                path="/social-messages" 
                element={<Social sampleFeedbacks={feedbacks} setNotificacoes={setNotificacoes} />} 
              />
              <Route 
                path="/todoscomentarios" 
                element={<TodosComentarios sampleFeedbacks={feedbacks} />} 
              />
              <Route 
                path="/reports" 
                element={<Reports sampleFeedbacks={feedbacks} />} 
              />
              <Route path="/analise" element={<AnaliseEmLote />} />
              
              {/* Redireciona qualquer rota interna desconhecida para o dashboard */}
              <Route path="*" element={<Navigate to="/dashboard" />} />
            </Routes>
          </main>
        </div>
      } />
    </Routes>
  );
}

export default App;