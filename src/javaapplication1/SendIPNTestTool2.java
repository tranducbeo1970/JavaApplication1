/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.ReceiveMsg;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400api.MSMessage;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;

public class SendIPNTestTool2 {

    private static String p7_message_store_presentation_address = "\"3001\"/Internet=192.168.22.199+3001";

    private static String p7_user_or_address = "/CN=VVNBZQZX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static String p7_user_password = "amhs";

    public static void main(String[] args) {

        System.out.println("Connecting to the P7 Message Store...");
        System.out.println(p7_message_store_presentation_address);

        P7BindSession connection = new P7BindSession(p7_message_store_presentation_address,p7_user_or_address,p7_user_password);

        try {
            connection.bind();

            ReceiveMsg rms = new ReceiveMsg(connection, 345281);
            
            MSMessage ipnMessage = new MSMessage();

            int result = X400ms.x400_ms_msgmakeIPN(rms,0,ipnMessage);

            System.out.println("Make IPN result: " + result);
            System.out.println("Message ID: " + rms.getMessageIdentifier());

            int code = X400ms.x400_ms_msgsend(ipnMessage);

            if (code != X400_att.X400_E_NOERROR) {
                System.out.println("ERROR: Send IPN failed. Code = " + code);
            } else {
                System.out.println("IPN sent successfully.");
            }

            connection.unbind();

        } catch (X400APIException e) {
            System.out.println("Exception code = " + e.getNativeErrorCode());
            try {
                connection.unbind();
            } catch (X400APIException ignored) {
            }
            System.exit(1);
        }
    }
}
