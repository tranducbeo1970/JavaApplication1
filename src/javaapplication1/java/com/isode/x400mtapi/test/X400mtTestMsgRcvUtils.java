/*  Copyright (c) 2008-2009, Isode Limited, London, England.
 *  All rights reserved.
 *                                                                       
 *  Acquisition and use of this software and related materials for any      
 *  purpose requires a written licence agreement from Isode Limited,
 *  or a written licence from an organisation licenced by Isode Limited
 *  to grant such a licence.
 *
 */

/**
 * Support methods for transferring message from an MTA
 */


package com.isode.x400mtapi.test;

import com.isode.x400api.X400_att;
import com.isode.x400api.DLExpHist;
import com.isode.x400api.Message;
import com.isode.x400api.BodyPart;
import com.isode.x400api.Recip;
import com.isode.x400api.InternalTraceinfo;
import com.isode.x400api.Traceinfo;
import com.isode.x400api.RediHist;
import com.isode.x400api.PSS;
import com.isode.x400api.ALI;
import com.isode.x400api.OtherRecip;
import com.isode.x400api.DistField;
import com.isode.x400mtapi.MTMessage;

import com.isode.x400api.test.config;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;

public class X400mtTestMsgRcvUtils
{

  
    private static int get_ea_recips(MTMessage mtmessage_obj) {
	int status;
	int len = -1, maxlen = -1;
	int int_value; // int to contain value returned from API 
        int recip_num;
        int paramtype;
        
	// Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();
        
	// instantiate a recip object, and retrieve a recip
	// putting it into an API object 
	Recip recip_obj = new Recip();
	for ( recip_num = 1; ; recip_num++ ) {
	
	    // get the recip
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipget(
                mtmessage_obj, 
                X400_att.X400_EXEMPTED_ADDRESS,
                recip_num,
                recip_obj);
	    if (status == X400_att.X400_E_NO_RECIP) {
		System.out.println("no more recips ...");
		break;
	    }
	    else if (status != X400_att.X400_E_NOERROR) {
		System.out.println("x400_mt_recipget failed " + status);
		break;
	    }

            paramtype = X400_att.X400_S_IOB_OR_ADDRESS;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("oraddress" + "(" + len + ")" + ret_value.toString());
	    }

            paramtype = X400_att.X400_S_IOB_DN_ADDRESS;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
	recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("dnaddress" + "(" + len + ")" + ret_value.toString());
	    }
            
            paramtype = X400_att.X400_S_IOB_FREE_FORM_NAME;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("free form name" + "(" + len + ")" + ret_value.toString());
	    }
            
            paramtype = X400_att.X400_S_IOB_TEL;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("free form name" + "(" + len + ")" + ret_value.toString());
	    }
            
            
        }
        return X400_att.X400_E_NOERROR;
    }




    
    static public int get_p772 (MTMessage mtmessage_obj)
    {
        int status;
        int paramtype;
        int int_value;
        int len;
        StringBuffer ret_value = new StringBuffer();
        
        System.out.println("Fetching P772");

        /* STANAG 4406 A1.1 Exempted Address*/
        System.out.println("Fetching exempted addresses");
        get_ea_recips(mtmessage_obj);

        /* STANAG 4406 A1.3	Distribution codes */
        System.out.println("Fetching distribution codes");
        PSS dc_obj = new PSS();
        int entry;
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_pss(
                    mtmessage_obj,
                    entry,
                    dc_obj,
                    X400_att.X400_S_DIST_CODES_SIC
                );
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_dist_codes failed " + status);
		    break;
		}
		entry++;
	    }
        /* Now get the dist fields */
        DistField df_obj = new DistField();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_distfield(
                    mtmessage_obj,
                    entry,
                    df_obj
                );
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_distfield failed " + status);
		    break;
		}
		entry++;
	    }

        
        
        
        /* STANAG 4406 A1.4	Handling instructions */
        System.out.println("Fetching handling instructions");
        PSS hi_obj = new PSS();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_pss(
                    mtmessage_obj,
                    entry,
                    hi_obj,
                    X400_att.X400_S_HANDLING_INSTRUCTIONS
                );
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_dist_codes failed " + status);
		    break;
		}
		entry++;
	    }
        
        /* STANAG 4406 A1.5	Message instructions */
        System.out.println("Fetching message instructions");
        PSS mi_obj = new PSS();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_pss(
                    mtmessage_obj,
                    entry,
                    mi_obj,
                    X400_att.X400_S_MESSAGE_INSTRUCTIONS
                );
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_pss failed " + status);
		    break;
		}
		entry++;
	    }

        
        
        /* STANAG 4406 A1.6	Codress message */
	paramtype = X400_att.X400_N_EXT_CODRESS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Coderess " + int_value);
	}

        /*STANAG 4406 A1.7	Originator reference*/
        paramtype = X400_att.X400_S_ORIG_REF;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Originator ref (" + len + ") " +  ret_value.toString());
	}

        
        /* STANAG 4406 A1.8	Primary precedence */
	paramtype = X400_att.X400_N_EXT_PRIM_PREC;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Primary precedence " + int_value);
	}
        

        /* STANAG 4406 A1.9    Copy precedence */
	paramtype = X400_att.X400_N_EXT_COPY_PREC;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Copy precedence " + int_value);
	}
        
        /* STANAG 4406 A1.10	Message type */
	paramtype = X400_att.X400_N_EXT_MSG_TYPE;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Message type " + int_value);
	}
        
        
        
        /*STANAG 4406 A1.11	Address list indicator*/
        System.out.println("Address list indicator");
        ALI ali = new ALI();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_ali(
                    mtmessage_obj,
                    entry,
                    ali
                );
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_ali failed " + status);
		    break;
		}
		entry++;
	    }



        
        /*STANAG 4406 A1.12	Other Recipient*/
        System.out.println("Other Recipients");
        OtherRecip otherrecip = new OtherRecip();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_otherrecip(
                    mtmessage_obj,
                    entry,
                    otherrecip
                );
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_otherrecip failed " + status);
		    break;
		}
		entry++;
	    }

        
        /*STANAG 4406 A1.2	Extended authorisation information */
	paramtype = X400_att.X400_S_EXT_AUTH_INFO;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Ext auth info (" + len + ") " +  ret_value.toString());
	}
        
        /* STANAG 4406 A1.14	ACP127 message identifier */
	paramtype = X400_att.X400_S_ACP127_MSG_ID;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("ACP127 MSG ID (" + len + ") " +  ret_value.toString());
	}
        

        /* STANAG 4406 A1.15	Originator PLAD */
	paramtype = X400_att.X400_S_ORIG_PLAD;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("ORIG PLAD (" + len + ") " +  ret_value.toString());
	}

        
        
        
        /* Now get binary blobs */
        
        byte[] binarydata = new byte[config.maxlen];
        
        /* STANAG 4406 A1.13	Pilot forwarding information */
        paramtype = X400_att.X400_S_PILOT_FWD_INFO;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
	    mtmessage_obj, paramtype, binarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to fetch pilot fwd info " + status);

            if (status == X400_att.X400_E_NOSPACE) {
                System.out.println("Attempting again with a bigger buffer");
                binarydata = new byte [mtmessage_obj.GetAttLen()];
                status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
                    mtmessage_obj, paramtype, binarydata);
            }
            
	}
        
        if (status == X400_att.X400_E_NOERROR)   {
	    len = mtmessage_obj.GetAttLen();
	    System.out.println("pilot fwd info: Binary data (" + len + ")" );
            write_bin_file("pilot.ber",binarydata,len);
	}

        /* STANAG 4406 A1.16	Security Information Labels */
        paramtype = X400_att.X400_S_INFO_SEC_LABEL;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
	    mtmessage_obj, paramtype, binarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to fetch security info labels " + status);
            if (status == X400_att.X400_E_NOSPACE) {
                System.out.println("Attempting again with a bigger buffer");
                binarydata = new byte [mtmessage_obj.GetAttLen()];
                status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
                    mtmessage_obj, paramtype, binarydata);
            }
            
	}
        
        if (status == X400_att.X400_E_NOERROR)   {
	    len = mtmessage_obj.GetAttLen();
	    System.out.println("security info labels: Binary data (" + len + ")" );
            write_bin_file("sec_info_label.ber",binarydata,len);
	}

        return X400_att.X400_E_NOERROR;
    }
    
    /* Cheap noddy way of writing out sample binary file */
    static  private void write_bin_file (
         String filename,
         byte[] bytes,
         int num_of_bytes
    )
    {
     
        try {
            File fd = new File(config.outdir, filename);
            FileOutputStream fos = new FileOutputStream(fd);
            
            fos.write(bytes,0,num_of_bytes);
            fos.close();
            System.out.println("Finished write_bin_file: ");
            return;
           
            
        } catch (IOException obj) {
            System.out.println("Failed to write bin file: "+obj.getMessage());
        }
        System.out.println("Finished write_bin_file: " + filename);
        return;
    }
    
    /**
     * Display a MT Message envelope 
     */
    public static int do_msg_env(MTMessage mtmessage_obj)
    {
	int status;
	int paramtype;
	int len = -1, maxlen = -1;
	int recip_type = 1;
	int recip_num = 1;
	int entry = 1;
	
	String value; // string to contain value returned from API 
	int int_value; // int to contain value returned from API 

/*
        NYI:
        X.411 Per-domain-bilateral-information  12.2.1.1.1.2
       
        X.411 Originally-specified-recipient-number 12.2.1.1.1.5
        X.411 DL-exempted-recipients 8.2.1.1.1.40
        X.411 Alternate-recipient-allowed 8.2.1.1.1.3
        X.411 Originator-requested-alternate-recipient  8.2.1.1.1.5
       
      
        X.411 Explicit-conversion  12.2.1.1.1.9
        X.411 Deferred-delivery-time 12.2.1.1.1.7
        X.411 Requested-delivery-method  8.2.1.1.1.14
        X.411 Physical-forwarding-prohibited  8.2.1.1.1.15
        X.411 Physical-forwarding-address-request  8.2.1.1.1.16
        X.411 Physical-delivery-modes 8.2.1.1.1.17
        X.411 Registered-mail-type 8.2.1.1.1.18
        X.411 Recipient-number-for-advice 8.2.1.1.1.19
        X.411 Physical-rendition-attributes  8.2.1.1.1.20
        X.411 Physical-delivery-report-request 8.2.1.1.1.24
        
        X.411 Message-token  8.2.1.1.1.26
        X.411 Content-confidentiality-algorithm-identifier  8.2.1.1.1.27
        X.411 Content-integrity-check  8.2.1.1.1.28
        
        X.411 Proof-of-delivery-request  8.2.1.1.1.32
        X.411 Multiple-originator-certificates 8.2.1.1.1.41
        X.411 Recipient-certificate 8.2.1.1.1.42
        X.411 Certificate-selectors 8.2.1.1.1.43
        X.411 Certificate-selectors-override 8.2.1.1.1.44
        X.411 Content-correlator 8.2.1.1.1.36
        X.411 Content  8.2.1.1.1.37
        X.411 Notification-type  8.2.1.1.1.38
        X.411 Service-message  8.2.1.1.1.39
*/
        
	System.out.println("Message Envelope:");
	System.out.println("----------------");

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();

	// get orig address: string attribute from the message envelope
        /* X.411 Originator-name  8.2.1.1.1.1 */
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

	// get some string attributes from the message envelope
        
        /* X.411 Message-identifier  12.2.1.1.1.1*/
	paramtype = X400_att.X400_S_MESSAGE_IDENTIFIER;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Message ID(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message envelope
        /* X.411 Content-identifier  8.2.1.1.1.35 */
	paramtype = X400_att.X400_S_CONTENT_IDENTIFIER;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Content ID(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message envelope
        /* X.411 Original-encoded-information-types  8.2.1.1.1.33 */
	paramtype = X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Orig EITs(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message envelope
	paramtype = X400_att.X400_S_MESSAGE_SUBMISSION_TIME;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Message submission time(" + len + ") " +  ret_value.toString());
	}

	// get the latest delivery time 
        /* X.411 Latest-delivery-time 8.2.1.1.1.13 */
	paramtype = X400_att.X400_S_LATEST_DELIVERY_TIME;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status + "(as expected)" );
	} else {
	    len = ret_value.length();
	    System.out.println("Latest delivery time(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message envelope
        /* X.411 Originator-return-address 8.2.1.1.1.21 */
	paramtype = X400_att.X400_S_ORIGINATOR_RETURN_ADDRESS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no string value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Orig return address(" + len + ") " +  ret_value.toString());
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
        
	paramtype = X400_att.X400_N_NUM_ATTACHMENTS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Num attachments " + int_value);
	}
        
        /* X.411 Priority 8.2.1.1.1.8 */
	paramtype = X400_att.X400_N_PRIORITY;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Priority " + int_value);
	}

        /* X.411 Disclosure-of-other-recipients  8.2.1.1.1.7 */
	paramtype = X400_att.X400_N_DISCLOSURE;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_BADPARAM) {
	    System.out.println("no int value for Disclosure of recips prohibited(" + paramtype 
	    + ") (as expected)");
	}
	else if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Disclosure of recips prohibited " + int_value);
	}

        /* X.411 Implicit-conversion-prohibited  8.2.1.1.1.9 */
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
	    System.out.println("Implicit conversion prohibited " + int_value);
	}

        /* X.411 Content-return-request 8.2.1.1.1.23 */
	paramtype = X400_att.X400_N_CONTENT_RETURN_REQUEST;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_BADPARAM) {
	    System.out.println("no int value for Content return request(" + paramtype 
	    + ") (as expected)");
	}
	else if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Content return request " + int_value);
	}

        /* X.411 Recipient-reassignment-prohibited  8.2.1.1.1.4 */
	paramtype = X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_BADPARAM) {
	    System.out.println("no int value for Recipient reassignment prohibited(" + paramtype 
	    + ") (as expected)");
	}
	else if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Recipient reassignment prohibited " + int_value);
	}
        
        /* X.411 DL-expansion-prohibited 8.2.1.1.1.6 */
	paramtype = X400_att.X400_N_DL_EXPANSION_PROHIBITED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_BADPARAM) {
	    System.out.println("no int value for Distribution List expansion prohibited(" + paramtype 
	    + ") (as expected)");
	}
	else if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Distribution List expansion prohibited " + int_value);
	}
        
        /* X.411 Conversion-with-loss-prohibited  8.2.1.1.1.10 */
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
	    System.out.println("Conversion with loss prohibited " + int_value);
	}

	// Find out how many recips
	status = com.isode.x400mtapi.X400mt.x400_mt_msgcountrecip(mtmessage_obj, 
	    X400_att.X400_RECIP_ENVELOPE);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("can't count recips");
	} else {
	    System.out.println("Number of recipients is " 
		+ mtmessage_obj.GetNumRecips() );
	}

	status = get_mt_recips(mtmessage_obj, X400_att.X400_RECIP_ENVELOPE, 
	    "envelope: ");
	if ( status != X400_att.X400_E_NOERROR 
	    && status != X400_att.X400_E_NO_RECIP) {
	    System.out.println("get_mt_recips failed (env)" + status);
	    return status;
	}


        status = get_mt_recips(mtmessage_obj, X400_att.X400_DL_EXEMPTED_RECIP, 
	    "DL Exempted: ");
	if ( status != X400_att.X400_E_NOERROR 
	    && status != X400_att.X400_E_NO_RECIP) {
	    System.out.println("get_mt_recips failed (env)" + status);
	    return status;
	}
        
        /* X.411 DL-expansion-history 1 8.3.1.1.1.7 */
	/* Distribution List Expansion History */
	DLExpHist dleh_obj = new DLExpHist();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
	while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_dleh(mtmessage_obj, entry, dleh_obj);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_dleh failed " + status);
		    break;
		}
		entry++;
	    }

        System.out.println("Get_mt_traceinfo ");
        /* X.411 Trace-information  12.2.1.1.1.3 */
        Traceinfo traceinfo_obj = new Traceinfo();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_traceinfo(mtmessage_obj,
                                                             entry,
                                                             traceinfo_obj,
                                                             X400_att.X400_TRACE_INFO);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_traceinfo failed " + status);
		    break;
		}
		entry++;
	    }


        /* X.411 Internal-trace-information  12.2.1.1.1.4 */
        InternalTraceinfo internaltraceinfo_obj = new InternalTraceinfo();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_internaltraceinfo(mtmessage_obj,
                                                             entry,
                                                             internaltraceinfo_obj);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_internaltraceinfo failed " + status);
		    break;
		}
		entry++;
	    }


        

        
        /* X.411 Content-correlator 8.2.1.1.1.36 */
        paramtype = X400_att.X400_S_CONTENT_CORRELATOR_IA5_STRING;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for IA5String cont corr(" + paramtype + ") failed " + status );
	} else {
	    len = ret_value.length();
	    System.out.println("Cont correlator IA5String: (" + len + ")" +  ret_value.toString());
	}
        
        byte[] bin_cc = new byte[config.maxlen];
        
        paramtype = X400_att.X400_S_CONTENT_CORRELATOR_OCTET_STRING;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
	    mtmessage_obj, paramtype, bin_cc);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to fetch Content correlator octet string " + status);

            if (status == X400_att.X400_E_NOSPACE) {
                System.out.println("Attempting again with a bigger buffer");
                bin_cc = new byte [mtmessage_obj.GetAttLen()];
                status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
                    mtmessage_obj, paramtype, bin_cc);
            }
        }
        if (status == X400_att.X400_E_NOERROR)   {
	    len = mtmessage_obj.GetAttLen();
	    System.out.println("Content Correlator Octet String: Binary data (" + len + ")" );
	}
        
        

        byte[] binarydata = new byte[config.maxlen];
        /* X.411 Message-origin-authentication-check  8.2.1.1.1.29 */
        paramtype = X400_att.X400_S_MOAC;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
	    mtmessage_obj, paramtype, binarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to fetch MOAC " + status);

            if (status == X400_att.X400_E_NOSPACE) {
                System.out.println("Attempting again with a bigger buffer");
                binarydata = new byte [mtmessage_obj.GetAttLen()];
                status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
                    mtmessage_obj, paramtype, binarydata);
            }
        }
        if (status == X400_att.X400_E_NOERROR)   {
	    len = mtmessage_obj.GetAttLen();
	    System.out.println("MOAC: Binary data (" + len + ")" );
            write_bin_file("moac.ber",binarydata,len);
	}
        
         /* X.411 Originator-certificate  8.2.1.1.1.25 */
        paramtype = X400_att.X400_S_ORIG_CERT;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
	    mtmessage_obj, paramtype, binarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to fetch orig cert " + status);
            
            if (status == X400_att.X400_E_NOSPACE) {
                System.out.println("Attempting again with a bigger buffer");
                binarydata = new byte [mtmessage_obj.GetAttLen()];
                status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
                    mtmessage_obj, paramtype, binarydata);
            }
        }

         if (status == X400_att.X400_E_NOERROR)   {
	    len = mtmessage_obj.GetAttLen();
	    System.out.println("Originator cert: Binary data (" + len + ")" );
            write_bin_file("orig_cert.p12",binarydata,len);
	}
        
        /*  X.411 Message-security-label  8.2.1.1.1.30 */
        paramtype = X400_att.X400_S_SECURITY_LABEL;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
	    mtmessage_obj, paramtype, binarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to fetch orig cert " + status);
            
            if (status == X400_att.X400_E_NOSPACE) {
                System.out.println("Attempting again with a bigger buffer");
                binarydata = new byte [mtmessage_obj.GetAttLen()];
                status = com.isode.x400mtapi.X400mt.x400_mt_msggetbyteparam(
                    mtmessage_obj, paramtype, binarydata);
            }
        }

        if (status == X400_att.X400_E_NOERROR)   {
	    len = mtmessage_obj.GetAttLen();
	    System.out.println("X411 secuirty label: Binary data (" + len + ")" );
            write_bin_file("x411seclabel.ber",binarydata,len);
	}
        
        return X400_att.X400_E_NOERROR;

    }

    /**
     * Display the MT Message headers 
     */
    public static int do_msg_headers(MTMessage mtmessage_obj)
    {
	int status;
	int paramtype;
	int len = -1, maxlen = -1;

	String value; // string to contain value returned from API 
	int int_value; // int to contain value returned from API 

	System.out.println("Message Content:");
	System.out.println("----------------");

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();

        /* The following attributes are mostly from X.420 */
	// get some string attributes from the message content
        /* X.420 7.2.1 This IPM */
	paramtype = X400_att.X400_S_IPM_IDENTIFIER;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for IPM ID(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("IPM IDentifier(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X.420 7.2.10 Subject  */
	paramtype = X400_att.X400_S_SUBJECT;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for Subject(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Subject(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X.420 7.2.7 Replied-to IPM */
	paramtype = X400_att.X400_S_REPLIED_TO_IDENTIFIER;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for replied to ID(" 
		+ paramtype + ") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Replied-to-identifier(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X.420 7.2.8 Obsoleted IPMs */
	paramtype = X400_att.X400_S_OBSOLETED_IPMS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for obsoleted IPMs(" 
		+ paramtype + ") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Obsoleted IPMs(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X.420 7.2.9 Related IPMs */
	paramtype = X400_att.X400_S_RELATED_IPMS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for related IPMs(" 
		+ paramtype + "result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Related IPMs(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X.420 7.2.11 Expiry Time */
	paramtype = X400_att.X400_S_EXPIRY_TIME;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for expiry time(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Expiry Time(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X.420 7.2.12 Reply Time */
	paramtype = X400_att.X400_S_REPLY_TIME;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for reply time(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Reply Time(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X400(1999) IPM Authorization Time  */
	paramtype = X400_att.X400_S_AUTHORIZATION_TIME;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for auth time(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Authorisation Time(" + len + ") " +  ret_value.toString());
	}

	// get some string attributes from the message content
        /* X400(1999) IPM Originator's reference */
	paramtype = X400_att.X400_S_ORIGINATORS_REFERENCE;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for orig ref(" + paramtype + 
	    	") result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("Originator's Reference(" + len + ") " +  ret_value.toString());
	}
        
        /* X.420 7.2.14 Importance */
	paramtype = X400_att.X400_N_IMPORTANCE;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for importance " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Importance " + int_value);
	}

        /* X.420 7.2.15 Sensitivity  */
	paramtype = X400_att.X400_N_SENSITIVITY;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for sensitivity" 
	    + paramtype + ")");
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Sensitivity " + int_value);
	}
        /* X.420 7.2.16 Auto-forwarded */
	paramtype = X400_att.X400_N_AUTOFORWARDED;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for autoforwarded (" 
	    + paramtype + ")");
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " + status);
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("Autoforwarded " + int_value);
	}

	paramtype = X400_att.X400_N_NUM_ATTACHMENTS;
    	status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
	    mtmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for num atts " 
	    + paramtype + ")");
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetintparam failed " 
	    + paramtype + ")");
	} else {
	    int_value = mtmessage_obj.GetIntValue();
	    System.out.println("number of attachments " + int_value);
	}

	status = get_mt_recips(mtmessage_obj, X400_att.X400_ORIGINATOR, 
	    "originator: ");
	if ( status != X400_att.X400_E_NOERROR 
	    && status != X400_att.X400_E_NO_RECIP) {
	    System.out.println("get_mt_recips failed " + status);
	    return status;
	}
	status = get_mt_recips(mtmessage_obj, X400_att.X400_RECIP_PRIMARY, 
	    "primary: ");
	if ( status != X400_att.X400_E_NOERROR 
	    && status != X400_att.X400_E_NO_RECIP) {
	    System.out.println("get_mt_recips failed " + status);
	    return status;
	}
	return status;
    }

    /**
     * Display the MT Message recipients
     */
    public static int get_mt_recips(MTMessage mtmessage_obj,
                                    int type,
                                    String logstr)  {
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

            /* X.411 Intented recipient name / redirection reason  8.3.1.1.1.5 */
            RediHist redihist_obj = new RediHist();
            int entry = 1;
            status = X400_att.X400_E_NOERROR;
            while(status == X400_att.X400_E_NOERROR)
                {
                    status = X400mtTestRcvUtils.get_mt_redihist(null,
                                                                recip_obj,
                                                                entry,
                                                                redihist_obj);
                    if (status != X400_att.X400_E_NOERROR) {
                        System.out.println("get_mt_redihist failed " + status);
                        break;
                    }
                    entry++;
                }
            
	    System.out.println("-------------- Recipient " + recip_num + "--------------");
	    // display recip name
            /* X.411 Recipient-name  8.2.1.1.1.2 */
	    paramtype = X400_att.X400_S_OR_ADDRESS;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("oraddress" + "(" + len + ")" + ret_value.toString());
	    }

	    // Get envelope values 
	    if (type == X400_att.X400_RECIP_ENVELOPE) {
		// display recip properties
		paramtype = X400_att.X400_S_OR_ADDRESS;
		status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		    recip_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("no string value for originator address(" + paramtype + ") failed " + status);
		} else {
		    len =  ret_value.length();
		    System.out.println(logstr + recip_num + "(" + len + ")" + ret_value.toString());
		}

		// display recip properties
                /*X.411 Responsibility 12.2.1.1.1.6*/
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

	    }

	    // Get content values 
	    if (type != X400_att.X400_RECIP_ENVELOPE) {
		// display recip properties
                /* ??? */
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
                /* ??? */
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
		paramtype = X400_att.X400_S_TELEPHONE_NUMBER;
		status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		    recip_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("no string value for Telephone Number(" + paramtype + ") failed " + status);
		} else {
		    len =  ret_value.length();
		    System.out.println("Telephone Number" + "(" + len + ")" +  ret_value.toString());
		}
                
                if (config.mt_use_p772 == true) {
                    /* Get P772 per-recipient extensions */
                    paramtype = X400_att.X400_N_ACP127_NOTI_TYPE;
                    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
                        recip_obj, paramtype);
                    if (status != X400_att.X400_E_NOERROR) {
                        System.out.println("no int value for Notification request(" + paramtype + ") failed " + status);
                    } else {
                        int_value = recip_obj.GetIntValue();
                        
                        if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_NEG) != 0) {
                            System.out.println("P772 ACP127 Notification Request Type Negative\n");
                        }
                        if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_POS) != 0) {
                            System.out.println("P772 ACP127 Notification Request Type Positive\n");
                        }
                        
                        if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_TRANS) != 0) {
                            System.out.println("P772 ACP127 Notification Request Type Transfer\n");
                        }
                        
                    }
                }
	    }
	}
	return X400_att.X400_E_NOERROR;
    }

    /**
     * Display the MT Message content (ie the attachments/bodyparts) 
     */
    public static int do_msg_content(MTMessage mtmessage_obj)
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

	/*********************** ACP127 notification response is only present in Military Notification 
	System.out.println("Fetching ACP127 notification response");
	ACP127Resp resp_obj = new ACP127Resp();
	status = com.isode.x400mtapi.X400mt.x400_mt_acp127respget(mtmessage_obj, resp_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_acp127respget failed " + status);
	} else {
	    status  = com.isode.x400api.X400.x400_acp127respgetintparam(resp_obj);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("x400_acp127respgetintparam failed " + status);
	    } else {
		int_value = resp_obj.GetIntValue();
                            
		if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_NEG) != 0) {
		    System.out.println("P772 ACP127 Notification response Negative\n");
		}
		if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_POS) != 0) {
		    System.out.println("P772 ACP127 Notification response Positive\n");
		}
                            
		if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_TRANS) != 0) {
		    System.out.println("P772 ACP127 Notification response Transfer\n");
		}
                            
	    }
                        
	    status = com.isode.x400api.X400.x400_acp127respgetstrparam(resp_obj, X400_att.X400_S_ACP127_NOTI_RESP_TIME, ret_value);
	    if (status != X400_att.X400_E_NOERROR) { 
		System.out.println("x400_acp127respgetstrparam failed(" + X400_att.X400_S_ACP127_NOTI_RESP_TIME + 
				   "): result is " + status);
	    } else {
		System.out.println("P772 ACP127 Resp time " +  ret_value.toString());
	    }
                    
	    status = com.isode.x400api.X400.x400_acp127respgetstrparam(
								       resp_obj, X400_att.X400_S_ACP127_NOTI_RESP_RECIPIENT, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("x400_acp127respgetstrparam failed(" + X400_att.X400_S_ACP127_NOTI_RESP_RECIPIENT + 
				   "): result is " + status);
	    } else {
		System.out.println("P772 ACP127 Resp Recipient" +  ret_value.toString());
	    }
                        
	    status = com.isode.x400api.X400.x400_acp127respgetstrparam(
								       resp_obj, X400_att.X400_S_ACP127_NOTI_RESP_SUPP_INFO, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("x400_acp127respgetstrparam failed(" + X400_att.X400_S_ACP127_NOTI_RESP_SUPP_INFO + 
				   "): result is " + status);
	    } else {
		System.out.println("P772 ACP127 Resp Supp info" +  ret_value.toString());
	    }
                        
	    System.out.println("Address list indicator");
	    ALI ali = new ALI();
	    int entry = 1;
	    status = X400_att.X400_E_NOERROR;
	    while(status == X400_att.X400_E_NOERROR)
		{
		    status = X400mtTestRcvUtils.get_acp127respali(
								  resp_obj,
								  entry,
								  ali
								  );
		    if (status != X400_att.X400_E_NOERROR) {
			System.out.println("get_acp127respali failed " + status);
			break;
		    }
		    entry++;
		}
                        
	}
        *************************/

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
		
	// Get an IA5 attachment as a string in the message
	System.out.println("------------------------");
	System.out.println("Reading IA5 Attachment");
	paramtype = X400_att.X400_T_IA5TEXT;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msggetstrparam failed(" + paramtype + 
	    	"): result is " + status);
	} else {
	    len = ret_value.length();
	    System.out.println("IA5 Text " + "(" + len + ")" +  ret_value.toString());
	}

	// Get all the attachments/bodyparts..
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
        
        System.out.println("Read " + att_num + " attachments");
        System.out.println("------------------------");
	return X400_att.X400_E_NOERROR;
    }


   


}

