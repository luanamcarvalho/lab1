import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Main {
    private static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST", "redis");
    private static final int REDIS_PORT = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
    private static JedisPool jedisPool;

    public static void main(String[] args) throws IOException {
        jedisPool = new JedisPool(new JedisPoolConfig(), REDIS_HOST, REDIS_PORT);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/rota1", new CachedHandler("Hello world 1", 10, "rota1"));
        server.createContext("/rota2", new CachedHandler("Hello world 2", 10, "rota2"));
        server.setExecutor(null);
        server.start();
    }

    private static class CachedHandler implements HttpHandler {
        private String responseText;
        private int ttlSeconds;
        private String cacheKey;

        CachedHandler(String responseText, int ttlSeconds, String cacheKey) {
            this.responseText = responseText;
            this.ttlSeconds = ttlSeconds;
            this.cacheKey = cacheKey;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] bytes = getCachedBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private byte[] getCachedBytes() {
            try (Jedis jedis = jedisPool.getResource()) {
                String cached = jedis.get(cacheKey);
                if (cached == null) {
                    jedis.setex(cacheKey, ttlSeconds, responseText);
                    cached = responseText;
                }
                return cached.getBytes(StandardCharsets.UTF_8);
            }
        }
    }
}
