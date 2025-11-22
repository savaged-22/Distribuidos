import pika
import json
import requests
import time

GA_URL = "http://0.0.0.0:8081"

connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

channel.queue_declare(queue="prestamo")
channel.queue_declare(queue="respuestas_gc")

def procesar_prestamo(ch, method, props, body):
    data = json.loads(body)
    corr = data["correlationId"]

    print("[ActorPréstamos] Evento recibido:", data)

    # 1. Enviar al GA para actualizar BD
    resp = requests.post(f"{GA_URL}/api/prestamo", json=data)

    respuesta = {
        "correlationId": corr,
        "status": resp.status_code,
        "body": resp.json() if resp.status_code == 200 else resp.text
    }

    # 2. Enviar respuesta al GC
    channel.basic_publish(
        exchange='',
        routing_key="respuestas_gc",
        body=json.dumps(respuesta)
    )

    print("[ActorPréstamos] Respuesta enviada al GC:", respuesta)


channel.basic_consume(
    queue="prestamo",
    on_message_callback=procesar_prestamo,
    auto_ack=True
)

print("[ActorPréstamos] Esperando eventos...")
channel.start_consuming()
