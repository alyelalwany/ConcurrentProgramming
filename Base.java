
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Base {

    private static final int STARTER_PEASANT_NUMBER = 5;
    private static final int STARTER_PEASENT_MINING = 3;
    private static final int PEASANT_NUMBER_GOAL = 10;

    // lock to ensure only one unit can be trained at one time
    private final ReentrantLock trainingLock = new ReentrantLock();

    private final String name;
    private final Resources resources = new Resources();
    private final List<Peasant> peasants = Collections.synchronizedList(new LinkedList<>());
    private final List<Building> buildings = Collections.synchronizedList(new LinkedList<>());

    // fixed size executor service
    // Create the initial 5 peasants - Use the STARTER_PEASANT_NUMBER constant
    // 3 of them should mine gold
    // 1 of them should cut tree
    // 1 should do nothing
    // Use the createPeasant() method
    public Base(String name) {
        this.name = name;
        for (var i = 0; i < STARTER_PEASANT_NUMBER; i++) {
            synchronized (peasants) {
                this.peasants.add(createPeasant());
            }
        }

        for (var i = 0; i < STARTER_PEASENT_MINING; i++) {
            this.peasants.get(i).startMining();
        }
        this.peasants.get(3).startCuttingWood();

    }

    private void addPeasants() {
        for (var i = STARTER_PEASANT_NUMBER; i < PEASANT_NUMBER_GOAL; i++) {
            Peasant peasant = this.createPeasant();
            if (peasant != null) {
                peasants.add(peasant);
            } else {
                i--;
            }
        }
        for (var i = 0; i < STARTER_PEASANT_NUMBER; i++) {
            peasants.get(i).startMining();
        }
        for (var i = STARTER_PEASANT_NUMBER; i < 7; i++) {
            peasants.get(i).startCuttingWood();
        }
    }

    private void buildBuilding(UnitType unitType) {
        var built = false;
        while (!built) {
            Peasant peasant = this.getFreePeasant();
            if (peasant != null && peasant.tryBuilding(unitType)) {
                built = true;
            }
        }
    }

    private void buildLumbermill() {
        buildBuilding(UnitType.LUMBERMILL);
    }

    private void buildBlacksmith() {
        buildBuilding(UnitType.BLACKSMITH);
    }

    private void buildFarm() {
        while (!hasEnoughBuilding(UnitType.FARM, 3)) {
            Peasant peasant = this.getFreePeasant();
            if (peasant != null) {
                peasant.tryBuilding(UnitType.FARM);
            }
            sleepForMsec(UnitType.FARM.buildTime);
        }
    }

    // Start the building and training preparations on separate threads
    // Tip: use the hasEnoughBuilding method

    // Build 3 farms - use getFreePeasant() method to see if there is a peasant
    // without any work

    // Create remaining 5 peasants - Use the PEASANT_NUMBER_GOAL constant
    // 5 of them should mine gold
    // 2 of them should cut tree
    // 3 of them should do nothing
    // Use the createPeasant() method

    // Build a lumbermill - use getFreePeasant() method to see if there is a
    // peasant without any work

    // Build a blacksmith - use getFreePeasant() method to see if there is a
    // peasant without any work

    // Wait for all the necessary preparations to finish

    // Stop harvesting with the peasants once everything is ready
    public void startPreparation() {

        try {
            var t1 = new Thread(() -> buildFarm());
            t1.start();
            var t2 = new Thread(() -> addPeasants());
            t2.start();
            var t3 = new Thread(() -> buildLumbermill());
            t3.start();
            var t4 = new Thread(() -> buildBlacksmith());
            t4.start();

            t1.join();
            t2.join();
            t3.join();
            t4.join();

            for (Peasant p : peasants) {
                p.stopHarvesting();
            }

            System.out.println(this.name + " finished creating a base");
            System.out.println(this.name + " peasants: " + this.peasants.size());
            for (Building b : buildings) {
                System.out.println(this.name + " has a  " + b.getUnitType().toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a peasants that is currently free.
     * Being free means that the peasant currently isn't harvesting or building.
     *
     * @return Peasant object, if found one, null if there isn't one
     */
    private Peasant getFreePeasant() {
        // TODO implement - use the peasant's isFree() method
        synchronized (this.peasants) {
            for (var peasant : peasants) {
                if (peasant.isFree()) {
                    return peasant;
                }
            }
        }
        return null;
    }

    /**
     * Creates a peasant.
     * A peasant could only be trained if there are sufficient
     * gold, wood and food for him to train.
     *
     * At one time only one Peasant can be trained.
     *
     * @return The newly created peasant if it could be trained, null otherwise
     */
    // 1: Sleep as long as it takes to create a peasant - use sleepForMsec()
    // method
    // 2: Remove costs
    // 3: Update capacity
    // 4: Use the Peasant class' createPeasant method to create the new Peasant

    // Remember that at one time only one peasant can be trained
    // return result;
    private Peasant createPeasant() {
        Peasant result;
        if (resources.canTrain(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost, UnitType.PEASANT.foodCost)) {
            sleepForMsec(UnitType.PEASANT.buildTime);
            this.trainingLock.lock();
            this.resources.removeCost(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost);
            this.resources.updateCapacity(UnitType.PEASANT.foodCost);
            result = Peasant.createPeasant(this);
            this.trainingLock.unlock();
            return result;
        }
        return null;
    }

    public Resources getResources() {
        return this.resources;
    }

    public List<Building> getBuildings() {
        return this.buildings;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Helper method to determine if a base has the required number of a certain
     * building.
     *
     * @param unitType Type of the building
     * @param required Number of required amount
     * @return true, if required amount is reached (or surpassed), false otherwise
     */
    // check in the buildings list if the type has reached the required amount
    private boolean hasEnoughBuilding(UnitType unitType, int required) {
        int buildingNumber = 0;
        synchronized (this.buildings) {
            for (var building : buildings) {
                if (building.getUnitType() == unitType && building != null) {
                    buildingNumber += 1;
                }

                if (buildingNumber == required) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void sleepForMsec(int sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

}
