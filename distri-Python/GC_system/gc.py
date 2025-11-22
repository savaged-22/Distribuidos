import json
import uuid
import threading
import pika
import time
from fastapi import FastAPI
from fastapi.responses import JSONResponse

RABBIT_HOST = "localhost"
COLA_PRESTAMO = "prestamo"
COLA_DEVOLUCION = "devolucion"
COLA_RENOVACION = "renovacion"
COLA_RESPUESTAS = "respuestas_gc"

esperando_respuesta = {}

app = FastAPI(title="GC - Gestor de Carga")

connection = pika.BlockingConnection(pika.ConnectionParameters(RABBIT_HOST))
channel = connection.channel()

channel.queue_declare(queue=COLA_PRESTAMO)
channel.queue_declare(queue=COLA_DEVOLUCION)
channel.queue_declare(queue=COLA_RENOVACION)
channel.queue_declare(queue=COLA_RESPUESTAS)

def escuchar_respuestas():
    def callback(ch, method, properties, body):
        data = json.loads(body)
        corr = data.get("correlationId")

        print(f"[GC] Respuesta recibida del actor: {data}")

        if corr in esperando_respuesta:
            esperando_respuesta[corr] = data

    channel.basic_consume(
        queue=COLA_RESPUESTAS,
        on_message_callback=callback,
        auto_ack=True
    )
    print("[GC] Escuchando respuestas de actores…")
    channel.start_consuming()

threading.Thread(target=escuchar_respuestas, daemon=True).start()

def publicar(cola, mensaje):
    correlationId = str(uuid.uuid4())
    mensaje["correlationId"] = correlationId
    esperando_respuesta[correlationId] = None

    channel.basic_publish(
        exchange='',
        routing_key=cola,
        body=json.dumps(mensaje)
    )

    print(f"[GC] Publicado en {cola}: {mensaje}")
    return correlationId


def esperar_respuesta(correlationId, timeout=12):
    inicio = time.time()
    while time.time() - inicio < timeout:
        if esperando_respuesta.get(correlationId):
            return esperando_respuesta.pop(correlationId)
        time.sleep(0.2)
    return {"status": "timeout", "correlationId": correlationId}


@app.post("/api/prestamo")
def solicitar_prestamo(body: dict):
    corr = publicar(COLA_PRESTAMO, body)
    respuesta = esperar_respuesta(corr)
    return JSONResponse(content=respuesta)


@app.post("/api/devolucion")
def procesar_devolucion(body: dict):
    publicar(COLA_DEVOLUCION, body)
    return {"status": "OK", "mensaje": "Devolución enviada al actor"}


@app.post("/api/renovacion")
def procesar_renovacion(body: dict):
    publicar(COLA_RENOVACION, body)
    return {"status": "OK", "mensaje": "Renovación enviada al actor"}


