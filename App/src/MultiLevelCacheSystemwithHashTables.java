import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

// L1 Cache: In-memory with LRU
class L1Cache {

    private final int capacity;
    private LinkedHashMap<String, VideoData> map;

    public int hits = 0;
    public int misses = 0;
    public long totalAccessTime = 0;

    public L1Cache(int capacity) {
        this.capacity = capacity;
        // access-order LinkedHashMap for LRU
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1Cache.this.capacity;
            }
        };
    }

    public VideoData get(String videoId) {
        long start = System.nanoTime();
        VideoData data = map.get(videoId);
        long duration = System.nanoTime() - start;
        totalAccessTime += duration;
        if (data != null) hits++; else misses++;
        return data;
    }

    public void put(String videoId, VideoData data) {
        map.put(videoId, data);
    }

    public double avgAccessTimeMs() {
        return totalAccessTime / 1_000_000.0 / (hits + misses);
    }

    public double hitRate() {
        return hits * 100.0 / (hits + misses);
    }
}

// L2 Cache: SSD-backed simulation
class L2Cache {

    private final int capacity;
    private LinkedHashMap<String, String> map; // videoId -> filePath
    public HashMap<String, Integer> accessCount = new HashMap<>();

    public int hits = 0;
    public int misses = 0;
    public long totalAccessTime = 0;

    public L2Cache(int capacity) {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L2Cache.this.capacity;
            }
        };
    }

    // get video from L2
    public VideoData get(String videoId) {
        long start = System.nanoTime();
        String path = map.get(videoId);
        long duration = System.nanoTime() - start + 5_000_000; // 5ms simulated
        totalAccessTime += duration;

        if (path != null) {
            hits++;
            accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
            // simulate reading file from SSD
            return new VideoData(videoId, "Content from SSD: " + path);
        } else {
            misses++;
            return null;
        }
    }

    public void put(String videoId, String filePath) {
        map.put(videoId, filePath);
        accessCount.put(videoId, 1);
    }

    public double avgAccessTimeMs() {
        return totalAccessTime / 1_000_000.0 / (hits + misses);
    }

    public double hitRate() {
        return hits * 100.0 / (hits + misses);
    }
}

// L3 Cache: Database simulation
class L3Database {

    private HashMap<String, VideoData> db = new HashMap<>();
    public int hits = 0;
    public int misses = 0;
    public long totalAccessTime = 0;

    public void addVideo(String videoId, String content) {
        db.put(videoId, new VideoData(videoId, content));
    }

    public VideoData get(String videoId) {
        long start = System.nanoTime();
        VideoData data = db.get(videoId);
        long duration = System.nanoTime() - start + 150_000_000; // 150ms simulated
        totalAccessTime += duration;

        if (data != null) hits++; else misses++;
        return data;
    }

    public double avgAccessTimeMs() {
        return totalAccessTime / 1_000_000.0 / (hits + misses);
    }

    public double hitRate() {
        return hits * 100.0 / (hits + misses);
    }
}

// Multi-Level Cache System
class MultiLevelCache {

    private L1Cache l1;
    private L2Cache l2;
    private L3Database l3;
    private final int promoteThreshold = 2; // access count threshold for promotion

    public MultiLevelCache(L1Cache l1, L2Cache l2, L3Database l3) {
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
    }

    public VideoData getVideo(String videoId) {

        // check L1
        VideoData data = l1.get(videoId);
        if (data != null) {
            System.out.println("L1 Cache HIT (0.5ms)");
            return data;
        } else {
            System.out.println("L1 Cache MISS");
        }

        // check L2
        data = l2.get(videoId);
        if (data != null) {
            System.out.println("L2 Cache HIT (5ms)");
            // promote if access count > threshold
            if (l2.accessCount.get(videoId) >= promoteThreshold) {
                l1.put(videoId, data);
                System.out.println("Promoted to L1");
            }
            return data;
        } else {
            System.out.println("L2 Cache MISS");
        }

        // check L3
        data = l3.get(videoId);
        if (data != null) {
            System.out.println("L3 Database HIT (150ms)");
            // add to L2
            l2.put(videoId, "ssd_path_for_" + videoId);
            return data;
        } else {
            System.out.println("Video not found in database");
            return null;
        }
    }

    public void printStatistics() {
        System.out.println("\nCache Statistics:");
        System.out.println("L1: Hit Rate " + String.format("%.2f", l1.hitRate()) + "%, Avg Time: " + String.format("%.2f", l1.avgAccessTimeMs()) + "ms");
        System.out.println("L2: Hit Rate " + String.format("%.2f", l2.hitRate()) + "%, Avg Time: " + String.format("%.2f", l2.avgAccessTimeMs()) + "ms");
        System.out.println("L3: Hit Rate " + String.format("%.2f", l3.hitRate()) + "%, Avg Time: " + String.format("%.2f", l3.avgAccessTimeMs()) + "ms");

        int totalHits = l1.hits + l2.hits + l3.hits;
        int totalAccess = l1.hits + l1.misses + l2.hits + l2.misses + l3.hits + l3.misses;
        double overallHit = totalHits * 100.0 / totalAccess;
        double overallAvgTime = (l1.totalAccessTime + l2.totalAccessTime + l3.totalAccessTime) / 1_000_000.0 / totalAccess;

        System.out.println("Overall: Hit Rate " + String.format("%.2f", overallHit) + "%, Avg Time: " + String.format("%.2f", overallAvgTime) + "ms");
    }
}

public class MultiLevelCacheSystemwithHashTables {
    public static void main(String[] args) {

        L1Cache l1 = new L1Cache(3); // small for testing
        L2Cache l2 = new L2Cache(5);
        L3Database l3 = new L3Database();

        // populate database
        for (int i = 1; i <= 10; i++) {
            l3.addVideo("video_" + i, "Video Content " + i);
        }

        MultiLevelCache cacheSystem = new MultiLevelCache(l1, l2, l3);

        System.out.println("Request video_1");
        cacheSystem.getVideo("video_1"); // L1 miss, L2 miss, L3 hit

        System.out.println("\nRequest video_1 again");
        cacheSystem.getVideo("video_1"); // promoted to L1

        System.out.println("\nRequest video_2");
        cacheSystem.getVideo("video_2");

        System.out.println("\nRequest video_1 third time");
        cacheSystem.getVideo("video_1");

        cacheSystem.printStatistics();
    }
}