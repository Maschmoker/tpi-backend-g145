#!/usr/bin/env bash
# Crea las bases de datos y usuarios de cada microservicio más la base de Keycloak.
# Se ejecuta automáticamente por la imagen oficial de PostgreSQL solo cuando el
# volumen de datos está vacío (/docker-entrypoint-initdb.d).
set -euo pipefail

PSQL=(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres)

create_db_and_user() {
  local db="$1" user="$2" pass="$3"

  if [ -z "$db" ] || [ -z "$user" ] || [ -z "$pass" ]; then
    echo "ERROR: faltan variables de entorno para crear la base '$db'" >&2
    exit 1
  fi

  if [ "$("${PSQL[@]}" -tAc "SELECT 1 FROM pg_roles WHERE rolname = '$user'")" != "1" ]; then
    echo "Creando rol '$user'..."
    "${PSQL[@]}" -v user="$user" -v pass="$pass" <<'SQL'
CREATE ROLE :"user" LOGIN PASSWORD :'pass';
SQL
  else
    echo "El rol '$user' ya existe, se omite."
  fi

  if [ "$("${PSQL[@]}" -tAc "SELECT 1 FROM pg_database WHERE datname = '$db'")" != "1" ]; then
    echo "Creando base '$db'..."
    "${PSQL[@]}" -v db="$db" -v user="$user" <<'SQL'
CREATE DATABASE :"db" OWNER :"user";
SQL
  else
    echo "La base '$db' ya existe, se omite la creación."
  fi

  # Aislamiento entre servicios: solo el dueño puede conectarse.
  "${PSQL[@]}" -v db="$db" -v user="$user" <<'SQL'
REVOKE ALL ON DATABASE :"db" FROM PUBLIC;
GRANT CONNECT, TEMPORARY ON DATABASE :"db" TO :"user";
ALTER DATABASE :"db" OWNER TO :"user";
SQL
}

create_db_and_user "$CLIENTES_DB" "$CLIENTES_DB_USER" "$CLIENTES_DB_PASSWORD"
create_db_and_user "$ESTADIAS_DB" "$ESTADIAS_DB_USER" "$ESTADIAS_DB_PASSWORD"
create_db_and_user "$LOGISTICA_DB" "$LOGISTICA_DB_USER" "$LOGISTICA_DB_PASSWORD"
create_db_and_user "$KEYCLOAK_DB" "$KEYCLOAK_DB_USER" "$KEYCLOAK_DB_PASSWORD"

echo "Inicialización finalizada: $CLIENTES_DB, $ESTADIAS_DB, $LOGISTICA_DB, $KEYCLOAK_DB"
