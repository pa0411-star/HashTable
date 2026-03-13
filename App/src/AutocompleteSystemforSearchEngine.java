import java.util.*;

class TrieNode {

    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfQuery = false;
}

class AutocompleteSystem {

    private TrieNode root = new TrieNode();

    // query -> frequency
    private HashMap<String, Integer> queryFrequency = new HashMap<>();


    // insert query into trie
    public void insertQuery(String query) {

        TrieNode node = root;

        for (char c : query.toCharArray()) {

            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }

        node.isEndOfQuery = true;

        queryFrequency.put(query,
                queryFrequency.getOrDefault(query, 0) + 1);
    }


    // update frequency after search
    public void updateFrequency(String query) {

        insertQuery(query);

        System.out.println(query + " → Frequency: "
                + queryFrequency.get(query));
    }


    // search suggestions for prefix
    public List<String> search(String prefix) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {

            if (!node.children.containsKey(c))
                return new ArrayList<>();

            node = node.children.get(c);
        }

        List<String> results = new ArrayList<>();

        dfs(node, prefix, results);

        // sort by frequency
        PriorityQueue<String> pq =
                new PriorityQueue<>(
                        (a, b) -> queryFrequency.get(a) - queryFrequency.get(b)
                );

        for (String r : results) {

            pq.offer(r);

            if (pq.size() > 10)
                pq.poll();
        }

        List<String> topResults = new ArrayList<>();

        while (!pq.isEmpty())
            topResults.add(pq.poll());

        Collections.reverse(topResults);

        return topResults;
    }


    // DFS to collect queries
    private void dfs(TrieNode node, String prefix, List<String> results) {

        if (node.isEndOfQuery)
            results.add(prefix);

        for (char c : node.children.keySet()) {

            dfs(node.children.get(c), prefix + c, results);
        }
    }


    // print suggestions
    public void printSuggestions(String prefix) {

        List<String> suggestions = search(prefix);

        System.out.println("\nSearch suggestions for \"" + prefix + "\"");

        int rank = 1;

        for (String s : suggestions) {

            System.out.println(rank + ". "
                    + s + " (" + queryFrequency.get(s) + " searches)");

            rank++;
        }
    }
}

public class AutocompleteSystemforSearchEngine {

    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        // sample queries
        system.insertQuery("java tutorial");
        system.insertQuery("javascript");
        system.insertQuery("java download");
        system.insertQuery("java tutorial");
        system.insertQuery("java 21 features");
        system.insertQuery("java stream api");
        system.insertQuery("java interview questions");
        system.insertQuery("java tutorial");
        system.insertQuery("javascript tutorial");

        system.printSuggestions("jav");

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        system.printSuggestions("jav");
    }
}