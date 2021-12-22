package concurent.student.second;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Base {

    private static final int STARTER_PEASANT_NUMBER = 5;
    private static final int PEASANT_NUMBER_GOAL = 10;
    private static final int FOOTMAN_NUMBER_GOAL = 10;

    // lock to ensure only one unit can be trained at one time
    private final ReentrantLock trainingLock = new ReentrantLock();

    private final String name;
    private final Resources resources = new Resources();
    private final List<Peasant> peasants = Collections.synchronizedList(new LinkedList<>());
    private final List<Footman> footmen = Collections.synchronizedList(new LinkedList<>());
    private final List<Building> buildings = Collections.synchronizedList(new LinkedList<>());
    private final List<Personnel> army = Collections.synchronizedList(new LinkedList<>());

    public Base(String name) {
        this.name = name;
        for (int i = 0; i < STARTER_PEASANT_NUMBER; i++) {
            peasants.add(createPeasant());
        }
        peasants.get(0).startMining();
        peasants.get(1).startMining();
        peasants.get(2).startMining();
        peasants.get(3).startCuttingWood();
    }

    public void startPreparation() {
        ExecutorService es = Executors.newFixedThreadPool(6);

        es.submit(() -> {
            while (!hasEnoughBuilding(UnitType.FARM, 3)) {
                Peasant p = getFreePeasant();
                if (p != null) {
                    p.tryBuilding(UnitType.FARM);
                }
            }
        });

        es.submit(() -> {
            for (var i = STARTER_PEASANT_NUMBER; i < PEASANT_NUMBER_GOAL; i++) {
                Peasant peasant = this.createPeasant();
                if (peasant != null) {
                    synchronized (this.peasants) {
                        peasants.add(peasant);
                    }
                } else {
                    i--;
                }
            }
            for (var i = 0; i < STARTER_PEASANT_NUMBER; i++) {
                synchronized (this.peasants) {
                    peasants.get(i).startMining();
                }
            }
            for (var i = STARTER_PEASANT_NUMBER; i < 7; i++) {
                synchronized (this.peasants) {
                    peasants.get(i).startCuttingWood();
                }
            }
        });

        es.submit(() -> {
            while (!hasEnoughBuilding(UnitType.LUMBERMILL, 1)) {
                Peasant p = getFreePeasant();
                if (p != null) {
                    p.tryBuilding(UnitType.LUMBERMILL);
                }
            }
        });

        es.submit(() -> {
            while (!hasEnoughBuilding(UnitType.BLACKSMITH, 1)) {
                Peasant p = getFreePeasant();
                if (p != null) {
                    p.tryBuilding(UnitType.BLACKSMITH);
                }
            }
        });

        es.submit(() -> {
            while (!hasEnoughBuilding(UnitType.BARRACKS, 1)) {
                Peasant p = getFreePeasant();
                if (p != null) {
                    p.tryBuilding(UnitType.BARRACKS);
                }
            }
        });

        es.submit(() -> {
            while (footmen.size() < FOOTMAN_NUMBER_GOAL) {
                Footman f = createFootman();
                synchronized (this.footmen) {
                    footmen.add(f);
                }
            }
        });

        es.shutdown();
        try {
            es.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }

        synchronized (peasants) {
            for (Peasant p : peasants) {
                p.stopHarvesting();
            }
        }

        System.out.println(this.name + " finished creating a base");
        System.out.println(this.name + " peasants: " + this.peasants.size());
        System.out.println(this.name + " footmen: " + this.footmen.size());
        for (Building b : buildings) {
            System.out.println(this.name + " has a  " + b.getUnitType().toString());
        }
    }

    /**
     * Assemble the army - call the peasants and footmen to arms
     * 
     * @param latch
     */
    public void assembleArmy(CountDownLatch latch) {
        synchronized (peasants) {
            for (Peasant p : peasants) {
                army.add(p);
            }
        }
        synchronized (footmen) {
            for (Footman f : footmen) {
                army.add(f);
            }
        }
        System.out.println(this.name + " is ready for war");
        // the latch is used to keep track of both factions
        latch.countDown();
    }

    /**
     * Starts a war between the two bases.
     *
     * @param enemy    Enemy base's personnel
     * @param warLatch Latch to make sure they attack at the same time
     */
    public void goToWar(List<Personnel> enemy, CountDownLatch warLatch) {
        // This is necessary to ensure that both armies attack at the same time
        warLatch.countDown();
        try {
            // Waiting for the other army to be ready for war
            warLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExecutorService es = Executors.newFixedThreadPool(this.army.size());

        for (int i = 0; i < this.army.size(); i++) {
            final int index = i;
            es.submit(() -> {
                army.get(index).startWar(enemy);
            });
        }

        es.shutdown();
        try {
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }

        // If our army has no personnel, we failed
        if (army.isEmpty()) {
            System.out.println(this.name + " has lost the fight");
        } else {
            System.out.println(this.name + " has won the fight");
        }
    }

    /**
     * Resolves the event when a personnel dies;
     * Remove it from the army and update the capacity.
     * 
     * @param p The fallen personnel
     */
    public void signalPersonnelDeath(Personnel p) {
        resources.updateCapacity(-1 * p.getUnitType().foodCost);
        army.remove(p);
        if (p.getUnitType() == UnitType.PEASANT) {
            peasants.remove(p);
        } else if (p.getUnitType() == UnitType.FOOTMAN) {
            footmen.remove(p);
        }
        System.out.println(this.name + " has lost a " + p.getUnitType().toString());
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
    private Peasant createPeasant() {
        Peasant result;
        if (resources.canTrain(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost, UnitType.PEASANT.foodCost)) {
            trainingLock.lock();
            try {
                sleepForMsec(UnitType.PEASANT.buildTime);
                resources.removeCost(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost);
                resources.updateCapacity(UnitType.PEASANT.foodCost);
                result = Peasant.createPeasant(this);
                System.out.println(this.name + " created a peasant");
                return result;
            } finally {
                trainingLock.unlock();
            }
        }
        return null;
    }

    private Footman createFootman() {
        Footman result;
        if (resources.canTrain(UnitType.FOOTMAN.goldCost, UnitType.FOOTMAN.woodCost, UnitType.FOOTMAN.foodCost) &&
                hasEnoughBuilding(UnitType.BARRACKS, 1)) {
            trainingLock.lock();
            try {
                sleepForMsec(UnitType.FOOTMAN.buildTime);
                resources.removeCost(UnitType.FOOTMAN.goldCost, UnitType.FOOTMAN.woodCost);
                resources.updateCapacity(UnitType.FOOTMAN.foodCost);
                result = Footman.createFootman(this);
                System.out.println(this.name + " created a footman");
                return result;
            } finally {
                trainingLock.unlock();
            }
        }
        return null;
    }

    public Resources getResources() {
        return this.resources;
    }

    public List<Personnel> getArmy() {
        return this.army;
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