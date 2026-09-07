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
 * Send an IPM with unsupported content-type.
 *
 * content-type = interpersonal-messaging-1984(2)
 *
 * Expected result:
 * - Gateway rejects the IPM.
 * - Gateway generates NDR.
 * - non-delivery-reason-code = unable-to-transfer
 * - non-delivery-diagnostic-code = content-type-not-supported
 */
public final class SendUnsupportedContentType {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS
            = "\"3001\"/Internet=192.168.22.199+3001";

    private static final String P7_USER_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String P7_USER_PASSWORD = "amhs";

    private static final String RECIPIENT_OR_ADDRESS
            = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String ATS_PRIORITY = "FF";

    /*
     * CTSW008 - Message 2
     *
     * 22 = interpersonal-messaging-1988  -> supported
     *  2 = interpersonal-messaging-1984  -> unsupported
     * 35 = edi-messaging                 -> unsupported
     *  0 = unidentified                  -> unsupported
     */
    private static final int CONTENT_TYPE = 2;

    private static final String MESSAGE_TEXT
            = "CTSW008 TEST MESSAGE\r\n"
            + "ORIGIN: VVTSMHSA\r\n"
            + "DESTINATION: VVTSOPTA\r\n"
            + "CONTENT-TYPE: interpersonal-messaging-1984(2)\r\n"
            + "EXPECTED RESULT: NDR - CONTENT TYPE NOT SUPPORTED.";

    private SendUnsupportedContentType() {
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
            System.out.println("CTSW008 - UNSUPPORTED CONTENT TYPE TEST");
            System.out.println("========================================");
            System.out.println("Server       : "
                    + P7_MESSAGE_STORE_PRESENTATION_ADDRESS);
            System.out.println("Originator   : "
                    + P7_USER_OR_ADDRESS);
            System.out.println("Recipient    : "
                    + RECIPIENT_OR_ADDRESS);
            System.out.println("Content-Type : "
                    + CONTENT_TYPE
                    + " (interpersonal-messaging-1984)");
            System.out.println();

            System.out.println("Connecting to P7 Message Store...");

            session.bind();

            System.out.println("P7 bind successful.");

            /*
             * X400Msg constructor creates the native message.
             */
            message = new X400Msg(session);

            buildCTSW008Message(message);

            System.out.println("Submitting CTSW008 test message...");

            /*
             * Important:
             *
             * sendMsg() succeeding only means the message was
             * successfully submitted to the Message Store.
             *
             * CTSW008 expects the downstream AMHS/SWIM Gateway
             * to reject the message and return an NDR.
             */
            message.sendMsg(session);

            System.out.println();
            System.out.println("Message submitted successfully.");
            System.out.println("Message ID      : "
                    + message.getMessageIdentifier());
            System.out.println("IPM ID          : "
                    + message.getMessageIPMIdentifier());
            System.out.println("Submission time : "
                    + message.getSubmissionTime());

            System.out.println();
            System.out.println("Expected CTSW008 result:");
            System.out.println(
                    "  Gateway must REJECT this IPM.");
            System.out.println(
                    "  NDR reason     : unable-to-transfer");
            System.out.println(
                    "  NDR diagnostic : content-type-not-supported");

        } catch (X400APIException ex) {

            System.err.println();
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

    private static void buildCTSW008Message(X400Msg message)
            throws X400APIException {

        SimpleDateFormat filingTimeFormat
                = new SimpleDateFormat(
                        "ddHHmm",
                        Locale.ENGLISH
                );

        filingTimeFormat.setTimeZone(
                TimeZone.getTimeZone("UTC")
        );

        /*
         * Originator
         */
        message.setFrom(P7_USER_OR_ADDRESS);

        /*
         * Recipient
         *
         * Request NDR because CTSW008 expects an NDR
         * when the gateway rejects the unsupported content-type.
         */
        message.setTo(
                RECIPIENT_OR_ADDRESS,
                X400Msg.DR_Request.DR_NON_DELIVERY_REPORT,
                X400Msg.IPN_NON_RECEIPT_NOTIFICATION
        );

        /*
         * Keep the same settings as the known-good sender.
         */
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
                "CTSW008 UNSUPPORTED CONTENT TYPE"
        );

        /*
         * =====================================================
         * CTSW008 - THE IMPORTANT PART
         * =====================================================
         *
         * Normal working message:
         *
         *     content-type = 22
         *     interpersonal-messaging-1988
         *
         * CTSW008 test message:
         *
         *     content-type = 2
         *     interpersonal-messaging-1984
         *
         * The AMHS/SWIM Gateway should reject this message.
         */
        message.setIntParam(
                X400_att.X400_N_CONTENT_TYPE,
                CONTENT_TYPE
        );

        /*
         * CTSW008 still requires the message to contain
         * an IPM body with ATS-message-header and
         * ATS-message-text.
         */
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