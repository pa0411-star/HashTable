import java.util.concurrent.*;
import java.util.*;

class TokenBucket {

    private int maxTokens;
    private double refillRate; // tokens per second
    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.nanoTime();
    }

    // refill tokens based on elapsed time
    private synchronized void refill() {

        long now = System.nanoTime();

        double secondsPassed = (now - lastRefillTime) / 1_000_000_000.0;

        double tokensToAdd = secondsPassed * refillRate;

        tokens = Math.min(maxTokens, tokens + tokensToAdd);

        lastRefillTime = now;
    }

    // attempt to consume a token
    public synchronized boolean allowRequest() {

        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }

        return false;
    }

    public synchronized int remainingTokens() {
        return (int) tokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}

class RateLimiter {

    // clientId -> token bucket
    private ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    private static final int LIMIT = 1000;
    private static final double REFILL_RATE = LIMIT / 3600.0; // tokens per second

    public String checkRateLimit(String clientId) {

        TokenBucket bucket = clients.computeIfAbsent(
                clientId,
                id -> new TokenBucket(LIMIT, REFILL_RATE)
        );

        boolean allowed = bucket.allowRequest();

        if (allowed) {

            return "Allowed (" +
                    bucket.remainingTokens() +
                    " requests remaining)";
        } else {

            return "Denied (0 requests remaining, retry later)";
        }
    }

    public void getRateLimitStatus(String clientId) {

        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) {
            System.out.println("Client not found");
            return;
        }

        int remaining = bucket.remainingTokens();

        int used = bucket.getMaxTokens() - remaining;

        System.out.println("{used: " + used +
                ", limit: " + bucket.getMaxTokens() +
                ", remaining: " + remaining + "}");
    }
}

public class DistributedRateLimiterforAPIGateway {

    public static void main(String[] args) {

        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        // simulate requests
        for (int i = 0; i < 10; i++) {

            String result = limiter.checkRateLimit(client);

            System.out.println(result);
        }

        limiter.getRateLimitStatus(client);
    }
}