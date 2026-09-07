package javaapplication1;

import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;

public class sendProbe {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS = "\"3001\"/Internet=192.168.22.199+3001";
    private static final String P7_USER_OR_ADDRESS = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String P7_USER_PASSWORD = "amhs";
    private static final String ORIGIN_OR_ADDRESS = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String RECIPIENT_OR_ADDRESS = "/CN=VVTSSWIM/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private P7BindSession session;
    private String origin;
    private String recipient;
    private int extended = 0;

    private String messageId;
    private String submissionTimeStr;
    private String ipmId;

    public sendProbe(P7BindSession session, String origin, String recipient) {
        this.session = session;
        this.origin = origin;
        this.recipient = recipient;
    }

    public static void main(String[] args) {

        P7BindSession session = null;

        try {
            System.out.println("Creating P7 session...");

            session = new P7BindSession(P7_MESSAGE_STORE_PRESENTATION_ADDRESS, P7_USER_OR_ADDRESS, P7_USER_PASSWORD,false);
            session.bind();

            System.out.println("Bind OK");

            sendProbe probe = new sendProbe(session, ORIGIN_OR_ADDRESS, RECIPIENT_OR_ADDRESS);
            probe.setExtended(0);

            System.out.println("Sending PROBE...");

            probe.createMessageAndSend(X400_att.X400_MSG_PROBE);

            System.out.println("PROBE SENT OK");
            System.out.println("Message ID      = " + probe.getMessageId());
            System.out.println("Submission Time = " + probe.getSubmissionTimeStr());
            System.out.println("IPM ID          = " + probe.getIpmId());

        } catch (X400APIException e) {
            System.out.println("X400 ERROR: " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();

        } finally {
            if (session != null) {
                try {
                    if (session.isBound()) session.unbind();
                } catch (Exception e) {
                    System.out.println("UNBIND ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void createMessageAndSend(int mode) throws X400APIException {

        int type = mode;
        X400Msg message = new X400Msg(session);
        int code = X400ms.x400_ms_msgnew(session, type, message);

        if (code != X400_att.X400_E_NOERROR) {
            String errorDetail = X400ms.x400_ms_get_string_error(session, code);
            throw new X400APIException("Khong tao duoc doi tuong dien van: " + errorDetail, code);
        }

        try {
            System.out.println("Build envelope...");
            buildEnvelope(message);

            System.out.println("Build recipient...");
            buildRecipt(message);

            System.out.println("Sending...");
            code = X400ms.x400_ms_msgsend(message);

            if (code != X400_att.X400_E_NOERROR) {
                String errorDetail = X400ms.x400_ms_get_string_error(session, code);
                throw new X400APIException("Sending message fail: " + errorDetail, code);
            }

            System.out.println("Send OK");

            this.messageId = getStrParam(message, X400_att.X400_S_MESSAGE_IDENTIFIER);
            this.submissionTimeStr = getStrParam(message, X400_att.X400_S_MESSAGE_SUBMISSION_TIME);
            this.ipmId = getStrParam(message, X400_att.X400_S_IPM_IDENTIFIER);

        } finally {
            try {
                message.delete(true);
            } catch (Exception e) {
                System.out.println("Cannot delete X400 message: " + e.getMessage());
            }
        }
    }

    protected void buildEnvelope(MSMessage msMessage) throws X400APIException {

        Integer priority = 0;
        Integer contentType = null;
        Integer recipientDisclosure = 1;
        Integer implicitConversionProhibited = 0;
        Integer alternateRecipientAllowed = 1;
        Integer contentReturnRequest = 1;
        Integer recipientReassignmentProhibition = 0;
        Integer dlExpansionProhibited = 0;
        Integer conversionWithLossProhibited = 0;

        System.out.println("Origin = " + origin);

        setStrParam(msMessage, X400_att.X400_S_OR_ADDRESS, origin);
        setIntParam(msMessage, X400_att.X400_N_PRIORITY, priority);
        setIntParam(msMessage, X400_att.X400_N_CONTENT_TYPE, contentType);
        setIntParam(msMessage, X400_att.X400_N_DISCLOSURE, recipientDisclosure);
        setIntParam(msMessage, X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED, implicitConversionProhibited);
        setIntParam(msMessage, X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED, alternateRecipientAllowed);
        setIntParam(msMessage, X400_att.X400_N_CONTENT_RETURN_REQUEST, contentReturnRequest);
        setIntParam(msMessage, X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, recipientReassignmentProhibition);
        setIntParam(msMessage, X400_att.X400_N_DL_EXPANSION_PROHIBITED, dlExpansionProhibited);
        setIntParam(msMessage, X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED, conversionWithLossProhibited);

        if (this.extended == 1) {
            setStrParam(msMessage, X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES, "2.6.1.12.0");
        } else {
            setStrParam(msMessage, X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES, "ia5-text");
        }
    }

    protected void buildRecipt(X400Msg msMessage) throws X400APIException {

        System.out.println("Origin    = " + origin);
        System.out.println("Recipient = " + recipient);

        addRecip(msMessage, X400_att.X400_ORIGINATOR, 1, origin);
        addRecip(msMessage, X400_att.X400_RECIP_STANDARD, 1, recipient);
    }

    protected void addRecip(MSMessage msMessage, int type, int no, String address) throws X400APIException {

        Recip recip = new Recip();
        int status = X400ms.x400_ms_recipnew(msMessage, type, recip);

        if (status != X400_att.X400_E_NOERROR) {
            throw new X400APIException("Khong the tao duoc dia chi nhan dien van", status);
        }

        setStrParam(recip, X400_att.X400_S_OR_ADDRESS, address, -1);
        setIntParam(recip, X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, no);
        setIntParam(recip, X400_att.X400_N_MTA_REPORT_REQUEST, 1);
        setIntParam(recip, X400_att.X400_N_REPORT_REQUEST, 1);
        setIntParam(recip, X400_att.X400_N_NOTIFICATION_REQUEST, 1);
    }

    protected void setIntParam(MSMessage ms, int attribute, Integer value) {

        if (value == null) return;

        int status = X400ms.x400_ms_msgaddintparam(ms, attribute, value);

        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("setIntParam FAIL: attribute=" + attribute + ", value=" + value + ", code=" + status);
        }
    }

    protected void setStrParam(MSMessage ms, int attribute, String value) {

        if (value == null) return;

        int status = X400ms.x400_ms_msgaddstrparam(ms, attribute, value, value.length());

        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("setStrParam FAIL: attribute=" + attribute + ", value=" + value + ", code=" + status);
        }
    }

    protected void setStrParam(MSMessage ms, int attribute, String value, int flag) {

        if (value == null) return;

        int status = X400ms.x400_ms_msgaddstrparam(ms, attribute, value, flag);

        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("setStrParam FAIL: attribute=" + attribute + ", value=" + value + ", flag=" + flag + ", code=" + status);
        }
    }

    protected void setIntParam(Recip recip, int attribute, Integer value) {

        if (value == null) return;

        int status = X400ms.x400_ms_recipaddintparam(recip, attribute, value);

        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("setIntParam Recip FAIL: attribute=" + attribute + ", value=" + value + ", code=" + status);
        }
    }

    protected void setStrParam(Recip recip, int attribute, String value, int mode) {

        if (value == null) return;

        int status = X400ms.x400_ms_recipaddstrparam(recip, attribute, value, mode);

        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("setStrParam Recip FAIL: attribute=" + attribute + ", value=" + value + ", mode=" + mode + ", code=" + status);
        }
    }

    protected String getStrParam(MSMessage ms, int attribute) {

        StringBuffer value = new StringBuffer();
        int status = X400ms.x400_ms_msggetstrparam(ms, attribute, value);

        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("getStrParam FAIL: attribute=" + attribute + ", code=" + status);
            return null;
        }

        return value.toString();
    }

    public void setExtended(int extended) {
        this.extended = extended;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSubmissionTimeStr() {
        return submissionTimeStr;
    }

    public String getIpmId() {
        return ipmId;
    }
}