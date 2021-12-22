package concurent.student.second;

import java.util.concurrent.atomic.AtomicInteger;

public class Resources {

    private static final int CAPACITY_LOWER_LIMIT = UnitType.PEASANT.foodCost * 5;

    private AtomicInteger gold;
    private AtomicInteger wood;
    private AtomicInteger capacityLimit;
    private AtomicInteger capacity;

    public Resources() {
        this.gold = new AtomicInteger(UnitType.PEASANT.goldCost * 5);
        this.wood = new AtomicInteger(0);
        this.capacityLimit = new AtomicInteger(CAPACITY_LOWER_LIMIT);
        this.capacity = new AtomicInteger(0);
    }

    public int getGold() {
        return gold.get();
    }

    public void addGold(int amount) {
        this.gold.set(this.gold.get() + amount);
    }

    public int getWood() {
        return wood.get();
    }

    public void addWood(int amount) {
        this.wood.set(this.wood.get() + amount);
    }

    /**
     * Determines if a building can be built or not based on the current resources.
     *
     * @param goldCost Gold cost of the building
     * @param woodCost Wood cost of the building
     * @return True, if there are enough resources to build it, false otherwise
     */
    public boolean canBuild(int goldCost, int woodCost) {
        return gold.get() >= goldCost && wood.get() >= woodCost;
    }

    /**
     * Determines if a unit can be trained based on the current resources.
     *
     * @param goldCost Gold cost of the unit
     * @param woodCost Wood cost of the unit
     * @param foodCost Food cost of the unit, uses the capacity resource
     * @return True, if there are enough resources to train it, false otherwise
     */
    public boolean canTrain(int goldCost, int woodCost, int foodCost) {
        return gold.get() >= goldCost && wood.get() >= woodCost && (capacity.get() + foodCost <= capacityLimit.get());
    }

    public void removeCost(int gold, int wood) {
        this.gold.set(this.gold.get() - gold);
        this.wood.set(this.wood.get() - wood);
    }

    public int getCapacityLimit() {
        return this.capacityLimit.get();
    }

    /**
     * Building a farm increases the capacity limit by 10
     */
    public void farmBuilt() {
        this.capacityLimit.set(this.capacityLimit.get() + 10);
    }

    public int getCapacity() {
        return this.capacity.get();
    }

    public void updateCapacity(int foodCost) {
        this.capacity.set(this.capacity.get() + foodCost);
    }

}
