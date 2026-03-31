import http.server
import socketserver
import urllib.parse
import os
import redis
from datetime import datetime

PORT = 8080
REDIS_HOST = os.getenv("REDIS_HOST", "redis")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
CACHE_TTL_SECONDS = 60

class TimeHandler(http.server.BaseHTTPRequestHandler):
    redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)

    def do_GET(self):
        path = urllib.parse.urlparse(self.path).path
        if path in ("/rota1", "/rota2"):
            response = self.get_cached_response(path)
            self.send_response(200)
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(response.encode("utf-8"))))
            self.end_headers()
            self.wfile.write(response.encode("utf-8"))
        else:
            self.send_error(404, "Rota não encontrada")

    def get_cached_response(self, path):
        cache_key = f"time:{path}"
        response = self.redis_client.get(cache_key)
        if response is None:
            now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            response = f"Horário do servidor: {now}\n"
            self.redis_client.setex(cache_key, CACHE_TTL_SECONDS, response)
        return response

    def log_message(self, format, *args):
        pass

if __name__ == "__main__":
    with socketserver.TCPServer(("0.0.0.0", PORT), TimeHandler) as httpd:
        httpd.serve_forever()
