/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

/**
 *
 * @author Lam
 */
//import com.attech.amhs.messages.flightplan.cdn.Cdn;
import java.awt.MouseInfo;
import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CheckSystemIdle extends Thread {

    private Thread t;
    private String threadName;
    private String msg = "";
    private static DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    private static long idleTime = 0;
    private static long start;
    private static Point currLocation;

    public CheckSystemIdle(String name) {
        threadName = name;
        System.out.println("Creating " + threadName);

    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }

        start = System.currentTimeMillis();
        currLocation = MouseInfo.getPointerInfo().getLocation();
    }

    public void run() {
//    
        try {

            while (true) {
                Thread.sleep(1000);
                Point newLocation = MouseInfo.getPointerInfo().getLocation();
                if (newLocation.equals(currLocation)) {
                    //not moved
                    idleTime = System.currentTimeMillis() - start;
                    System.out.println("Idle time was" + idleTime);
                } else {
                    System.out.println("Idle move" + idleTime);
                    idleTime = 0;
                    start = System.currentTimeMillis();
                     System.out.println("Idle move" + idleTime);
                    
                }
                currLocation = newLocation;

            }
        } catch (Exception e) {
            System.out.println("Error - CheckSystemIdle.run: " + e.getMessage());

        }
    }

}
