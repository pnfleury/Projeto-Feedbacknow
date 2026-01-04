# 📊 API FeedbackNow - Documentação:

## Análise de Sentimentos para Feedback de Clientes

# 1. Visão Geral
Este sistema automatiza a classificação de feedbacks de clientes, identificando se um comentário é Positivo ou Negativo.  
O projeto utiliza uma arquitetura híbrida onde o Java gerencia as regras de negócio e persistência, enquanto o Python provê a inteligência preditiva.

# 2. Dependências e Versões

## 2.1. API Rest (Java)
A camada de aplicação foi construída com foco em segurança, robustez e performance:

* Java 21 / Spring Boot 3.x: Núcleo da aplicação.
* Spring Security (Basic Auth): Proteção de todos os endpoints contra acesso não autorizado.
* Hibernate (JPA): Mapeamento Objeto-Relacional para persistência no banco de dados.
*	PostgreSQL: Banco de dados relacional para armazenamento dos feedbacks.
*	Bean Validation: Garantia de que os dados de entrada (como o texto do comentário) seguem as regras de negócio.
*	SLF4J / Logback: Sistema de log detalhado para auditoria e depuração.
* 	Swagger UI (SpringDoc): Interface interativa para documentação e teste da API.

---

## 2.2. Motor de Inteligência (Python)
A camada de IA é um microserviço especializado em Processamento de Linguagem Natural (PLN):

*	Python 3.10+ / Flask: Servidor leve para exposição do modelo.
*	scikit-learn (1.8.0): Biblioteca de Machine Learning utilizada no treinamento e predição.
*	Joblib: Carregamento do modelo serializado.

---

# 3. Arquitetura do Sistema
A arquitetura segue o modelo de microserviços, onde o Java atua como o Gateway principal:
1. O cliente envia o feedback para o endpoint protegido via Basic Auth.
2. O Spring Boot valida a requisição e delega a análise textual para o Flask.
3. O Flask utiliza o modelo de ML carregado pelo Joblib para classificar o texto.
4. O resultado retorna ao Java, que utiliza o Hibernate para salvar no PostgreSQL.
5. Todo o fluxo é registrado via log SLF4J.

# 4. Exemplo de Requisição e Resposta

Especificação da API (Interface de Uso)  
O acesso à inteligência é centralizado no endpoint abaixo. O sistema aceita textos individuais e permite o ajuste fino da sensibilidade da classificação.
* Endpoint: POST http://localhost:8080/sentiment
* Autenticação: Basic Auth (Username/Password)

Corpo da Requisição (JSON)
  
  ```json
    {
     "comentario": "O aparelho é muito potente, porém a entrega demorou demais.",
     "threshold": 0.5
    }
  ```

Detalhamento dos Campos

* "comentario" é um tipo string obrigatório.
* "threshold" é um tipo float que pode ser opcional.  
  Observação sobre o Threshold: Este campo permite ajustar o rigor da classificação.  
  Por padrão (0.5), qualquer predição com probabilidade superior a esse valor é marcada como POSITIVO.  
  Se você deseja que o modelo seja mais criterioso para classificar algo como positivo, você pode aumentar este valor (ex: 0.8).

Resposta (JSON)  

```json
{
  "id": 102,
  "sentimento": "NEGATIVO",
  "probabilidade": 0.67,
  "topFeatures": ["demais", "muito", "entrega"],
  "criadoEm": "2025-12-30T10:15:30"
}
```
Detalhamento dos Campos  

* "sentimento" mostra o resultado da predição (positivo ou negativo).
* "probabilidade" mostra a probabilidade (confiança).
* "topFeatures" mostra as palavras de maior peso na predição.
* "criadoEm" fixa a data e hora da resposta.  

Todas os elementos da resposta são gravados no banco de dados.

---

# 5. Como o Modelo chega à Previsão
O diferencial deste modelo é a sua capacidade de explicar a decisão através da extração de termos determinantes:
1.	***Vetorização TF-IDF:*** O texto é convertido em valores numéricos baseados na frequência e importância de cada palavra.
2.	***N-grams (Unigramas e Bigramas):*** O modelo aprende termos simples ("lento") e compostos ("não recomendo").
3.	***Regressão Logística:*** Atribui um peso (coeficiente) para cada palavra.  
Palavras com pesos positivos altos (ex: "excelente") empurram o veredito para a classe Positiva; pesos negativos (ex: "defeito") empurram para a classe Negativa.
4.	***Extração de Features:*** O sistema filtra as palavras com os maiores pesos absolutos na frase e as expõe no campo topFeatures, fornecendo transparência ao usuário.  


# 6. Documentação da Interface (Swagger UI)
A API possui uma documentação interativa via Swagger, que permite testar todos os endpoints em tempo real.
## 6.1. Autenticação (Basic Auth)
Para utilizar os endpoints, é necessário autenticar-se no sistema.
1.	Clique no botão "Authorize" no topo da página.
2.	Insira o Username e Password configurados no Spring security.
3.	Clique em "Authorize" e feche a janela. O ícone de cadeado nos endpoints mudará para "trancado", indicando acesso liberado.
## 6.2. Endpoints de Consulta e Utilidade (GET)
Além do processamento de inteligência, a API oferece endpoints para gestão, auditoria e monitoramento de saúde do sistema.
* Endpoint: *GET /health*  
Descrição: Utilizado por ferramentas de monitoramento para garantir que a aplicação está ativa e respondendo, retorna a string “ok” com status HTTP 200.
*	Endpoint:*GET /sentiments*  
Descrição: Retorna todos os feedbacks processados e armazenados no banco de dados com paginação (padrão: dez elementos por página).
*	Endpoint: *GET /sentiments/stats*  
Descrição: Fornece um resumo quantitativo da base de dados (ex: total de positivos, total de negativos e total de registros nos ultimos x dias).  
Também mostra a porcentagem de positivos e negativos.
*	Endpoint: *GET /sentiment/{id}*  
Descrição: Busca os detalhes de um processamento específico através do seu identificador único.
*	Endpoint: *GET /estado/{sentimentoStr}*  
Descrição: Permite filtrar a base de dados mostrando só os feedbacks POSITIVO ou NEGATIVO, de acordo com a escolha do cliente e mostrando o total no final da página.
## 6.3. Endpoints de Análise de Sentimento (POST)
*	Endpoint:  *POST /sentiment*  
Utilizado para análises pontuais.  
Descrição: Retorna o objeto completo salvo no banco, incluindo o sentimento predito, a probabilidade (confiança) e as topFeatures (palavras que mais influenciaram o modelo).

*	Endpoint:  *POST /batch*  
Utilizado para análises em lote.  
Descrição: Aceita arquivos via multipart/form-data (ex: arquivo .csv).  
Clique em "Try it out", selecione o arquivo local e clique em "Execute".  
O sistema retorna uma lista de objetos processados salvos no banco de dados, ideal para auditorias em larga escala ou carga inicial de dados.

---

# 7. Logs e Rastreabilidade (SLF4J / Logback)
O sistema registra todas as operações críticas. Ao observar o console ou o arquivo de log durante as requisições, você verá entradas como:
Plaintext  
```json
INFO  - Recebendo requisição de análise individual: [O aparelho é muito potente...]
INFO  - Chamando microserviço Flask em http://localhost:5000/predict
INFO  - Resposta recebida da IA: NEGATIVO (Confiança: 0.67)
INFO  - Registro salvo com sucesso no PostgreSQL. ID: 128
```
# 8. Tratamento de Erros e Resiliência  
A API foi desenvolvida seguindo o princípio de Fail-Fast e utiliza as anotações:  
 * @RestControllerAdvice  
 * @ExceptionHandler  
 
 para centralizar a gestão de respostas de erro, garantindo que o cliente da API sempre receba um retorno claro e padronizado.  

## Principais Cenários Tratados:
Status	Erro	Mensagem de Retorno	Descrição  
* 400	Validação	"Dados inválidos"  
Disparado quando campos obrigatórios no JSON faltam ou estão incorretos.
* 400	JSON Malformado	"Tentativa de requisição com JSON inválido"  
Erro de sintaxe no corpo da requisição enviada.
* 400	Tipo de Dado	"O valor '%s' não é válido..."  
Erro na conversão de tipos (ex: enviar texto onde se espera um número).
* 400	Regra de Negócio	"Sentimento Inválido: Use POSITIVO ou NEGATIVO"  
Validação específica para o domínio da aplicação.
* 404	Rota Inexistente	"Rota não encontrada. A URL digitada não existe."  
Tratamento personalizado para URLs digitadas incorretamente.
* 405	Método Errado	"Método Inválido"  
Quando se tenta, por exemplo, um POST em uma rota que só aceita GET.
* 503	IA Indisponível	"Serviço de análise fora do ar."  
Resiliência: Captura falhas de conexão quando o motor Flask está offline.
* 500	Erro Interno	"Ocorreu um erro inesperado no servidor."  
Tratamento genérico para qualquer outra falha não prevista.

---

## 📝 Notas importantes:
- ***Segurança:*** Todos os endpoints também estão protegidos pelo Spring Security (Basic Auth), exigindo o cabeçalho de autorização.
- ***Logs:*** Cada consulta gera um log via SLF4J, permitindo rastrear quem acessou os dados e quando.
- ***Persistência:*** Todos os dados são recuperados diretamente do PostgreSQL através das interfaces do Spring Data JPA, garantindo alta performance nas consultas.
- ***Conteinerização:*** O projeto está totalmente conteinerizado, permitindo que toda a infraestrutura (Banco de Dados, Motor de IA e API Rest) suba de forma coordenada.  

---  
# 9. 🐳 Como baixar o projeto e executar via Docker.
Este guia orienta como baixar e rodar toda a infraestrutura (Banco de dados, Motor de IA e API Rest) utilizando Docker.
## 9.1. Pré-requisitos 
* Docker instalado e em execução.  
* Faça download e instale o Docker Desktop na sua máquina: https://www.docker.com/get-started
## 9.2. Download do Projeto  
Existem duas formas de obter os arquivos do repositório:  

### Opção A: Download Direto (ZIP) - Recomendado para quem não usa Git
1.	Acesse o link do repositório: https://github.com/pnfleury/Projeto-Feedbacknow
2.	No topo desta página, clique no botão verde ```<> Code``` .
3.	Selecione a opção ```Download ZIP```.
4.	Após o download, extraia o conteúdo do arquivo .zip para uma pasta em seu computador
### Opção B: Via Git Clone – git instalado  
1. No seu terminal, execute o comando:	
2.  git clone https://github.com/pnfleury/Projeto-Feedbacknow.git  

### A Estrutura do projeto fica assim: 
```Feedbacknow/  
Feedbacknow/
├── api-flask/
│   ├── app.py
│   ├── Dockerfile
│   ├── modelo.joblib
│   └── requirements.txt
├── api-java/
│   ├── src /
|   ├── .dockerignore
|   ├── Dockerfile
│   └── pom.xml
├── .env.example
├── docker-compose.yml
└── README.md
```


## 9.3. Configuração das Variáveis de Ambiente (Obrigatório)  
Por questões de segurança e boas práticas, o arquivo que contém as senhas (.env) não é enviado para o GitHub.  
Para que o sistema funcione, você precisa ativar as configurações padrão:
1.	Na raiz do projeto, localize o arquivo (.env.example).
2.	Crie uma cópia deste arquivo e mude o nome da cópia para (.env).
3.	Este arquivo já possui as credenciais padrão para que o ambiente de teste suba imediatamente.
## 9.4. Inicialização com Docker  
* Abra o terminal (ou Prompt de Comando) na pasta raiz do projeto (onde está o arquivo docker-compose.yml) e execute:
```docker-compose up --build```
* O Docker irá baixar as imagens do PostgreSQL, configurar o ambiente Python, compilar a API Java e orquestrar a comunicação entre eles.
## 9.5. Acesso ao Sistema  
Assim que os logs indicarem que os serviços estão ativos, você poderá acessar:
*	Documentação Swagger (Para Testes): http://localhost:8080/swagger-ui.html   
* Para fins de avaliação e facilidade de deploy, as credenciais de acesso ao Swagger foram mantidas fixas no arquivo application.properties.  
Em um ambiente produtivo, estas seriam gerenciadas via Secrets ou Variáveis de Ambiente no Docker Compose.  
    Username:  _user_  
    Password:  _123456_ 

---

## 🔒 Nota sobre a Estrutura e Segurança
* Integridade: Ao baixar via ZIP ou Git, você receberá a estrutura de diretórios completa, necessária para a construção das imagens Docker.
* Seguindo boas práticas de desenvolvimento, o arquivo .env está incluído no .gitignore para evitar a exposição de credenciais sensíveis no repositório.  
O uso do .env.example permite que novos desenvolvedores configurem seu ambiente local de forma rápida e segura.

---

## 🌐 CORS

Configurado para permitir acesso do frontend local desenvolvido em Streamlit (em desenvolvimento):

```text
http://localhost:8501
```
---

## 🧠 Recursos Implementados  
✔ Persistência dos resultados  
✔ Estatísticas agregadas  
✔ Explicabilidade básica (topFeatures)  
✔ Batch processing, prediçoes em lote  
✔ Threshold para ajustar o rigor da probabilidade  
✔ Tratamento global de erros  
✔ Logs estruturados  
✔ Segurança com Spring Security

---

## 🚧 Recursos Opcionais / Próximos Passos

* 📈 Interface Web (Streamlit ou Frontend JS)
* 🧪 Testes automatizados (unitários e integração)

---

## 👨‍💻 Autor
### Back end
* Carlos Oberto Pereira Lima
* Everton Guedes 
* Kauê Araujo 
* Paulo Fleury 

### Data Science 
* Felipe Miguel  
* Gabriela Duarte do Nascimento
* João Batista
* Tainah Torres   

### Projeto desenvolvido para fins educacionais e profissionais, com foco em arquitetura limpa, integração com IA e boas práticas em APIs REST.

---

## 📄 Licença

* Este projeto é livre para uso educacional e estudos.
