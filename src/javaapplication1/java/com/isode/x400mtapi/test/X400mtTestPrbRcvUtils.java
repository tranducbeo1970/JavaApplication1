/*  Copyright (c) 2008, Isode Limited, London, England.
 *  All rights reserved.
 *                                                                       
 *  Acquisition and use of this software and related materials for any      
 *  purpose requires a written licence agreement from Isode Limited,
 *  or a written licence from an organisation licenced by Isode Limited
 *  to grant such a licence.
 *
 */


/**
 * Support methods for Java X.400 API test program
 */


package com.isode.x400mtapi.test;

import com.isode.x400api.X400_att;
import com.isode.x400api.Recip;
import com.isode.x400mtapi.MTMessage;

public class X400mtTestPrbRcvUtils
{  

    public static int do_prb_env (MTMessage mtmessage_obj)
    {
        int paramtype;
        int status;
        StringBuffer ret_value = new StringBuffer();
        int len;
        int int_value;
        /*X.411 Probe-identifier 12.2.1.2.1.1 */
        System.out.println("Getting message id");
        paramtype = X400_att.X400_S_MESSAGE_IDENTIFIER;
        status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println(
		"x400_mt_msggetstrparam failed X400_S_OR_ADDRESS(" 
		+ paramtype + ") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Originator address (" + len + ") " + ret_value.toString());
	}
        
        System.out.println("Getting orig name");
        /* X.411 Originator-name 8.2.1.1.1.1 */
        paramtype = X400_att.X400_S_OR_ADDRESS;
        status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
        if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println(
		"x400_mt_msggetstrparam failed X400_S_OR_ADDRESS(" 
		+ paramtype + ") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Originator address (" + len + ") " + ret_value.toString());
	}

        get_probe_recips(mtmessage_obj,X400_att.X400_ORIGINATOR,"Originator: ");
        get_probe_recips(mtmessage_obj,X400_att.X400_RECIP_STANDARD,"Recip std: ");

        System.out.println("Getting DL expansion prohib");
        /* X.411 DL-expansion-prohibited 8.2.1.1.1.6 (0 = not prohibited) */
        paramtype = X400_att.X400_N_DL_EXPANSION_PROHIBITED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("DL expansion prohibited: " + int_value);
	}

        System.out.println("Getting alt recip name");
        /* X.411 Alternate-recipient-allowed 8.2.1.1.1.3 (1 = yes) */
        paramtype = X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Alt recipient allowed: " + int_value);
	}

        
        /* X.411 Recipient-reassignment-prohibited 8.2.1.1.1.4 (0 = no)*/
        paramtype = X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Recipient reassignment prohibited: " + int_value);
	}

        /*
          X.411 Intended-recipient-name / redirection history 8.3.1.1.1.5
          X.411 Redirection-reason
        */

        System.out.println("Getting orig name");
        /* X.411 Implicit-conversion-prohibited 8.2.1.1.1.9 */
        paramtype = X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("implicit conversion prohibited: " + int_value);
	}

        System.out.println("Getting conv with loss");
        /* X.411 Conversion-with-loss-prohibited 8.2.1.1.1.10 */
        paramtype = X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("conversion with loss prohibited: " + int_value);
	}
        
          /*
           NYI:
           X.411 Explicit-conversion 8.2.1.1.1.11
           X.411 Requested-delivery-method  8.2.1.1.1.14
           X.411 Physical-rendition-attributes 8.2.1.1.1.20 
         */
        
        /*
           X.411 Originator-certificate  8.2.1.1.1.25
           X.411 Probe-origin-authentication-check  8.2.1.2.1.1
           X.411 Message-security-label 8.2.1.1.1.30
        */

        System.out.println("Getting OEIT");
        /* X.411 Original-encoded-information-types 8.2.1.1.1.33 */
        paramtype = X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES;
        status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	} else {
	    len = ret_value.length();
	    System.out.println("OEIT: " + ret_value.toString());
	}
        
        /* X.411 Content-type 8.2.1.1.1.34
         * Normally content type will be P2 or P22
         * However if your are manipulating a P772 message,
         * then you need to use an externally defined content type
         */
       
        System.out.println("Getting content type");
        paramtype = X400_att.X400_S_EXTERNAL_CONTENT_TYPE;
        status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
            /* If the External content type isn't set use the normal content type*/
            paramtype = X400_att.X400_N_CONTENT_TYPE;
            status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
            if (status == X400_att.X400_E_NO_VALUE) {
                System.out.println("no int value for " + paramtype);
            } else {
                int_value = mtmessage_obj.GetIntValue();
                System.out.println("Content type: " + int_value);
            }
	} else {
	    len = ret_value.length();
	    System.out.println("Ext content type: " + ret_value.toString());
	}

        
        return X400_att.X400_E_NOERROR;
    }

    public static int do_prb_content(MTMessage mtmessage_obj)
    {

        /* probes don't have content */
        return X400_att.X400_E_NOERROR;
    }

    
    
     private static int get_probe_recips(MTMessage mtmessage_obj,
                                         int type,
                                         String logstr)
     {
         int status;
         int paramtype;
         int recip_num = 1;
         int len = -1, maxlen = -1;
         int int_value; // int to contain value returned from API 
         
         // Initialise object for returning string values
         StringBuffer ret_value = new StringBuffer();

         // instantiate a recip object, and retrieve a recip
         // putting it into an API object 
         Recip recip_obj = new Recip();
         for ( recip_num = 1; ; recip_num++ ) {
             System.out.println("Getting recip:" + logstr);
             // get the recip
             status = com.isode.x400mtapi.X400mt.x400_mt_recipget(mtmessage_obj, 
                      type, recip_num, recip_obj);
             if (status == X400_att.X400_E_NO_RECIP) {
                 System.out.println("no more recips ...");
                 break;
             }
             else if (status != X400_att.X400_E_NOERROR) {
                 System.out.println("x400_ms_recipget failed " + status);
                 break;
             }
             
             System.out.println("-------------- Recipient " + recip_num + "--------------");
             System.out.println("Getting recip name");
             // display recip name
             /* X.411 Recipient-name  8.2.1.1.1.2 */
             paramtype = X400_att.X400_S_OR_ADDRESS;
             status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
                 recip_obj, paramtype, ret_value);
             if (status != X400_att.X400_E_NOERROR) {
                 System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
             } else {
                 len =  ret_value.length();
                 System.out.println(logstr + recip_num + "(" + len + ")" + ret_value.toString());
             }
             
             // Get envelope values
             
             if (type == X400_att.X400_RECIP_ENVELOPE) {
                 System.out.println("Getting originally specified recip num");
                 /* X.411 Originally-specified-recipient-number 12.2.1.1.1.5 */
                 paramtype = X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER;
                 status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
                     recip_obj, paramtype);
                 if (status != X400_att.X400_E_NOERROR) {
                     System.out.println("no int value for orig recip num(" + paramtype + ") failed " + status);
                 } else {
                     int_value = recip_obj.GetIntValue();
                     System.out.println("Originally-specified-recipient-number " + int_value);
                 }
                 
                 /* PerRecipientIndicators */
                 System.out.println("Getting recip responsibility");
                 /* X.411 Responsibility 12.2.1.1.1.6 */
                 paramtype = X400_att.X400_N_RESPONSIBILITY;
                 status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
                     recip_obj, paramtype);
                 if (status != X400_att.X400_E_NOERROR) {
                     System.out.println("no int value for Responsibility(" + paramtype + ") failed " + status);
                 } else {
                     int_value = recip_obj.GetIntValue();
                     System.out.println("Responsibility " + int_value);
                 }

                 // display recip properties
                 System.out.println("Getting recip orig MTA report request");
                 /* X.411 Originating-MTA-report-request 12.2.1.1.1.8 */
                 paramtype = X400_att.X400_N_MTA_REPORT_REQUEST;
                 status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
                     recip_obj, paramtype);
                 if (status != X400_att.X400_E_NOERROR) {
                     System.out.println("no int value for MTA report req(" + paramtype + ") failed " + status);
                 } else {
                     int_value = recip_obj.GetIntValue();
                     System.out.println("MTA report request " + int_value);
                 }

                 // display recip properties
                 System.out.println("Getting recip report request");
                 /* X.411 Originator-report-request  8.2.1.1.1.22 */
                 paramtype = X400_att.X400_N_REPORT_REQUEST;
                 status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
                     recip_obj, paramtype);
                 if (status != X400_att.X400_E_NOERROR) {
                     System.out.println("no int value for report req(" + paramtype + ") failed " + status);
                 } else {
                     int_value = recip_obj.GetIntValue();
                     System.out.println("Originator report request " + int_value);
                 }

                 /* X.411 Originator-requested-alternate-recipient 8.2.1.1.1.5 */
                 System.out.println("Getting recip orig req alt recip");
                 paramtype = X400_att.X400_S_ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT;
                 status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
                 recip_obj, paramtype, ret_value);
             if (status != X400_att.X400_E_NOERROR) {
                 System.out.println("no string value for orig req alt recip(" + paramtype + ") failed " + status);
             } else {
                 len =  ret_value.length();
                 System.out.println(logstr + recip_num + "(" + len + ")" + ret_value.toString());
             }

             /* NYI: redirection history */
             
             }

	    // Get content values 
	    if (type != X400_att.X400_RECIP_ENVELOPE) {
		// display recip properties
                System.out.println("Getting recip notification request");
		paramtype = X400_att.X400_N_NOTIFICATION_REQUEST;
		status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		    recip_obj, paramtype);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("no int value for notification req(" + paramtype + ") failed " + status);
		} else {
		    int_value = recip_obj.GetIntValue();
		    System.out.println("Responsibility " + int_value);
		}

		// display recip properties
                System.out.println("Getting recip reply request");
		paramtype = X400_att.X400_N_REPLY_REQUESTED;
		status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		    recip_obj, paramtype);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("no int value for reply requested(" + paramtype + ") failed " + status);
		} else {
		    int_value = recip_obj.GetIntValue();
		    System.out.println("Reply Request " + int_value);
		}

		// display recip properties
                System.out.println("Getting recip free form name");
		paramtype = X400_att.X400_S_FREE_FORM_NAME;
		status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		    recip_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("no string value for Free form name(" + paramtype + ") failed " + status);
		} else {
		    len =  ret_value.length();
		    System.out.println("Free form name" + "(" + len + ")" +  ret_value.toString());
		}

		// display recip properties
                System.out.println("Getting recip tel number");
		paramtype = X400_att.X400_S_TELEPHONE_NUMBER;
		status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		    recip_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("no string value for Telephone Number(" + paramtype + ") failed " + status);
		} else {
		    len =  ret_value.length();
		    System.out.println("Telephone Number" + "(" + len + ")" +  ret_value.toString());
		}
	    }
	}
	return X400_att.X400_E_NOERROR;
            
        }
         
}

