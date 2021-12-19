public enum UnitType {

    FARM(80, 20, 0, 2000),
    LUMBERMILL(120, 0, 0, 4000),
    BLACKSMITH(140, 60, 0, 5000),
    BARRACKS(160, 60, 0, 6000),

    PEASANT(75, 0, 1, 1000);

    public int goldCost;
    public int woodCost;
    public int foodCost;
    public int buildTime;

    private UnitType(int goldCost, int woodCost, int foodCost, int buildTime) {
        this.goldCost = goldCost;
        this.woodCost = woodCost;
        this.foodCost = foodCost;
        this.buildTime = buildTime;
    }

}
