import java.util.*;

class DNSEntry {

    String domain;
    String ipAddress;
    long expiryTime;

    public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {

    private int capacity;

    // LRU Cache using LinkedHashMap
    private LinkedHashMap<String, DNSEntry> cache;

    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {

        this.capacity = capacity;

        cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {

            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {

        long start = System.nanoTime();

        if (cache.containsKey(domain)) {

            DNSEntry entry = cache.get(domain);

            if (!entry.isExpired()) {
                hits++;
                System.out.println("Cache HIT → " + entry.ipAddress);
                return entry.ipAddress;
            } else {
                cache.remove(domain);
                System.out.println("Cache EXPIRED");
            }
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        cache.put(domain, new DNSEntry(domain, ip, 5)); // TTL = 5 seconds for demo

        long end = System.nanoTime();

        System.out.println("Cache MISS → Query upstream → " + ip +
                " (" + ((end - start) / 1000000.0) + " ms)");

        return ip;
    }

    // Simulated upstream DNS query
    private String queryUpstreamDNS(String domain) {

        try {
            Thread.sleep(100); // simulate network delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Random r = new Random();

        return "172.217.14." + r.nextInt(255);
    }

    // Cache statistics
    public void getCacheStats() {

        int total = hits + misses;

        double hitRate = total == 0 ? 0 : (hits * 100.0) / total;

        System.out.println("\nCache Statistics:");
        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
    }

    // Background cleanup thread
    private void startCleanupThread() {

        Thread cleaner = new Thread(() -> {

            while (true) {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (this) {

                    Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                    while (it.hasNext()) {

                        Map.Entry<String, DNSEntry> entry = it.next();

                        if (entry.getValue().isExpired()) {
                            it.remove();
                        }
                    }
                }
            }

        });

        cleaner.setDaemon(true);
        cleaner.start();
    }
}

public class DNSCachewithTTL {

    public static void main(String[] args) throws InterruptedException {

        DNSCache dnsCache = new DNSCache(3);

        System.out.println("\nFirst lookup:");
        dnsCache.resolve("google.com");

        System.out.println("\nSecond lookup (should be HIT):");
        dnsCache.resolve("google.com");

        Thread.sleep(6000);

        System.out.println("\nAfter TTL expiry:");
        dnsCache.resolve("google.com");

        System.out.println("\nMore queries:");
        dnsCache.resolve("openai.com");
        dnsCache.resolve("github.com");
        dnsCache.resolve("stackoverflow.com"); // triggers LRU eviction

        dnsCache.getCacheStats();
    }
}