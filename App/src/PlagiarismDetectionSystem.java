import java.util.*;

class Document {

    String id;
    String content;

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }
}

class PlagiarismDetector {

    // n-gram -> set of document IDs
    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    // documentId -> set of ngrams
    private Map<String, Set<String>> documentNgrams = new HashMap<>();

    private int n = 5; // 5-gram

    // Add document to database
    public void addDocument(Document doc) {

        Set<String> ngrams = generateNgrams(doc.content);

        documentNgrams.put(doc.id, ngrams);

        for (String gram : ngrams) {

            ngramIndex
                    .computeIfAbsent(gram, k -> new HashSet<>())
                    .add(doc.id);
        }
    }

    // Generate n-grams
    private Set<String> generateNgrams(String text) {

        String[] words = text.toLowerCase().split("\\s+");

        Set<String> ngrams = new HashSet<>();

        for (int i = 0; i <= words.length - n; i++) {

            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < n; j++) {
                gram.append(words[i + j]).append(" ");
            }

            ngrams.add(gram.toString().trim());
        }

        return ngrams;
    }

    // Analyze document
    public void analyzeDocument(Document newDoc) {

        Set<String> newNgrams = generateNgrams(newDoc.content);

        System.out.println("Extracted " + newNgrams.size() + " n-grams\n");

        Map<String, Integer> matchCounts = new HashMap<>();

        for (String gram : newNgrams) {

            if (ngramIndex.containsKey(gram)) {

                for (String docId : ngramIndex.get(gram)) {

                    matchCounts.put(docId,
                            matchCounts.getOrDefault(docId, 0) + 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {

            String docId = entry.getKey();
            int matches = entry.getValue();

            int total = documentNgrams.get(docId).size();

            double similarity = (matches * 100.0) / total;

            System.out.println("Found " + matches +
                    " matching n-grams with \"" + docId + "\"");

            System.out.println("Similarity: "
                    + String.format("%.2f", similarity) + "%");

            if (similarity > 60) {
                System.out.println("⚠ PLAGIARISM DETECTED\n");
            } else if (similarity > 10) {
                System.out.println("Suspicious similarity\n");
            } else {
                System.out.println("Low similarity\n");
            }
        }
    }
}

public class PlagiarismDetectionSystem {

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        Document essay1 = new Document(
                "essay_089.txt",
                "Artificial intelligence is transforming the world. "
                        + "Machine learning allows computers to learn from data "
                        + "and improve their performance over time."
        );

        Document essay2 = new Document(
                "essay_092.txt",
                "Artificial intelligence is transforming the world. "
                        + "Machine learning allows computers to learn from data "
                        + "and improve their performance over time. "
                        + "These technologies are used in healthcare and finance."
        );

        detector.addDocument(essay1);
        detector.addDocument(essay2);

        Document newEssay = new Document(
                "essay_123.txt",
                "Artificial intelligence is transforming the world. "
                        + "Machine learning allows computers to learn from data "
                        + "and improve their performance over time."
        );

        System.out.println("Analyzing essay_123.txt\n");

        detector.analyzeDocument(newEssay);
    }
}