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
import com.isode.x400api.ORandDL;
import com.isode.x400mtapi.MTMessage;
import com.isode.x400api.InternalTraceinfo;
import com.isode.x400api.Traceinfo;
import com.isode.x400api.RediHist;



public class X400mtTestRepRcvUtils
{

    /**
     * Display a report envelope 
     */
    public static int do_rep_env(MTMessage mtmessage_obj)
    {
	int status;
	int paramtype;
	int len;
	String value;
        int entry;
        
	System.out.println("---------------");
	System.out.println("Report Envelope");

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();
        
        
        /*X.411 Report-destination-name 12.2.1.3.1.2*/
        status = get_rep_recips(mtmessage_obj, X400_att.X400_RECIP_ENVELOPE, 
                               "envelope originator: ");
	if ( status != X400_att.X400_E_NOERROR 
	    && status != X400_att.X400_E_NO_RECIP) {
	    System.out.println("get_mt_recips failed (env)" + status);
	 
	}
        
        paramtype = X400_att.X400_S_MESSAGE_IDENTIFIER;
        status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
            System.out.println("no string value for report id");
	} else {
	    System.out.println("Repor id: " + ret_value.toString());
	}
        

        /* X.411 Trace-information 12.2.1.1.1.3 */
        System.out.println("Get trace info");
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
        System.out.println("Get internal trace info");
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


        

        

        
        /* NYI: X.411 Message-security-label  8.2.1.1.1.30 

        X.411 Intended-recipient-name / redirection history 8.3.1.2.1.5
        X.411 Redirection-reason
        
        */
        System.out.println("get envelope redihist");
        RediHist redihist_obj = new RediHist();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
            {
                status = X400mtTestRcvUtils.get_mt_redihist(mtmessage_obj,
                                                            null,
                                                            entry,
                                                            redihist_obj);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("get_mt_redihist failed " + status);
                    break;
                }
                entry++;
            }
            

        
        /* X.411 Originator-and-DL-expansion-history 8.3.1.2.1.3 */
        ORandDL oranddl_obj = new ORandDL();
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_oranddl(mtmessage_obj,
                                                           entry,
                                                           oranddl_obj);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_oranddl failed " + status);
		    break;
		}
		entry++;
	    }
        
        
	return X400_att.X400_E_NOERROR;
    }

    /**
     * Display report content 
     */
    public static int do_rep_content(MTMessage mtmessage_obj)
    {
	int status;
	int paramtype;
	int recip_num = 1;
	int len = -1, maxlen = -1;
	String value; // string to contain value returned from API 
	int int_value; // int to contain value returned from API 

	System.out.println("-------------");
	System.out.println("Report Content");

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();
        
        System.out.println("Get rep subject id");
        /*X.411 Subject-identifier 12.2.1.3.1.3 */
	paramtype = X400_att.X400_S_SUBJECT_IDENTIFIER;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for Subject ID(" + paramtype + ") failed " + status );
	} else {
	    len = ret_value.length();
	    System.out.println("Subject Identifier: (" + len + ")" +  ret_value.toString());
	}
        
        /*
          X.411 Subject-intermediate-trace-information  12.2.1.3.1.4
        */
        System.out.println("Get subject intermediate trace info");
        Traceinfo traceinfo_obj = new Traceinfo();
        int entry;
        entry = 1;
        status = X400_att.X400_E_NOERROR;
        while(status == X400_att.X400_E_NOERROR)
	    {
		status = X400mtTestRcvUtils.get_mt_traceinfo(mtmessage_obj,
                                                             entry,
                                                             traceinfo_obj,
                                                             X400_att.X400_SUBJECT_TRACE_INFO);
		if (status != X400_att.X400_E_NOERROR) {
		    System.out.println("get_mt_traceinfo failed " + status);
		    break;
		}
		entry++;
	    }

        
        

        
        /* X.411 Original-encoded-information-types  8.2.1.1.1.33 */
        System.out.println("Get OEIT");
        paramtype = X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for OEIT(" + paramtype + ") failed " + status );
	} else {
	    len = ret_value.length();
	    System.out.println("OEIT: (" + len + ")" +  ret_value.toString());
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

        
        /* X.411 Content-identifier  8.2.1.1.1.35 */
        System.out.println("Get content identifier");
        paramtype = X400_att.X400_S_CONTENT_IDENTIFIER;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for contid(" + paramtype + ") failed " + status );
	} else {
	    len = ret_value.length();
	    System.out.println("Cont id: (" + len + ")" +  ret_value.toString());
	}
        
        /* X.411 Content-correlator  8.2.1.1.1.36 */
        System.out.println("Get content correlator");
        paramtype = X400_att.X400_S_CONTENT_CORRELATOR;
	status = com.isode.x400mtapi.X400mt.x400_mt_msggetstrparam(
	    mtmessage_obj, paramtype, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("no string value for cont corr(" + paramtype + ") failed " + status );
	} else {
	    len = ret_value.length();
	    System.out.println("Cont correlator: (" + len + ")" +  ret_value.toString());
	}


        /* Getting report recipient. */
        status = get_rep_recips(mtmessage_obj, X400_att.X400_RECIP_REPORT, 
                               "report recip: ");
	if ( status != X400_att.X400_E_NOERROR 
	    && status != X400_att.X400_E_NO_RECIP) {
	    System.out.println("get_mt_recips failed (env)" + status);
	 
	}
        

        
	return X400_att.X400_E_NOERROR;
    }

    /**
     * Display the report returned content
     * Note that there will only be returned 
     * content for negative reports.
     */
    public static int do_rep_retcontent(MTMessage mtmessage_obj)
    {

	System.out.println("-----------------------");
	System.out.println("Report Returned Content");

	/* the content can be retrieved as attachments or
	 * bodyparts. Use do_msg_content for the former */
	return (X400mtTestBodyPartRcvUtils.do_msg_content_as_bp(mtmessage_obj));
    }

    public static int get_rep_recips(MTMessage mtmessage_obj,
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
	// There should only be one recip of course
	for ( recip_num = 1; ; recip_num++ ) {
            System.out.println("Get rep recip");
	    // get the recip
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipget(mtmessage_obj, 
		type, recip_num, recip_obj);
	    if (status == X400_att.X400_E_NO_RECIP) {
		System.out.println("no more recips ...");
		break;
	    }
	    else if (status != X400_att.X400_E_NOERROR) {
		System.out.println("x400_mt_recipget failed " + status);
		break;
	    }

	    System.out.println("-------------- Recipient " + recip_num + "--------------");
            System.out.println("Get rep recip or address");
	    // display recip properties
	    paramtype = X400_att.X400_S_OR_ADDRESS;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for originator address(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("Subject Recipient " + recip_num + ": (" + len + ")" +  ret_value.toString());
	    }

            

            // display recip properties
            /* X.411 Originally-specified-recipient-number 12.2.1.1.1.5 */
            System.out.println("Get rep recip MTS type");
	    paramtype = X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for orig recip number(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("orig recip number " + int_value);
	    }

            // display recip properties
            /* Per recipient indicators: responsibility */
            System.out.println("Get per recip responsibility");
	    paramtype = X400_att.X400_N_RESPONSIBILITY;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for responsibility(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("responsibility " + int_value);
	    }
            
            // display recip properties
            /* Per recipient indicators: report request */
            System.out.println("Get rep recip MTS type");
	    paramtype = X400_att.X400_N_REPORT_REQUEST;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for recip rep req(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("Recip report request: " + int_value);
	    }
            

            /* Per recipient indicators: report request */
            System.out.println("Get rep recip MTS type");
	    paramtype = X400_att.X400_N_MTA_REPORT_REQUEST;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for recip MTA rep req(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("Recip MTA report request: " + int_value);
	    }

            
            /* X.411 Arrival-time  12.2.1.3.1.5 */
            System.out.println("Get report arrival time");
	    paramtype = X400_att.X400_S_ARRIVAL_TIME;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for report arrival Time(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("Report arrival Time:" + "(" + len + ")" +  ret_value.toString());
	    }
            
            /* X.411 Converted-encoded-information-types 8.3.1.2.1.5 */
            System.out.println("Get rep recip CEIT");
            paramtype = X400_att.X400_S_CONVERTED_ENCODED_INFORMATION_TYPES;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for CEIT (" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("CEIT:" + "(" + len + ")" +  ret_value.toString());
	    }
            
	    // display recip properties
	    // Delivery time should be used to determine whether this
	    // is a positive or negative report
            System.out.println("Get message delivery time");
	    paramtype = X400_att.X400_S_MESSAGE_DELIVERY_TIME;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for Subject Message Delivery Time(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("Subject Message Delivery Time:" + "(" + len + ")" +  ret_value.toString());
	    }

	    // display recip properties
            /* X.411 Type-of-MTS-user  8.3.1.2.1.10 */
            System.out.println("Get rep recip MTS type");
	    paramtype = X400_att.X400_N_TYPE_OF_USER;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for Type of MTS user(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("Type of MTS user " + int_value);
	    }

	    // display recip properties
            System.out.println("Get rep recip supp info");
            /* X.411 Supplementary-information  8.3.1.2.1.6 */
	    paramtype = X400_att.X400_S_SUPPLEMENTARY_INFO;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetstrparam(
		recip_obj, paramtype, ret_value);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no string value for Supplementary info(" + paramtype + ") failed " + status);
	    } else {
		len =  ret_value.length();
		System.out.println("Supplementary info" + "(" + len + ")" +  ret_value.toString());
	    }

	    // display recip properties
            System.out.println("Get rep recip Non delivery reason");
            /* X.411 Non-delivery-reason-code 8.3.1.2.1.10 */
	    paramtype = X400_att.X400_N_NON_DELIVERY_REASON;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for Non-delivery Reason(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("Non-delivery Reason  " + int_value);
	    }

	    // display recip properties
            System.out.println("Get rep recip Non delivery diagnostic");
            /* X.411 Non-delivery-diagnostic-code 8.3.1.2.1.11 */
	    paramtype = X400_att.X400_N_NON_DELIVERY_DIAGNOSTIC;
	    status = com.isode.x400mtapi.X400mt.x400_mt_recipgetintparam(
		recip_obj, paramtype);
	    if (status != X400_att.X400_E_NOERROR) {
		System.out.println("no int value for Non-delivery diagnostic(" + paramtype + ") failed " + status);
	    } else {
		int_value = recip_obj.GetIntValue();
		System.out.println("Non-delivery diagnostic  " + int_value);
	    }


            /* Get per recipient redirection history */
            RediHist redihist_obj = new RediHist();
            int entry = 1;
            status = X400_att.X400_E_NOERROR;
            System.out.println("get redihist for this recipient");
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
            
          

            
	}
	return X400_att.X400_E_NOERROR;
    }

    
}

