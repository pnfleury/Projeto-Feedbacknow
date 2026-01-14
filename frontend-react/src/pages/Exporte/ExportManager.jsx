import React, { useState } from "react";
import { Download } from "lucide-react";
import jsPDF from "jspdf";
import "jspdf-autotable";
import * as XLSX from "xlsx";

const ExportManager = ({ todasMensagens }) => {
  const [quantidade, setQuantidade] = useState(10);

  const exportarPDF = () => {
    const doc = new jsPDF();
    const dados = todasMensagens.slice(0, quantidade).map(m => [m.sentiment, m.text]);
    
    doc.text("Relatório de Feedbacks", 14, 15);
    doc.autoTable({
      head: [['Sentimento', 'Mensagem']],
      body: dados,
      startY: 20,
    });
    doc.save("feedbacks.pdf");
  };

  const exportarExcel = () => {
    const dados = todasMensagens.slice(0, quantidade);
    const worksheet = XLSX.utils.json_to_sheet(dados);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Feedbacks");
    XLSX.writeFile(workbook, "feedbacks.xlsx");
  };

  return (
    <div style={containerExport}>
      <h4 style={{color: "#94a3b8", marginBottom: "10px"}}>Exportar Relatórios</h4>
      <div style={{display: "flex", gap: "10px", alignItems: "center"}}>
        <input 
          type="number" 
          value={quantidade} 
          onChange={(e) => setQuantidade(e.target.value)}
          style={inputStyle}
          placeholder="Qtd"
        />
        <button onClick={exportarPDF} style={btnExport}>PDF</button>
        <button onClick={exportarExcel} style={btnExport}>Excel/CSV</button>
      </div>
    </div>
  );
};

// Estilos rápidos
const containerExport = { background: "#1e293b", padding: "15px", borderRadius: "10px", marginTop: "20px", border: "1px solid #334155" };
const inputStyle = { width: "70px", background: "#0f172a", border: "1px solid #334155", color: "white", padding: "8px", borderRadius: "5px" };
const btnExport = { background: "#3b82f6", color: "white", border: "none", padding: "8px 15px", borderRadius: "5px", cursor: "pointer", fontWeight: "bold" };

export default ExportManager;