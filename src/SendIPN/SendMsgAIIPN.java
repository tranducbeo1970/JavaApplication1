//CTSW015 //

package SendIPN;


import com.isode.x400.highlevel.P3BindSession;
import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.ReceiveMsg;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400api.X400_att;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Submits a fictitious positive IPN/RN without receiving a subject IPM first.
 */
public final class SendMsgAIIPN {

    // CACTI CT301 is a Gateway conformance test submitted through an MTA.
    private static final boolean USE_P3 = false;

    private static final String P7_PRESENTATION_ADDRESS
            = "\"3001\"/Internet=192.168.22.199+3001";
    private static final String P3_PRESENTATION_ADDRESS
            = "\"593\"/URI+0000+URL+itot://192.168.22.199";

    private static final String P7_USER_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String P3_USER_OR_ADDRESS
            = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String PASSWORD = "amhs";
    private static final String RECIPIENT_OR_ADDRESS
            = "/CN=VVTSSWIM/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String SYNTHETIC_USER_RELATIVE_ID = "CTSW015.";
    private static final int IPN_NO_NOTIFICATION = 0;

    private SendMsgAIIPN() {
    }

    public static void main(String[] args) {
        P3BindSession session = null;
        X400Msg ipn = null;

        try {
            String subjectIpm = createSyntheticSubjectIpmIdentifier();
            String receiptTime = createUtcTime();

            session = createSession();
            printConfiguration(subjectIpm, receiptTime);
            session.bind();
            System.out.println((USE_P3 ? "P3" : "P7") + " bind successful.");

            ipn = new X400Msg(session);
            ipn.setFrom(getOriginatorAddress());
            ipn.setTo(
                    RECIPIENT_OR_ADDRESS,
                    X400Msg.DR_Request.DR_NON_DELIVERY_REPORT,
                    IPN_NO_NOTIFICATION
            );

            // This is the exact minimal attribute pattern used by CACTI
            // CT301 Subtest 4 for a fictitious positive RN.
            ipn.setIntParam(X400_att.X400_N_CONTENT_TYPE, 22);
            ipn.setIntParam(X400_att.X400_N_IS_IPN, 1);
            ipn.setStringparam(X400_att.X400_S_SUBJECT_IPM, subjectIpm);
            ipn.setStringparam(X400_att.X400_S_RECEIPT_TIME, receiptTime);

            System.out.println("Submitting direct fictitious IPN/RN...");
            ipn.sendMsg(session);

            System.out.println("IPN/RN submitted successfully.");
            System.out.println("Message ID      : " + ipn.getMessageIdentifier());
            System.out.println("Submission time : " + ipn.getSubmissionTime());
            verifyEncodedContent(session, ipn);
        } catch (X400APIException ex) {
            System.err.println("X.400 operation failed: " + ex.getMessage());
            System.err.println("Native error code: " + ex.getNativeErrorCode());
            ex.printStackTrace();
            System.exit(1);
        } finally {
            if (ipn != null) {
                try {
                    ipn.delete(false);
                } catch (X400APIException ex) {
                    System.err.println("Cannot release IPN object: " + ex.getMessage());
                }
            }

            if (session != null && session.isBound()) {
                try {
                    session.unbind();
                    System.out.println((USE_P3 ? "P3" : "P7") + " session closed.");
                } catch (X400APIException ex) {
                    System.err.println("Cannot unbind session: " + ex.getMessage());
                }
            }
        }
    }

    private static P3BindSession createSession() {
        if (USE_P3) {
            return new P3BindSession(
                    P3_PRESENTATION_ADDRESS,
                    P3_USER_OR_ADDRESS,
                    PASSWORD,
                    true
            );
        }

        return new P7BindSession(
                P7_PRESENTATION_ADDRESS,
                P7_USER_OR_ADDRESS,
                PASSWORD
        );
    }

    private static String createSyntheticSubjectIpmIdentifier() {
        return SYNTHETIC_USER_RELATIVE_ID
                + createUtcTime()
                + "*"
                + getOriginatorAddress();
    }

    private static String getOriginatorAddress() {
        return USE_P3 ? P3_USER_OR_ADDRESS : P7_USER_OR_ADDRESS;
    }

    private static void verifyEncodedContent(
            P3BindSession session,
            X400Msg sentIpn) throws X400APIException {
        ReceiveMsg decoded = null;

        try {
            byte[] encodedContent = sentIpn.getContentBytes();
            System.out.println("Encoded bytes   : " + encodedContent.length);

            // CACTI sendTestMessage() uses the same constructor to inspect
            // the content generated by X400Msg after submission.
            decoded = new ReceiveMsg(
                    session,
                    X400_att.X400_MSG_MESSAGE,
                    encodedContent
            );

            System.out.println("Decoded type    : " + decoded.getTypeAsString());
            System.out.println("Decoded isIPN   : " + decoded.isIPN());
            System.out.println("Raw IS_IPN      : " + decoded.getIntParam(
                    X400_att.X400_N_IS_IPN));
            System.out.println("Decoded subject : " + decoded.getStringParam(
                    X400_att.X400_S_SUBJECT_IPM));
            System.out.println("Decoded receipt : " + decoded.getStringParam(
                    X400_att.X400_S_RECEIPT_TIME));
            System.out.println("IPN content     : " + decoded.getIPNContentAsText());
        } finally {
            if (decoded != null) {
                decoded.finishWithMessage(0, 0);
                decoded.delete(false);
            }
        }
    }

    private static String createUtcTime() {
        SimpleDateFormat format
                = new SimpleDateFormat("yyMMddHHmmss'Z'", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private static void printConfiguration(String subjectIpm, String receiptTime) {
        System.out.println("Creating direct fictitious IPN/RN...");
        System.out.println("Channel      : " + (USE_P3 ? "P3" : "P7"));
        System.out.println("Server       : " + (USE_P3
                ? P3_PRESENTATION_ADDRESS : P7_PRESENTATION_ADDRESS));
        System.out.println("Originator   : " + getOriginatorAddress());
        System.out.println("Recipient    : " + RECIPIENT_OR_ADDRESS);
        System.out.println("Subject IPM  : " + subjectIpm);
        System.out.println("Receipt time : " + receiptTime);
        System.out.println("IPN type     : positive Receipt Notification (RN)");
    }
}
