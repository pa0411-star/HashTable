import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class FlashSaleInventoryManager {

    // productId -> stock count
    private ConcurrentHashMap<String, Integer> inventory;

    // productId -> waiting list (FIFO)
    private ConcurrentHashMap<String, LinkedHashMap<Integer, Long>> waitingList;

    public FlashSaleInventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Add product to inventory
    public void addProduct(String productId, int stock) {
        inventory.put(productId, stock);
        waitingList.put(productId, new LinkedHashMap<>());
    }

    // Check stock availability
    public int checkStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    // Purchase item (thread-safe)
    public synchronized String purchaseItem(String productId, int userId) {

        int stock = inventory.getOrDefault(productId, 0);

        if (stock > 0) {

            inventory.put(productId, stock - 1);

            return "Success! User " + userId +
                    " purchased " + productId +
                    ". Remaining stock: " + (stock - 1);

        } else {

            LinkedHashMap<Integer, Long> queue = waitingList.get(productId);

            queue.put(userId, System.currentTimeMillis());

            int position = queue.size();

            return "Out of stock. User " + userId +
                    " added to waiting list. Position #" + position;
        }
    }

    // View waiting list
    public void showWaitingList(String productId) {

        LinkedHashMap<Integer, Long> queue = waitingList.get(productId);

        System.out.println("\nWaiting List for " + productId + ":");

        int pos = 1;
        for (Integer userId : queue.keySet()) {
            System.out.println("Position " + pos + " -> User " + userId);
            pos++;
        }
    }
}

public class EcommerceFlashSaleInventoryManager {

    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        String product = "IPHONE15_256GB";

        // Add product with limited stock
        manager.addProduct(product, 5);

        System.out.println("Initial Stock: "
                + manager.checkStock(product) + " units\n");

        // Simulate users purchasing
        for (int userId = 1001; userId <= 1010; userId++) {

            String result = manager.purchaseItem(product, userId);

            System.out.println(result);
        }

        // Show waiting list
        manager.showWaitingList(product);
    }
}