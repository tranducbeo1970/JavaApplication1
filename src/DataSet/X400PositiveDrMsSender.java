package DataSet;

import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.Session;
import com.isode.x400api.X400_attConstants;
import com.isode.x400api.X400ms;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Experimental positive DR submission using only X400ms/P7.
 * Object creation support does NOT guarantee report submission support.
 * Isode documents arbitrary DR generation as a Gateway API capability.
 * This class reports the actual failing API operation; it never falls back
 * to MT, IPN or an ordinary message.
 */
public final class X400PositiveDrMsSender implements X400_attConstants {
    private static final String REPORT_RECIPIENT =
            "/CN=VVTSAMHS/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String BIND_OR_ADDRESS =
            "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String PRESENTATION_ADDRESS =
            "\"3001\"/Internet=192.168.22.199+3001";
    private static final String PASSWORD = "amhs";
    private static final String BIND_DN = "";
    private static final String DELIVERED_RECIPIENT = BIND_OR_ADDRESS;

    // TEST DATA ONLY. Replace with the actual original envelope message ID
    // to correlate the DR with a real message. Do NOT put an IPM ID here.
    //-----------------------------------------------------------------------------------------------*/
    private static final String ORIGINAL_MESSAGE_ID =
            "260907000027020Z*/CN=VVTSYFYX/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final int ORIGINAL_RECIPIENT_NUMBER = 1;
    private static final String ORIGINAL_CONTENT_ID = "ContentId";
    private static final String ORIGINAL_ENCODED_INFORMATION_TYPES = "ia5-text";
    private static final int REPORT_REQUEST = 2;
    private static final int MTA_REPORT_REQUEST = 3;

    // Test timestamps default to run time. Replace with actual delivery times
    // when reporting on a real message. A unique ID prevents report ID reuse.
    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("yyMMddHHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final String ARRIVAL_TIME = TIME.format(Instant.now());
    private static final String DELIVERY_TIME = ARRIVAL_TIME;
    private static final String NEW_REPORT_ID =
            "[/PRMD=VIETNAM/ADMD=ICAO/C=XX/;dr-"
            + UUID.randomUUID().toString().replace("-", "") + "]";

    private X400PositiveDrMsSender() { }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            System.out.println("Using hard-coded X400ms/P7 positive DR configuration");
            System.out.println("REPORT_RECIPIENT=" + REPORT_RECIPIENT);
            System.out.println("SUBJECT_IDENTIFIER=" + ORIGINAL_MESSAGE_ID);
            if (ORIGINAL_MESSAGE_ID.contains("test-original-message")) {
                System.out.println("TEST DATA: original message ID is a sample;"
                        + " this report will not correlate with a real message.");
            }
            submit();
        } catch (RuntimeException | LinkageError e) {
            System.err.println("STATUS=ERROR");
            System.err.println(e.getMessage());
            exitCode = 1;
        }
        if (exitCode != 0) System.exit(exitCode);
    }

    private static void submit() {
        Session session = new Session();
        MSMessage report = new MSMessage();
        boolean opened = false;
        try {
            check(session, "x400_ms_open(P7)", X400ms.x400_ms_open(0,
                    BIND_OR_ADDRESS,
                    BIND_DN, PASSWORD,
                    PRESENTATION_ADDRESS, session));
            opened = true;
            check(session, "x400_ms_msgnew(REPORT)",
                    X400ms.x400_ms_msgnew(session, X400_MSG_REPORT, report));

            // Report destination is the original message's envelope originator.
            add(session, report, X400_S_MESSAGE_IDENTIFIER, NEW_REPORT_ID);
            add(session, report, X400_S_OR_ADDRESS, REPORT_RECIPIENT);
            add(session, report, X400_S_SUBJECT_IDENTIFIER, ORIGINAL_MESSAGE_ID);
            add(session, report, X400_S_CONTENT_IDENTIFIER, ORIGINAL_CONTENT_ID);
            add(session, report, X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES,
                    ORIGINAL_ENCODED_INFORMATION_TYPES);

            Recip delivered = new Recip();
            check(session, "x400_ms_recipnew(REPORT)",
                    X400ms.x400_ms_recipnew(report, X400_RECIP_REPORT, delivered));
            add(session, delivered, X400_S_OR_ADDRESS, DELIVERED_RECIPIENT);
            check(session, "original_recipient_number", X400ms.x400_ms_recipaddintparam(
                    delivered, X400_N_ORIGINAL_RECIPIENT_NUMBER, ORIGINAL_RECIPIENT_NUMBER));
            // Same report-request values as the SDK positive-DR Tcl example.
            check(session, "report_request", X400ms.x400_ms_recipaddintparam(
                    delivered, X400_N_REPORT_REQUEST, REPORT_REQUEST));
            check(session, "mta_report_request", X400ms.x400_ms_recipaddintparam(
                    delivered, X400_N_MTA_REPORT_REQUEST, MTA_REPORT_REQUEST));
            add(session, delivered, X400_S_ARRIVAL_TIME, ARRIVAL_TIME);
            // This attribute selects the positive delivery outcome.
            add(session, delivered, X400_S_MESSAGE_DELIVERY_TIME, DELIVERY_TIME);

            check(session, "x400_ms_msgsend(REPORT)", X400ms.x400_ms_msgsend(report));
            System.out.println("STATUS=SUBMITTED");
            System.out.println("Submission accepted by API; verify report at destination.");
            System.out.println("SUBJECT_IDENTIFIER=" + ORIGINAL_MESSAGE_ID);
            StringBuffer id = new StringBuffer(1024);
            if (X400ms.x400_ms_msggetstrparam(report, X400_S_MESSAGE_IDENTIFIER, id)
                    == X400_E_NOERROR) System.out.println("REPORT_ID=" + id);
        } finally {
            // Close releases session resources, without deleting stored messages.
            if (opened) {
                int code = X400ms.x400_ms_close(session);
                if (code != X400_E_NOERROR) System.err.println("Close warning: code=" + code);
            }
        }
    }

    private static void add(Session s, MSMessage m, int attribute, String value) {
        check(s, "message attribute " + attribute,
                X400ms.x400_ms_msgaddstrparam(m, attribute, value, -1));
    }

    private static void add(Session s, Recip r, int attribute, String value) {
        check(s, "reported recipient attribute " + attribute,
                X400ms.x400_ms_recipaddstrparam(r, attribute, value, -1));
    }

    private static void check(Session s, String operation, int code) {
        if (code == X400_E_NOERROR) {
            System.out.println("OK: " + operation);
            return;
        }
        String detail;
        try {
            detail = X400ms.x400_ms_get_string_error(s, code);
        } catch (RuntimeException e) {
            detail = "Error text unavailable";
        }
        throw new IllegalStateException(operation + " failed: code=" + code + "; " + detail);
    }
}
