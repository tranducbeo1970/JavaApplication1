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

// The X400.java file

import com.isode.x400api.*;
import com.isode.x400api.test.*;

/**
 * X400_test is the main class of the X400 example client
 *
 * Invocation:
LD_LIBRARY_PATH=/opt/isode/lib:$LD_LIBRARY_PATH java -classpath .:/opt/isode/lib/java/classes/isode-x400.jar -Xms20m X400_test 
 */
public class X400_mstest
{
    public static void main(String[] args)
    {
        //String path = System.getProperty("java.library.path");
        // System.out.println("java.library.path is " + path);

	System.out.println("X400_att.X400_API_VERSN = " 
	    + X400_att.X400_API_VERSN);
	// Submit a message 
	X400msTestSendUtils.send_msg(args);
	System.out.println("sent message");
	// Retrieve a message 
	// It could be a message (which we sent to ourselves)
	// or it could be a positive or negative report.
	// or it could be an IPN
	System.out.println("=================================================");
	System.out.println("Receive another message");
	System.out.println("=================================================");
	X400msTestRcvUtils.rcv_msg(args);
	System.out.println("=================================================");
	System.out.println("Receive another message");
	System.out.println("=================================================");
	X400msTestRcvUtils.rcv_msg(args);
	System.out.println("=================================================");
	System.out.println("Receive another message");
	System.out.println("=================================================");
	X400msTestRcvUtils.rcv_msg(args);

    }
}

