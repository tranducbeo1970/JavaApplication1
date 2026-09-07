/*  Copyright (c) 2008-2011, Isode Limited, London, England.
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
import com.isode.x400api.Session;
import com.isode.x400api.BodyPart;
import com.isode.x400api.DLExpHist;
import com.isode.x400api.Traceinfo;
import com.isode.x400api.RediHist;
import com.isode.x400api.Recip;
import com.isode.x400api.InternalTraceinfo;
import com.isode.x400api.ORandDL;
import com.isode.x400api.PSS;
import com.isode.x400api.ALI;
import com.isode.x400api.OtherRecip;
import com.isode.x400api.ACP127Resp;
import com.isode.x400api.DistField;
import com.isode.x400api.Message;

import com.isode.x400mtapi.MTMessage;

import com.isode.x400api.test.config;

public class X400mtTestRcvUtils
{

    
    private static int rcv_a_msg(Session session_obj, String[] args)
    {
	int status;
	int seqn = 0;
	int len = -1;
	int paramtype;
	int recip_type = 1;
	int recip_num = 1;
	int i = 0;
	int type;
	
	// instantiate a message object, and retrieve a msg
	// putting it into an API object 
	MTMessage mtmessage_obj = new MTMessage();
	// use seqn of 0 to retrieve the next msg 
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgget(session_obj, 
                                                         mtmessage_obj);
        if (status == X400_att.X400_E_NO_MESSAGE) {
            return status;
        }
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgget failed " + status);
	    // close the API session 
	    status = com.isode.x400mtapi.X400mt.x400_mt_close(session_obj);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("x400_mt_close failed " + status);
		return status;
	    }
	    System.out.println("Closed MT Session successfully\n");
	    return status;
	}

	// check what we got back
	type = mtmessage_obj.GetType();

        switch(type) {
        case X400_att.X400_MSG_PROBE:
        {
            System.out.println("Retrieved MT Probe successfully - displaying");
        
            status = X400mtTestPrbRcvUtils.do_prb_env(mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            
            status = X400mtTestPrbRcvUtils.do_prb_content(mtmessage_obj);
            
            break;
        }
        case X400_att.X400_MSG_REPORT:
        {
            System.out.println("Retrieved MT Report successfully - displaying");
        
            status = X400mtTestRepRcvUtils.do_rep_env(mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            
            status = X400mtTestRepRcvUtils.do_rep_content(mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            
            status = X400mtTestRepRcvUtils.do_rep_retcontent(mtmessage_obj);
        
            
            break;
        }
        case X400_att.X400_MSG_MESSAGE:
        {
             int int_value;
                     
             // got a message - is it an IPN ?
             System.out.println("Retrieved MT Message successfully - displaying");
             status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(
                 mtmessage_obj, X400_att.X400_N_IS_IPN);
             if (status != X400_att.X400_E_NOERROR) {
                 System.out.println("failed to test whether IPN or message "
                                    + status);
             } 
             int_value = mtmessage_obj.GetIntValue();
             if (int_value != 0) {
                 // It's an IPN ...
                 System.out.println
                     ("Retrieved IPN successfully - displaying");
             } else {
                 // It's not an IPN ...
                 System.out.println
                     ("Retrieved msg (not ipn) successfully - displaying");
             }
             status = X400mtTestMsgRcvUtils.do_msg_env(mtmessage_obj);
             if (status != X400_att.X400_E_NOERROR)
                 return status;
        
             status = X400mtTestMsgRcvUtils.do_msg_headers(mtmessage_obj);
             if (status != X400_att.X400_E_NOERROR)
                 return status;
       
             status = X400mtTestMsgRcvUtils.do_msg_content(mtmessage_obj);
             if (status != X400_att.X400_E_NOERROR)
                 return status;

             status = X400mtTestMsgRcvUtils.get_p772(mtmessage_obj);
             
             break;
        }
        default:
            System.out.println("Unknown message type");
            status = X400_att.X400_E_INT_ERROR;
        }
        
        return status;
    }


    /** 
     * Open a session,
     * Retrieve messages from the MTA
     * Close the session
     */ 
    public static void rcv_msgs(String[] args)
    {
        int status;
        Session session_obj = new Session();
        status = com.isode.x400mtapi.X400mt.x400_mt_open(
            config.x400mt_chan_name,
            session_obj
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_open failed " + status);
            return;
        }

        /* wait a second for messages to be transfered out.
         * This sets the value GetNumMsgs returns too. */
        status = com.isode.x400mtapi.X400mt.x400_mt_wait(session_obj,1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_mtwait failed " + status);
            return;
        }
        
        
        System.out.println("Opened MT session successfully, " + session_obj.GetNumMsgs() + " messages waiting");

        // turn on trace level logging
    	status = com.isode.x400mtapi.X400mt.x400_mt_setstrdefault(session_obj, 
	    X400_att.X400_S_LOG_CONFIGURATION_FILE, "x400api.xml", -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_setstrdefault failed " + status);
	    return;
	}

        /* Fetch each message */

        while (status == X400_att.X400_E_NOERROR ) {
            status = rcv_a_msg(session_obj, args);
        }

        if (status == X400_att.X400_E_NO_MESSAGE) {
            System.out.println("Finished fetching messages");
        } else {
            System.out.println("Failed to fetch message: " + status);
        }
        
        
        // close the API session 
    	status = com.isode.x400mtapi.X400mt.x400_mt_close(session_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_close failed " + status);
	    return;
	}
	System.out.println("Closed Session successfully\n");
	return;
        
    }

     public static int get_mt_distfield(
        MTMessage mtmessage_obj,
        int entry,
        DistField df
    ) {
        int status;
        StringBuffer ret_value = new StringBuffer();
        status = com.isode.x400mtapi.X400mt.x400_mt_distfieldget(mtmessage_obj,entry,df);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("get_distfield failed " + status);
	    return status;
	}
        
        status = com.isode.x400api.X400.x400_distfieldgetstrparam(df,X400_att.X400_S_DIST_CODES_EXT_OID, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for distfieldgetstrparam: " + status);
	} else {
	    System.out.println("Dist Field OID: " + ret_value.toString());
	}


        byte[] binarydata = new byte[config.maxlen];
        status = com.isode.x400api.X400.x400_distfieldgetbyteparam(df,X400_att.X400_S_DIST_CODES_EXT_VALUE, binarydata);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for distfieldgetstrparam: " + status);
	} else {
            int len = df.GetAttLen();
	    System.out.println("Dist Field: (" + len + ")" );
	}
        
        
        return X400_att.X400_E_NOERROR;
    }
    
    public static int get_mt_pss(
        MTMessage mtmessage_obj,
        int entry,
        PSS dc,
        int type
    ) {
        int status;
        StringBuffer ret_value = new StringBuffer();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssget(mtmessage_obj,entry,dc,type);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("get_pss failed " + status);
	    return status;
	}
        
        status = com.isode.x400api.X400.x400_pssgetstrparam(dc, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for PSS: " + status);
	} else {
	    System.out.println("PSS: " + ret_value.toString());
	}
        return X400_att.X400_E_NOERROR;
    }

    public static int get_mt_otherrecip(
        MTMessage mtmessage_obj,
        int entry,
        OtherRecip otherrecip
    ) {
        int status;
        StringBuffer ret_value = new StringBuffer();
        int ret_int;
        status = com.isode.x400mtapi.X400mt.x400_mt_otherrecipget(mtmessage_obj,entry,otherrecip);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("get_otherrecip failed " + status);
	    return status;
	}
        
        status = com.isode.x400api.X400.x400_otherrecipgetstrparam(otherrecip, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for OtherRecip: " + status);
	} else {
	    System.out.println("OtherRecip OR address: " + ret_value.toString());
	}

        
        status = com.isode.x400api.X400.x400_otherrecipgetintparam(otherrecip);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for otherrecip" + status);
	} else {
            ret_int = otherrecip.GetIntValue();
	    System.out.println("Got otherrecip int: " + ret_int);
        }
        return X400_att.X400_E_NOERROR;
      }
    
    public static int get_mt_ali(
        MTMessage mtmessage_obj,
        int entry,
        ALI ali
    ) {
        int status;
        StringBuffer ret_value = new StringBuffer();
        int ret_int;
        status = com.isode.x400mtapi.X400mt.x400_mt_aliget(mtmessage_obj,entry,ali);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("get_ali failed " + status);
	    return status;
	}
        
        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_OR_ADDRESS, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI OR address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_DN_ADDRESS, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI DN address: " + ret_value.toString());
	}
        
        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_FREE_FORM_NAME, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI free form  address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_TEL, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI Tel  address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_aligetintparam(
            ali,
            X400_att.X400_N_ALI_TYPE);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for ALI" + status);
	} else {
            ret_int = ali.GetIntValue();
	    System.out.println("Got ALI type: " + ret_int);
        }

        status = com.isode.x400api.X400.x400_aligetintparam(
            ali,
            X400_att.X400_N_ALI_NOTIFICATION_REQUEST);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for ALI" + status);
	} else {
            ret_int = ali.GetIntValue();
	    System.out.println("Got ALI notification request: " + ret_int);
        }

        status = com.isode.x400api.X400.x400_aligetintparam(
            ali,
            X400_att.X400_N_ALI_REPLY_REQUEST);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for ALI" + status);
	} else {
            ret_int = ali.GetIntValue();
	    System.out.println("Got ALI reply request: " + ret_int);
        }
        

        
        return X400_att.X400_E_NOERROR;
    }
    
    public static int get_acp127respali(
        ACP127Resp resp_obj,
        int entry,
        ALI ali
    ) {
        int status;
        StringBuffer ret_value = new StringBuffer();
        int ret_int;
        status = com.isode.x400api.X400.x400_acp127respaliget(resp_obj,entry,ali);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("get_ali failed " + status);
	    return status;
	}
        
        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_OR_ADDRESS, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI OR address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_DN_ADDRESS, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI DN address: " + ret_value.toString());
	}
        
        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_FREE_FORM_NAME, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI free form  address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_aligetstrparam(ali,X400_att.X400_S_IOB_TEL, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ALI: " + status);
	} else {
	    System.out.println("ALI Tel  address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_aligetintparam(
            ali,
            X400_att.X400_N_ALI_TYPE);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for ALI" + status);
	} else {
            ret_int = ali.GetIntValue();
	    System.out.println("Got ALI type: " + ret_int);
        }

        status = com.isode.x400api.X400.x400_aligetintparam(
            ali,
            X400_att.X400_N_ALI_NOTIFICATION_REQUEST);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for ALI" + status);
	} else {
            ret_int = ali.GetIntValue();
	    System.out.println("Got ALI notification request: " + ret_int);
        }

        status = com.isode.x400api.X400.x400_aligetintparam(
            ali,
            X400_att.X400_N_ALI_REPLY_REQUEST);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for ALI" + status);
	} else {
            ret_int = ali.GetIntValue();
	    System.out.println("Got ALI reply request: " + ret_int);
        }
        

        
        return X400_att.X400_E_NOERROR;
    }
    
    public static int get_mt_internaltraceinfo(
        MTMessage mtmessage_obj,
        int entry,
        InternalTraceinfo traceinfo_obj
    )
    {
        int status, len;
        // Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();
        int ret_int = 0;

        status = com.isode.x400mtapi.X400mt.x400_mt_internaltraceinfoget(
	    mtmessage_obj,entry, traceinfo_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no trace info found \n" + " " + status);
	    return status;
	} 
	System.out.println("Internal Traceinfo found " + " " + status);
        

        status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
            traceinfo_obj,
            X400_att.X400_S_GLOBAL_DOMAIN_ID, 
            ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for internal trace info GDI " + status);
	} else {
	    System.out.println("Internal Trace info GDI: " + ret_value.toString());
	}
        
        status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
            traceinfo_obj, X400_att.X400_S_MTA_NAME, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for internal trace info MTA Name " + status);
	} else {
	    System.out.println("Internal Trace info MTA Name: " + ret_value.toString());
	}

        /* Now the MTA supplied information */

        /* MTA SI Arrival time */
        status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
            traceinfo_obj, X400_att.X400_S_MTA_SI_TIME, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for internal trace info MTA SI Time " + status);
	} else {
	    System.out.println("Internal Trace info MTA SI Time: " + ret_value.toString());
	}

        /* MTA SI routing action can either be:
         * X400_MTA_SI_ROUTING_ACTION_RELAYED or
         * X400_MTA_SI_ROUTING_ACTION_REROUTED
         * */
        status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(
            traceinfo_obj,
            X400_att.X400_N_MTA_SI_ROUTING_ACTION);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for routing action" + status);
	} else {
            ret_int = traceinfo_obj.GetIntValue();
	    System.out.println("Got trace info routing action: ");
	
            if (ret_int == X400_att.X400_MTA_SI_ROUTING_ACTION_RELAYED) {
                System.out.println ("SI routing action "+ entry +"  :relayed");
            } else {
                System.out.println ("SI routing action "+ entry +"  :rerouted");
            }

        }

        /* Now a choice between attempted "mtaname" and "GDI" */

          status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(
            traceinfo_obj,
            X400_att.X400_N_MTA_SI_ATTEMPTED_ACTION);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for attempted action" + status);
	} else {
            ret_int = traceinfo_obj.GetIntValue();
	    System.out.println("Internal Trace info attempted action: ");
        
            if (ret_int == X400_att.X400_MTA_SI_RA_MTA) {
                status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
                    traceinfo_obj, X400_att.X400_S_MTA_SI_ATTEMPTED_MTA, ret_value);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("no string value for internal trace info Attempted MTA Name " + status);
                } else {
                    System.out.println("Internal Trace info Attempted MTA Name: " + ret_value.toString());
                }
                
            } else {
                status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
                    traceinfo_obj, X400_att.X400_S_MTA_SI_ATTEMPTED_DOMAIN, ret_value);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("no string value for internal trace info Attempted Domain " + status);
                } else {
                    System.out.println("Internal Trace info Attempted Domain : " + ret_value.toString());
                }
                
            }
        }
        /* followed by additional actions */

        /* Additional action: deferred time */
        status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
            traceinfo_obj, X400_att.X400_S_MTA_SI_DEFERRED_TIME, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for internal trace info X400_S_MTA_SI_DEFERRED_TIME " + status);
	} else {
	    System.out.println("Internal Trace info additional action deferred time: " + ret_value.toString());
	}

        /* Additional action: CEIT */
        
        status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(
            traceinfo_obj, X400_att.X400_S_MTA_SI_CEIT, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for internal trace info X400_S_MTA_SI_CEIT " + status);
	} else {
	    System.out.println("Internal Trace info additional action CEIT: " + ret_value.toString());
	}

        /* Additional action: other actions */
        
        status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(
            traceinfo_obj,
            X400_att.X400_N_MTA_SI_OTHER_ACTIONS);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for other action" + status);
	} else {
            ret_int = traceinfo_obj.GetIntValue();
	    System.out.println("Trace info other action: " + ret_int);
	}
        
        if ((ret_int & X400_att.X400_MTA_SI_OTHER_ACTION_REDIRECTED) > 0) {
            System.out.println ("SI Other Action "+ entry +"  : redirected");
        }
        
        if ((ret_int & X400_att.X400_MTA_SI_OTHER_ACTION_DLOPERATION) > 0) {
            System.out.println ("SI Other Action "+ entry +" : DL operation");
        }
      
        return X400_att.X400_E_NOERROR;
    }

     public static int get_mt_redihist(
        MTMessage mtmessage_obj,
        Recip recip_obj,
        int entry,
        RediHist redihist_obj
    )
    {
        int status, len;
        // Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();
        int ret_int = 0;

        if (mtmessage_obj == null) {
            status = com.isode.x400api.X400.x400_redihistget(
                recip_obj, entry, redihist_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("no redirection history found \n" + " " + status);
                return status;
            }
        } else {
            status = com.isode.x400mtapi.X400mt.x400_mt_redihistgetenv(mtmessage_obj,
                                                                       entry,
                                                                       redihist_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("no redirection history found \n" + " " + status);
                return status;
            }
        }
	System.out.println("Redirection History found " + " " + status);
        

        status = com.isode.x400api.X400.x400_redihistgetstrparam(
            redihist_obj, X400_att.X400_S_REDIRECTION_TIME,ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for redirection history redirection time " + status);
	} else {
	    System.out.println("redirection history redirection time: " + ret_value.toString());
	}
        
        status = com.isode.x400api.X400.x400_redihistgetstrparam(
            redihist_obj, X400_att.X400_S_OR_ADDRESS, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for redirection history redirection address " + status);
	} else {
	    System.out.println("redirection history redirection address: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_redihistgetstrparam(
            redihist_obj, X400_att.X400_S_DIRECTORY_NAME, ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for redirection history redirection DN " + status);
	} else {
	    System.out.println("redirection history redirection DN: " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_redihistgetintparam(
            redihist_obj, X400_att.X400_N_REDIRECTION_REASON);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for redirection history redirection reason" + status);
	} else {
            ret_int = redihist_obj.GetIntValue();
	    System.out.println("Got redirection history redirection reason: " + ret_int);
        }
              
        return X400_att.X400_E_NOERROR;
    }

    
    
     /**
     * Display the Trace info  if present,
     */
    public static int get_mt_traceinfo(
        MTMessage mtmessage_obj,
        int entry,
        Traceinfo traceinfo_obj,
        int type
    )
    {
        int status, len;
        // Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();
        int ret_int = 0;
        
        status = com.isode.x400mtapi.X400mt.x400_mt_traceinfoget(
	    mtmessage_obj,entry, traceinfo_obj,type);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no trace info found \n" + " " + status);
	    return status;
	} 
	System.out.println("Traceinfo found " + " " + status);
        
        status = com.isode.x400api.X400.x400_traceinfogetstrparam(
            traceinfo_obj,
            X400_att.X400_S_GLOBAL_DOMAIN_ID, 
            ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for trace info GDI " + status);
	} else {
	    System.out.println("Trace info GDI: " + ret_value.toString());
	}

        
        status = com.isode.x400api.X400.x400_traceinfogetstrparam(
            traceinfo_obj,
            X400_att.X400_S_DSI_ARRIVAL_TIME, 
            ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for trace info arrival time " + status);
	} else {
	    System.out.println("Trace info arrival time: " + ret_value.toString());
	}


        status = com.isode.x400api.X400.x400_traceinfogetstrparam(
            traceinfo_obj,
            X400_att.X400_S_DSI_ATTEMPTED_DOMAIN, 
            ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for trace info attempted domain " + status);
	} else {
	    System.out.println("Trace info attempted domain: " + ret_value.toString());
	}
        
        status = com.isode.x400api.X400.x400_traceinfogetstrparam(
            traceinfo_obj,
            X400_att.X400_S_DSI_AA_DEF_TIME, 
            ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for trace info deferred delivery time " + status);
	} else {
	    System.out.println("Trace info deferred delivery time : " + ret_value.toString());
	}
        
        status = com.isode.x400api.X400.x400_traceinfogetstrparam(
            traceinfo_obj,
            X400_att.X400_S_DSI_AA_CEIT, 
            ret_value);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for trace info CEIT" + status);
	} else {
	    System.out.println("Trace info CEIT : " + ret_value.toString());
	}

        status = com.isode.x400api.X400.x400_traceinfogetintparam(
            traceinfo_obj,
            X400_att.X400_N_DSI_ROUTING_ACTION);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for routing action" + status);
	} else {
            ret_int = traceinfo_obj.GetIntValue();
	    System.out.println("Trace info routing action: " + ret_int);
	}
        
        status = com.isode.x400api.X400.x400_traceinfogetintparam(
            traceinfo_obj,
            X400_att.X400_N_DSI_AA_REDIRECTED);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for redirection" + status);
	} else {
            ret_int = traceinfo_obj.GetIntValue();
	    System.out.println("Trace info redirected: " + ret_int);
	}
        
        status = com.isode.x400api.X400.x400_traceinfogetintparam(
            traceinfo_obj,
            X400_att.X400_N_DSI_AA_DLOPERATION);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no int value for dloperation" + status);
	} else {
            ret_int = traceinfo_obj.GetIntValue();
	    System.out.println("Trace info dloperation: " + ret_int);
	}
        
        return X400_att.X400_E_NOERROR;
    }
    
    /**
     * Display the Distribution List Expansion History if present,
     */
    public static int get_mt_dleh(MTMessage mtmessage_obj, int entry, DLExpHist dleh_obj)
    {
	int status, len;
	// Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();

    	status = com.isode.x400mtapi.X400mt.x400_mt_DLexphistget(
	    mtmessage_obj,entry, dleh_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no DLEH found \n" + " " + status);
	    return status;
	} 
	System.out.println("DLEH found " + " " + status);
	
	// got this DLEH element - print the values
	status = com.isode.x400api.X400.x400_DLgetstrparam(
	    dleh_obj,
	    X400_att.X400_S_OR_ADDRESS,
	    ret_value);
	
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for OR Address in DL  failed " + status);
	} else {
	    len =  ret_value.length();
	    System.out.println("DLExpansion List entry " +  "OR Address: " + ret_value.toString());
	}

	// got this DLEH element - print the values
	status = com.isode.x400api.X400.x400_DLgetstrparam(
	    dleh_obj,
	    X400_att.X400_S_DIRECTORY_NAME,
	    ret_value);
	
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for DN in DL  failed " + status);
	} else {
	    len =  ret_value.length();
	    System.out.println("DLExpansion List entry " +  "DN: " + ret_value.toString());
	}

        
        status = com.isode.x400api.X400.x400_DLgetstrparam(
	    dleh_obj,
	    X400_att.X400_S_DLEXP_TIME,
	    ret_value);
	
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for DL exp hist time in DL  failed " + status);
	} else {
	    len =  ret_value.length();
	    System.out.println("DLExpansion List exp hist time: "  + ret_value.toString());
	}

        
	return X400_att.X400_E_NOERROR;
    }

    /**
     * Display the Originator and DL expansion hist if present.
     */
    public static int get_mt_oranddl(MTMessage mtmessage_obj,
                                     int entry,
                                     ORandDL oranddl_obj)
    {
	int status;
        int len;
	// Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();

    	status = com.isode.x400mtapi.X400mt.x400_mt_oranddlget(
	    mtmessage_obj, entry, oranddl_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no ORandDL found \n" + " " + status);
	    return status;
	} 
	System.out.println("ORandDL found " + " " + status);
	
	// got this ORandDL element - print the values
	status = com.isode.x400api.X400.x400_oranddlgetstrparam(
	    oranddl_obj,
	    X400_att.X400_S_OR_ADDRESS,
	    ret_value);
	
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for OR Address in ORandDL " + status);
	} else {
	    System.out.println("ORandDL OR Address: " + ret_value.toString());
	}

	// got this ORandDL element - print the values
	status = com.isode.x400api.X400.x400_oranddlgetstrparam(
	    oranddl_obj,
	    X400_att.X400_S_DIRECTORY_NAME,
	    ret_value);
	
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for DN in ORandDL " + status);
	} else {
	    len =  ret_value.length();
	    System.out.println("ORandDL DN: " + ret_value.toString());
	}

        
        status = com.isode.x400api.X400.x400_oranddlgetstrparam(
	    oranddl_obj,
	    X400_att.X400_S_ORIG_OR_EXAP_TIME,
	    ret_value);
	
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for ORIG_OR_EXAP_TIME in ORandDL " + status);
	} else {
	    System.out.println("ORandDL expan time: "  + ret_value.toString());
	}

        
	return X400_att.X400_E_NOERROR;
    }


     /**
     * Fetch and display a MT bodypart 
     */
    public static int get_bp(MTMessage mtmessage_obj, BodyPart bodypart_obj, int att_num)
    {
	int status;
	int int_value;
	int len;
	int bp_type;
        byte[] binarydata = new byte[config.maxlen];

	System.out.println("----------------");
	System.out.println("Reading MT BodyPart " + att_num);

	// Initialise object for returning string values
	StringBuffer ret_value = new StringBuffer();

	status = com.isode.x400mtapi.X400mt.x400_mt_msggetbodypart(
	    mtmessage_obj, att_num, bodypart_obj);
	if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_MESSAGE_BODY) {
	    System.out.println("x400_mt_msggetbodypart failed " + status);
	    return status;
	}
        
	// what kind of body part ?
	status = com.isode.x400api.X400.x400_bodypartgetintparam(
	    bodypart_obj, X400_att.X400_N_BODY_TYPE);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_bodypartgetintparam failed " + status);
	    return status;
	}
	int_value = bodypart_obj.GetIntValue();
	System.out.println("Got BodyPart int value : bp type is "
                           + int_value);
        Message message_obj = new Message();
        switch (int_value) {
        case X400_att.X400_T_FWD_CONTENT:
            System.out.println("Got Forwarded Content");
            status = X400mtTestBodyPartRcvUtils.get_msgbp(mtmessage_obj,message_obj,att_num,int_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("get_msgbp failed " + status);
                return status;
            }
            break;
        case X400_att.X400_T_MESSAGE:            
            System.out.println("Got Forwarded Message");

            status = X400mtTestBodyPartRcvUtils.get_msgbp(mtmessage_obj,message_obj,att_num,int_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("get_msgbp failed " + status);
                return status;
            }
            
            break;
        case X400_att.X400_T_ADATP3:
            /*STANAG 4406 B1.1	ADatP3*/
            System.out.println("Got P772 ADatP3");
            
            status = com.isode.x400api.X400.x400_bodypartgetintparam(
                bodypart_obj, X400_att.X400_N_ADATP3_PARM);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetintparam failed " + status);
                
            } else {
                int_value = bodypart_obj.GetIntValue();
                System.out.println("Got ADatP3 param is:" + int_value);
            }
            
            status = com.isode.x400api.X400.x400_bodypartgetintparam(
                bodypart_obj, X400_att.X400_N_ADATP3_CHOICE);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetintparam failed " + status);
                
            } else {
                int_value = bodypart_obj.GetIntValue();
                System.out.println("Got ADatP3 Choice is:" + int_value);
            }
            
            status = com.isode.x400api.X400.x400_bodypartgetstrparam(
                bodypart_obj, X400_att.X400_S_ADATP3_DATA, ret_value,
                binarydata);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetstrparam failed "
                                   + status);
                return status;
            } 
            System.out.println("Got ADatP3 data" + ret_value.toString());
            break;
            
        case X400_att.X400_T_CORRECTIONS:
            /*STANAG 4406 B1.2	Corrections */
            System.out.println("Got P772 Correction");
            status = com.isode.x400api.X400.x400_bodypartgetintparam(
                bodypart_obj, X400_att.X400_N_CORREC_PARM);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetintparam failed " + status);
                
            } else {
                int_value = bodypart_obj.GetIntValue();
                System.out.println("Got correction param is:" + int_value);
            }

            status = com.isode.x400api.X400.x400_bodypartgetstrparam(
                bodypart_obj, X400_att.X400_S_CORREC_DATA, ret_value,
                binarydata);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetstrparam failed "
                                   + status);
                return status;
            } 
            System.out.println("Got Correction data" + ret_value.toString());
            break;

        case X400_att.X400_T_FWDENC:
            System.out.println("Got P772 Forwarded encrypted");

            status = X400mtTestBodyPartRcvUtils.get_msgbp(mtmessage_obj,message_obj,att_num,int_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("get_msgbp failed " + status);
                return status;
            }
            break;
            
        case X400_att.X400_T_MM:
            System.out.println("Got P772 T MM");
         
            status = X400mtTestBodyPartRcvUtils.get_msgbp(mtmessage_obj,message_obj,att_num,int_value);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("get_msgbp failed " + status);
                return status;
            }
            break;
            
        case X400_att.X400_T_ACP127DATA:
            System.out.println("Got P772 ACP127 Data");
            status = com.isode.x400api.X400.x400_bodypartgetintparam(
                bodypart_obj, X400_att.X400_N_ACP127DATA_PARM);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetintparam failed " + status);
                
            } else {
                int_value = bodypart_obj.GetIntValue();
                System.out.println("Got ACP127 data param is:" + int_value);
            }

            status = com.isode.x400api.X400.x400_bodypartgetstrparam(
                bodypart_obj, X400_att.X400_S_ACP127_DATA, ret_value,
                binarydata);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_bodypartgetstrparam failed "
                                   + status);
                return status;
            } 
            System.out.println("Got ACP127Data" + ret_value.toString());
            break;

            
        case X400_att.X400_T_BINARY:
        case X400_att.X400_T_FTBP:
                // read the data from the body part
                status = com.isode.x400api.X400.x400_bodypartgetstrparam(
                    bodypart_obj, X400_att.X400_S_BODY_DATA, ret_value,
                    binarydata);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("x400_bodypartgetstrparam failed "
                                       + status);
                    return status;
                }
                
		bp_type = bodypart_obj.GetBpType();
		len = bodypart_obj.GetBpLen();
		System.out.println("Binary Attachment (" 
                                   + int_value + "), len = " + len);
		for (int i = 0; i < len ; i++) 
		    System.out.print(binarydata[i] + " ");
		System.out.println();
		break;
	    default:
                 // read the data from the body part
                status = com.isode.x400api.X400.x400_bodypartgetstrparam(
                    bodypart_obj, X400_att.X400_S_BODY_DATA, ret_value,
                    binarydata);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("x400_bodypartgetstrparam failed "
                                       + status);
                    return status;
                }
		System.out.println("Got BodyPart string value(" 
                                   + ret_value.capacity() + 
                                   "): bp value is \n" +
                                   ret_value.toString());
		break;
            }
	return status;
    }

    



}

