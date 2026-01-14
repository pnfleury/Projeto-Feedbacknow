import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

export default function GraficoSentimentos({ positivos = 0, negativos = 0 }) {
  const data = {
    labels: ['Positivos', 'Negativos'],
    datasets: [{
      data: [positivos, negativos],
      backgroundColor: ['#10b981', '#ef4444'],
      hoverOffset: 4,
    }]
  };

  return (
    <div className="card">
      <h3>Distribuição de Sentimentos</h3>
      <div style={{ height: "250px" }}>
        <Doughnut data={data} options={{ maintainAspectRatio: false }} />
      </div>
    </div>
  )
}