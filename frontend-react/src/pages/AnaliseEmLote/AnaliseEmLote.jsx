import React, { useState, useRef } from "react";
import { Send, UploadCloud, FileText, X, ChevronLeft, ChevronRight, BarChart2 } from "lucide-react";
import api from "../../services/api";
import mammoth from "mammoth";
import * as XLSX from "xlsx";

const AnaliseEmLote = () => {
  const [texto, setTexto] = useState("");
  const [arquivo, setArquivo] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isAnalysing, setIsAnalysing] = useState(false); // Estado de carregamento
  const [feedbacks, setFeedbacks] = useState([]);
  const [page, setPage] = useState(0);
  const pageSize = 10; // Reduzi para 10 para caber melhor com a tabela

  const fileInputRef = useRef(null);

  // --- LEITURA DO ARQUIVO ---
  const processarArquivo = async (file) => {
    let conteudo = "";
    const reader = new FileReader();

    if (file.type === "text/plain" || file.name.endsWith(".csv")) {
      reader.onload = (e) => {
        conteudo = e.target.result;
        setTexto(conteudo);
        setArquivo(file);
      };
      reader.readAsText(file);
    } else if (file.name.endsWith(".xlsx") || file.name.endsWith(".xls")) {
      reader.onload = (e) => {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, { type: "array" });
        conteudo = XLSX.utils.sheet_to_csv(workbook.Sheets[workbook.SheetNames[0]]);
        setTexto(conteudo);
        setArquivo(file);
      };
      reader.readAsArrayBuffer(file);
    } else if (file.name.endsWith(".docx")) {
      reader.onload = async (e) => {
        try {
          const result = await mammoth.extractRawText({ arrayBuffer: e.target.result });
          setTexto(result.value);
          setArquivo(file);
        } catch (err) {
          alert("Erro ao ler arquivo Word.");
        }
      };
      reader.readAsArrayBuffer(file);
    } else {
      alert("Formato não suportado!");
    }
  };

  // --- ENVIO REAL PARA O BACKEND ---
  const handleEnviar = async () => {
    if (!texto.trim()) return alert("Adicione conteúdo!");

    setIsAnalysing(true);
    try {
      const linhas = texto.split("\n").map(l => l.trim()).filter(l => l !== "");
      
      // Chamada para o novo endpoint JSON
      const response = await api.post("/batch/json", linhas);

      const resultadosFormatados = response.data.map(item => ({
        id: item.id || crypto.randomUUID(),
        text: item.comentario,
        sentiment: item.sentimento,
        probabilidade: item.probabilidade,
        topFeatures: item.topFeatures, // Pegando as palavras-chave do Java
        criadoEm: item.criadoEm
      }));

      setFeedbacks(resultadosFormatados);
      setTexto("");
      setArquivo(null);
      setPage(0);
      alert("Análise concluída!");
    } catch (error) {
      console.error(error);
      alert("Erro ao processar lote.");
    } finally {
      setIsAnalysing(false);
    }
  };

  // --- PAGINAÇÃO ---
  const totalPages = Math.ceil(feedbacks.length / pageSize);
  const currentComments = feedbacks.slice(page * pageSize, (page + 1) * pageSize);

  // --- HELPER PARA CORES ---
  const getSentimentStyle = (sent) => {
    const s = sent?.toUpperCase();
    if (s === "POSITIVO") return { color: "#10b981", bg: "rgba(16, 185, 129, 0.1)" };
    if (s === "NEGATIVO") return { color: "#ef4444", bg: "rgba(239, 68, 68, 0.1)" };
    return { color: "#94a3b8", bg: "rgba(148, 163, 184, 0.1)" };
  };

  return (
    <div style={pageContainer}>
      <div style={{ width: "100%", maxWidth: "1000px" }}>
        
        {/* CARD DE UPLOAD */}
        <div style={cardStyle}>
          <h2 style={titleStyle}><UploadCloud /> Análise em Lote</h2>
          <div 
            onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={(e) => { e.preventDefault(); setIsDragging(false); processarArquivo(e.dataTransfer.files[0]); }}
            style={{...dropZoneStyle, borderColor: isDragging ? "#3b82f6" : "#334155"}}
          >
            {arquivo && (
              <div style={fileBadge}>
                <FileText size={14} /> {arquivo.name} 
                <X size={14} onClick={() => setArquivo(null)} style={{cursor:"pointer"}}/>
              </div>
            )}
            <textarea
              style={textAreaStyle}
              placeholder="Cole os dados ou arraste arquivos..."
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
            />
          </div>

          <div style={actionsStyle}>
            <button onClick={() => fileInputRef.current.click()} style={secondaryBtn}>
              Selecionar Arquivo
            </button>
            <input type="file" ref={fileInputRef} onChange={(e) => processarArquivo(e.target.files[0])} style={{display:"none"}} />
            
            <button onClick={handleEnviar} disabled={isAnalysing} style={primaryBtn}>
              {isAnalysing ? "Processando..." : <><Send size={18} /> Analisar Dados</>}
            </button>
          </div>
        </div>

        {/* TABELA DE RESULTADOS */}
        {feedbacks.length > 0 && (
          <div style={resultsContainer}>
            <h3 style={{marginBottom: '20px'}}>Resultados da Inteligência Artificial</h3>
            <table style={tableStyle}>
              <thead>
                <tr>
                  <th style={thStyle}>Data/Hora</th>
                  <th style={thStyle}>Comentário</th>
                  <th style={thStyle}>Sentimento</th>
                  <th style={thStyle}>Confiança</th>
                  <th style={thStyle}>Destaques</th>
                </tr>
              </thead>
              <tbody>
                {currentComments.map((c) => {
                  const style = getSentimentStyle(c.sentiment);
                  return (
                    <tr key={c.id} style={trStyle}>
                    <td style={tdStyle}>
                      <span style={{ color: '#94a3b8', fontSize: '0.8rem' }}>
                        {c.criadoEm} 
                      </span>
                      </td>
                      <td style={tdTextStyle}>{c.text}</td>
                      <td style={tdStyle}>
                        <span style={{...badgeStyle, color: style.color, backgroundColor: style.bg}}>
                          {c.sentiment}
                        </span>
                      </td>
                      <td style={tdStyle}>
                        <span style={{color: '#94a3b8', fontSize: '0.85rem'}}>
                          {(c.probabilidade * 100).toFixed(1)}%
                        </span>
                      </td>
                      <td style={tdStyle}>
                        <div style={featureBox}>
                          {c.topFeatures?.map((f, i) => (
                            <span key={i} style={featureTag}>{f}</span>
                      
                          ))}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>

            {/* PAGINAÇÃO */}
            <div style={paginationStyle}>
              <button disabled={page === 0} onClick={() => setPage(page - 1)} style={pageBtn}>
                <ChevronLeft size={18}/>
              </button>
              <span>Página {page + 1} de {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} style={pageBtn}>
                <ChevronRight size={18}/>
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

// --- NOVOS ESTILOS ---
const pageContainer = { padding: "40px", backgroundColor: "#0f172a", minHeight: "100vh", display: "flex", justifyContent: "center" };
const cardStyle = { backgroundColor: "#1e293b", padding: "30px", borderRadius: "15px", border: "1px solid #334155", marginBottom: "30px" };
const titleStyle = { color: "white", marginBottom: "20px", display: "flex", alignItems: "center", gap: "10px", fontSize: '1.5rem' };
const dropZoneStyle = { border: "2px dashed", borderRadius: "12px", padding: "15px", backgroundColor: "#0f172a" };
const textAreaStyle = { width: "100%", height: "180px", background: "transparent", color: "#e2e8f0", border: "none", outline: "none", resize: "none" };
const actionsStyle = { display: "flex", justifyContent: "space-between", marginTop: "20px" };
const resultsContainer = { backgroundColor: "#1e293b", padding: "25px", borderRadius: "15px", color: "white", border: "1px solid #334155" };
const tableStyle = { width: "100%", borderCollapse: "collapse" };
const thStyle = { textAlign: "left", padding: "12px", borderBottom: "1px solid #334155", color: "#94a3b8", fontSize: "0.85rem" };
const trStyle = { borderBottom: "1px solid #1e293b" };
const tdStyle = { padding: "12px", verticalAlign: "middle" };
const tdTextStyle = { ...tdStyle, maxWidth: "300px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", fontSize: "0.9rem" };
const badgeStyle = { padding: "4px 10px", borderRadius: "6px", fontSize: "0.75rem", fontWeight: "bold" };
const featureBox = { display: "flex", flexWrap: "wrap", gap: "4px" };
const featureTag = { backgroundColor: "#334155", color: "#cbd5e1", padding: "2px 6px", borderRadius: "4px", fontSize: "0.7rem" };
const paginationStyle = { display: "flex", justifyContent: "center", alignItems: "center", gap: "20px", marginTop: "30px" };

const primaryBtn = { display: "flex", alignItems: "center", gap: "8px", backgroundColor: "#3b82f6", color: "white", border: "none", padding: "12px 24px", borderRadius: "8px", cursor: "pointer", fontWeight: "600" };
const secondaryBtn = { backgroundColor: "transparent", color: "#94a3b8", border: "1px solid #334155", padding: "12px 20px", borderRadius: "8px", cursor: "pointer" };
const pageBtn = { ...secondaryBtn, padding: "8px" };
const fileBadge = { display: "flex", alignItems: "center", gap: "8px", backgroundColor: "#3b82f6", color: "white", padding: "4px 12px", borderRadius: "20px", fontSize: "0.75rem", marginBottom: "10px", width: "fit-content" };

export default AnaliseEmLote;