# ===== Build =====
FROM node:18-alpine AS build
WORKDIR /app

# ===== Injeção da variável de ambiente =====
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ===== Runtime =====
FROM nginx:alpine

# remove config padrão do nginx
RUN rm /etc/nginx/conf.d/default.conf

# adiciona config SPA
COPY nginx.conf /etc/nginx/conf.d

# copia build do Vite
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
