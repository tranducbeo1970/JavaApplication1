/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.awt.MouseInfo;
import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class Clock_Timer extends TimerTask {

    Clock_Timer() {
    }

    /**
     * When the timer executes, this code is run.
     */
    public void run() {
        try {
//            long diffTime = 0, diffDays = 0;
//            Date now = new Date();
//            diffTime = now.getTime() - Global.lastTime.getTime();
//            diffDays = diffTime / (1000 * 60);
//            if (diffDays > 1) {
//                System.out.println("Time out roi");
//            }

//            long idleTime = 0;
            
            Global.currLocation = MouseInfo.getPointerInfo().getLocation();
            Global.newLocation = MouseInfo.getPointerInfo().getLocation();
            if (Global.newLocation.equals(Global.currLocation)) {
                //not moved
                Global.idleTime = System.currentTimeMillis() - Global.start;
//                System.out.println("Idle time was: " + Global.idleTime);
                if (Global.idleTime > 1000) {
                    System.out.println("dung yen 1000 roi ");
                }

            } else {
                System.out.println("Idle time was: " + Global.idleTime);
                Global.idleTime = 0;
                Global.start = System.currentTimeMillis();

            }
            Global.currLocation = Global.newLocation;

        } catch (Exception e) {
            System.out.println("Error - ClockDisplay_Timer.run: " + e.getMessage());

        }
    }

}
