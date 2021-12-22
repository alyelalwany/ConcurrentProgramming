package concurent.student.first;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Base {

    private static final int STARTER_PEASANT_NUMBER = 5;
    private static final int START_PEASANT_MINING = 3;
    private static final int PEASANT_NUMBER_GOAL = 10;

    // lock to ensure only one unit can be trained at one time
    private final ReentrantLock trainingLock = new ReentrantLock();

    private final String name;
    private final Resources resources = new Resources();
    private final List<Peasant> peasants = Collections.synchronizedList(new LinkedList<>());
    private final List<Building> buildings = Collections.synchronizedList(new LinkedList<>());

    // Create the initial 5 peasants - Use the STARTER_PEASANT_NUMBER constant
    // 3 of them should mine gold
    // 1 of them should cut tree
    // 1 should do nothing
    // Use the createPeasant() method
    public Base(String name) {
        this.name = name;
        for (var i = 0; i < STARTER_PEASANT_NUMBER; i++) {
            new Thread(() -> {
                Peasant peasant = createPeasant();
                if (this.peasants.size() < 4) {
                    peasant.startMining();
                }
                if (this.peasants.size() == 4) {
                    peasant.startCuttingWood();
                }
            }).start();
        }
    }

    private void addPeasants() {
        while (peasants.size() < PEASANT_NUMBER_GOAL) {
            Peasant peasant = this.createPeasant();
            if (peasant != null) {
                if (peasants.size() < 8) {
                    peasant.startMining();
                } else if (peasants.size() == 8) {
                    peasant.startMining();
                }
            }
            sleepForMsec(10);
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
        while (!hasEnoughBuilding(UnitType.LUMBERMILL, 1)) {
            Peasant peasant = this.getFreePeasant();
            if (peasant != null) {
                peasant.tryBuilding(UnitType.LUMBERMILL);
            }
            sleepForMsec(10);
        }
    }

    private void buildBlacksmith() {
        while (!hasEnoughBuilding(UnitType.BLACKSMITH, 1)) {
            Peasant peasant = this.getFreePeasant();
            if (peasant != null) {
                peasant.tryBuilding(UnitType.BLACKSMITH);
            }
            sleepForMsec(10);
        }
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

        List<Thread> creationThreads = new ArrayList<>();
        var t1 = new Thread(() -> buildFarm());
        var t2 = new Thread(() -> addPeasants());
        var t3 = new Thread(() -> buildLumbermill());
        var t4 = new Thread(() -> buildBlacksmith());

        creationThreads.add(t1);
        creationThreads.add(t2);
        creationThreads.add(t3);
        creationThreads.add(t4);
        creationThreads.forEach(t -> t.start());

        try {
            for (Thread t : creationThreads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (this.peasants) {
            for (Peasant p : peasants) {
                p.stopHarvesting();
            }
        }

        System.out.println(this.name + " finished creating a base");
        System.out.println(this.name + " peasants: " + this.peasants.size());
        synchronized (this) {
            for (Building b : buildings) {
                System.out.println(this.name + " has a " + b.getUnitType().toString());
            }
        }
    }

    /**
     * Returns a peasants that is currently free.
     * Being free means that the peasant currently isn't harvesting or building.
     *
     * @return Peasant object, if found one, null if there isn't one
     */
    private Peasant getFreePeasant() {
        synchronized (peasants) {
            return peasants.stream().filter(p -> p.isFree()).findFirst().orElse(null);
        }
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
            try {
                this.trainingLock.lockInterruptibly();
                sleepForMsec(UnitType.PEASANT.buildTime);
                this.resources.removeCost(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost);
                this.resources.updateCapacity(UnitType.PEASANT.foodCost);
                result = Peasant.createPeasant(this);
                peasants.add(result);
                System.out.println(this.name + " created a peasant");
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.trainingLock.unlock();
            }
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
        synchronized (buildings) {
            return buildings.stream().filter(b -> b.getUnitType() == unitType).count() >= required;
        }
    }

    private static void sleepForMsec(int sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

}
