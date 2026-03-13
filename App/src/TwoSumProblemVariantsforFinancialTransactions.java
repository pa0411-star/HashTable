import java.util.*;

class Transaction {

    int id;
    int amount;
    String merchant;
    String account;
    long timestamp;

    public Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }

    public String toString() {
        return "Transaction{id=" + id + ", amount=" + amount + "}";
    }
}

class TransactionAnalyzer {

    private List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two Sum
    public List<List<Transaction>> findTwoSum(int target) {

        HashMap<Integer, Transaction> map = new HashMap<>();
        List<List<Transaction>> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                result.add(Arrays.asList(map.get(complement), t));
            }

            map.put(t.amount, t);
        }

        return result;
    }


    // Two Sum with Time Window
    public List<List<Transaction>> findTwoSumWithinWindow(int target, long windowMillis) {

        List<List<Transaction>> result = new ArrayList<>();

        HashMap<Integer, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                for (Transaction prev : map.get(complement)) {

                    if (Math.abs(t.timestamp - prev.timestamp) <= windowMillis) {
                        result.add(Arrays.asList(prev, t));
                    }
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }

        return result;
    }


    // K-Sum
    public List<List<Transaction>> findKSum(int k, int target) {

        List<List<Transaction>> result = new ArrayList<>();

        backtrack(0, k, target, new ArrayList<>(), result);

        return result;
    }

    private void backtrack(int start, int k, int target,
                           List<Transaction> path,
                           List<List<Transaction>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(path));
            return;
        }

        if (k == 0 || start >= transactions.size())
            return;

        for (int i = start; i < transactions.size(); i++) {

            Transaction t = transactions.get(i);

            path.add(t);

            backtrack(i + 1, k - 1, target - t.amount, path, result);

            path.remove(path.size() - 1);
        }
    }


    // Duplicate Detection
    public Map<String, List<String>> detectDuplicates() {

        Map<String, List<String>> result = new HashMap<>();

        Map<String, Set<String>> map = new HashMap<>();

        for (Transaction t : transactions) {

            String key = t.amount + "_" + t.merchant;

            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (String key : map.keySet()) {

            if (map.get(key).size() > 1) {
                result.put(key, new ArrayList<>(map.get(key)));
            }
        }

        return result;
    }
}

public class TwoSumProblemVariantsforFinancialTransactions {

    public static void main(String[] args) {

        long baseTime = System.currentTimeMillis();

        List<Transaction> transactions = Arrays.asList(

                new Transaction(1, 500, "Store A", "acc1", baseTime),
                new Transaction(2, 300, "Store B", "acc2", baseTime + 1000),
                new Transaction(3, 200, "Store C", "acc3", baseTime + 2000),
                new Transaction(4, 500, "Store A", "acc4", baseTime + 3000)
        );

        TransactionAnalyzer analyzer = new TransactionAnalyzer(transactions);

        System.out.println("Two Sum Target 500:");
        System.out.println(analyzer.findTwoSum(500));

        System.out.println("\nTwo Sum within 1 hour:");
        System.out.println(analyzer.findTwoSumWithinWindow(500, 3600000));

        System.out.println("\nK-Sum k=3 target=1000:");
        System.out.println(analyzer.findKSum(3, 1000));

        System.out.println("\nDuplicate Detection:");
        System.out.println(analyzer.detectDuplicates());
    }
}