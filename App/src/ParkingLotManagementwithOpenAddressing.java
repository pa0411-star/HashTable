import java.util.*;

class ParkingSpot {

    String licensePlate;
    long entryTime;
    boolean occupied;

    public ParkingSpot() {
        this.licensePlate = null;
        this.entryTime = 0;
        this.occupied = false;
    }
}

class ParkingLot {

    private ParkingSpot[] table;
    private int capacity;
    private int occupiedSpots = 0;
    private int totalProbes = 0;
    private int totalParkOperations = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];

        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // park vehicle
    public void parkVehicle(String licensePlate) {

        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].occupied) {
            index = (index + 1) % capacity;
            probes++;
        }

        table[index].licensePlate = licensePlate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].occupied = true;

        occupiedSpots++;
        totalProbes += probes;
        totalParkOperations++;

        System.out.println("parkVehicle(\"" + licensePlate + "\") → Assigned spot #" +
                index + " (" + probes + " probes)");
    }

    // exit vehicle
    public void exitVehicle(String licensePlate) {

        int index = hash(licensePlate);

        while (table[index].occupied) {

            if (licensePlate.equals(table[index].licensePlate)) {

                long exitTime = System.currentTimeMillis();

                long durationMillis = exitTime - table[index].entryTime;

                double hours = durationMillis / (1000.0 * 60 * 60);

                double fee = Math.ceil(hours) * 5; // $5 per hour

                table[index].occupied = false;
                table[index].licensePlate = null;

                occupiedSpots--;

                System.out.println("exitVehicle(\"" + licensePlate + "\") → Spot #" + index +
                        " freed, Duration: " +
                        String.format("%.2f", hours) +
                        " hours, Fee: $" + fee);

                return;
            }

            index = (index + 1) % capacity;
        }

        System.out.println("Vehicle not found.");
    }

    // find nearest available spot
    public void findNearestSpot() {

        for (int i = 0; i < capacity; i++) {

            if (!table[i].occupied) {
                System.out.println("Nearest available spot: #" + i);
                return;
            }
        }

        System.out.println("Parking full.");
    }

    // statistics
    public void getStatistics() {

        double occupancy = (occupiedSpots * 100.0) / capacity;

        double avgProbes = totalParkOperations == 0 ? 0 :
                (double) totalProbes / totalParkOperations;

        System.out.println("\nParking Statistics:");
        System.out.println("Occupancy: " + String.format("%.2f", occupancy) + "%");
        System.out.println("Avg Probes: " + String.format("%.2f", avgProbes));
    }
}

public class ParkingLotManagementwithOpenAddressing  {

    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = new ParkingLot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        lot.findNearestSpot();

        Thread.sleep(3000);

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();
    }
}