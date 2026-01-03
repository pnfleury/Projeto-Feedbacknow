# Importar as ferramentas necessarias
from flask import Flask, request, jsonify
from werkzeug.exceptions import HTTPException
from logging.handlers import RotatingFileHandler
import joblib
import logging
import sys

# Configuração de logs o terminal e arquivo
# Configuração de Rodízio (Substitui o FileHandler comum)
# maxBytes=1000000 (1MB) | backupCount=5 (guarda os últimos 5 arquivos)
handler = RotatingFileHandler("analise_sentimentos.log", maxBytes=1000000, backupCount=5, encoding='utf-8')
handler.setFormatter(logging.Formatter('%(asctime)s [%(levelname)s] %(message)s', '%d-%m-%Y %H:%M:%S'))
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%d-%m-%Y %H:%M:%S',
    handlers=[
        logging.FileHandler("analise_sentimentos.log", encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

app = Flask(__name__)
logger = logging.getLogger(__name__)

# Carrega o modelo
PIPELINE_PATH = "modelo.joblib"
pipeline = None

try:
    pipeline = joblib.load(PIPELINE_PATH)
    print(f"Pipeline ML carregado com sucesso de: {PIPELINE_PATH}")
except Exception as e:
    print(f"ERRO FATAL: Falha ao carregar o pipeline: {e}")
    sys.exit(1)

# --- FUNÇÃO DE NÚCLEO (OTIMIZAÇÃO) ---
# --- FUNÇÃO DE NÚCLEO (COM THRESHOLD DINÂMICO) ---
def executar_predicao(lista_textos, threshold=0.5): # Adicionado parâmetro threshold
    # Obtemos as probabilidades de todas as classes
    # probabilidades_todas terá o formato: [[prob_neg, prob_pos], [prob_neg, prob_pos]]
    predicoes = pipeline.predict(lista_textos)
    probabilidades_todas = pipeline.predict_proba(lista_textos)
    
    # Identificamos qual o índice da classe 'positivo' no modelo
    # Geralmente é 1, mas buscamos dinamicamente para garantir
    classes = pipeline.classes_.tolist()
    idx_positivo = classes.index('positivo')
    
    vect = pipeline.steps[0][1]
    clf = pipeline.steps[1][1]
    weights = clf.coef_[0] 
    analyzer = vect.build_analyzer()
    
    resultados = []

    for i, texto in enumerate(lista_textos):
        print(f"Texto: {texto[:40]}... | Predição: {predicoes[i]} | Probas: {probabilidades_todas[i]}")
        # Pegamos a probabilidade específica de ser positivo
        prob_pos = probabilidades_todas[i][idx_positivo]
        
        # LÓGICA DO THRESHOLD: Se a prob for >= threshold, é positivo.
        pred_classe = 'positivo' if prob_pos >= threshold else 'negativo'
        
        # O valor de exibição da probabilidade para o usuário
        proba_exibicao = prob_pos if pred_classe == 'positivo' else (1 - prob_pos)

        # EXPLICABILIDADE
        importance = []
        tokens = set(analyzer(texto)) 
        for word in tokens:
            if word in vect.vocabulary_:
                idx = vect.vocabulary_[word]
                importance.append((word, weights[idx]))
        
        # Se mudou para negativo por causa do threshold, invertemos a importância
        reverse_sort = (pred_classe == 'positivo')
        importance.sort(key=lambda x: x[1], reverse=reverse_sort)
        top_features = [p[0] for p in importance[:3]]

        resultados.append({
            "comentario": texto,
            "sentimento": pred_classe,
            "probabilidade": round(float(proba_exibicao), 2),
            "topFeatures": top_features
        })
    
    return resultados

# --- ENDPOINTS ATUALIZADOS ---

@app.route('/sentiment', methods=['POST'])
def analyze_sentiment():
    data = request.get_json(silent=True)
    if not data or 'comentario' not in data:
        return jsonify({"error": "Campo 'comentario' obrigatório"}), 422
    
    # Captura o threshold do Java (default 0.5)
    threshold = data.get('threshold', 0.5)
    
    resultado = executar_predicao([data['comentario']], threshold)
    return jsonify(resultado[0]), 200

@app.route('/predict_batch', methods=['POST'])
def predict_batch():
    data = request.get_json(silent=True)
    if not data or 'textos' not in data:
        return jsonify({"error": "Campo 'textos' (lista) é obrigatório"}), 400
    
    threshold = data.get('threshold', 0.5)
    lista_textos = [t.strip() for t in data['textos'] if t.strip()]
    
    resultados = executar_predicao(lista_textos, threshold)
    return jsonify(resultados), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)