// package concurent.student.second;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Peasant extends Personnel {

    private static final int HARVEST_WAIT_TIME = 100;
    private static final int HARVEST_AMOUNT = 10;

    private AtomicBoolean isHarvesting = new AtomicBoolean(false);
    private AtomicBoolean isBuilding = new AtomicBoolean(false);

    private Resources baseResources = this.getOwner().getResources();
    private List<Building> baseBuildings = this.getOwner().getBuildings();

    private Peasant(Base owner) {
        super(220, owner, 5, 6, UnitType.PEASANT);
    }

    public static Peasant createPeasant(Base owner) {
        return new Peasant(owner);
    }

    /**
     * Starts gathering gold.
     */
    public void startMining() {
        this.isHarvesting.set(true);
        new Thread(() -> {
            while (this.isHarvesting.get()) {
                sleepForMsec(HARVEST_WAIT_TIME);
                baseResources.addGold(HARVEST_AMOUNT);
            }
        }).start();
        System.out.println("Peasant starting mining");
    }

    /**
     * Starts gathering wood.
     */
    public void startCuttingWood() {
        this.isHarvesting.set(true);
        new Thread(() -> {
            while (this.isHarvesting.get()) {
                sleepForMsec(HARVEST_WAIT_TIME);
                baseResources.addWood(HARVEST_AMOUNT);
            }
        }).start();
        System.out.println("Peasant starting cutting wood");
    }

    /**
     * Peasant should stop all harvesting once this is invoked
     */
    public void stopHarvesting() {
        this.isHarvesting.set(false);
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
    public boolean tryBuilding(UnitType buildingType) {
        if (baseResources.canBuild(buildingType.goldCost, buildingType.woodCost)) {
            new Thread(() -> {
                startBuilding(buildingType);
            }).start();
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
    private void startBuilding(UnitType buildingType) {
        if (isBuilding.compareAndSet(false, true)) {
            baseResources.removeCost(buildingType.goldCost, buildingType.woodCost);
            Building newBuilding = Building.createBuilding(buildingType, this.getOwner());
            baseBuildings.add(newBuilding);
            sleepForMsec(buildingType.buildTime);
            isBuilding.set(false);
        }
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
