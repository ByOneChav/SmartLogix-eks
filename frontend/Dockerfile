# ===================== ETAPA 1: BUILD =====================
# Imagen liviana de Node.js 20. "AS build" le da nombre a esta etapa
FROM node:20-alpine AS build

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copia SOLO package.json/package-lock.json primero
# (truco de cache de Docker: si no cambian, reutiliza la capa de "npm install"
# en builds futuros, más rápido)
COPY package*.json .

# Instala dependencias de Angular
RUN npm install

# Recién ahora copia el resto del código fuente
COPY . .

# Compila Angular en modo producción
# -> genera dist/smartlogix-frontend/browser (HTML/JS/CSS estáticos y minificados)
RUN npm run build

# ===================== ETAPA 2: NGINX =====================
# Imagen NUEVA y liviana, solo nginx
# (descarta toda la etapa "build": node_modules, código fuente, etc.
#  -> la imagen final no carga ese peso)
FROM nginx:alpine

# Copia SOLO los archivos compilados desde la etapa "build"
# a la carpeta donde nginx sirve contenido estático
COPY --from=build /app/dist/smartlogix-frontend/browser /usr/share/nginx/html

# Copia "nginx.conf" (NO "nginx-k8s.conf") como configuración POR DEFECTO
# de nginx en la imagen. En EKS, el ConfigMap "frontend-nginx-config"
# sobrescribe este mismo archivo en runtime con el contenido de "nginx-k8s.conf"
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Documenta que el contenedor escucha en el puerto 80 (informativo, no abre el puerto)
EXPOSE 80

# Arranca nginx en foreground (necesario para que el contenedor no termine apenas inicia)
CMD ["nginx", "-g", "daemon off;"]