package concurent.student.first;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Peasant extends Unit {

    private static final int HARVEST_WAIT_TIME = 100;
    private static final int HARVEST_AMOUNT = 10;

    private AtomicBoolean isHarvesting = new AtomicBoolean(false);
    private AtomicBoolean isBuilding = new AtomicBoolean(false);

    private ExecutorService pool;
    private Lock lock;

    private Peasant(Base owner) {
        super(owner, UnitType.PEASANT);
        pool = Executors.newSingleThreadExecutor();
        lock = new ReentrantLock();
    }

    public static Peasant createPeasant(Base owner) {
        return new Peasant(owner);
    }

    /**
     * Starts gathering gold.
     */
    // Set isHarvesting to true
    // Start harvesting on a new thread
    // Harvesting: Sleep for HARVEST_WAIT_TIME, then add the resource -
    // HARVEST_AMOUNT
    public void startMining() {
        System.out.println("Peasant starting mining");
        isHarvesting.set(true);
        pool.submit(() -> {
            lock.lock();
            while (isHarvesting.get() && !isBuilding.get()) {
                try {
                    sleepForMsec(HARVEST_WAIT_TIME);
                } catch (Exception e) {
                    e.getStackTrace();
                }
                this.getOwner().getResources().addGold(HARVEST_AMOUNT);
            }
            lock.unlock();
        });
    }

    /**
     * Starts gathering wood.
     */
    // Set isHarvesting to true
    // Start harvesting on a new thread
    // Harvesting: Sleep for HARVEST_WAIT_TIME, then add the resource -
    // HARVEST_AMOUNT
    public void startCuttingWood() {
        System.out.println("Peasant starting cutting wood");
        isHarvesting.set(true);
        pool.submit(() -> {
            lock.lock();
            while (isHarvesting.get() && !isBuilding.get()) {
                try {
                    sleepForMsec(HARVEST_WAIT_TIME);
                } catch (Exception e) {
                    e.getStackTrace();
                }
                this.getOwner().getResources().addWood(HARVEST_AMOUNT);
            }
            lock.unlock();
        });
    }

    /**
     * Peasant should stop all harvesting once this is invoked
     */
    public void stopHarvesting() {
        this.isHarvesting.set(false);
        pool.shutdown();
    }

    /**
     * Tries to build a certain type of building.
     * Can only build if there are enough gold and wood for the building
     * to be built.
     *
     * @param buildingType Type of the building
     * @return true, if the building process has started
     *         false, if there are insufficient resources
     */
    // Start building on a separate thread if there are enough resources
    // Use the Resources class' canBuild method to determine
    // Use the startBuilding method if the process can be started
    public boolean tryBuilding(UnitType buildingType) {
        if (this.getOwner().getResources().canBuild(buildingType.goldCost, buildingType.woodCost) && this.isFree()) {
            pool.submit(() -> {
                try {
                    this.startBuilding(buildingType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Start building a certain type of building.
     * Keep in mind that a peasant can only build one building at one time.
     *
     * @param buildingType Type of the building
     */
    // Ensure that only one building can be built at a time - use isBuilding
    // atomic boolean
    // Building steps: Remove cost, build the building, wait the wait time
    // Use Building's createBuilding method to create the building
    private void startBuilding(UnitType buildingType) throws Exception {
        isBuilding.set(true);
        System.out.println("Peasant started building..");
        lock.lock();
        this.getOwner().getResources().removeCost(buildingType.goldCost, buildingType.woodCost);
        sleepForMsec(buildingType.buildTime);
        this.getOwner().getBuildings().add(Building.createBuilding(buildingType, this.getOwner()));
        System.out.println("Peasant done building " + buildingType.name());
        isBuilding.set(false);
        lock.unlock();
    }

    /**
     * Determines if a peasant is free or not.
     * This means that the peasant is neither harvesting, nor building.
     *
     * @return Whether he is free
     */
    public boolean isFree() {
        return !isHarvesting.get() && !isBuilding.get();
    }

}
