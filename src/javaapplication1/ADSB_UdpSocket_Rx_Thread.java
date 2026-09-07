/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
//import com.attech.stripserver.main.Global;
//import com.attech.stripserver.main.JFrame_Main;
//import org.apache.log4j.Logger;

/**
 *
 * @author ADMIN
 */
public class ADSB_UdpSocket_Rx_Thread extends Thread {

    private Thread thread;
    private String threadName;
    private boolean running;
    DatagramSocket Adsb_Rx_serverSocket;
    private boolean requestingStop;
//    private static final Logger logger = Logger.getLogger(ADSB_UdpSocket_Rx_Thread.class);
//    private static DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    public ADSB_UdpSocket_Rx_Thread(String name) {
        threadName = name;
        System.out.println("Creating " + threadName);
        this.running = false;
//        System.out.println("SocketReceiveSimData_Thread");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (thread == null) {
            thread = new Thread(this, threadName);
            thread.start();
        }
//        Global.server_Socket_ADSB_Rx_Status = true;
    }

    public void run() {
        try {
            Date d = new Date();
//            JFrame_Main.HienLog("Info - " + df.format(d) + " - ADSB UdpSocket_Rx_Thread run ", 0);
//            int length;
            while (requestingStop == false) {
                Adsb_Rx_serverSocket = new DatagramSocket(1111);
//            System.out.println("UdpSocket_ADSB_port" + Global.server_Adsb_Rx_Port);
//             Global.server_Socket_ADSB_Rx_Status = true;
//             Global.adsbStatus="ON";
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                byte[] data = new byte[1024];
                this.running = true;

//            while (Global.server_Socket_ADSB_Rx_requestingStop ==false) {
                Adsb_Rx_serverSocket.receive(receivePacket);
                data = receivePacket.getData();
//                Global.adsbData.addLast(data);
//
//                Global.adsbStatusTime = new Date();
//
//                JFrame_Main.HienAdsbRxMonitor(data);
                Thread.sleep(10);
                Arrays.fill(receiveData, (byte) 0);
            }
//             serverSocket.close();
        } catch (Exception e) {
//            System.err.println("Error - UdpSocket_ADSB_Rx_Thread.run: " + e.getMessage());
//            Date d = new Date();
//            JFrame_Main.HienLog("Error - " + df.format(d) + " - UdpSocket_ADSB_Rx_Thread.run: " + e.getMessage(), 1);
//            logger.error(e.getMessage());
        } finally {
//            Global.Adsb_Rx_serverSocket.close();
//            Global.server_Socket_ADSB_Rx_Status = false;
//            this.running = false;
//            logger.info("ADSB UdpSocket_Rx_Thread is closed!");
            System.err.println("Stop UdpSocket_ADSB_Rx_Thread: " + threadName);
//            Date d = new Date();
//            JFrame_Main.HienLog("Info - " + df.format(d) + " - ADSB UdpSocket_Rx_Thread Stop ", 0);
        }
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * @return the requestingStop
     */
    public boolean isRequestingStop() {
//        return Global.server_Socket_ADSB_Rx_requestingStop;
        return true;
    }

    /**
     * @param requestingStop the requestingStop to set
     */
    public synchronized void setRequestingStop(boolean requestingStop) {
        requestingStop = true;
    }
}
