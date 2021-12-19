import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Base {

    private static final int STARTER_PEASANT_NUMBER = 5;
    private static final int PEASANT_NUMBER_GOAL = 10;

    // lock to ensure only one unit can be trained at one time
    private final ReentrantLock trainingLock = new ReentrantLock();

    private final String name;
    private final Resources resources = new Resources();
    private final List<Peasant> peasants = Collections.synchronizedList(new LinkedList<>());
    private final List<Building> buildings = Collections.synchronizedList(new LinkedList<>());

    // TODO Create the initial 5 peasants - Use the STARTER_PEASANT_NUMBER constant
    // TODO 3 of them should mine gold
    // TODO 1 of them should cut tree
    // TODO 1 should do nothing
    // TODO Use the createPeasant() method
    public Base(String name) {
        this.name = name;
        for (int i = 0; i < STARTER_PEASANT_NUMBER; i++) {
            var peasant = createPeasant();
            if (i < 3) {
                peasant.startMining();
            } else if (i == 3) {
                peasant.startCuttingWood();
                synchronized (peasants) {
                    peasants.add(peasant);
                }
            }
        }
    }

    // TODO Start the building and training preparations on separate threads
    // TODO Tip: use the hasEnoughBuilding method
    // TODO Build 3 farms - use getFreePeasant() method to see if there is a peasant
    // without any work
    // TODO Build a lumbermill - use getFreePeasant() method to see if there is a
    // peasant without any work
    // TODO Build a blacksmith - use getFreePeasant() method to see if there is a
    // peasant without any work

    // TODO Create remaining 5 peasants - Use the PEASANT_NUMBER_GOAL constant
    // TODO 5 of them should mine gold
    // TODO 2 of them should cut tree
    // TODO 3 of them should do nothing
    // TODO Use the createPeasant() method

    // TODO Wait for all the necessary preparations to finish
    // TODO Stop harvesting with the peasants once everything is ready
    public void startPreparation() {
        Thread buildingThread = new Thread(() -> {
            try {
                if (!hasEnoughBuilding(UnitType.FARM, 3)) {
                    try {
                        synchronized (peasants) {
                            if (getFreePeasant().isFree())
                                Building.createFarm(Base.this);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                if (!hasEnoughBuilding(UnitType.LUMBERMILL, 1)) {
                    try {
                        synchronized (peasants) {
                            if (getFreePeasant().isFree())
                                Building.createLumbermill(Base.this);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                if (!hasEnoughBuilding(UnitType.BLACKSMITH, 1)) {
                    try {
                        synchronized (peasants) {
                            if (getFreePeasant().isFree())
                                Building.createFarm(Base.this);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread trainingThread = new Thread(() -> {
            try {
                while (peasants.size() == PEASANT_NUMBER_GOAL) {
                    this.wait();
                }
                synchronized (peasants) {
                    for (int i = 0; i < 5; i++) {
                        var minerPeasant = createPeasant();
                        System.out.println(minerPeasant);
                        minerPeasant.startMining();
                        peasants.add(minerPeasant);
                    }
                    for (int i = 0; i < 2; i++) {
                        peasants.get(i).startCuttingWood();
                        ;
                    }
                    for (int i = 0; i < 3; i++) {
                        peasants.get(i).stopHarvesting();
                    }
                }
                notify();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        trainingThread.start();
        buildingThread.start();
        synchronized (this.peasants) {
            for (var peasant : peasants) {
                peasant.stopHarvesting();
            }
        }
        System.out.println(this.name + " finished creating a base");
        System.out.println(this.name + " peasants: " + this.peasants.size());

        for (Building b : buildings) {
            System.out.println(this.name + " has a  " + b.getUnitType().toString());
        }

    }

    /**
     * Returns a peasant that is currently free.
     * Being free means that the peasant currently isn't harvesting or building.
     *
     * @return Peasant object, if found one, null if there isn't one
     */
    private Peasant getFreePeasant() {
        // TODO implement - use the peasant's isFree() method
        synchronized (peasants) {
            for (var peasant : peasants) {
                if (peasant.isFree())
                    return peasant;
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
    // TODO 1: Sleep as long as it takes to create a peasant - use sleepForMsec()
    // TODO 2: Remove costs
    // TODO 3: Update capacity
    // TODO 4: Use the Peasant class' createPeasant method to create the new Peasant
    // TODO Remember that at one time only one peasant can be trained
    private Peasant createPeasant() {
        Peasant result;
        System.out.println(this.resources.getCapacity());
        System.out.println(this.resources.getCapacityLimit());
        if (resources.canTrain(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost, UnitType.PEASANT.foodCost)) {
            trainingLock.lock();
            sleepForMsec(UnitType.PEASANT.buildTime);
            resources.removeCost(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost);
            resources.updateCapacity(UnitType.PEASANT.foodCost);
            result = Peasant.createPeasant(this);
            trainingLock.unlock();
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
    // TODO check in the buildings list if the type has reached the required amount
    private boolean hasEnoughBuilding(UnitType unitType, int required) {
        int numOfBuildings = 0;
        synchronized (this.buildings) {
            for (var building : buildings) {
                if (building.getUnitType() == unitType) {
                    numOfBuildings++;
                }
                if (required == numOfBuildings)
                    return true;
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
