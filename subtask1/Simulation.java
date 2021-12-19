package concurent.student.first;

public class Simulation {

    public static void main(String[] args) {
        Base col1 = new Base("SuperCoolBase");
        new Thread(() -> {
            col1.startPreparation();
        }).start();
    }
}
