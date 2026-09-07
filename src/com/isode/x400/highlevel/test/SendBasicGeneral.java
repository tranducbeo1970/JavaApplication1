/* 
 * Copyright (c) 2008-2010, Isode Limited, London, England.
 * All rights reserved.
 * 
 * Acquisition and use of this software and related materials for any
 * purpose requires a written licence agreement from Isode Limited,
 * or a written licence from an organisation licenced by Isode Limited
 * to grant such a licence.
 */
package com.isode.x400.highlevel.test;

import com.isode.x400.highlevel.BodypartGeneralText;
import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg.X400_Priority;
import com.isode.x400api.AMHS_att;
import com.isode.x400api.X400_att;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Test program that connects to either an MTA's P3 channel or to a P7 Message
 * Store, and submits a test message to itself.
 *
 */
public class SendBasicGeneral {

    private static boolean send_military_message = false;
    private static String p7_message_store_presentation_address = "\"3001\"/URI+0000+URL+itot://192.168.22.186:3001";

    private static String p7_user_or_address = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static String recipient_or_address = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static String p7_user_password = "amhs";

    final public static char CHAR_SOH = 1;
    final public static char CHAR_STX = 2;

    // AMHS 
    public static final String AMHS_PRI = "PRI: ";
    public static final String AMHS_FT = "FT: ";
    public static final String AMHS_OHI = "OHI: ";
    public static final String ICAO_EXTENDED_AMHS_POLICY_IDENTIFIER = "1.3.27.8.0.0";
    public static final String CR_LF = "\r\n";

    
     private static String BuildIA5Content(String in) {
        String sohCharacter = "\u0001";
        String sotCharacter = "\u0002";
        String ohi = "OHI";
        //String dtime = outbox.getAtsFilingTime().toString();
        //2024-08-12 12:06:37
        
        //String fillingtime = dtime.substring(8, 10) + dtime.substring(11, 13) + dtime.substring(14, 16);
         
        String tmp = sohCharacter + "PRI:" + "FF" + "\r\nFT: " + "121212" + "\r\n";
        if (ohi.length() > 0) {
            tmp = tmp + "OHI:" + ohi + "\r\n";
        }
        tmp =  tmp + sotCharacter  + in;
        
        return (tmp);
    }
    
    public static void main(String[] args) {

        P7BindSession bind_session = null;

        System.out.println("Connecting to the P7 Message Store...");

        // Create a new P7 bind session object with the session values necessary for the bind
        P7BindSession connection = new P7BindSession(p7_message_store_presentation_address,
                p7_user_or_address, p7_user_password);

        // Bind to the P7 Message Store
        try {
            connection.bind();
        } catch (X400APIException e) {
            System.exit(1);
        }

        bind_session = connection;

        X400Msg x400msg = new X400Msg(bind_session);

        int ipn_request = X400Msg.IPN_NON_RECEIPT_NOTIFICATION;

        try {

            x400msg.setIntParam(AMHS_att.ATS_N_EXTENDED, 0);
            x400msg.setAllRecipPrecedence(57); // For example
            x400msg.setTo(recipient_or_address, X400Msg.DR_Request.DR_NON_DELIVERY_REPORT, ipn_request);

            x400msg.setPriority(X400_Priority.NORMAL_PRIORITY);

            /*
                        x400msg.setStringparam(AMHS_att.ATS_S_FILING_TIME, "022712");
                        x400msg.setStringparam(AMHS_att.ATS_S_PRIORITY_INDICATOR, "FF");
                        String msgText = "BASIC GENERAL 0002";
                        x400msg.setStringparam(AMHS_att.ATS_S_TEXT, msgText);
             */
            
            
             //int status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(x400msg, AMHS_att.ATS_S_FILING_TIME, "022712",0);
             
             
            
            //x400msg.setStringparam(AMHS_att.ATS_S_PRIORITY_INDICATOR, "DD");
            
        ////    SimpleDateFormat formatter2 = new SimpleDateFormat("yyMMddHHmmssZ", new Locale("en"));
       //     Date now = new Date();

            /*
            x400msg.setStringparam(X400_att.X400_S_AUTHORIZATION_TIME, formatter2.format(now));
            x400msg.setStringparam(X400_att.X400_S_ORIGINATORS_REFERENCE, "MY BASIC REFERENCE OHI");
            x400msg.setAllRecipPrecedence(57);
            */
            
            
            
// Create the content of the message, using the Basic AMHS Encoding
            StringBuilder sb = new StringBuilder();

// Add the mandatory Priority and Filing Time
            sb.append(CHAR_SOH + AMHS_PRI + "FF" + CR_LF + AMHS_FT + "121212" + CR_LF);
            // Add the Optional Heading Information in case that it is set
            sb.append(AMHS_OHI + "THIS IS OHI " + CR_LF + CR_LF);
//TX and the message body
            sb.append(CHAR_STX + "TEST NEW FROM LEO'S GUIDANCE");

            // Create the General Text bodypart by LEO guide
            BodypartGeneralText gtbp = new BodypartGeneralText("1 6", sb.toString());
            x400msg.addBodypart(gtbp);

           // BodypartGeneralText gtbp = new BodypartGeneralText("1 6",BuildIA5Content("TEST DUC FUNCTION"));
           // x400msg.addBodypart(gtbp);
            
                            
  //          BodypartGeneralText gt1 = new BodypartGeneralText("1 6", BuildIA5Content("TEST BASIC GENERAL"));
  //          x400msg.addBodypart(gt1);

            // Set the content type 
            x400msg.setIntParam(X400_att.X400_N_CONTENT_TYPE, 22);

            // If a Message IPM Identifier wasn't assigned, read the value that was calculated
            String ipm_id = x400msg.getMessageIPMIdentifier();

            // Send the message to the Message Store
            x400msg.sendMsg(bind_session);

            // Unbind cleanly from the Message Store
            bind_session.unbind();

            // The message was correctly submitted to the Message Store
            // Retrieve the message submission ID (assigned by the MTA) and the submission time
            final String msg_sub_id = x400msg.getMessageIdentifier();
            final String msg_st = x400msg.getSubmissionTime();

            System.out.println("Message submitted.\nMessage Submission ID: "
                    + msg_sub_id + "\nIPM ID: " + ipm_id + "\nSubmission time: " + msg_st);

        } catch (X400APIException e1) {
            System.out.println("Exception code = " + e1.getNativeErrorCode());
            try {
                bind_session.unbind();
            } catch (X400APIException e) {
            }
            System.exit(1);
        }

    }

}
