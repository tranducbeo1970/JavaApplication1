package javaapplication1;

import com.isode.x400.highlevel.BodypartIA5Text;
import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400api.AMHS_att;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;

public class SendIPN {

    private static final String P7_MESSAGE_STORE_PRESENTATION_ADDRESS = "\"3001\"/Internet=192.168.22.186+3001";
    private static final String P7_USER_OR_ADDRESS = "/CN=VVNBZQZX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String P7_USER_PASSWORD = "amhs";

    private static final String ORIGIN_OR_ADDRESS = "/CN=VVNBZQZX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String RECIPIENT_OR_ADDRESS = "/CN=VVTSOPTA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";

    private static final String MESSAGE_IPM_IDENTIFIER = "/PRMD=VIETNAM/ADMD=ICAO/C=XX/;gateway.attech.446-260820.15531";

    private static final char CHAR_SOH = 1;
    private static final char CHAR_STX = 2;

    private static final String AMHS_PRI = "PRI: ";
    private static final String AMHS_FT = "FT: ";
    private static final String AMHS_OHI = "OHI: ";
    private static final String CR_LF = "\r\n";

    private static final int PRIORITY = 0;
    private static final int CONTENT_TYPE = 22;
    private static final int DISCLOSURE = 1;
    private static final int IMPLICIT_CONVERSION_PROHIBITED = 0;
    private static final int ALTERNATE_RECIPIENT_ALLOWED = 1;
    private static final int CONTENT_RETURN_REQUEST = 1;
    private static final int RECIPIENT_REASSIGNMENT_PROHIBITED = 0;
    private static final int DL_EXPANSION_PROHIBITED = 0;
    private static final int CONVERSION_WITH_LOSS_PROHIBITED = 0;

    private static final String OEIT = "ia5-text";
    private static final String GENERAL_TEXT_CHARSETS = "1 6";

    public static void main(String[] args) {

        P7BindSession session = null;
        X400Msg message = null;
        MSMessage ipnMessage = null;

        try {

            System.out.println("========================================");
            System.out.println("Creating P7 session...");
            System.out.println("========================================");

            session = new P7BindSession(P7_MESSAGE_STORE_PRESENTATION_ADDRESS, P7_USER_OR_ADDRESS, P7_USER_PASSWORD);

            System.out.println("Binding...");

            session.bind();

            System.out.println("Bind OK");
            System.out.println("P7 User   = " + P7_USER_OR_ADDRESS);
            System.out.println("Origin    = " + ORIGIN_OR_ADDRESS);
            System.out.println("Recipient = " + RECIPIENT_OR_ADDRESS);

            System.out.println("----------------------------------------");
            System.out.println("Creating X400 MESSAGE...");
            System.out.println("----------------------------------------");

            message = new X400Msg(session);

            int code = X400ms.x400_ms_msgnew(session, X400_att.X400_MSG_MESSAGE, message);
            
             //set(message, X400_att.X400_N_IS_IPN, 1);
    //         X400ms.x400_ms_msgaddintparam(message, X400_att.X400_N_IS_IPN, 1);   ddđ

            if (code != X400_att.X400_E_NOERROR) {
                throwX400Error(session, "Cannot create X400 MESSAGE", code);
            }

            System.out.println("X400 MESSAGE created OK");

            message.setMessageIPMIdentifier(MESSAGE_IPM_IDENTIFIER);

            System.out.println("IPM Identifier = " + MESSAGE_IPM_IDENTIFIER);

            message.setFrom(ORIGIN_OR_ADDRESS);

            System.out.println("From set OK");

            message.setTo(RECIPIENT_OR_ADDRESS, X400Msg.DR_Request.DR_NON_DELIVERY_REPORT, 0);

            System.out.println("To set OK");

            System.out.println("----------------------------------------");
            System.out.println("Building envelope...");
            System.out.println("----------------------------------------");

            buildEnvelope(message, session);

            System.out.println("Envelope OK");

            System.out.println("----------------------------------------");
            System.out.println("Building IA5 content...");
            System.out.println("----------------------------------------");

            buildContent(message, session);

            System.out.println("Content OK");

            System.out.println("----------------------------------------");
            System.out.println("Creating IPN...");
            System.out.println("----------------------------------------");

//            ipnMessage = new MSMessage();
//
//            int result = X400ms.x400_ms_msgmakeIPN(message, 1, ipnMessage);
//
//            if (result != X400_att.X400_E_NOERROR) {
//                throwX400Error(session, "Cannot create IPN", result);
//            }

            System.out.println("IPN created OK");

            System.out.println("----------------------------------------");
            System.out.println("Sending IPN...");
            System.out.println("----------------------------------------");

            //set(message, X400_att.X400_N_IS_IPN, 1);
             X400ms.x400_ms_msgaddintparam(message, X400_att.X400_N_IS_IPN, 1);
            code = X400ms.x400_ms_msgsend(message);

            if (code != X400_att.X400_E_NOERROR) {
                throwX400Error(session, "Sending IPN fail", code);
            }

            System.out.println("========================================");
            System.out.println("IPN SENT OK");
            System.out.println("========================================");

        } catch (X400APIException e) {

            System.out.println();
            System.out.println("X400 ERROR: " + e.getMessage());
            System.out.println("Native error code = " + e.getNativeErrorCode());

            e.printStackTrace();

        } catch (Exception e) {

            System.out.println();
            System.out.println("ERROR: " + e.getMessage());

            e.printStackTrace();

        } finally {

            if (message != null) {
                try {
                    message.delete(true);
                } catch (Exception e) {
                    System.out.println("Cannot delete X400 message: " + e.getMessage());
                }
            }

            if (session != null) {
                try {
                    if (session.isBound()) session.unbind();
                    System.out.println("Unbind OK");
                } catch (Exception e) {
                    System.out.println("UNBIND ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void buildEnvelope(MSMessage msMessage, P7BindSession session) throws X400APIException {

        
        int val = X400ms.x400_ms_msgaddintparam(msMessage, X400_att.X400_N_IS_IPN, 1);
        
        System.out.println("Envelope Origin = " + ORIGIN_OR_ADDRESS);
        System.out.println("Priority     = " + PRIORITY);
        System.out.println("Content Type = " + CONTENT_TYPE);
        System.out.println("OEIT         = " + OEIT);
        System.out.println("Charset      = " + GENERAL_TEXT_CHARSETS);

        setStrParam(msMessage, X400_att.X400_S_OR_ADDRESS, ORIGIN_OR_ADDRESS, session);
        setIntParam(msMessage, X400_att.X400_N_PRIORITY, PRIORITY, session);
        setIntParam(msMessage, X400_att.X400_N_CONTENT_TYPE, CONTENT_TYPE, session);
        setIntParam(msMessage, X400_att.X400_N_DISCLOSURE, DISCLOSURE, session);
        setIntParam(msMessage, X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED, IMPLICIT_CONVERSION_PROHIBITED, session);
        setIntParam(msMessage, X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED, ALTERNATE_RECIPIENT_ALLOWED, session);
        setIntParam(msMessage, X400_att.X400_N_CONTENT_RETURN_REQUEST, CONTENT_RETURN_REQUEST, session);
        setIntParam(msMessage, X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, RECIPIENT_REASSIGNMENT_PROHIBITED, session);
        setIntParam(msMessage, X400_att.X400_N_DL_EXPANSION_PROHIBITED, DL_EXPANSION_PROHIBITED, session);
        setIntParam(msMessage, X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED, CONVERSION_WITH_LOSS_PROHIBITED, session);
        setStrParam(msMessage, X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES, OEIT, session);
  //      setStrParam(msMessage, X400_att.X400_S_GENERAL_TEXT_CHARSETS, GENERAL_TEXT_CHARSETS, session);

           }

    private static void buildContent(X400Msg message, P7BindSession session) throws X400APIException {

        setIntParam(message, AMHS_att.ATS_N_EXTENDED, 0, session);

        String ia5Text = buildIA5Content();

        BodypartIA5Text ia5 = new BodypartIA5Text(ia5Text);

        message.addBodypart(ia5);

        System.out.println("IA5 content:");
        System.out.println(ia5Text);
    }

    private static String buildIA5Content() {

        String ohi = "";

        String dtime = "2024-08-12 12:06:37";

        String fillingTime = dtime.substring(8, 10) + dtime.substring(11, 13) + dtime.substring(14, 16);

        StringBuilder sb = new StringBuilder();

        sb.append(CHAR_SOH).append(AMHS_PRI).append("DD").append(CR_LF).append(AMHS_FT).append(fillingTime).append(CR_LF);

        if (ohi.length() > 0) {
            sb.append(AMHS_OHI).append(ohi).append(CR_LF);
        }

        sb.append(CHAR_STX).append("AAAAAAAAAAA");

        return sb.toString();
    }

    private static void setIntParam(MSMessage ms, int attribute, Integer value, P7BindSession session) throws X400APIException {

        if (value == null) return;

        int status = X400ms.x400_ms_msgaddintparam(ms, attribute, value);

        if (status != X400_att.X400_E_NOERROR) {
            throwX400Error(session, "setIntParam FAIL: attribute=" + attribute + ", value=" + value, status);
        }
    }

    private static void setStrParam(MSMessage ms, int attribute, String value, P7BindSession session) throws X400APIException {

        if (value == null) return;

        int status = X400ms.x400_ms_msgaddstrparam(ms, attribute, value, value.length());

        if (status != X400_att.X400_E_NOERROR) {
            throwX400Error(session, "setStrParam FAIL: attribute=" + attribute + ", value=" + value, status);
        }
    }

    private static void setStrParam(MSMessage ms, int attribute, String value, int flag, P7BindSession session) throws X400APIException {

        if (value == null) return;

        int status = X400ms.x400_ms_msgaddstrparam(ms, attribute, value, flag);

        if (status != X400_att.X400_E_NOERROR) {
            throwX400Error(session, "setStrParam FAIL: attribute=" + attribute + ", value=" + value + ", flag=" + flag, status);
        }
    }

    private static void setIntParam(Recip recip, int attribute, Integer value, P7BindSession session) throws X400APIException {

        if (value == null) return;

        int status = X400ms.x400_ms_recipaddintparam(recip, attribute, value);

        if (status != X400_att.X400_E_NOERROR) {
            throwX400Error(session, "setIntParam Recip FAIL: attribute=" + attribute + ", value=" + value, status);
        }
    }

    private static void setStrParam(Recip recip, int attribute, String value, int mode, P7BindSession session) throws X400APIException {

        if (value == null) return;

        int status = X400ms.x400_ms_recipaddstrparam(recip, attribute, value, mode);

        if (status != X400_att.X400_E_NOERROR) {
            throwX400Error(session, "setStrParam Recip FAIL: attribute=" + attribute + ", value=" + value + ", mode=" + mode, status);
        }
    }

    private static String getStrParam(MSMessage ms, int attribute, P7BindSession session) throws X400APIException {

        StringBuffer value = new StringBuffer();

        int status = X400ms.x400_ms_msggetstrparam(ms, attribute, value);

        if (status != X400_att.X400_E_NOERROR) {
            throwX400Error(session, "getStrParam FAIL: attribute=" + attribute, status);
        }

        return value.toString();
    }

    private static void throwX400Error(P7BindSession session, String message, int code) throws X400APIException {

        String errorDetail = X400ms.x400_ms_get_string_error(session, code);

        throw new X400APIException(message + ": " + errorDetail, code);
    }
}