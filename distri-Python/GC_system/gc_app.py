import json
import uuid
import threading
import time

import pika
from fastapi import FastAPI
from fastapi.responses import JSONResponse

# -----------------------------------------------------------
# CONFIGURACIÓN
# -----------------------------------------------------------
RABBIT_HOST = "localhost"
COLA_PRESTAMO = "prestamo"
COLA_DEVOLUCION = "devolucion"
COLA_RENOVACION = "renovacion"
COLA_RESPUESTAS = "respuestas_gc"

# Diccionario para emparejar respuestas (corrId -> respuesta)
esperando_respuesta: dict[str, dict | None] = {}

# API REST
app = FastAPI(title="GC - Gestor de Carga")


# -----------------------------------------------------------
# CANAL SOLO PARA PUBLICAR (se crea y cierra por cada envío)
# -----------------------------------------------------------
def get_publish_channel():
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=RABBIT_HOST)
    )
    channel = connection.channel()

    # Aseguramos que las colas existen
    channel.queue_declare(queue=COLA_PRESTAMO, durable=False)
    channel.queue_declare(queue=COLA_DEVOLUCION, durable=False)
    channel.queue_declare(queue=COLA_RENOVACION, durable=False)
    channel.queue_declare(queue=COLA_RESPUESTAS, durable=False)

    return connection, channel


# -----------------------------------------------------------
# HILO PARA ESCUCHAR RESPUESTAS (con SU propia conexión)
# -----------------------------------------------------------
def escuchar_respuestas():
    while True:
        try:
            connection = pika.BlockingConnection(
                pika.ConnectionParameters(host=RABBIT_HOST)
            )
            channel = connection.channel()
            channel.queue_declare(queue=COLA_RESPUESTAS, durable=False)

            def callback(ch, method, properties, body):
                try:
                    data = json.loads(body)
                except Exception:
                    print("[GC] Respuesta inválida del actor:", body)
                    return

                corr = data.get("correlationId")
                print("[GC] Respuesta de actor:", data)

                if corr in esperando_respuesta:
                    esperando_respuesta[corr] = data

            channel.basic_consume(
                queue=COLA_RESPUESTAS,
                on_message_callback=callback,
                auto_ack=True,
            )

            print("[GC] Escuchando respuestas en cola:", COLA_RESPUESTAS)
            channel.start_consuming()
        except Exception as exc:
            print(f"[GC] Error escuchando respuestas: {exc}. Reintentando en 5 segundos...")
            time.sleep(5)


# Lanzamos el hilo en el evento de startup de FastAPI
@app.on_event("startup")
def start_respuesta_listener():
    t = threading.Thread(target=escuchar_respuestas, daemon=True)
    t.start()
    print("[GC] Hilo de escucha de respuestas iniciado")


# -----------------------------------------------------------
# PUBLICAR EVENTOS
# -----------------------------------------------------------
def publicar(cola: str, mensaje: dict) -> str:
    correlation_id = str(uuid.uuid4())
    mensaje["correlationId"] = correlation_id
    esperando_respuesta[correlation_id] = None

    connection, channel = get_publish_channel()
    try:
        channel.basic_publish(
            exchange="",
            routing_key=cola,
            body=json.dumps(mensaje),
        )
        print(f"[GC] Publicado en {cola}: {mensaje}")
    finally:
        connection.close()

    return correlation_id


# -----------------------------------------------------------
# ESPERAR RESPUESTA DEL ACTOR
# -----------------------------------------------------------
def esperar_respuesta(correlation_id: str, timeout: int = 12) -> dict:
    inicio = time.time()
    while time.time() - inicio < timeout:
        respuesta = esperando_respuesta.get(correlation_id)
        if respuesta is not None:
            esperando_respuesta.pop(correlation_id, None)
            return respuesta
        time.sleep(0.2)

    esperando_respuesta.pop(correlation_id, None)
    return {"status": "timeout", "correlationId": correlation_id}


# -----------------------------------------------------------
# ENDPOINTS FASTAPI
# -----------------------------------------------------------
@app.post("/api/prestamo")
def solicitar_prestamo(body: dict):
    correlation_id = publicar(COLA_PRESTAMO, body)
    respuesta = esperar_respuesta(correlation_id)
    return JSONResponse(content=respuesta)


@app.post("/api/devolucion")
def procesar_devolucion(body: dict):
    publicar(COLA_DEVOLUCION, body)
    return {"status": "OK", "mensaje": "Devolución enviada al actor"}


@app.post("/api/renovacion")
def procesar_renovacion(body: dict):
    publicar(COLA_RENOVACION, body)
    return {"status": "OK", "mensaje": "Renovación enviada al actor"}
