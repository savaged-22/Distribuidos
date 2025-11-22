#!/usr/bin/env python3
import argparse
import datetime
import json
import uuid

import requests

# ==========================
# CONFIGURACIÓN BÁSICA
# ==========================
# Cambia esto según dónde esté corriendo el GA
# Ejemplos:
#  - Sede A local: http://localhost:8080
#  - Sede B en otra máquina: http://192.168.103.168:8080
URL = "http://10.43.102.101:9030"  # CORRECCIÓN 1: Eliminado 'global'


# ==========================
# HELPERS HTTP
# ==========================
def _request(method: str, path: str, **kwargs):
    """Wrapper simple para hacer requests contra GA."""
    # Se lee la variable global URL aquí.
    # 'url' es una variable local en esta función, no necesita 'global'.
    url = f"{URL}{path}"
    resp = requests.request(method, url, timeout=10, **kwargs)
    try:
        body = resp.json()
    except ValueError:
        body = resp.text

    print(f"\n>>> {method} {url}")
    print(f"<<< HTTP {resp.status_code}")
    print("<<< Body:")
    print(json.dumps(body, indent=2, ensure_ascii=False) if isinstance(body, dict) or isinstance(body, list) else body)
    print()
    resp.raise_for_status()
    return body


# ==========================
# ENDPOINTS LIBROS
# ==========================
def listar_libros():
    """
    Simula: GET /api/libros
    """
    return _request("GET", "/api/libros")


def obtener_libro(libro_id: str):
    """
    Simula: GET /api/libros/{id}
    """
    return _request("GET", f"/api/libros/{libro_id}")


# ==========================
# CONSTRUCCIÓN DEL PrestamoCmd
# ==========================
def build_prestamo_cmd(usuario_id: str, libro_id: str, sede_origen: str = "A") -> dict:
    """
    Construye el JSON del comando PrestamoCmd alineado con los contracts de GA.

    Estructura esperada (Jackson):
    {
      "kind": "PrestamoCmd",
      "headers": {
        "correlationId": "...",
        "idempotencyKey": "...",
        "usuarioId": "...",
        "libroId": "...",
        "sedeOrigen": "A" | "B",
        "timestamp": "2025-11-21T20:00:00Z",
        "schemaVersion": "1.0.0"
      }
    }
    """
    now = datetime.datetime.now(datetime.timezone.utc)

    correlation_id = str(uuid.uuid4())
    # Puedes ajustar la estrategia de idempotencyKey (por ejemplo, por usuario+libro+fecha)
    idempotency_key = f"LOAN-{libro_id}-{usuario_id}-{now.date().isoformat()}"

    headers = {
        "correlationId": correlation_id,
        "idempotencyKey": idempotency_key,
        "usuarioId": usuario_id,
        "libroId": libro_id,
        "sedeOrigen": sede_origen,  # "A" o "B"
        # Instant ISO-8601 con "Z" al final para UTC
        "timestamp": now.isoformat().replace("+00:00", "Z"),
        # Debe coincidir con ContractsVersion.CURRENT
        "schemaVersion": "1.0.0",
    }

    cmd = {
        "kind": "PrestamoCmd",
        "headers": headers,
    }
    return cmd


# ==========================
# ENDPOINT PRESTAMO (COMMAND)
# ==========================
def crear_prestamo(usuario_id: str, libro_id: str, sede_origen: str = "A"):
    """
    Simula: POST /api/commands/prestamo
    Body: PrestamoCmd (JSON)
    """
    cmd = build_prestamo_cmd(usuario_id, libro_id, sede_origen)
    return _request("POST", "/api/commands/prestamo", json=cmd)


# ==========================
# CLI (simulador Gestor de Carga)
# ==========================
def main():
    # CORRECCIÓN DE PYTHON 3.10: 
    # Usamos una constante local para el valor por defecto 
    # para evitar la lectura temprana de la variable global URL.
    DEFAULT_BASE_URL = "http://localhost:9020" 
    
    parser = argparse.ArgumentParser(
        description="Simulador de Gestor de Carga (GC) para consumir el Gestor de Archivos (GA)."
    )
    parser.add_argument(
        "--base-url",
        default=DEFAULT_BASE_URL,
        help=f
    )