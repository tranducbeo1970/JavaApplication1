/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.awt.Point;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author root
 */
public class Global {
    static public String active;
    static public Date lastTime;
    static public Point currLocation;
    static public Point newLocation;
    static long idleTime; 
    static long start; 
     public static Map<String, ADSB_UdpSocket_Rx_Thread> mapAdsb;
}
