import React, { useState, useEffect } from 'react';
import {getRelatorios} from "../../services/api"; 
import { FileText, Home, CheckSquare, Square, RefreshCw, Table, FileDown } from 'lucide-react';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default function Relatorios() {
  const [dados, setDados] = useState([]);
  const [quantidade, setQuantidade] = useState(10);
  const [carregando, setCarregando] = useState(false);
  
  const [colunas, setColunas] = useState({
    id: true,
    comentario: true,
    sentimento: true,
    criadoEm: true
  });

  // --- BUSCA DE DADOS USANDO A INSTÂNCIA 'api' ---
  const buscarDados = async (qtd) => {
    setCarregando(true);
    try {
      const limit = qtd === 0 ? 99999 : qtd; 
      
      // O token/auth agora é injetado automaticamente pelo seu services/api.js
      const response = await getRelatorios (qtd)
      console.log("DADOS VINDOS DO JAVA:", response.data); // Verifique isso no console (F12)
      
      const lista = response.data.content || // Se for paginado
                       (Array.isArray(response.data) ? response.data : []) || // Se for lista pura
                       [];
      setDados(lista);
    } catch (error) {
      console.error("Erro ao carregar dados para relatório", error);
      // O erro 403/401 será tratado aqui se o token falhar
      alert("Não foi possível carregar os dados. Verifique sua conexão.");
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    buscarDados(quantidade);
  }, []);

  // --- EXPORTAR EXCEL ---
  const exportarExcel = () => {
  try {
    if (!dados || dados.length === 0) return alert("Sem dados para exportar");

    // FILTRO: Se quantidade for 0 (Todos), pega tudo. Senão, corta a lista.
    const dadosFiltrados = quantidade === 0 ? dados : dados.slice(0, quantidade);

    const dadosParaExportar = dadosFiltrados.map(item => {
      let linha = {};
      if (colunas.id) linha["ID"] = item.id;
      if (colunas.comentario) linha["Comentário"] = item.comentario;
      if (colunas.sentimento) linha["Sentimento"] = item.sentimento;
      if (colunas.criadoEm) linha["Data"] = item.criadoEm 
    ? item.criadoEm.split('T')[0].split('-').reverse().join('-') + ' ' + item.criadoEm.split('T')[1]
    : "---";
      return linha;
    });

    const ws = XLSX.utils.json_to_sheet(dadosParaExportar);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Relatorio");
    XLSX.writeFile(wb, `relatorio_.xlsx`);
  } catch (err) {
    console.error(err);
  }
};

  // --- GERAR PDF ---
 const gerarPDF = () => {
  try {
    if (!dados || dados.length === 0) return alert("Sem dados para exportar");

    const dadosFiltrados = quantidade === 0 ? dados : dados.slice(0, quantidade);
    const doc = new jsPDF(); // Aqui ele usa o jsPDF importado no topo

    doc.text("Relatório de Sentimentos", 14, 15);

    const colunasAtivas = [];
    if (colunas.id) colunasAtivas.push("ID");
    if (colunas.comentario) colunasAtivas.push("Comentário");
    if (colunas.sentimento) colunasAtivas.push("Sentimento");
    if (colunas.criadoEm) colunasAtivas.push("Data");

    const linhas = dadosFiltrados.map(item => {
      const row = [];
      if (colunas.id) row.push(item.id);
      if (colunas.comentario) row.push(item.comentario);
      if (colunas.sentimento) row.push(item.sentimento);
      if (colunas.criadoEm) row.push(item.criadoEm 
    ? item.criadoEm.split('T')[0].split('-').reverse().join('-') + ' ' + item.criadoEm.split('T')[1]
    : "---");
      return row;
    });

    // Usa a função autoTable importada
    autoTable(doc, {
      head: [colunasAtivas],
      body: linhas,
      startY: 20,
      theme: 'grid',
      headStyles: { fillColor: [16, 185, 129] }
    });

    doc.save(`relatorio_.pdf`);
  } catch (err) {
    console.error("Erro no PDF:", err);
  }
};

  const toggleColuna = (col) => setColunas(prev => ({ ...prev, [col]: !prev[col] }));

  return (
    <div style={styles.container}>
      <div style={styles.mainContent}>
        <div style={styles.headerRow}>
          <button onClick={() => window.location.href = "/"} style={styles.btnHome}>
            <Home size={18} /> Voltar
          </button>
          {carregando && <RefreshCw size={20} color="#10b981" className="animate-spin" />}
        </div>

        <div style={styles.configBox}>
          <h2 style={styles.title}><FileText size={28} color="#10b981" /> Exportar Dados</h2>
          
          <div style={styles.section}>
            <label style={styles.label}>Quantidade de registros:</label>
            <select 
              value={quantidade} 
              onChange={(e) => {
                const val = Number(e.target.value);
                setQuantidade(val);
                buscarDados(val);
              }}
              style={styles.select}
            >
              <option value={5}>Últimos 5</option>
              <option value={10}>Últimos 10</option>
              <option value={50}>Últimos 50</option>
              <option value={0}>Todos os registros</option>
            </select>
          </div>

          <div style={styles.section}>
            <label style={styles.label}>Colunas do Relatório:</label>
            <div style={styles.checkboxGroup}>
              {Object.keys(colunas).map(col => (
                <div key={col} onClick={() => toggleColuna(col)} style={styles.checkboxItem}>
                  {colunas[col] ? <CheckSquare color="#10b981" size={20} /> : <Square color="#94a3b8" size={20} />}
                  <span style={{ color: colunas[col] ? "white" : "#94a3b8", fontSize: '0.8rem' }}>
                    {col === 'criadoEm' ? 'DATA/HORA' : col.toUpperCase()}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div style={{ display: "flex", gap: "10px", marginTop: "30px" }}>
            <button onClick={exportarExcel} style={styles.btnExcel} disabled={dados.length === 0 || carregando}>
              <Table size={20} /> Excel (.xlsx)
            </button>
            <button onClick={gerarPDF} style={styles.btnPDF} disabled={dados.length === 0 || carregando}>
              <FileDown size={20} /> PDF (.pdf)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// Estilos mantidos para consistência visual
const styles = {
  container: { backgroundColor: "#0f172a", minHeight: "100vh", padding: "40px 20px", color: "white", display: "flex", justifyContent: "center" },
  mainContent: { width: "100%", maxWidth: "600px" },
  headerRow: { display: "flex", justifyContent: "space-between", marginBottom: "20px" },
  btnHome: { background: "#1e293b", color: "#94a3b8", padding: "10px 15px", borderRadius: "8px", border: "1px solid #334155", cursor: "pointer", display: "flex", gap: "8px" },
  configBox: { background: "#1e293b", padding: "30px", borderRadius: "20px", border: "1px solid #334155" },
  title: { display: "flex", alignItems: "center", gap: "12px", marginBottom: "30px", fontSize: '1.5rem' },
  section: { marginBottom: "25px" },
  label: { color: "#94a3b8", display: "block", marginBottom: "12px", fontSize: '0.9rem' },
  select: { width: "100%", padding: "12px", backgroundColor: "#0f172a", color: "white", border: "1px solid #334155", borderRadius: "8px" },
  checkboxGroup: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" },
  checkboxItem: { display: "flex", alignItems: "center", gap: "10px", cursor: "pointer", padding: "12px", background: "#0f172a", borderRadius: "8px", border: "1px solid #334155" },
  btnExcel: { flex: 1, padding: "15px", backgroundColor: "#10b981", color: "white", border: "none", borderRadius: "10px", fontWeight: "bold", cursor: "pointer", display: "flex", justifyContent: "center", gap: "10px" },
  btnPDF: { flex: 1, padding: "15px", backgroundColor: "#ef4444", color: "white", border: "none", borderRadius: "10px", fontWeight: "bold", cursor: "pointer", display: "flex", justifyContent: "center", gap: "10px" }
};