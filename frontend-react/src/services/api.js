import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080"
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error)
});

// Para a lista de feedbacks (usada no App.jsx)
export const getFeedbacks = () => api.get("/sentiments/all"); 

// Para as estatísticas (usada nos gráficos do Dashboard)
export const getStats = () => api.get("/sentiments/stats"); 

// Para salvar um novo feedback (se houver um formulário)
export const createFeedback = (data) => api.post("/sentiments", data);

// Função para análise em lote (enviando a lista de uma vez)
export const analisarEmLote = (listaComentarios) => api.post("/batch/json", listaComentarios);

//export const Relatorios = () => api.get
export const getRelatorios = () => api.get("/sentiments/all");

// Para buscar os dados processados que vieram da Meta (usado no Dashboard)
export const getWebhookDados = () => api.get("/webhook/dados");

// Caso precise disparar um teste de sentimento manualmente via API
export const analisarSentimentoManual = (dados) => api.post("/webhook", dados);

export default api;