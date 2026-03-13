import java.util.*;
import java.util.concurrent.*;

class PageViewEvent {

    String url;
    String userId;
    String source;

    public PageViewEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class AnalyticsEngine {

    // page -> visit count
    private ConcurrentHashMap<String, Integer> pageViews = new ConcurrentHashMap<>();

    // page -> unique visitors
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // traffic source -> count
    private ConcurrentHashMap<String, Integer> trafficSources = new ConcurrentHashMap<>();


    // Process page view event
    public void processEvent(PageViewEvent event) {

        // total page views
        pageViews.merge(event.url, 1, Integer::sum);

        // unique visitors
        uniqueVisitors
                .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                .add(event.userId);

        // traffic source
        trafficSources.merge(event.source, 1, Integer::sum);
    }


    // Get Top N pages
    public List<Map.Entry<String, Integer>> getTopPages(int n) {

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {

            pq.offer(entry);

            if (pq.size() > n) {
                pq.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(pq);
        result.sort((a, b) -> b.getValue() - a.getValue());

        return result;
    }


    // Print dashboard
    public void getDashboard() {

        System.out.println("\n========= REAL-TIME DASHBOARD =========");

        System.out.println("\nTop Pages:");

        int rank = 1;

        for (Map.Entry<String, Integer> entry : getTopPages(10)) {

            String page = entry.getKey();
            int views = entry.getValue();

            int unique = uniqueVisitors.getOrDefault(page, Set.of()).size();

            System.out.println(rank + ". " + page +
                    " - " + views + " views (" + unique + " unique)");

            rank++;
        }

        System.out.println("\nTraffic Sources:");

        int total = trafficSources.values().stream().mapToInt(i -> i).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {

            double percent = (entry.getValue() * 100.0) / total;

            System.out.println(entry.getKey() + ": "
                    + String.format("%.1f", percent) + "%");
        }

        System.out.println("=======================================");
    }
}

public class RealTimeAnalyticsDashboardforWebsiteTraffic {

    public static void main(String[] args) {

        AnalyticsEngine engine = new AnalyticsEngine();

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        // Update dashboard every 5 seconds
        scheduler.scheduleAtFixedRate(
                engine::getDashboard,
                5,
                5,
                TimeUnit.SECONDS
        );


        // Simulated event stream
        String[] pages = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-revolution",
                "/politics/election-update",
                "/health/wellness-guide"
        };

        String[] sources = {
                "Google",
                "Facebook",
                "Direct",
                "Twitter",
                "Other"
        };

        Random random = new Random();

        while (true) {

            PageViewEvent event = new PageViewEvent(
                    pages[random.nextInt(pages.length)],
                    "user_" + random.nextInt(5000),
                    sources[random.nextInt(sources.length)]
            );

            engine.processEvent(event);

            try {
                Thread.sleep(100); // simulate traffic
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}