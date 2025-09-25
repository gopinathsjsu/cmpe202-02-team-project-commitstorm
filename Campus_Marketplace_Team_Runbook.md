# Campus Marketplace — Team Runbook (Local Development)

A short, copy‑paste guide your teammates can follow to get the backend running on their laptops.

---

## 1) Prerequisites
- **Docker Desktop** (macOS/Windows) or Docker Engine (Linux)
- **Java 17** (`java -version` should show 17.x)
- **Maven 3.9+** (`mvn -v`)
- **Git**

> Check everything:
```bash
java -version
mvn -v
docker --version
docker compose version
```

---

## 2) Clone & configure
```bash
git clone <your-repo-url>
cd cmpe202-02-team-project-commitstorm/backend
```
Create `.env` (same folder as `start.sh`). You can copy our sample and tweak:
```bash
cp .env.sample .env   # if the file exists in repo (otherwise create it by hand)
```
**.env**
```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/campusMarket?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SERVER_PORT=8080
START_DB=true   # start Docker MySQL automatically from the script
```

> Keep `.env` out of git (already in `.gitignore`).

---

## 3) Start database services (Docker)
From the `backend` folder:
```bash
docker compose up -d
```
This starts:
- **MySQL 8.0** on `localhost:3306`
- **Adminer** on `http://localhost:8081`

Open Adminer → Login:
- **System:** MySQL  
- **Server:** `mysql`  (service name inside Docker)  
- **Username:** `root`  
- **Password:** `root`  (or whatever you set in `.env`/compose)  
- **Database:** `campusMarket`

> The database is created by Compose on first run. Empty DB is expected—Flyway will create tables.

---

## 4) Run the backend
Using our script (recommended):
```bash
chmod +x start.sh
./start.sh
```
What it does:
- Loads **.env**
- Ensures Docker DB is up (`docker compose up -d` if `START_DB=true`)
- Waits for MySQL port
- Builds the app (`mvn -DskipTests clean package`)
- Runs it (`mvn spring-boot:run`)

**Windows note:** use **Git Bash** to run `start.sh`, or run commands manually:
```powershell
mvn -DskipTests clean package
mvn spring-boot:run
```

---

## 5) Verify it works
- Health check: `http://localhost:8080/health` → `{ "status": "ok" }`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

On first startup you should see Flyway logs like:
```
Successfully applied 1 migration (V1__init_mysql.sql)
```
Tables will then appear in Adminer under `campusMarket`.

---

## 6) (Optional) seed sample data
Use Adminer → select DB → **SQL** tab → paste and Execute:
```sql
-- categories
INSERT INTO categories (id, name) VALUES
  ('11111111-1111-1111-1111-111111111111','Books'),
  ('22222222-2222-2222-2222-222222222222','Electronics');

-- users
INSERT INTO users (id, name, email, role, status, created_at, updated_at) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa','Demo Seller','seller@univ.edu','seller','active', NOW(), NOW()),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb','Demo Buyer','buyer@univ.edu','buyer','active', NOW(), NOW());

-- one listing
INSERT INTO listings (id, seller_id, title, description, price, category_id, `condition`, images, status, created_at, updated_at) VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc',
   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
   'CS 202 Textbook', 'Lightly used textbook', 45.00,
   '11111111-1111-1111-1111-111111111111',
   'barely_used', JSON_ARRAY('https://example.com/img1.jpg'), 'active', NOW(), NOW());
```

Then try endpoints in Swagger (`/categories`, `/listings`, etc.).

---

## 7) Postman (optional)
- Import the provided Postman collection (if present in the repo) or create a new one.
- Set `{{baseUrl}} = http://localhost:8080/api/v1`.

---

## 8) Troubleshooting (quick)
**A) Port 3306 in use** (native MySQL/MariaDB running):  
- Change Compose mapping to `"3307:3306"` and set `SPRING_DATASOURCE_URL` to port `3307`, **or** stop local DB (macOS Homebrew: `brew services stop mysql` / `mariadb`).

**B) Docker not running**: Start Docker Desktop; verify with `docker info`.

**C) App port 8080 busy**:
```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
kill -15 <PID>  # then retry
# or set SERVER_PORT=8082 in .env
```

**D) App can’t connect to DB**:
- Confirm containers: `docker ps`
- Test login: `docker exec -it campus_mysql mysql -uroot -proot -e "SHOW DATABASES;"`
- Ensure `SPRING_DATASOURCE_URL` host/port match your mapping.

**E) Access denied**:
- Make sure `.env` username/password match MySQL root (or create an app user and grant privileges).

**F) Flyway errors**:
- Ensure migration files are under `src/main/resources/db/migration/` (e.g., `V1__init_mysql.sql`).
- If you see “Unsupported Database: MySQL 8.0”, ensure `flyway-mysql` dependency exists in `pom.xml` alongside `flyway-core` (version aligned, e.g., 10.10.0).

---

## 9) Stop & reset
```bash
# stop app: Ctrl+C in the terminal running it

# stop DB containers (data persists)
docker compose down

# stop and DELETE data volume (fresh DB)
docker compose down -v
```

---

## 10) Notes for running without Docker (optional)
If a teammate already has MySQL 8 locally:
- Create DB `campusMarket` and user/password.
- Point `SPRING_DATASOURCE_URL` to `jdbc:mysql://localhost:<their_port>/campusMarket?...` and set username/password.
- Do **not** run `docker compose up`.

---

### That’s it!
If anyone gets stuck, share the output of:
```
docker ps
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -nP -iTCP:3306 -sTCP:LISTEN
```
And the last 30 lines of the app log.
