import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class UsernameAvailabilitySystem {

    // username -> userId
    private ConcurrentHashMap<String, Integer> usernameMap;

    // username -> attempt count
    private ConcurrentHashMap<String, Integer> attemptFrequency;

    public UsernameAvailabilitySystem() {
        usernameMap = new ConcurrentHashMap<>();
        attemptFrequency = new ConcurrentHashMap<>();
    }

    // Register username
    public void registerUser(String username, int userId) {
        usernameMap.put(username, userId);
    }

    // Check availability (O1)
    public boolean checkAvailability(String username) {

        // Track attempts
        attemptFrequency.put(username,
                attemptFrequency.getOrDefault(username, 0) + 1);

        return !usernameMap.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String dotVersion = username.replace("_", ".");
        if (!usernameMap.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {

        String mostAttempted = "";
        int maxAttempts = 0;

        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + maxAttempts + " attempts)";
    }
}

public class SocialMediaUsernameAvailabilityChecker {

    public static void main(String[] args) {

        UsernameAvailabilitySystem system = new UsernameAvailabilitySystem();

        // Pre-existing users
        system.registerUser("john_doe", 1001);
        system.registerUser("admin", 1002);
        system.registerUser("alice", 1003);

        // Check usernames
        System.out.println("Checking john_doe: " +
                system.checkAvailability("john_doe"));

        System.out.println("Checking jane_smith: " +
                system.checkAvailability("jane_smith"));

        // Suggestions
        System.out.println("\nSuggestions for john_doe:");
        List<String> suggestions = system.suggestAlternatives("john_doe");

        for (String s : suggestions) {
            System.out.println(s);
        }

        // Simulate multiple attempts
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("john_doe");
        system.checkAvailability("john_doe");

        // Most attempted username
        System.out.println("\nMost attempted username:");
        System.out.println(system.getMostAttempted());
    }
}