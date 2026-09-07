package javaapplication1;

import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400api.AMHS_att;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Standalone Probe sender for the JavaApplication1 test project.
 *
 * Connection and default recipient values are copied from SendX400MailS.java.
 * This class does not modify or call SendX400MailS.
 *
 * Optional arguments:
 *   args[0] = first recipient (default: PR4 from SendX400MailS)
 *   args[1] = second recipient, or "-" to omit it (default: PR5)
 *   args[2] = hypothetical content length in bytes (default: 1024)
 *   args[3] = content identifier (default: generated)
 */
public final class SendProbeStandalone {

    // Copied from SendX400MailS.java.
    private static final String P7_SERVER =
            "\"3001\"/URI+0000+URL+itot://192.168.22.199:3001";
    private static final String P7_USER =
            "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String P7_PASSWORD = "amhs";

    private static final String DEFAULT_RECIPIENT_1 =
            "/CN=VVTSSWIA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String DEFAULT_RECIPIENT_2 =
            "/CN=WSSSFTUU/OU=WSSS/O=CAASG/PRMD=SINGAPORE/ADMD=ICAO/C=XX/";

    private static final int CONTENT_TYPE = 22;
    private static final String ORIGINAL_EIT = "ia5-text";
    private static final String ATS_PRIORITY = "FF";

    private SendProbeStandalone() {
    }

    public static void main(String[] args) {
        P7BindSession session = null;
        X400Msg probe = null;
        boolean probeCreated = false;

        try {
            String recipient1 = valueAt(args, 0, DEFAULT_RECIPIENT_1);
            String recipient2 = valueAt(args, 1, DEFAULT_RECIPIENT_2);
            int contentLength = parseContentLength(valueAt(args, 2, "1024"));
            String contentIdentifier = valueAt(
                    args,
                    3,
                    "PROBE-" + utc("yyyyMMddHHmmss")
            );

            if ("-".equals(recipient1)) {
                recipient1 = null;
            }
            if ("-".equals(recipient2)) {
                recipient2 = null;
            }
            if (recipient1 == null && recipient2 == null) {
                throw new IllegalArgumentException(
                        "At least one Probe recipient is required"
                );
            }

            System.out.println("Connecting to P7 Message Store...");
            System.out.println("Server         : " + P7_SERVER);
            System.out.println("Originator     : " + P7_USER);
            System.out.println("Recipient 1    : " + recipient1);
            System.out.println("Recipient 2    : " + recipient2);
            System.out.println("Content type   : " + CONTENT_TYPE);
            System.out.println("Content length : " + contentLength);
            System.out.println("Content ID     : " + contentIdentifier);

            session = new P7BindSession(P7_SERVER, P7_USER, P7_PASSWORD);
            session.bind();
            System.out.println("P7 bind        : OK");

            // File 1 creates the Probe explicitly with X400_MSG_PROBE.
            probe = new X400Msg(session);
            int status = X400ms.x400_ms_msgnew(
                    session,
                    X400_att.X400_MSG_PROBE,
                    probe
            );
            check(session, status, "Cannot create X.400 Probe");
            probeCreated = true;

            buildProbeEnvelope(
                    probe,
                    contentLength,
                    contentIdentifier
            );
            buildProbeAtsAttributes(probe);
            addOriginator(probe, P7_USER);

            int recipientNumber = 1;
            if (recipient1 != null) {
                addRecipient(probe, recipient1, recipientNumber++);
            }
            if (recipient2 != null) {
                addRecipient(probe, recipient2, recipientNumber);
            }

            // File 1 submits through the low-level Message Store API.
            status = X400ms.x400_ms_msgsend(probe);
            check(session, status, "Cannot send X.400 Probe");

            String messageId = getString(
                    probe,
                    X400_att.X400_S_MESSAGE_IDENTIFIER
            );
            String submissionTime = getString(
                    probe,
                    X400_att.X400_S_MESSAGE_SUBMISSION_TIME
            );

            System.out.println("Probe sent      : OK");
            System.out.println("Message ID      : " + messageId);
            System.out.println("Submission time : " + submissionTime);

        } catch (Exception ex) {
            System.err.println("Probe send failed: " + ex.getMessage());
            if (ex instanceof X400APIException) {
                System.err.println(
                        "Native error code: "
                                + ((X400APIException) ex).getNativeErrorCode()
                );
            }
            ex.printStackTrace(System.err);
            System.exit(1);
        } finally {
            if (probeCreated && probe != null) {
                try {
                    probe.delete(true);
                } catch (Exception ignored) {
                }
            }
            if (session != null && session.isBound()) {
                try {
                    session.unbind();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void buildProbeEnvelope(
            MSMessage probe,
            int contentLength,
            String contentIdentifier
    ) throws X400APIException {
        addString(probe, X400_att.X400_S_OR_ADDRESS, P7_USER);
        addInteger(probe, X400_att.X400_N_PRIORITY, 0);
        addInteger(probe, X400_att.X400_N_CONTENT_TYPE, CONTENT_TYPE);
        addInteger(probe, X400_att.X400_N_CONTENT_LENGTH, contentLength);
        addString(probe,
                X400_att.X400_S_CONTENT_IDENTIFIER,
                contentIdentifier);
        addString(probe,
                X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES,
                ORIGINAL_EIT);

        addInteger(probe, X400_att.X400_N_DISCLOSURE, 1);
        addInteger(probe,
                X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED, 0);
        addInteger(probe,
                X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED, 1);
        addInteger(probe, X400_att.X400_N_CONTENT_RETURN_REQUEST, 1);
        addInteger(probe,
                X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, 0);
        addInteger(probe, X400_att.X400_N_DL_EXPANSION_PROHIBITED, 0);
        addInteger(probe,
                X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED, 0);
    }

    private static void buildProbeAtsAttributes(MSMessage probe)
            throws X400APIException {
        addString(probe, X400_att.X400_S_SUBJECT, "PROBE MESSAGE");
        addString(probe,
                X400_att.X400_S_IPM_IDENTIFIER,
                System.currentTimeMillis() + "*");
        addInteger(probe, X400_att.X400_N_NUM_ATTACHMENTS, 0);

        addString(probe, AMHS_att.ATS_S_PRIORITY_INDICATOR, ATS_PRIORITY);
        addInteger(probe, AMHS_att.ATS_N_EXTENDED, 0);
        addString(probe, AMHS_att.ATS_S_FILING_TIME, utc("ddHHmm"));
        addString(probe, AMHS_att.ATS_S_TEXT, "PROBE MESSAGE");
        addString(probe,
                X400_att.X400_S_PRECEDENCE_POLICY_ID,
                "1.3.27.8.0.0");
    }

    private static void addOriginator(MSMessage message, String address)
            throws X400APIException {
        Recip originator = new Recip();
        int status = X400ms.x400_ms_recipnew(
                message,
                X400_att.X400_ORIGINATOR,
                originator
        );
        check(null, status, "Cannot create Probe originator");
        addRecipientString(originator, X400_att.X400_S_OR_ADDRESS, address);
        addRecipientInteger(originator,
                X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, 1);
    }

    private static void addRecipient(
            MSMessage message,
            String address,
            int recipientNumber
    ) throws X400APIException {
        Recip recipient = new Recip();
        int status = X400ms.x400_ms_recipnew(
                message,
                X400_att.X400_RECIP_STANDARD,
                recipient
        );
        check(null, status, "Cannot create Probe recipient: " + address);
        addRecipientString(recipient, X400_att.X400_S_OR_ADDRESS, address);
        addRecipientInteger(recipient,
                X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER,
                recipientNumber);

        // Match SendX400MailS: request delivery report and non-receipt IPN.
        addRecipientInteger(recipient,
                X400_att.X400_N_MTA_REPORT_REQUEST, 0);
        addRecipientInteger(recipient,
                X400_att.X400_N_REPORT_REQUEST, 2);
        addRecipientInteger(recipient,
                X400_att.X400_N_NOTIFICATION_REQUEST,
                X400Msg.IPN_NON_RECEIPT_NOTIFICATION);
    }

    private static void addInteger(MSMessage message, int attribute, int value)
            throws X400APIException {
        int status = X400ms.x400_ms_msgaddintparam(
                message,
                attribute,
                value
        );
        check(null, status, "Cannot set message integer attribute " + attribute);
    }

    private static void addString(
            MSMessage message,
            int attribute,
            String value
    ) throws X400APIException {
        if (value == null || value.isEmpty()) {
            return;
        }
        int status = X400ms.x400_ms_msgaddstrparam(
                message,
                attribute,
                value,
                value.length()
        );
        check(null, status, "Cannot set message string attribute " + attribute);
    }

    private static void addRecipientInteger(
            Recip recipient,
            int attribute,
            int value
    ) throws X400APIException {
        int status = X400ms.x400_ms_recipaddintparam(
                recipient,
                attribute,
                value
        );
        check(null, status,
                "Cannot set recipient integer attribute " + attribute);
    }

    private static void addRecipientString(
            Recip recipient,
            int attribute,
            String value
    ) throws X400APIException {
        int status = X400ms.x400_ms_recipaddstrparam(
                recipient,
                attribute,
                value,
                value.length()
        );
        check(null, status,
                "Cannot set recipient string attribute " + attribute);
    }

    private static String getString(MSMessage message, int attribute) {
        StringBuffer value = new StringBuffer();
        int status = X400ms.x400_ms_msggetstrparam(message, attribute, value);
        return status == X400_att.X400_E_NOERROR ? value.toString() : null;
    }

    private static void check(
            P7BindSession session,
            int status,
            String operation
    ) throws X400APIException {
        if (status == X400_att.X400_E_NOERROR) {
            return;
        }
        String detail = session == null
                ? null
                : X400ms.x400_ms_get_string_error(session, status);
        throw new X400APIException(
                detail == null || detail.isEmpty()
                        ? operation
                        : operation + ": " + detail,
                status
        );
    }

    private static int parseContentLength(String value) {
        int length = Integer.parseInt(value);
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Content length must be greater than or equal to zero"
            );
        }
        return length;
    }

    private static String valueAt(
            String[] args,
            int index,
            String fallback
    ) {
        return args.length > index && !args[index].trim().isEmpty()
                ? args[index].trim()
                : fallback;
    }

    private static String utc(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(
                pattern,
                Locale.ENGLISH
        );
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}
