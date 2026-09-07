/*  Copyright (c) 2008, Isode Limited, London, England.
 *  All rights reserved.
 *                                                                       
 *  Acquisition and use of this software and related materials for any      
 *  purpose requires a written licence agreement from Isode Limited,
 *  or a written licence from an organisation licenced by Isode Limited
 *  to grant such a licence.
 *
 */


/* 
 *
 * 16.0v21-0
 */


/** Set of functions to support transferring message from an MTA
 ** ** These function handle the Bodyparts of MT Messages received 
 ** as an API object resulting from mtMsgGet.
 */

package com.isode.x400mtapi.test;


import com.isode.x400api.X400_att;
import com.isode.x400api.Message;
import com.isode.x400api.BodyPart;

import com.isode.x400mtapi.MTMessage;

import com.isode.x400api.test.config;

public class X400mtTestBodyPartRcvUtils
{

    /**
     * Display the MT Message content (ie the attachments/bodyparts) of 
     * a message transferred from the MTA
     */
    public static int do_msg_content_as_bp(MTMessage mtmessage_obj)
    {
	int status;
	int paramtype;
	int int_value;
	int len;
	String value;
	int att_num;
	byte[] binarydata = new byte[config.maxlen];
	int att_type;
	int att_len;

	System.out.println("------");
	System.out.println("Content");

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();

	paramtype = X400_att.X400_N_NUM_ATTACHMENTS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam (" 
		+ X400_att.X400_N_NUM_ATTACHMENTS + ") failed " + status);
	    return status;
	} 
	int_value = mtmessage_obj.GetIntValue();
	System.out.println("number of attachments " + int_value);

	if ( int_value == 0) {
	    System.out.println("No attachments");
	    return X400_att.X400_E_NOERROR;
	}
		
	// Get all the bodyparts..
	BodyPart bodypart_obj = new BodyPart();
	Message message_obj = new Message();
	for ( att_num = 0; ; att_num++ ) {
	    status = X400mtTestRcvUtils.get_bp(mtmessage_obj, 
		bodypart_obj, att_num);
            if (status == X400_att.X400_E_MISSING_ATTR) {
		System.out.println("No more bodyparts");
		break;
	    } else if (status != X400_att.X400_E_NOERROR &&
                       status != X400_att.X400_E_MISSING_ATTR) {
		return status;
	    }
	}

        System.out.println("Read " + att_num + " bodyparts");
        System.out.println("------------------------");
	return X400_att.X400_E_NOERROR;
    }

    /**
     * Fetch and display a MT Message bodypart
     */
    public static int get_msgbp(
        MTMessage mtmessage_obj,
        Message message_obj,
        int att_num,
        int type
    )
    {
	int status;
	int int_value;

	int len;
	String value;

	System.out.println("------------------------");
	System.out.println("Reading Message BodyPart " + att_num);
	// get the message body part from the MTMessage
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetmessagebody(
	    mtmessage_obj, att_num, message_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetmessagebody failed " + status);
	    return status;
	}
        
        StringBuffer ret_value = new StringBuffer();
        System.out.println("get_msgbp "+ type);
        switch(type) {
        case X400_att.X400_T_MM:
            System.out.println("Fetched P772 Military Message bodypart");

            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_MESSAGE_DELIVERY_TIME,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            } 
            System.out.println("MM bodypart delivery time "  + ret_value.toString());
            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_SUBJECT,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            System.out.println("MM subject: " + ret_value.toString());
            break;
        case X400_att.X400_T_FWDENC:
            System.out.println("Fetched P772 Forwarded encrypted bodypart");
            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_MESSAGE_DELIVERY_TIME,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            System.out.println("P772 forwarded encrypted delivery time " + ret_value.toString());
            
            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_ENCRYPTED_DATA,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            System.out.println("P772 forwarded encrypted data: " + ret_value.length() + " Bytes");
            
            break;
        case X400_att.X400_T_FWD_CONTENT:
            System.out.println("Fetched Forwarded content bodypart");
            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_MESSAGE_DELIVERY_TIME,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            System.out.println("Forwarded Content delivery time " + ret_value.toString());

            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_MESSAGE_IDENTIFIER,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            System.out.println("Forwarded Content bodypart message id: " + ret_value.toString());

            
            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_FWD_CONTENT_STRING,
                ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            System.out.println("Forwarded Content bodypart message id: " + ret_value.length());
            
            break;
            
        default:
            // Initialise object to receive returned String value
           

            System.out.println("Read Message BodyPart successfully: " + type);

            // we've got the message body part OK, find out what it is ...
            status = com.isode.x400api.X400.x400_msggetstrparam(
                message_obj, X400_att.X400_S_OBJECTTYPE, ret_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_msggetstrparam failed " + status);
                return status;
            }
            // get the returned values, and display it
            len = ret_value.length();
            //value = message_obj.GetStrValue();
            value = ret_value.toString();
            System.out.println("Got BodyPart string object type successfully ");
            if (value.equals("message")) {
                System.out.println("++++++++++++++++++++++++++++++++++++++");
                System.out.println("Message body part (ie forwarded message)");
                // no envelope for a forwarded msg
                // status = X400mtTestRcvNativeUtils.do_xmsg_env(message_obj);
                status = X400mtTestRcvNativeUtils.do_xmsg_headers(message_obj);
                /* the content can be retrieved as attachments or
                 * bodyparts. Use do_xmsg_content for the former */
                status = X400mtTestRcvNativeUtils.do_xmsg_content_as_bp(message_obj);
                // status = do_xmsg_content(message_obj);
                System.out.println("++++++++++++++++++++++++++++++++++++++");
            } else if (value.equals("report")) {
                System.out.println("Retrieved report body part");
                status = X400mtTestRepRcvUtils.do_rep_env(mtmessage_obj);
                status = X400mtTestRepRcvUtils.do_rep_content(mtmessage_obj);
                status = X400mtTestRepRcvUtils.do_rep_retcontent(mtmessage_obj);
            } else if (value.equals("probe")) {
                // Not handling a probe here 
                System.out.println("Retrieved probe body part");
                return X400_att.X400_E_NYI;
            } else {
                // unknown type
                System.out.println("Retrieved unknown message  body part type " + value);
                return X400_att.X400_E_BADPARAM;
            }
            
        }
	return X400_att.X400_E_NOERROR;

    }
}

