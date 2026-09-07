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
 * Sends one ATS message using the union of Envelope To addresses found in the
 * three message records in BMYMYX.txt. Duplicate envelope addresses are kept
 * only once. P22 Primary To remains the common 12-address list from the file.
 */
public final class SendMsgAIMergedEnvelopeRecipients {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS
            = "\"3001\"/Internet=192.168.22.186+3001";

    /* Account used only to bind to the P7 Message Store. */
    private static final String P7_BIND_USER_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String P7_USER_PASSWORD = "amhs";

    private static final String FROM_OR_ADDRESS
            = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    /* The common P22 Primary To list in all three records. */
    private static final String[] PRIMARY_RECIPIENT_OR_ADDRESSES = {
        "/CN=VVBMZTZX/OU=VVBM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVBMYDYX/OU=VVBM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVTSYMYX/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVDNYMYX/OU=VVDN/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVNBYMYX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVGLYMYX/OU=VVGL/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVVVYMYC/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVVVYMYX/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/OU=VVVVYMYX/O=AFTN/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVHMZQZX/OU=VVHM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVHMZQZA/OU=VVHM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVTSXXXX/OU=VVTS/O=VVNB/PRMD=VIETNAM/ADMD=ICAO/C=XX/"
    };

    /* Union of Envelope To from message 1 (12), message 2 (13), message 3 (17). */
    private static final String[] MERGED_ENVELOPE_RECIPIENT_OR_ADDRESSES = {
        "/CN=VVBMZTZX/OU=VVBM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVBMYDYX/OU=VVBM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVTSYMYX/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVDNYMYX/OU=VVDN/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVNBYMYX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVGLYMYX/OU=VVGL/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVVVYMYC/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVVVYMYX/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/OU=VVVVYMYX/O=AFTN/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVHMZQZX/OU=VVHM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVHMZQZA/OU=VVHM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVTSXXXX/OU=VVTS/O=VVNB/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVBMZTZA/OU=VVBM/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/OU=VVBMZTZX/O=AFTN/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/OU=VVHMZQZX/O=AFTN/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVVVZDZX/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVVVYWYX/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVTSMNAR/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/",
        "/CN=VVTSATMS/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/"
        
        
    };

    private static final String NO_REPORT_ENVELOPE_OR_ADDRESS
            = "/CN=VVTSXXXX/OU=VVTS/O=VVNB/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String ATS_PRIORITY = "FF";
    private static final String MESSAGE_TEXT
            = "TEST MESSAGE WITH ENVELOPE RECIPIENTS MERGED FROM THREE RECORDS\r\n"
            + "FROM: VVBMYMYX\r\n"
            + "PRIMARY RECIPIENT COUNT: 12\r\n"
            + "MERGED ENVELOPE RECIPIENT COUNT: 19";

    private SendMsgAIMergedEnvelopeRecipients() {
    }

    public static void main(String[] args) {
        P7BindSession session = new P7BindSession(
                P7_MESSAGE_STORE_PRESENTATION_ADDRESS,
                P7_BIND_USER_OR_ADDRESS,
                P7_USER_PASSWORD,
                false
        );

        X400Msg message = null;

        try {
            printConfiguration();
            session.bind();
            System.out.println("P7 bind successful.");

            message = new X400Msg(session);
            buildAtsMessage(message);
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

    private static void buildAtsMessage(X400Msg message)
            throws X400APIException {
        SimpleDateFormat filingTimeFormat
                = new SimpleDateFormat("ddHHmm", Locale.ENGLISH);
        filingTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        message.setFrom(FROM_OR_ADDRESS);
        addMergedEnvelopeRecipients(message);
        addPrimaryRecipients(message);

        message.setPriority(X400Msg.X400_Priority.NORMAL_PRIORITY);
        message.setIntParam(AMHS_att.ATS_N_EXTENDED, 0);
        message.setStringparam(
                AMHS_att.ATS_S_FILING_TIME,
                filingTimeFormat.format(new Date())
        );
        message.setStringparam(AMHS_att.ATS_S_PRIORITY_INDICATOR, ATS_PRIORITY);
        message.setStringparam(
                X400_att.X400_S_ORIGINATORS_REFERENCE,
                "SENDMSGAI MERGED ENVELOPE FROM THREE MESSAGE RECORDS"
        );
        message.setIntParam(X400_att.X400_N_CONTENT_TYPE, 22);
        message.setStringparam(AMHS_att.ATS_S_TEXT, MESSAGE_TEXT);
    }

    private static void addMergedEnvelopeRecipients(X400Msg message)
            throws X400APIException {
        for (String address : MERGED_ENVELOPE_RECIPIENT_OR_ADDRESSES) {
            X400Msg.DR_Request reportRequest
                    = NO_REPORT_ENVELOPE_OR_ADDRESS.equals(address)
                            ? X400Msg.DR_Request.DR_NO_REPORT
                            : X400Msg.DR_Request.DR_NON_DELIVERY_REPORT;

            message.setRecipient(
                    false,
                    X400_att.X400_RECIP_ENVELOPE,
                    address,
                    reportRequest,
                    0
            );
        }
    }

    private static void addPrimaryRecipients(X400Msg message)
            throws X400APIException {
        for (String address : PRIMARY_RECIPIENT_OR_ADDRESSES) {
            message.setRecipient(
                    false,
                    X400_att.X400_RECIP_PRIMARY,
                    address,
                    X400Msg.DR_Request.DR_NO_REPORT,
                    0
            );
        }
    }

    private static void printConfiguration() {
        System.out.println("Connecting to P7 Message Store...");
        System.out.println("Server    : "
                + P7_MESSAGE_STORE_PRESENTATION_ADDRESS);
        System.out.println("Bind user : " + P7_BIND_USER_OR_ADDRESS);
        System.out.println("From      : " + FROM_OR_ADDRESS);
        printAddresses(
                "Merged Envelope",
                MERGED_ENVELOPE_RECIPIENT_OR_ADDRESSES
        );
        printAddresses("Primary", PRIMARY_RECIPIENT_OR_ADDRESSES);
    }

    private static void printAddresses(String label, String[] addresses) {
        System.out.println(label + " recipients (" + addresses.length + "):");
        for (int index = 0; index < addresses.length; index++) {
            System.out.println("  " + (index + 1) + ". " + addresses[index]);
        }
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
