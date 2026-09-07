/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SendIPN;

import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;



/**
 *
 * @author ANDH
 */
public abstract class DeliverMessageBase {

    protected String submissionTime;

    public DeliverMessageBase() {
    }

    public abstract void build(MSMessage msSessage);

    protected void addRecipient(MSMessage msMessage, int type, int rno, Recipient recipient) {

        final Recip recip = new Recip();

        final int status = com.isode.x400api.X400ms.x400_ms_recipnew(msMessage, type, recip);
       
        if (status != X400_att.X400_E_NOERROR) {
            return;
        }

        set(recip, X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, rno);
        set(recip, X400_att.X400_S_OR_ADDRESS, "/CN=VVNBZQZX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/");
        set(recip, X400_att.X400_N_MTA_REPORT_REQUEST, 0);
        set(recip, X400_att.X400_N_REPORT_REQUEST, 0);
        set(recip, X400_att.X400_N_NOTIFICATION_REQUEST, 0);
        set(recip, X400_att.X400_N_REPLY_REQUESTED, 0);
        set(recip, X400_att.X400_S_FREE_FORM_NAME, "");

//        set(recip, X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, rno);
//        set(recip, X400_att.X400_S_OR_ADDRESS, recipient.getAddress());
//        set(recip, X400_att.X400_N_MTA_REPORT_REQUEST, recipient.getMtaReportRequest());
//        set(recip, X400_att.X400_N_REPORT_REQUEST, recipient.getReportRequest());
//        set(recip, X400_att.X400_N_NOTIFICATION_REQUEST, recipient.getReceiptNotification());
//        // set(recip, X400_att.X400_N_REPLY_REQUESTED, messageCfg.getReplyRequest());
//        set(recip, X400_att.X400_S_FREE_FORM_NAME, recipient.getAddress());
    }

    protected void set(MSMessage ms, int attribute, String value) {
        if (value == null || value.isEmpty()) {
            X400ms.x400_ms_msgaddstrparam(ms, attribute, null, -1);
            return;
        }
        X400ms.x400_ms_msgaddstrparam(ms, attribute, value, value.length());
    }

    protected void set(MSMessage mt, int attribute, Integer value) {
        if (value == null) {
            return;
        }
        set(mt, attribute, value, 0);
    }

    protected void set(MSMessage mt, int attribute, Integer value, Integer mode) {
        if (value == null) {
            return;
        }

        X400ms.x400_ms_msgaddintparam(mt, attribute, value);

    }

    protected void set(Recip recipObj, int attribute, String value, int length, Integer mode) {
        if (value == null || value.isEmpty()) {
            return;
        }
        com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recipObj, attribute, value, length);

    }

    protected void set(Recip recipObj, int attribute, String value) {
        set(recipObj, attribute, value, value.length(), 0);
    }

    protected void set(Recip recipObj, int attribute, Integer value, Integer mode) {
        if (value == null) {
            return;
        }

        com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recipObj, attribute, value);

    }

    protected void set(Recip recipObj, int attribute, Integer value) {
        set(recipObj, attribute, value, 0);
    }

    protected void set(Recip recipObj, int attribute, String value, int length) {
        set(recipObj, attribute, value, length, 0);
    }

    protected Priority parseAmhsPriority(Integer value) {
        if (value == null || value < 0 || value > 2) {
            return null;
        }

        switch (value) {
            case 0:
                return Priority.NORMAL;
            case 1:
                return Priority.NONE_URGENT;
            case 2:
                return Priority.URGENT;
        }
        return null;
    }
}
