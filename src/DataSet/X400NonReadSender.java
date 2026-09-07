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

/** Sends an X.400 non-read IPN directly through the Isode Java API. */
public final class X400NonReadSender implements X400_attConstants {

    private static final String ORIGINATOR =
            "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String RECIPIENT =
            "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String SUBJECT_IPM =
            "11508.0 260906204811Z*/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String PASSWORD = "amhs";
    private static final String PRESENTATION_ADDRESS =
            "\"3001\"/Internet=192.168.22.186+3001";
    private static final String BIND_DN =
            System.getenv("ISODE_P7_BIND_DN") == null
                    ? "" : System.getenv("ISODE_P7_BIND_DN");

    private X400NonReadSender() {
    }

    public static void main(String[] args) {
        Session session = new Session();
        MSMessage message = new MSMessage();
        boolean opened = false;

        try {
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
                    X400ms.x400_ms_msgnew(
                            session, X400_MSG_MESSAGE, message));

            addString(session, message,
                    X400_S_MESSAGE_IDENTIFIER, "MsgId");
            addString(session, message,
                    X400_S_CONTENT_IDENTIFIER, "ContentId");
            addString(session, message,
                    X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES, "ia5-text");

            addInt(session, message, X400_N_PRIORITY, 1);
            addInt(session, message, X400_N_DISCLOSURE, 0);
            addInt(session, message,
                    X400_N_IMPLICIT_CONVERSION_PROHIBITED, 0);
            addInt(session, message,
                    X400_N_ALTERNATE_RECIPIENT_ALLOWED, 1);
            addInt(session, message,
                    X400_N_CONTENT_RETURN_REQUEST, 1);
            addInt(session, message,
                    X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, 1);
            addInt(session, message,
                    X400_N_DL_EXPANSION_PROHIBITED, 0);
            addInt(session, message,
                    X400_N_CONVERSION_WITH_LOSS_PROHIBITED, 0);

            // Do not set X400_S_LATEST_DELIVERY_TIME: the example value was
            // expired and malformed.
            addString(session, message,
                    X400_S_ORIGINATOR_RETURN_ADDRESS, ORIGINATOR);

            addRecipient(session, message,
                    X400_RECIP_ENVELOPE, RECIPIENT, true);
            addRecipient(session, message,
                    X400_ORIGINATOR, ORIGINATOR, false);

            addInt(session, message, X400_N_IS_IPN, 1);
            addString(session, message,
                    X400_S_SUBJECT_IPM, SUBJECT_IPM);

            // Same values as the working Isode nonread.tcl example:
            // non-receipt reason 0 = discarded; discard reason 1.
            addInt(session, message, X400_N_NON_RECEIPT_REASON, 0);
            addInt(session, message, X400_N_DISCARD_REASON, 1);

            // No returned-message body is attached. The old Tcl example body
            // used a placeholder identifier that did not match SUBJECT_IPM.
            check(session, "x400_ms_msgsend",
                    X400ms.x400_ms_msgsend(message));

            System.out.println("STATUS=SUCCESS");
            printString(message,
                    X400_S_MESSAGE_IDENTIFIER, "MESSAGE_ID");
            printString(message,
                    X400_S_MESSAGE_SUBMISSION_TIME, "SUBMISSION_TIME");
        } catch (RuntimeException e) {
            System.err.println("STATUS=ERROR");
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            if (opened) {
                int result = X400ms.x400_ms_close(session);
                if (result != X400_E_NOERROR) {
                    System.err.println(
                            "Warning: x400_ms_close returned " + result);
                }
            }
        }
    }

    private static void addRecipient(
            Session session,
            MSMessage message,
            int type,
            String address,
            boolean reportRequested) {

        Recip recipient = new Recip();
        check(session, "x400_ms_recipnew",
                X400ms.x400_ms_recipnew(message, type, recipient));
        check(session, "recipient OR-address",
                X400ms.x400_ms_recipaddstrparam(
                        recipient, X400_S_OR_ADDRESS, address, -1));

        if (reportRequested) {
            check(session, "recipient report request",
                    X400ms.x400_ms_recipaddintparam(
                            recipient, X400_N_REPORT_REQUEST, 1));
        }
    }

    private static void addString(
            Session session,
            MSMessage message,
            int attribute,
            String value) {
        check(session, "string attribute " + attribute,
                X400ms.x400_ms_msgaddstrparam(
                        message, attribute, value, value.length()));
    }

    private static void addInt(
            Session session,
            MSMessage message,
            int attribute,
            int value) {
        check(session, "integer attribute " + attribute,
                X400ms.x400_ms_msgaddintparam(
                        message, attribute, value));
    }

    private static void printString(
            MSMessage message, int attribute, String name) {
        StringBuffer value = new StringBuffer(1024);
        int result = X400ms.x400_ms_msggetstrparam(
                message, attribute, value);
        if (result == X400_E_NOERROR) {
            System.out.println(name + "=" + value);
        }
    }

    private static void check(
            Session session, String operation, int result) {
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

        throw new IllegalStateException(
                operation + " failed: " + detail);
    }
}
