/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.util.Map;
//import static javaapplication1.FrmViewLog.mapAdsb;

/**
 *
 * @author root
 */
public class JavaApplication1 {

//    public static Map<String, ADSB_UdpSocket_Rx_Thread> mapAdsb;
    public static void main(String[] args) {

        String inputPassword = "1Aann123";
        boolean s = validation_Password(inputPassword);

//        FrmViewLog mainForm = new FrmViewLog();
//        mainForm.setVisible(true);
//        ADSB_UdpSocket_Rx_Thread r = new ADSB_UdpSocket_Rx_Thread("ADSB");
//        r.start();
//        Global.mapAdsb.put("ADSB", r);
//            String current = new java.io
    }

    private static boolean validation_Password(final String PASSWORD_Arg) {
        boolean result = false;
        try {
            if (PASSWORD_Arg != null) {
                //_________________________
                //Parameteres
                final String MIN_LENGHT = "8";
                final String MAX_LENGHT = "20";
                final boolean SPECIAL_CHAR_NEEDED = true;

                //_________________________
                //Modules
                final String ONE_DIGIT = "(?=.*[0-9])";  //(?=.*[0-9]) a digit must occur at least once
                final String LOWER_CASE = "(?=.*[a-z])";  //(?=.*[a-z]) a lower case letter must occur at least once
                final String UPPER_CASE = "(?=.*[A-Z])";  //(?=.*[A-Z]) an upper case letter must occur at least once
                final String NO_SPACE = "(?=\\S+$)";  //(?=\\S+$) no whitespace allowed in the entire string
                //final String MIN_CHAR = ".{" + MIN_LENGHT + ",}";  //.{8,} at least 8 characters
                final String MIN_MAX_CHAR = ".{" + MIN_LENGHT + "," + MAX_LENGHT + "}";  //.{5,10} represents minimum of 5 characters and maximum of 10 characters

                final String SPECIAL_CHAR;
                if (SPECIAL_CHAR_NEEDED == true) {
                    SPECIAL_CHAR = "(?=.*[@#$%^&+=])"; //(?=.*[@#$%^&+=]) a special character must occur at least once
                } else {
                    SPECIAL_CHAR = "";
                }
                //_________________________
                //Pattern
                //String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
                //final String PATTERN = ONE_DIGIT + LOWER_CASE + UPPER_CASE + SPECIAL_CHAR + NO_SPACE + MIN_MAX_CHAR;
                final String PATTERN = ONE_DIGIT + LOWER_CASE + UPPER_CASE + MIN_MAX_CHAR;
                //_________________________
                result = PASSWORD_Arg.matches(PATTERN);
                //_________________________
            }

        } catch (Exception ex) {
            result = false;
        }

        return result;
    }
}
