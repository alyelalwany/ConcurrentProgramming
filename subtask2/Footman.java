// package concurent.student.second;

public class Footman extends Personnel {

    private Footman(Base owner) {
        super(420, owner, 12, 15, UnitType.FOOTMAN);
    }

    public static Footman createFootman(Base owner) {
        return new Footman(owner);
    }

}
