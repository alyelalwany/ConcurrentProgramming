package concurent.student.second;

import java.util.concurrent.TimeUnit;

public abstract class Unit {
    private final Base owner;
    private final UnitType unitType;

    public Unit(Base owner, UnitType unitType) {
        this.owner = owner;
        this.unitType = unitType;
    }

    public Base getOwner() {
        return owner;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    protected static void sleepForMsec(int sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

}
