/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SendIPN;

import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Session;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tranduc
 */
public class SendIPN {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS = "\"3001\"/Internet=192.168.22.186+3001";
    private static final String P7_USER_OR_ADDRESS = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String P7_USER_PASSWORD = "amhs";

    private static final String ORIGIN_OR_ADDRESS = "/CN=VVNBZQZX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String RECIPIENT_OR_ADDRESS = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String MESSAGE_IPM_IDENTIFIER = "/PRMD=VIETNAM/ADMD=ICAO/C=XX/;gateway.attech.446-260820.15531";

    private static final char CHAR_SOH = 1;
    private static final char CHAR_STX = 2;

    private static final String AMHS_PRI = "PRI: ";
    private static final String AMHS_FT = "FT: ";
    private static final String AMHS_OHI = "OHI: ";
    private static final String CR_LF = "\r\n";

    private static final int PRIORITY = 0;
    private static final int CONTENT_TYPE = 22;
    private static final int DISCLOSURE = 1;
    private static final int IMPLICIT_CONVERSION_PROHIBITED = 0;
    private static final int ALTERNATE_RECIPIENT_ALLOWED = 1;
    private static final int CONTENT_RETURN_REQUEST = 1;
    private static final int RECIPIENT_REASSIGNMENT_PROHIBITED = 0;
    private static final int DL_EXPANSION_PROHIBITED = 0;
    private static final int CONVERSION_WITH_LOSS_PROHIBITED = 0;

    private static final String OEIT = "ia5-text";
    private static final String GENERAL_TEXT_CHARSETS = "1 6";
    
    //private static Session session;
    static    P7BindSession session = null;
    
    private static int result;

      
    public static void main(String[] args) {

       // P7BindSession session = null;
        
        X400Msg message = null;
      //  MSMessage ipnMessage = null;

//        System.out.println("========================================");
//        System.out.println("Creating P7 session...");
//        System.out.println("========================================");

        session = new P7BindSession(P7_MESSAGE_STORE_PRESENTATION_ADDRESS, P7_USER_OR_ADDRESS, P7_USER_PASSWORD);

            System.out.println("Binding...");

        try {
            session.bind();
        } catch (X400APIException ex) {
            Logger.getLogger(SendIPN.class.getName()).log(Level.SEVERE, null, ex);
        }

        message = new X400Msg(session);

        int code = X400ms.x400_ms_msgnew(session, X400_att.X400_MSG_MESSAGE, message);

        //set(message, X400_att.X400_N_IS_IPN, 1);
        //X400ms.x400_ms_msgaddintparam(message, X400_att.X400_N_IS_IPN, 1);   ddđ
      //  if (code != X400_att.X400_E_NOERROR) {
       //     System.out.println("Cannot create X400 MESSAGE");
        //}

       // System.out.println("X400 MESSAGE created OK");
        
        DeliverIpnMessage ipnMessage = new DeliverIpnMessage();
        //-ipnMessage.setSubjectIpmId(refAmhsMessage.getIpmId());
        ipnMessage.setSubjectIpmId("AAAAAAAAAAAAAAAAAAAA");
        // ipnMessage.setPriority(MtAttributes.PRIORITY_URGENT);
        ipnMessage.setPriority(5);
        ipnMessage.setOrigin("/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/");
 //       ipnMessage.setRecipient(new Recipient("/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/"));
        ipnMessage.setReceiptTime("280828101010Z");
        ipnMessage.setConversionProhibited(1);
        ipnMessage.setAckMode(0);
        
        try {
            send(ipnMessage);
        } catch (X400APIException ex) {
            Logger.getLogger(SendIPN.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public static void send(DeliverIpnMessage message) throws X400APIException {

        final MSMessage msMessage = new MSMessage();

        result = X400ms.x400_ms_msgnew(session, X400_att.X400_N_IS_IPN, msMessage);
        //if (result != X400_att.X400_E_NOERROR) {
            // X400mt.x400_mt_close(session);
            //String errString = X400mt.x400_mt_get_string_error(session, result);
        //    System.out.println("Create IPN fail (code: {} - {})");
         //   throw new X400APIException("Ertor");
       // }

        try {
            message.build(msMessage);
            result = X400ms.x400_ms_msgsend(msMessage);

            if (result != X400_att.X400_E_NOERROR) {
                // X400mt.x400_mt_close(session);

                System.out.println("Create IPN fail (code: {} - {})");
                throw new X400APIException("Delivery message fail (%s)");
            }

        } finally {
            delele(msMessage);
        }

    }
    private static void delele(MSMessage msMessage) {
        int result = X400ms.x400_ms_msgdel(msMessage,0);
        if (result != X400_att.X400_E_NOERROR) {
            String errString = X400ms.x400_ms_get_string_error(session, result);
            
        }
    }
}


