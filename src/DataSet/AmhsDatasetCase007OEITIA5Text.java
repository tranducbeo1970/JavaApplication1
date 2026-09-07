/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataSet;

import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400api.AMHS_att;
import com.isode.x400api.X400_att;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * CASE_007
 *
 * Purpose:
 * Compare with CASE_006 and change ONLY OEIT:
 *
 * CASE_006:
 *   OEIT not explicitly set
 *
 * CASE_007:
 *   OEIT = "ia5-text"
 *
 * Everything else is intentionally kept identical.
 */
public final class AmhsDatasetCase007OEITIA5Text {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS
            = "\"3001\"/Internet=192.168.22.186+3001";

    private static final String P7_USER_OR_ADDRESS
            = "/CN=VVTSTEST/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String P7_USER_PASSWORD
            = "amhs";

    private static final String RECIPIENT_1_OR_ADDRESS
            = "/CN=VVTSOPTB/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String RECIPIENT_2_OR_ADDRESS
            = "/CN=VVTSYFYX/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String CASE_ID
            = "BASIC_GG_OEIT_IA5TEXT_007";

    private static final String ATS_PRIORITY
            = "GG";

    private static final X400Msg.X400_Priority X400_PRIORITY
            = X400Msg.X400_Priority.NORMAL_PRIORITY;

    private static final int ATS_EXTENDED
            = 0;

    private static final int CONTENT_TYPE
            = 22;

    private static final String ORIGINATORS_REFERENCE
            = "ORIGINATOR REF TEST 003";

    private static final String MESSAGE_TEXT
            = "TEST BASIC GG 002\r\n"
            + "ORIGIN: VVTSMHSA\r\n"
            + "DESTINATION: VVTSOPTA\r\n"
            + "CONTROLLED AMHS DATASET MESSAGE.";

    private static final String OEIT
            = "ia5-text";

    private AmhsDatasetCase007OEITIA5Text() {
    }

    public static void main(String[] args) {

        P7BindSession session = new P7BindSession(
                P7_MESSAGE_STORE_PRESENTATION_ADDRESS,
                P7_USER_OR_ADDRESS,
                P7_USER_PASSWORD,
                false
        );

        X400Msg message = null;

        try {
            printTestCase();

            System.out.println();
            System.out.println("Connecting to P7 Message Store...");

            session.bind();

            System.out.println("P7 bind successful.");

            message = new X400Msg(session);

            buildBasicAtsMessage(message);

            System.out.println();
            System.out.println("Submitting message...");

            message.sendMsg(session);

            System.out.println();
            System.out.println("==========================================");
            System.out.println("MESSAGE SUBMITTED SUCCESSFULLY");
            System.out.println("==========================================");

            System.out.println("Case ID         : " + CASE_ID);
            System.out.println("Message ID      : " + message.getMessageIdentifier());
            System.out.println("IPM ID          : " + message.getMessageIPMIdentifier());
            System.out.println("Submission time : " + message.getSubmissionTime());

            System.out.println("==========================================");

        } catch (X400APIException ex) {

            System.err.println();
            System.err.println("X.400 operation failed.");
            System.err.println("Message           : " + ex.getMessage());
            System.err.println("Native error code : " + ex.getNativeErrorCode());
            System.err.println("Native error text : " + ex.getNativeErrorString());

            ex.printStackTrace(System.err);

        } finally {
            releaseMessage(message);
            unbind(session);
        }
    }

    private static void buildBasicAtsMessage(X400Msg message)
            throws X400APIException {

        SimpleDateFormat filingTimeFormat
                = new SimpleDateFormat("ddHHmm", Locale.ENGLISH);

        filingTimeFormat.setTimeZone(
                TimeZone.getTimeZone("UTC")
        );

        String filingTime
                = filingTimeFormat.format(new Date());

        // Originator
        message.setFrom(
                P7_USER_OR_ADDRESS
        );

        // Recipients
        message.setTo(
                RECIPIENT_1_OR_ADDRESS,
                X400Msg.DR_Request.DR_NON_DELIVERY_REPORT,
                X400Msg.IPN_NON_RECEIPT_NOTIFICATION
        );

        message.setTo(
                RECIPIENT_2_OR_ADDRESS,
                X400Msg.DR_Request.DR_NON_DELIVERY_REPORT,
                X400Msg.IPN_NON_RECEIPT_NOTIFICATION
        );

        // X.400 priority
        message.setPriority(
                X400_PRIORITY
        );

        // BASIC ATSMHS
        message.setIntParam(
                AMHS_att.ATS_N_EXTENDED,
                ATS_EXTENDED
        );

        // ATS Header
        message.setStringparam(
                AMHS_att.ATS_S_FILING_TIME,
                filingTime
        );

        message.setStringparam(
                AMHS_att.ATS_S_PRIORITY_INDICATOR,
                ATS_PRIORITY
        );

        // Originators reference
        message.setStringparam(
                X400_att.X400_S_ORIGINATORS_REFERENCE,
                ORIGINATORS_REFERENCE
        );

        // Content type
        message.setIntParam(
                X400_att.X400_N_CONTENT_TYPE,
                CONTENT_TYPE
        );

        // ========================================================
        // CONTROLLED VARIABLE FOR CASE_007
        // ========================================================

        message.setStringparam(
                X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES,
                OEIT
        );

        // ATS text
        message.setStringparam(
                AMHS_att.ATS_S_TEXT,
                MESSAGE_TEXT
        );

        System.out.println();
        System.out.println("Message parameters prepared:");
        System.out.println("Filing time      : " + filingTime);
        System.out.println("Originator       : " + P7_USER_OR_ADDRESS);
        System.out.println("Recipient[1]     : " + RECIPIENT_1_OR_ADDRESS);
        System.out.println("Recipient[2]     : " + RECIPIENT_2_OR_ADDRESS);
        System.out.println("ATS priority     : " + ATS_PRIORITY);
        System.out.println("X400 priority    : " + X400_PRIORITY);
        System.out.println("Originator ref   : " + ORIGINATORS_REFERENCE);
        System.out.println("OEIT             : " + OEIT);
    }

    private static void printTestCase() {

        System.out.println("==========================================");
        System.out.println("AMHS DATASET CASE 007");
        System.out.println("BASIC GG - OEIT ia5-text");
        System.out.println("==========================================");

        System.out.println("Case ID          : " + CASE_ID);
        System.out.println("Originator       : " + P7_USER_OR_ADDRESS);
        System.out.println("Recipient[1]     : " + RECIPIENT_1_OR_ADDRESS);
        System.out.println("Recipient[2]     : " + RECIPIENT_2_OR_ADDRESS);
        System.out.println("Service          : BASIC");
        System.out.println("ATS priority     : " + ATS_PRIORITY);
        System.out.println("X400 priority    : " + X400_PRIORITY);
        System.out.println("Originator ref   : " + ORIGINATORS_REFERENCE);
        System.out.println("Content type     : " + CONTENT_TYPE);
        System.out.println("OEIT             : " + OEIT);

        System.out.println();
        System.out.println("ATS MESSAGE TEXT:");
        System.out.println("------------------------------------------");
        System.out.println(MESSAGE_TEXT);
        System.out.println("------------------------------------------");
    }

    private static void releaseMessage(X400Msg message) {

        if (message == null) {
            return;
        }

        try {
            message.delete(true);
        } catch (X400APIException ex) {
            System.err.println(
                    "Cannot release X.400 message: "
                            + ex.getMessage()
            );
        }
    }

    private static void unbind(P7BindSession session) {

        if (session == null || !session.isBound()) {
            return;
        }

        try {
            session.unbind();
            System.out.println("P7 session closed.");
        } catch (X400APIException ex) {
            System.err.println(
                    "Cannot unbind P7 session: "
                            + ex.getMessage()
            );
        }
    }
}
