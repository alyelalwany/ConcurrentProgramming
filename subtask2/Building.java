// package concurent.student.second;

public class Building extends Unit {

    private Building(Base owner, UnitType type) {
        super(owner, type);
    }

    public static Building createBuilding(UnitType type, Base owner) {
        if (type == UnitType.FARM)
            return createFarm(owner);
        if (type == UnitType.LUMBERMILL)
            return createLumbermill(owner);
        if (type == UnitType.BLACKSMITH)
            return createBlacksmith(owner);
        if (type == UnitType.BARRACKS)
            return createBarracks(owner);
        return null;
    }

    public static Building createFarm(Base owner) {
        System.out.println(owner.getName() + " is creating farm");
        Building farm = new Building(owner, UnitType.FARM);
        // building a farm increases the capacity
        owner.getResources().farmBuilt();
        return farm;
    }

    public static Building createLumbermill(Base owner) {
        System.out.println(owner.getName() + " is creating lumbermill");
        return new Building(owner, UnitType.LUMBERMILL);
    }

    public static Building createBlacksmith(Base owner) {
        System.out.println(owner.getName() + " is creating blacksmith");
        return new Building(owner, UnitType.BLACKSMITH);
    }

    public static Building createBarracks(Base owner) {
        System.out.println(owner.getName() + " is creating barracks");
        return new Building(owner, UnitType.BARRACKS);
    }
}
