# 📊 API FeedbackNow - Documentação:

## Análise de Sentimentos para Feedback de Clientes

# 1. Visão Geral
Este sistema automatiza a classificação de feedbacks de clientes, identificando se um comentário é Positivo ou Negativo.  
O projeto utiliza uma arquitetura híbrida onde o Java gerencia as regras de negócio e persistência, enquanto o Python provê a inteligência preditiva. Tudo isso é acessível através de um frontend React intuitivo e moderno.

# 2. Dependências e Versões

## 2.1. API Rest (Java)
A camada de aplicação foi construída com foco em segurança, robustez e performance:

* Java 21 / Spring Boot 3.x: Núcleo da aplicação.
* Spring Security (JSON Web Tokens) arquitetura Stateless para garantir a integridade e a autenticação das requisições do frontend.
* Spring Security (Basic Auth): Apenas para facilitar o acesso  pelo Postman, Insomnia ou Swagger.
* Hibernate (JPA): Mapeamento Objeto-Relacional para persistência no banco de dados.
* PostgreSQL: Banco de dados relacional para armazenamento dos feedbacks.
* Bean Validation: Garantia de que os dados de entrada (como o texto do comentário) seguem as regras de negócio.
* SLF4J / Logback: Sistema de log detalhado para auditoria e depuração.
* Swagger UI (SpringDoc): Interface interativa para documentação e teste da API.

---

## 2.2. Motor de Inteligência (Python)
A camada de IA é um microserviço especializado em Processamento de Linguagem Natural (PLN):
*	Python 3.10+ / Flask: Servidor leve para exposição do modelo.
*	scikit-learn (1.6.1): Biblioteca de Machine Learning utilizada no treinamento e predição.
*	Joblib: Carregamento do modelo serializado.

---

## 2.3. Frontend React
Frontend React intuitivo e moderno.:
* React.js (Vite) – Framework principal e ferramenta de build.
* Axios – Cliente HTTP para consumo da API REST.
* EventSource (SSE) – Protocolo para recebimento de alertas em tempo real.
* React-Chartjs-2 – Integração para visualização de dados (utilizando Chart.js).
* Lucide-React – Biblioteca de ícones leves e modernos.
* CSS Dinâmico – Estilização baseada em estados para feedback visual do usuári

---

# 3. Arquitetura do Sistema
A arquitetura segue o modelo de microserviços, onde o Java atua como o Gateway principal:
1. O cliente envia o feedback.
2. O Spring Boot valida a requisição e delega a análise textual para o Flask.
3. O Flask utiliza o modelo de ML carregado pelo Joblib para classificar o texto.
4. O resultado retorna ao Java, que utiliza o Hibernate para salvar no PostgreSQL.
5. Todo o fluxo é registrado via log SLF4J.

# 4. Exemplo de Requisição e Resposta
Especificação da API (Interface de Uso)  
O acesso à inteligência é centralizado no endpoint abaixo. O sistema aceita textos individuais e permite o ajuste fino da sensibilidade da classificação.
* Endpoint: POST http://localhost:8080/sentiment

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
  "probabilidade": 0.73,
  "topFeatures": [
    "aparelho",
    "demais",
    "entrega"
  ],
  "criadoEm": "30/12/2025 10:15:30"
}
```
Detalhamento dos Campos  

* "sentimento" mostra o resultado da predição (positivo ou negativo).
* "probabilidade" mostra a probabilidade (confiança).
* "topFeatures" mostra as palavras de maior peso na predição.
* "criadoEm" data e hora da resposta.  

*Todas os elementos da resposta são gravados no banco de dados.*

---

# 5. Como o Modelo chega à Previsão
O diferencial deste modelo é a sua capacidade de explicar a decisão através da extração de termos determinantes:
1.	***Vetorização TF-IDF:*** O texto é convertido em valores numéricos baseados na frequência e importância de cada palavra.
2.	***N-grams (Unigramas e Bigramas):*** O modelo aprende termos simples ("lento") e compostos ("não recomendo").
3.	***Regressão Logística:*** Atribui um peso (coeficiente) para cada palavra.  
Palavras com pesos positivos altos (ex: "excelente") empurram o veredito para a classe Positiva; pesos negativos (ex: "defeito") empurram para a classe Negativa.
4.	***Extração de Features:*** O sistema filtra as palavras com os maiores pesos absolutos na frase e as expõe no campo topFeatures, fornecendo transparência ao usuário.  
## 5.1. 🔬 Pesquisa e Treinamento (Notebook)
* Todo o processo de análise exploratória, pré-processamento de texto e treinamento do modelo pode ser visualizado no Google Colab:
* [Link para o Notebook do Projeto] https://colab.research.google.com/drive/1JUXChsX75nqmHp2DfWP6nJo0srwcwX0_?usp=sharing


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

* Endpoint *POST /feedbacknow/usuarios/cadastrar*  
Descrição: Cadastra usuarios pra usar o sistema.

* Endpoint *POST /debug/comentario*  
Descrição: Testa a integração Instagram/Facebook Graph API manualmente, sem precisar que alguém realmente comente no seu Facebook ou Instagram. Ainda em implementação.

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
- ***Segurança:*** O uso do token JWT é integrada ao Frontend; após o login, o token é armazenado de forma segura e anexado ao Header de Autorização de todas as chamadas subsequentes às rotas protegidas.
- ***Logs:*** Cada consulta gera um log via SLF4J, permitindo rastrear quem acessou os dados e quando.
- ***Persistência:*** Todos os dados são recuperados diretamente do PostgreSQL através das interfaces do Spring Data JPA, garantindo alta performance nas consultas.
- ***Conteinerização:*** O projeto está totalmente conteinerizado, permitindo que toda a infraestrutura (Banco de Dados, Motor de IA e API Rest) suba de forma coordenada.  

---  
# 9. 🐳 Como baixar o projeto e executar via Docker.
Este guia orienta como baixar e rodar toda a infraestrutura (Banco de dados, Motor de IA, API Rest e frontend) utilizando Docker.
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
├── frontend-react/
│   ├── src /
|   ├── .dockerignore
|   ├── Dockerfile
│   ├── package.json
│   └── vite.config.js
│   
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
* Abra o terminal na pasta raiz do projeto (onde está o arquivo docker-compose.yml) e execute:
```
docker-compose up --build -d
```
* Vai subir todo o ecossistema (Front, Back, Motor de IA e Banco)
* O Docker irá baixar as imagens do PostgreSQL, configurar o ambiente Python, compilar a API Java, subir o Frontend React e orquestrar a comunicação entre eles.
## 9.5. Acesso ao Sistema  
Assim que os logs indicarem que os serviços estão ativos um usuario padrão é salvo no banco de dados com senha criptografada, e então você poderá acessar:
### 9.5.1. Documentação Swagger (Para Testes): http://localhost:8080/swagger-ui.html      
Username:  _admin_  
Password:  _123456_ 

### 9.5.2. Frontend React: http://localhost:5173/
* Dashboard inteligente para monitoramento de sentimentos em tempo real, intuitivo e moderno.
  * Depois de entrar basta escolher "Enviar feedback" no menu do Cliente.
  * Para ver os comentarios salvos no banco, estatisticas, gerar relatorios e analisar lotes de comentários
  escolha "Menu Empresa".
    * usuario: _admin_
    * senha: _123456_

---

## 🔒 Nota sobre a Estrutura e Segurança
* Integridade: Ao baixar via ZIP ou Git, você receberá a estrutura de diretórios completa, necessária para a construção das imagens Docker.
* Seguindo boas práticas de desenvolvimento, o arquivo .env está incluído no .gitignore para evitar a exposição de credenciais sensíveis no repositório.  
O uso do .env.example permite que novos desenvolvedores configurem seu ambiente local de forma rápida e segura.

---
# 10. Integração Instagram/Facebook Graph API (Webhook + ngrok) 
Esta integração permite que o sistema receba feedbacks diretamente do Instagram Direct e Facebook em tempo real.  
O feedback chega no Java, é enviado para a api flask fazer a classificação, o resultado retorna ao Java que salva no banco de dados.

# Importante:
Para implementar a integração do Instagram/Facebook com este sistema, é necessária a configuração prévia no painel da Meta (Meta for Developers).  
Esse processo é mais complexo e envolve várias etapas como:  
* Ter uma conta de desenvolvedor Meta
* Criar um app no Meta for Developers
* Adicionar os produtos: Instagram Graph API e/ou Facebook Login
* Configurar permissões e escopos
* Gerar access tokens válidos
* Configurar webhooks no painel com o callback (ngrok)
* Testar eventos usando o botão de teste no dashboard  

Apesar de estar preparado para essa integração, essa configuração não é obrigatória para o uso básico do sistema.  

## 10.1. Configurando o Túnel (ngrok api gateway)  
O ngrok cria um túnel seguro que expõe o sistema em execução no ambiente local por meio de uma URL pública.  
Assim, a Meta pode enviar os dados para a aplicação.  

* Instruçoes para instalação:  
* Faça a inscrição no ngrok para receber seu free token
* Baixe e instale o ngrok  https://ngrok.com/download/windows  
* Autenticação: ngrok config add-authtoken SEU_TOKEN.
* Execução: ngrok http 8080  
* Vai gerar uma URL (https://xxxx.ngrok-free.dev).  
* Copie esta URL, posteriormente será usada na configuração do webhook na Meta developer.

## 10.2. Configuração no Painel da Meta  

* Instruções para configuração da Meta https://www.youtube.com/watch?v=BuF9g9_QC04
* Acesse https://developers.facebook.com e cadastre-se como desenvolvedor.
* No painel clique em Criar App, selecione o tipo certo (geralmente Business ou For Everything Else).  
* Adicione os produtos Instagram Graph API e Messenger (ou qualquer outro necessário, como Webhooks).
* Conectar sua página do Facebook e conta Instagram Business.  
Sua Página do Facebook deve estar vinculada a uma conta profissional do Instagram.  
As contas devem estar conectadas nos próprios settings do Facebook/Instagram.  
* Gerar tokens de acesso, usando a ferramenta Graph API Explorer ou a seção de Tokens do painel para gerar um token com as permissões necessárias (como instagram_manage_messages para receber mensagens).
* Configurar webhooks, vá para a seção de Webhooks do app no dashboard.
Insira a URL de callback do webhook (que será seu endpoint local exposto via ngrok, por exemplo https://<seu-ngrok>.ngrok-free.dev/webhook).
Defina um verify token e publique. Escolha os campos/assinaturas que você quer receber, como messages e messaging_postbacks para Messenger/Instagram. 
* Testar o webhook, no próprio dashboard você pode testar o webhook e ver se sua API local está recebendo os eventos de teste antes de testar com mensagens reais.
* Configurar os tokens da Meta no sistema java no arquivo ".env" da estrutura do docker e subir novamente o sistema.   
---
 
 ## 10.3. Simulação e Testes de Integração (Postman)  
 Como o Webhook depende de eventos externos da Meta, utilizamos o Postman para simular o envio de dados reais para o sistema.  
 Isso permite validar o processamento de IA e a persistência no banco de dados de forma controlada.  
 
### 10.3.1. Como executar os testes de configuração da URL 
 * No Postman, utilize a sua URL do ngrok: POST https://seu-link-ngrok.ngrok-free.dev/webhookHeaders   
 * Campos obrigatórios no postman:  
   * **Headers:**  
   Key: *Content-Type*  
   Value: *application/json*  
   Key: *ngrok-skip-browser-warning* (Necessário para túneis gratuitos do ngrok)  
   Value: *true*

 * Exemplos de Payloads para Teste:
 Simulação Instagram (Sentimento Positivo)

```json
{
  "object": "instagram",
  "entry": [{
    "changes": [{
      "field": "comments",
      "value": {
        "text": "O atendimento foi excelente, amei o produto!",
        "from": { "username": "cliente_teste" }
      }
    }]
  }]
}
```
Simulação Facebook (Sentimento Negativo)
```json

{
  "object": "page",
  "entry": [{
    "changes": [{
      "field": "feed",
      "value": {
        "message": "Odiei o produto, a entrega demorou demais!",
        "from": { "name": "Usuario Teste" }
      }
    }]
  }]
}
```
### 11.3.2 Resultados Esperados
Ao disparar os testes acima, você poderá observar em tempo real: 
* Logs do Java: O sistema exibindo a captura do texto e a chamada ao motor de IA.
* Logs do Flask: A inteligência processando a predição.
* Banco de Dados: O registro sendo criado no PostgreSQL com a classificação correta e as topFeatures.

---

## 🌐 CORS

Configurado para permitir acesso do frontend local:

```text
http://localhost:5173
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
✔ Segurança com Spring Security (JWT)  
✔ Integração Instagram Graph API  
✔ Frontend React  
✔ Testes automatizados (unitários e integração)

---

## 🚧 Recursos Opcionais / Próximos Passos
* Implementação do sistema na OCI Oracle
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
---


