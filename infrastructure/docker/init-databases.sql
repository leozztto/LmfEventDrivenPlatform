-- Cria um banco por serviço (o POSTGRES_DB do compose só cria um).
-- Idempotente: usado tanto pelo hook /docker-entrypoint-initdb.d (volume novo) quanto
-- pelo serviço db-init, que roda a cada `docker compose up`. O \gexec executa o comando
-- montado pelo SELECT só quando o banco ainda não existe.
SELECT 'CREATE DATABASE orderservice'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'orderservice')\gexec

SELECT 'CREATE DATABASE paymentservice'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'paymentservice')\gexec

SELECT 'CREATE DATABASE inventoryservice'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventoryservice')\gexec

SELECT 'CREATE DATABASE notificationservice'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notificationservice')\gexec

SELECT 'CREATE DATABASE fraudservice'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'fraudservice')\gexec
