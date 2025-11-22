import pika
import json
import requests

GA_URL = "http://0.0.0.0:8081"

connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

channel.queue_declare(queue="devolucion")

def procesar_devolucion(ch, method, props, body):
    data = json.loads(body)
    print("[ActorDevolucion] Devolución recibida:", data)
    requests.post(f"{GA_URL}/api/devolucion", json=data)

channel.basic_consume(
    queue="devolucion",
    on_message_callback=procesar_devolucion,
    auto_ack=True
)

print("[ActorDevolucion] Escuchando devoluciones…")
channel.start_consuming()
