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
 * Sends an ATS message whose P1 envelope recipient and P2 primary (To)
 * recipient are different OR-addresses.
 */
public final class SendMsgAISeparateRecipients {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS
            = "\"3001\"/Internet=192.168.22.186+3001";

    private static final String P7_USER_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String P7_USER_PASSWORD = "amhs";

    /** The P1 address to which the Message Store actually delivers. */
    private static final String ENVELOPE_RECIPIENT_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    /** The P2 address displayed in the message's To/primary field. */
    private static final String PRIMARY_RECIPIENT_OR_ADDRESS
            = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String ATS_PRIORITY = "FF";
    private static final String MESSAGE_TEXT
            = "TEST MESSAGE WITH SEPARATE ENVELOPE AND PRIMARY RECIPIENTS\r\n"
            + "ENVELOPE DESTINATION: VVTSMHSA\r\n"
            + "PRIMARY TO: VVTSOPTA\r\n"
            + "THIS MESSAGE VERIFIES DIFFERENT P1 AND P2 RECIPIENTS.";

    private SendMsgAISeparateRecipients() {
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
            System.out.println("Connecting to P7 Message Store...");
            System.out.println("Server             : "
                    + P7_MESSAGE_STORE_PRESENTATION_ADDRESS);
            System.out.println("Mailbox            : " + P7_USER_OR_ADDRESS);
            System.out.println("Envelope recipient : "
                    + ENVELOPE_RECIPIENT_OR_ADDRESS);
            System.out.println("Primary To         : "
                    + PRIMARY_RECIPIENT_OR_ADDRESS);

            session.bind();
            System.out.println("P7 bind successful.");

            message = new X400Msg(session);
            buildBasicAtsMessage(message);
            message.sendMsg(session);

            System.out.println("Message submitted successfully.");
            System.out.println("Message ID      : "
                    + message.getMessageIdentifier());
            System.out.println("IPM ID          : "
                    + message.getMessageIPMIdentifier());
            System.out.println("Submission time : "
                    + message.getSubmissionTime());
        } catch (X400APIException ex) {
            System.err.println("X.400 operation failed: " + ex.getMessage());
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

    private static void buildBasicAtsMessage(X400Msg message)
            throws X400APIException {
        SimpleDateFormat filingTimeFormat
                = new SimpleDateFormat("ddHHmm", Locale.ENGLISH);
        filingTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        message.setFrom(P7_USER_OR_ADDRESS);

        /*
         * false is essential here. If true is used with X400_RECIP_PRIMARY,
         * Isode ORs X400_RECIP_ENVELOPE into the recipient type, producing a
         * standard recipient whose envelope and primary addresses are equal.
         */
        message.setRecipient(
                false,
                X400_att.X400_RECIP_ENVELOPE,
                ENVELOPE_RECIPIENT_OR_ADDRESS,
                X400Msg.DR_Request.DR_NON_DELIVERY_REPORT,
                0 // IPN no notification
        );

        message.setRecipient(
                false,
                X400_att.X400_RECIP_PRIMARY,
                PRIMARY_RECIPIENT_OR_ADDRESS,
                X400Msg.DR_Request.DR_NO_REPORT,
                X400Msg.IPN_NON_RECEIPT_NOTIFICATION
        );

        message.setPriority(X400Msg.X400_Priority.NORMAL_PRIORITY);
        message.setIntParam(AMHS_att.ATS_N_EXTENDED, 0);
        message.setStringparam(
                AMHS_att.ATS_S_FILING_TIME,
                filingTimeFormat.format(new Date())
        );
        message.setStringparam(AMHS_att.ATS_S_PRIORITY_INDICATOR, ATS_PRIORITY);
        message.setStringparam(
                X400_att.X400_S_ORIGINATORS_REFERENCE,
                "SENDMSGAI SEPARATE P1/P2 RECIPIENT TEST"
        );
        message.setIntParam(X400_att.X400_N_CONTENT_TYPE, 22);
        message.setStringparam(AMHS_att.ATS_S_TEXT, MESSAGE_TEXT);
    }

    private static void releaseMessage(X400Msg message) {
        if (message == null) {
            return;
        }

        try {
            message.delete(true);
        } catch (X400APIException ex) {
            System.err.println("Cannot release X.400 message: "
                    + ex.getMessage());
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
            System.err.println("Cannot unbind P7 session: "
                    + ex.getMessage());
        }
    }
}
