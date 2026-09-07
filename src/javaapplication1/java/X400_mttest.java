/*  Copyright (c) 2008-2009, Isode Limited, London, England.
 *  All rights reserved.
 *                                                                       
 *  Acquisition and use of this software and related materials for any      
 *  purpose requires a written licence agreement from Isode Limited,
 *  or a written licence from an organisation licenced by Isode Limited
 *  to grant such a licence.
 *
 */




import com.isode.x400api.*;
import com.isode.x400mtapi.test.*;

/**
 * X400_test is the main class of the X400 example client
 *
 * Invocation:
LD_LIBRARY_PATH=/opt/isode/lib:$LD_LIBRARY_PATH java -classpath .:/opt/isode/lib/java/classes/isode-x400.jar -Xms20m X400_test 
 */
public class X400_mttest
{
    private static void sleep (int secs)
    {
        try {
            Thread.sleep( secs * 1000 );
        } catch (InterruptedException e){
            /* do nothing */
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
	System.out.println("X400_att.X400_API_VERSN = " 
	    + X400_att.X400_API_VERSN);

/*        System.out.println("=================================================");
        System.out.println("Transfer Probe");
        System.out.println("=================================================");
	X400mtTestSendUtils.send_msg(args,X400_att.X400_MSG_PROBE);
	System.out.println("Sent Probe. Now try to receive it");
        sleep(10);
        X400mtTestRcvUtils.rcv_msgs(args);
        
	System.out.println("=================================================");
*/
        System.out.println("Transfer Message");
	System.out.println("=================================================");
	X400mtTestSendUtils.send_msg(args,X400_att.X400_MSG_MESSAGE);
        System.out.println("Sent Message. Now try to receive it");
        sleep(10);
        X400mtTestRcvUtils.rcv_msgs(args);
              
/*	System.out.println("=================================================");
	System.out.println("Transfer Report");
	System.out.println("=================================================");
	X400mtTestSendUtils.send_msg(args,X400_att.X400_MSG_REPORT);
        System.out.println("Sent Report. Now try to receive it");
        sleep(10);
        X400mtTestRcvUtils.rcv_msgs(args);
*/              

    }
}

