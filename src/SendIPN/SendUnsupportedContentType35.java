/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SendIPN;

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
 * CTSW008
 *
 * Send an IPM with unsupported content-type:
 * edi-messaging(35)
 *
 * Expected:
 * - Gateway rejects message
 * - Returns NDR
 * - reason = unable-to-transfer
 * - diagnostic = content-type-not-supported
 */
public final class SendUnsupportedContentType35 {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS
            = "\"3001\"/Internet=192.168.22.199+3001";

    private static final String P7_USER_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String P7_USER_PASSWORD = "amhs";

    private static final String RECIPIENT_OR_ADDRESS
            = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String ATS_PRIORITY = "FF";

    private static final int CONTENT_TYPE = 35;

    private static final String MESSAGE_TEXT
            = "CTSW008 TEST MESSAGE\r\n"
            + "ORIGIN: VVTSMHSA\r\n"
            + "DESTINATION: VVTSOPTA\r\n"
            + "CONTENT-TYPE: edi-messaging(35)\r\n"
            + "EXPECTED RESULT: NDR - CONTENT TYPE NOT SUPPORTED.";

    private SendUnsupportedContentType35() {
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
            System.out.println("========================================");
            System.out.println("CTSW008 - CONTENT TYPE 35 TEST");
            System.out.println("========================================");
            System.out.println("Originator   : " + P7_USER_OR_ADDRESS);
            System.out.println("Recipient    : " + RECIPIENT_OR_ADDRESS);
            System.out.println("Content-Type : 35 (edi-messaging)");
            System.out.println();

            session.bind();

            System.out.println("P7 bind successful.");

            message = new X400Msg(session);

            buildMessage(message);

            System.out.println("Submitting message...");

            message.sendMsg(session);

            System.out.println("Message submitted successfully.");
            System.out.println("Message ID      : "
                    + message.getMessageIdentifier());
            System.out.println("IPM ID          : "
                    + message.getMessageIPMIdentifier());
            System.out.println("Submission time : "
                    + message.getSubmissionTime());

            System.out.println();
            System.out.println("Expected CTSW008 result:");
            System.out.println("  Gateway REJECTS this IPM");
            System.out.println("  NDR reason     : unable-to-transfer");
            System.out.println("  NDR diagnostic : content-type-not-supported");

        } catch (X400APIException ex) {

            System.err.println("X.400 operation failed: "
                    + ex.getMessage());

            System.err.println("Native error code: "
                    + ex.getNativeErrorCode());

            System.err.println("Native error text: "
                    + ex.getNativeErrorString());

            ex.printStackTrace(System.err);

        } finally {

            releaseMessage(message);
            unbind(session);
        }
    }

    private static void buildMessage(X400Msg message)
            throws X400APIException {

        SimpleDateFormat filingTimeFormat
                = new SimpleDateFormat(
                        "ddHHmm",
                        Locale.ENGLISH
                );

        filingTimeFormat.setTimeZone(
                TimeZone.getTimeZone("UTC")
        );

        message.setFrom(P7_USER_OR_ADDRESS);

        message.setTo(
                RECIPIENT_OR_ADDRESS,
                X400Msg.DR_Request.DR_NON_DELIVERY_REPORT,
                X400Msg.IPN_NON_RECEIPT_NOTIFICATION
        );

        message.setPriority(
                X400Msg.X400_Priority.NORMAL_PRIORITY
        );

        message.setIntParam(
                AMHS_att.ATS_N_EXTENDED,
                0
        );

        message.setStringparam(
                AMHS_att.ATS_S_FILING_TIME,
                filingTimeFormat.format(new Date())
        );

        message.setStringparam(
                AMHS_att.ATS_S_PRIORITY_INDICATOR,
                ATS_PRIORITY
        );

        message.setStringparam(
                X400_att.X400_S_ORIGINATORS_REFERENCE,
                "CTSW008 CONTENT TYPE 35"
        );

        /*
         * CTSW008 test value:
         *
         * 35 = edi-messaging
         *
         * Unsupported by AMHS/SWIM Gateway.
         */
        message.setIntParam(
                X400_att.X400_N_CONTENT_TYPE,
                CONTENT_TYPE
        );

        message.setStringparam(
                AMHS_att.ATS_S_TEXT,
                MESSAGE_TEXT
        );
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