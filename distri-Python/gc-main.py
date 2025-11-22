import requests
import uuid
from datetime import datetime, timezone

BASE_URL = "http://0.0.0.0:8081"  # donde corre service-ga con perfil ga o ga-b


def listar_libros():
    url = f"{BASE_URL}/api/libros"
    resp = requests.get(url, timeout=5)
    print(f"\n[GET] {url} -> {resp.status_code}")
    print(resp.text)


def crear_prestamo(usuario_id: str, libro_id: str, sede: str):
    url = f"{BASE_URL}/api/commands/prestamo"

    now = datetime.now(timezone.utc).isoformat()

    cmd = {
        # OJO: el controlador recibe PrestamoCmd, y Jackson puede deserializar
        # sin que enviemos el campo "kind".
        "headers": {
            "correlationId": str(uuid.uuid4()),
            "idempotencyKey": f"PRESTAMO-{libro_id}-{usuario_id}-{int(datetime.now().timestamp())}",
            "usuarioId": usuario_id,
            "libroId": libro_id,
            "sedeOrigen": sede,          # "A" o "B"
            "timestamp": now,
            "schemaVersion": "1.0.0"
        }
    }

    resp = requests.post(url, json=cmd, timeout=5)
    print(f"\n[POST] {url} -> {resp.status_code}")
    print("Request JSON:")
    print(cmd)
    print("Response body:")
    print(resp.text)


if __name__ == "__main__":
    # 1) Ver libros
    listar_libros()

    # 2) Probar un préstamo
    # Ajusta estos valores a los que tienes en tu seed:
    #   - usuarioId: 'U001', 'U002', etc.
    #   - libroId  : '9780439785969', etc.
    crear_prestamo(usuario_id="U001", libro_id="9780439785969", sede="A")
