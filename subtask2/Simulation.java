// package concurent.student.second;

import java.util.concurrent.CountDownLatch;

public class Simulation {

    public static void main(String[] args) {
        Base col1 = new Base("Horde");
        Base col2 = new Base("Allience");
        CountDownLatch latch = new CountDownLatch(2);
        new Thread(() -> {
            col1.startPreparation();
            col1.assembleArmy(latch);
        }).start();
        new Thread(() -> {
            col2.startPreparation();
            col2.assembleArmy(latch);
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Both bases finished their war preparation and assembled their armies");
        CountDownLatch warLatch = new CountDownLatch(2);
        Thread col1War = new Thread(() -> col1.goToWar(col2.getArmy(), warLatch));
        Thread col2War = new Thread(() -> col2.goToWar(col1.getArmy(), warLatch));

        col1War.start();
        col2War.start();

        try {
            col1War.join();
            col2War.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
