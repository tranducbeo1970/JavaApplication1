/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataSet;

import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.Session;
import com.isode.x400api.X400_attConstants;
import com.isode.x400api.X400ms;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Sends an X.400 interpersonal notification directly through the Isode
 * Java X.400 Message Store API. No Tcl process is used.
 */
public final class X400IpnSender implements X400_attConstants {

    private static final String ORIGINATOR =
            "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String PASSWORD = "amhs";
    // The Java API takes the P7 bind DN separately. The Tcl script did not
    // specify one, so leave it empty unless the installation requires it.
    private static final String BIND_DN =
            System.getenv("ISODE_P7_BIND_DN") == null
                    ? "" : System.getenv("ISODE_P7_BIND_DN");
    private static final String PRESENTATION_ADDRESS =
            "\"3001\"/Internet=192.168.22.199+3001";

    // Defaults used when NetBeans runs main() without program arguments.
    private static final Kind DEFAULT_KIND = Kind.READ;
    private static final String DEFAULT_RECIPIENT =
            "/CN=VVTSAMHS/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String DEFAULT_SUBJECT_IPM =
            "11508.0 260906204811Z*/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final DateTimeFormatter UTC_TIME =
            DateTimeFormatter.ofPattern("yyMMddHHmmss'Z'")
                    .withZone(ZoneOffset.UTC);

    private X400IpnSender() {
    }

    private enum Kind {
        READ, NONREAD
    }

    public static void main(String[] args) {
        if (args.length != 0 && args.length != 3) {
            System.err.println(
                    "Usage: java X400IpnSender [READ|NONREAD <recipient-or-address> <subject-ipm>]");
            System.exit(2);
        }

        final Kind kind;
        final String recipientAddress;
        final String subjectIpm;

        if (args.length == 0) {
            kind = DEFAULT_KIND;
            recipientAddress = DEFAULT_RECIPIENT;
            subjectIpm = DEFAULT_SUBJECT_IPM;
            System.out.println("Using values configured in X400IpnSender.java");
        } else {
            try {
                kind = Kind.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Receipt type must be READ or NONREAD");
                System.exit(2);
                return;
            }
            recipientAddress = args[1];
            subjectIpm = args[2];
        }

        Session session = new Session();
        MSMessage message = new MSMessage();
        boolean opened = false;

        try {
            // Confirmed from Isode's X400msTestSendUtils bytecode:
            // flags, bind OR-address, bind DN, credentials, PA, output session.
            check(session, "x400_ms_open",
                    X400ms.x400_ms_open(
                            0,
                            ORIGINATOR,
                            BIND_DN,
                            PASSWORD,
                            PRESENTATION_ADDRESS,
                            session));
            opened = true;

            check(session, "x400_ms_msgnew",
                    X400ms.x400_ms_msgnew(session, X400_MSG_MESSAGE, message));

            addString(session, message, X400_S_MESSAGE_IDENTIFIER, "MsgId");
            addString(session, message, X400_S_CONTENT_IDENTIFIER, "ContentId");
            addString(session, message,
                    X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES, "ia5-text");

            addInt(session, message, X400_N_PRIORITY, 1);
            addInt(session, message, X400_N_DISCLOSURE, 0);
            addInt(session, message,
                    X400_N_IMPLICIT_CONVERSION_PROHIBITED, 0);
            addInt(session, message,
                    X400_N_ALTERNATE_RECIPIENT_ALLOWED, 1);
            addInt(session, message, X400_N_CONTENT_RETURN_REQUEST, 1);
            addInt(session, message,
                    X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, 1);
            addInt(session, message, X400_N_DL_EXPANSION_PROHIBITED, 0);
            addInt(session, message,
                    X400_N_CONVERSION_WITH_LOSS_PROHIBITED, 0);

            // Do not set X400_S_LATEST_DELIVERY_TIME. A fixed old value can
            // expire the notification immediately after submission.
            addString(session, message,
                    X400_S_ORIGINATOR_RETURN_ADDRESS, ORIGINATOR);

            addRecipient(session, message,
                    X400_RECIP_ENVELOPE, recipientAddress, true);

            // Match the supplied Tcl programs exactly: only nonread.tcl adds
            // this explicit header originator.
            if (kind == Kind.NONREAD) {
                addRecipient(session, message,
                        X400_ORIGINATOR, ORIGINATOR, false);
            }

            addInt(session, message, X400_N_IS_IPN, 1);
            addString(session, message, X400_S_SUBJECT_IPM, subjectIpm);

            if (kind == Kind.READ) {
                addString(session, message,
                        X400_S_RECEIPT_TIME, UTC_TIME.format(Instant.now()));
                addString(session, message,
                        X400_S_SUPP_RECEIPT_INFO, "I've got it");
            } else {
                // X.420/Isode values used by the original nonread.tcl:
                // non-receipt reason 0 = discarded; discard reason 1.
                addInt(session, message, X400_N_NON_RECEIPT_REASON, 0);
                addInt(session, message, X400_N_DISCARD_REASON, 1);

                // The example returned-message body is intentionally omitted:
                // its placeholder IPM identifier did not match subject_ipm.
            }

            check(session, "x400_ms_msgsend",
                    X400ms.x400_ms_msgsend(message));

            StringBuffer messageId = new StringBuffer(1024);
            int idResult = X400ms.x400_ms_msggetstrparam(
                    message, X400_S_MESSAGE_IDENTIFIER, messageId);

            System.out.println("STATUS=SUCCESS");
            if (idResult == X400_E_NOERROR) {
                System.out.println("MESSAGE_ID=" + messageId);
            }

            StringBuffer submissionTime = new StringBuffer(64);
            int timeResult = X400ms.x400_ms_msggetstrparam(
                    message, X400_S_MESSAGE_SUBMISSION_TIME, submissionTime);
            if (timeResult == X400_E_NOERROR) {
                System.out.println("SUBMISSION_TIME=" + submissionTime);
            }
        } catch (RuntimeException e) {
            System.err.println("STATUS=ERROR");
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            if (opened) {
                int closeResult = X400ms.x400_ms_close(session);
                if (closeResult != X400_E_NOERROR) {
                    System.err.println("Warning: x400_ms_close returned " + closeResult);
                }
            }
        }
    }

    private static void addRecipient(
            Session session,
            MSMessage message,
            int type,
            String orAddress,
            boolean requestReport) {

        Recip recipient = new Recip();
        check(session, "x400_ms_recipnew",
                X400ms.x400_ms_recipnew(message, type, recipient));
        check(session, "recipient OR-address",
                X400ms.x400_ms_recipaddstrparam(
                        recipient, X400_S_OR_ADDRESS,
                        orAddress, -1));

        if (requestReport) {
            check(session, "recipient report request",
                    X400ms.x400_ms_recipaddintparam(
                            recipient, X400_N_REPORT_REQUEST, 1));
        }
    }

    private static void addString(
            Session session, MSMessage message, int attribute, String value) {
        check(session, "string attribute " + attribute,
                X400ms.x400_ms_msgaddstrparam(
                        message, attribute, value, value.length()));
    }

    private static void addInt(
            Session session, MSMessage message, int attribute, int value) {
        check(session, "integer attribute " + attribute,
                X400ms.x400_ms_msgaddintparam(message, attribute, value));
    }

    private static void check(Session session, String operation, int result) {
        if (result == X400_E_NOERROR) {
            return;
        }

        String detail;
        try {
            detail = X400ms.x400_ms_get_string_error(session, result);
        } catch (RuntimeException e) {
            detail = null;
        }

        if (detail == null || detail.length() == 0) {
            detail = "Isode error " + result;
        }

        throw new IllegalStateException(operation + " failed: " + detail);
    }
}
