# 🩸 GlucoAPI — API REST de monitoreo de glucosa

API REST en **Java 21 + Jakarta REST (Jersey 3.1) + MySQL**, empaquetada como
WAR y lista para desplegarse en **Render** con Docker.

- Landing page: `/`
- API: `/api/glucosa/...`

## 🌐 Despliegue actual

| Recurso | URL |
|---------|-----|
| Landing page | https://glucoapi-mykechvz.onrender.com/ |
| Todas las medidas | https://glucoapi-mykechvz.onrender.com/api/glucosa/getAllMedidas |
| Medidas del usuario 1 | https://glucoapi-mykechvz.onrender.com/api/glucosa/getMedidasByUsuarioId?idUsuario=1 |
| Todos los usuarios | https://glucoapi-mykechvz.onrender.com/api/glucosa/getAllUsuarios |

- Hosting: **Render** (Docker, plan Free, región Oregon).
- Base de datos: **TiDB Cloud Starter** (MySQL compatible), base `gluco_api`.
- Repositorio: https://github.com/mykechvz/API_HealthBlood_512

---

## 📌 Endpoints

Base local (Tomcat con el WAR en la raíz): `http://localhost:8090/api/glucosa`
Base en Render: `https://glucoapi-mykechvz.onrender.com/api/glucosa`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/getAllMedidas` | Todas las medidas de glucosa |
| GET | `/getMedidasByUsuarioId?idUsuario=1` | Medidas de un usuario |
| GET | `/getAllUsuarios` | Todos los usuarios |
| POST | `/insertarMedida` | Inserta una medida (formulario o JSON) |
| POST | `/insertUsuario` | Inserta un usuario (formulario) |

### Ejemplos

```bash
curl "https://glucoapi-mykechvz.onrender.com/api/glucosa/getAllMedidas"

curl "https://glucoapi-mykechvz.onrender.com/api/glucosa/getMedidasByUsuarioId?idUsuario=1"

curl "https://glucoapi-mykechvz.onrender.com/api/glucosa/getAllUsuarios"
```

Respuesta de `getMedidasByUsuarioId?idUsuario=1`:

```json
[
  {
    "idMedida": 1,
    "idUsuario": 1,
    "glucosa": 95,
    "fechaHora": "2026-11-07 20:41:09",
    "contexto": "Ayunas",
    "notas": "Medicion matutina de prueba"
  }
]
```

Insertar una medida:

```bash
curl -X POST "https://glucoapi-mykechvz.onrender.com/api/glucosa/insertarMedida" \
  -H "Content-Type: application/json" \
  -d '{"idUsuario":1,"glucosa":115,"contexto":"Ayunas","notas":"Prueba"}'
```

Errores habituales:

- `400` → falta `idUsuario`, no es numérico, o el usuario no existe.
- `500` → no se pudo conectar a la base de datos.

---

## 🗄️ Base de datos

El esquema y los datos de prueba están en [`schema.sql`](schema.sql).

```bash
mysql -u root -p < schema.sql
```

Tablas:

- `usuario(idUsuario, nombre, primerApellido, segundoApellido, email)`
- `medida(idMedida, idUsuario, glucosa, fechaHora, contexto, notas)`

La conexión se configura con variables de entorno (si no existen, usa
`127.0.0.1:3306` / `root` / `root` / `gluco_api`):

| Variable | Ejemplo | Obligatoria |
|----------|---------|-------------|
| `DB_HOST` | `mysql-xxxx.aivencloud.com` | Sí |
| `DB_PORT` | `3306` | No (3306) |
| `DB_NAME` | `gluco_api` | No (`gluco_api`) |
| `DB_USER` | `avnadmin` | Sí |
| `DB_PASSWORD` | `********` | Sí |
| `DB_URL` | `jdbc:mysql://...` | No (tiene prioridad) |
| `DB_SSL` | `REQUIRED` | No (`REQUIRED` en la nube) |

---

## 💻 Ejecutar en local

### Opción A: NetBeans + Tomcat

1. Abre el proyecto en NetBeans.
2. Configura Tomcat 11 y ejecuta el proyecto.
3. URL: `http://localhost:8080/API_HealthBlood_512/api/glucosa/getAllMedidas`

### Opción B: Maven + Tomcat

```bash
mvn clean package
copy target\API_HealthBlood_512.war C:\apache-tomcat-11.0.27\webapps\ROOT.war
# inicia Tomcat y abre http://localhost:8080/
```

### Opción C: Docker (igual que Render)

```bash
docker build -t glucoapi .
docker run --rm -p 8080:8080 \
  -e DB_HOST=host.docker.internal -e DB_PORT=3306 \
  -e DB_NAME=gluco_api -e DB_USER=root -e DB_PASSWORD=root \
  -e DB_SSL=DISABLED \
  glucoapi
```

---

## ☁️ Desplegar en Render

### 1. MySQL en la nube (gratis)

Render no ofrece MySQL nativo. Usa uno de estos:

- **TiDB Cloud Serverless** (recomendado): MySQL compatible, capa gratuita.
- **Aiven for MySQL**: capa gratuita de 1 GB.
- **Clever Cloud**: MySQL gratuito/pequeño.

Crea la base `gluco_api`, ejecuta `schema.sql` y anota host, puerto, usuario y
contraseña.

### 2. Subir el código a GitHub

```bash
git init
git add .
git commit -m "feat: endpoints de medidas y configuracion para Render"
gh repo create API_HealthBlood_512 --public --source . --push
```

### 3. Crear el Web Service en Render

1. Entra a <https://dashboard.render.com> → **New +** → **Web Service**.
2. Conecta el repositorio de GitHub.
3. **Runtime**: `Docker` (Render detecta el `Dockerfile`).
4. **Instance Type**: `Free`.
5. **Health Check Path**: `/api/glucosa/getAllMedidas` (opcional).
6. Agrega las variables de entorno `DB_HOST`, `DB_PORT`, `DB_NAME`,
   `DB_USER`, `DB_PASSWORD` y `DB_SSL=REQUIRED`.
7. **Create Web Service**.

Render construye la imagen y publica:

```
https://glucoapi-mykechvz.onrender.com/
https://glucoapi-mykechvz.onrender.com/api/glucosa/getAllMedidas
https://glucoapi-mykechvz.onrender.com/api/glucosa/getMedidasByUsuarioId?idUsuario=1
https://glucoapi-mykechvz.onrender.com/api/glucosa/getAllUsuarios
```

> Nota: en el plan gratuito el servicio se suspende tras 15 minutos sin
> tráfico; la primera petición puede tardar ~50 segundos en responder.

---

## 🧪 Pruebas rápidas

```bash
curl -i "http://localhost:8090/"                                   # landing 200
curl -i "http://localhost:8090/api/glucosa/getAllMedidas"          # 200 + JSON
curl -i "http://localhost:8090/api/glucosa/getMedidasByUsuarioId?idUsuario=1"
curl -i "http://localhost:8090/api/glucosa/getAllUsuarios"
curl -i "http://localhost:8090/api/glucosa/getMedidasByUsuarioId"  # 400
```
