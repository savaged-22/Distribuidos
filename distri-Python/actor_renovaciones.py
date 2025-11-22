import json
import uuid
import threading
import time
import pika

RABBIT_HOST = "localhost"
COLA_PRESTAMO = "prestamo"
COLA_DEVOLUCION = "devolucion"
COLA_RENOVACION = "renovacion"
COLA_RESPUESTAS = "respuestas_gc"

esperando_respuesta = {}

# ---------------------------------------------------------
# CONEXIÓN PARA PUBLICAR (solo para enviar)
# ---------------------------------------------------------
def get_publish_channel():
    conn = pika.BlockingConnection(pika.ConnectionParameters(RABBIT_HOST))
    ch = conn.channel()
    return conn, ch


# ---------------------------------------------------------
# HILO PARA ESCUCHAR RESPUESTAS  (usa OTRA conexión)
# ---------------------------------------------------------
def escuchar_respuestas():
    conn = pika.BlockingConnection(pika.ConnectionParameters(RABBIT_HOST))
    ch = conn.channel()

    ch.queue_declare(queue=COLA_RESPUESTAS)

    def callback(ch, method, properties, body):
        data = json.loads(body)
        corr = data.get("correlationId")
        print("[GC] Respuesta recibida:", data)

        if corr in esperando_respuesta:
            esperando_respuesta[corr] = data

    ch.basic_consume(
        queue=COLA_RESPUESTAS,
        on_message_callback=callback,
        auto_ack=True
    )

    print("[GC] Esperando respuestas de actores…")
    ch.start_consuming()


threading.Thread(target=escuchar_respuestas, daemon=True).start()


# ---------------------------------------------------------
# FUNCIÓN PARA PUBLICAR EVENTOS (usa la conexión de publish)
# ---------------------------------------------------------
def publicar_evento(cola, mensaje):
    correlation = str(uuid.uuid4())
    mensaje["correlationId"] = correlation

    esperando_respuesta[correlation] = None

    conn, ch = get_publish_channel()
    ch.basic_publish(
        exchange="",
        routing_key=cola,
        body=json.dumps(mensaje)
    )
    conn.close()

    print(f"[GC] Evento publicado en '{cola}':", mensaje)
    return correlation


# ---------------------------------------------------------
# FUNCIÓN PARA ESPERAR RESPUESTA DEL ACTOR
# ---------------------------------------------------------
def esperar_respuesta(correlation, timeout=10):
    inicio = time.time()

    while time.time() - inicio < timeout:
        if esperando_respuesta.get(correlation) is not None:
            return esperando_respuesta.pop(correlation)
        time.sleep(0.2)

    return {"status": "timeout", "correlationId": correlation}


# ---------------------------------------------------------
# TEST MANUAL DEL GC
# ---------------------------------------------------------
if __name__ == "__main__":
    evento = {
        "tipo": "prestamo",
        "libroId": "LIB001",
        "usuarioId": "USR123",
    }

    corr = publicar_evento(COLA_PRESTAMO, evento)

    print("[GC] Esperando actualización en BD…")
    respuesta = esperar_respuesta(corr)

    print("[GC] Respuesta final:", respuesta)
