package javaapplication1;

/* 
 * Copyright (c) 2008-2012, Isode Limited, London, England.
 * All rights reserved.
 * 
 * Acquisition and use of this software and related materials for any
 * purpose requires a written licence agreement from Isode Limited,
 * or a written licence from an organisation licenced by Isode Limited
 * to grant such a licence.
 */

import java.util.ArrayList;

import com.isode.rbac.swig.JRSecurityLabel;

import com.isode.x400.highlevel.Bodypart;
import com.isode.x400.highlevel.BodypartFTBP;
import com.isode.x400.highlevel.BodypartForwardedMessage;
import com.isode.x400.highlevel.BodypartGeneralText;
import com.isode.x400.highlevel.BodypartIA5Text;
import com.isode.x400.highlevel.ListResult;
import com.isode.x400.highlevel.P3BindSession;
import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400.highlevel.ReceiveMsg;
import com.isode.x400.highlevel.Bodypart.Bodypart_Type;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400api.AMHS_att;
import com.isode.x400api.MSListResult;
import com.isode.x400api.Message;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test program that connects to either an MTA's P3 channel or to a P7 Message
 * Store, checks if a message is ready to be read, and if it is, reads it and
 * displays some values on the standard output.
 *
 */
public class ReceiveX400Mail_OK {

    // Mandatory P7 configuration settings. You must change these values for this program to work with P7
    private static final String p7_message_store_presentation_address = "\"3001\"/URI+0000+URL+itot://192.168.22.199:3001";
    private static final String p7_user_or_address = "/CN=VVTSMHSA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    //private static String p7_user_or_address = "/CN=VVTSAMHA/OU=VVTS/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static final String p7_user_password = "amhs";

    // Mandatory P3 configuration settings. You must change these values for this program to work with P3
    //private static final String p3_channel_presentation_address = "\"593\"/URI+0000+URL+itot://10.10.1.1:102";
    
    //private static String p3_channel_presentation_address = "\"593\"/Internet=10.10.1.1+102";
   
    
//< DIA CHI P3
    private static String p3_channel_presentation_address = "\"593\"/URI+0000+URL+itot://10.64.1.110";
    private static String p3_user_or_address = "/CN=VVTSNBZQZX/OU=VVNB/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static String p3_user_password = "amhs";
// DIA CHI P3 >
 
    
    // Optional configuration values
    private static boolean use_p3 = false;
    private static boolean only_show_mailbox_summary = false;
    private static boolean delete_p7_messages_after_reading_them = false;
    private static boolean save_bodyparts_in_files = false;
    private static P3BindSession connection;
    
    private boolean isExtend = false;

    public static void main(String[] args) {

        try {

            if (use_p3) {

                System.out.println("Connecting to the MTA P3 Channel");

                // Create a new P3 bind session object using the values necessary for the bind
                connection = new P3BindSession(p3_channel_presentation_address, p3_user_or_address, p3_user_password);

                // Bind to the P3 Channel
                connection.bind();

                // Since the bind was successful, try to read a message from the P3 channel
                // Now wait 10 seconds for new messages to be available
                do {
                    int wait = 1;
                    System.out.println("\n>>> Waiting " + wait + " seconds for a new message");
                    int status = connection.waitForNewMessages(wait);
                    if (status == X400_att.X400_E_NOERROR) {
                        System.out.println("\nFound a new message, reading it");
                        ReceiveMsg rm = new ReceiveMsg(connection);
                        //java.util.ArrayList<Recipient> ll = rm.getBccRecipients();
                        //if (ll.size() > 0) {
                        //    System.out.println("-------------------");
                        //    System.out.println(ll.get(0).toString());
                        //    System.out.println("-------------------");
                        //}
                        if (rm.getStringParam(X400_att.X400_S_PRECEDENCE_POLICY_ID).equals("1.3.27.8.0.0")) {          // Extended
                            
                        }
                         
                        showMessage(rm);
                    } else {
                        switch (status) {
//                            case X400_att.X400_E_TIMED_OUT:
//                                System.out.println("Timed out - no new messages");
//                                break;
//                            case X400_att.X400_E_NO_MESSAGE:
//                                System.out.println("No more messages");
//                                break;
                            default:
                                System.out.println("Failed to set the wait new - " + status);
                                break;
                        }
                    //    break;
                    }
                } while (true);

                // Unbind cleanly from the Message Store
                //connection.unbind();

            } else {

                System.out.println("Connecting to the P7 Message Store");

                // Create a new P7 bind session object with the session values necessary for the bind
                P7BindSession connection = new P7BindSession(p7_message_store_presentation_address,
                        p7_user_or_address, p7_user_password);

                // It is possible to request a the number of unread messages at the time of bind (SUMMARIZE)
                // If you are interested in this result, set SetSummarizeOnBind(true), otherwise
                // don't set it, as the operation isn't needed.
                // connection.getSession().SetSummarizeOnBind(true);
                // Bind to the P7 Message Store
                connection.bind();

                // We can now either request a SUMMARIZE operation to the Message Store and then just 
                // print a summary for each message, or just read all available messages, one after 
                // the other, and print all the information for each message
                if (only_show_mailbox_summary) {

                    listTheMailbox(connection);

                } else {

                    /*
                    // Since the bind was successful, try to read the messages from the Message Store
                    int num_of_msgs = connection.getRefreshNumberOfMessages();

                    // If there are any existing messages, display them.
                    if (num_of_msgs == 0) {
                        System.out.println("\nThere are no existing messages ready to display");
                    } else {
                        System.out.println("\nThere are " + num_of_msgs + " messages");

                        for (int i = 1; i <= num_of_msgs; i++) {
                            System.out.println("\nShowing message number " + i);
                            ReceiveMsg rm = connection.receiveNextAvailableMessage();

                            // Display information about the message in stdout 
                            showMessage(rm);

                            // Optionally delete the message from the mailbox after reading it
                            if (delete_p7_messages_after_reading_them) {
                                connection.deleteMessage(rm);
                            }
                        }

                        System.out.println("\nFinished displaying existing messages");
                    }
                    */
                    
                        while (true) {
                            System.out.println("\nShowing message number ");
                            ReceiveMsg rm = connection.receiveNextAvailableMessage();
                            if(rm == null) {
                                break;
                            }
                            // Display information about the message in stdout 
                            showMessage(rm);

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ReceiveX400Mail_OK.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    
                    // Now wait 10 seconds for new messages to be available  
                    int wait = 10;
                    System.out.println("\n>>> Waiting " + wait + " seconds for a new message");
                    int status = connection.waitForNewMessages(wait);

                    switch (status) {
                        case X400_att.X400_E_NOERROR:
                            System.out.println("\nFound a new message, reading it");
                            ReceiveMsg rm = new ReceiveMsg(connection);
                            showMessage(rm);
                            break;
                        case X400_att.X400_E_TIMED_OUT:
                            System.out.println("Timed out - no new messages");
                            break;
                        case X400_att.X400_E_NO_MESSAGE:
                            System.out.println("No more messages");
                            break;
                        default:
                            System.out.println("Failed to set the wait new - " + status);
                    }
                }

                // Unbind cleanly from the Message Store
                connection.unbind();

            }

        } catch (X400APIException e) {

            // Catch all exceptions, report it and exit the program
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Show the message that was received, by printing some of the attributes in
     * the stdout
     *
     * @param rm
     */
    public static void showMessage(ReceiveMsg rm) throws X400APIException {
        int status = 0;

        if (status == X400_att.X400_E_NOERROR) {
            
            int ext = rm.getIntParam(AMHS_att.ATS_N_EXTENDED);
            if(rm.getIntParam(AMHS_att.ATS_N_EXTENDED) == 1) {
            System.out.println("-------------The message is EXTENDED FORMAT--------------");
        }
        else  {
            System.out.println("-------------The message is BASIC FORMAT------------------");
        }

            System.out.println("The type is '" + rm.getTypeAsString() + "'");

            if (rm.GetType() == X400_att.X400_MSG_MESSAGE) {
                System.out.println("The message is from '" + rm.getFrom() + "'");
                System.out.println("The message primary recipients are '" + rm.getToRecipients() + "'");
                if (rm.getCcRecipients() != null) {
                    System.out.println("The message copy recipients are '" + rm.getCcRecipients() + "'");
                }
                if (rm.getBccRecipients() != null) {
                    System.out.println("The message blind copy recipients are '" + rm.getBccRecipients() + "'");
                }
                System.out.println("The Subject is '" + rm.getSubject() + "'");
                System.out.println("The Date is '" + rm.getMessageSubmissionTimeAsDate().toString() + "'");
                System.out.println("The Time is '" + rm.getMessageSubmissionTime() + "'");
                System.out.println("The Priority is '" + rm.getX400Priority().toString() + "'");

                // X.411 Security Labels are optional 
                JRSecurityLabel securityLabel = rm.getJRSecurityLabel();
                if (securityLabel != null) {
                    System.out.println("The Security Label is '" + securityLabel.getXML() + "'");
                }

                // Show information about the bodyparts, and their contents when possible
                int numOfBodyparts = rm.getNumberOfBodyparts();
                switch (numOfBodyparts) {
                    case 0:
                        System.out.println("There are no bodyparts");
                        break;
                    case 1:
                        System.out.println("There is one bodypart");
                        break;
                    default:
                        System.out.println("There are " + numOfBodyparts + " bodyparts");
                        break;
                }

                for (int i = 1; i <= numOfBodyparts; i++) {
                    Bodypart bp = rm.getBodypart(i);
                    System.out.println("Bodypart number " + i + " is of type " + bp.getTypeAsString());
                    if (bp instanceof BodypartIA5Text) {
                        BodypartIA5Text bpt = (BodypartIA5Text) bp;
                        System.out.println(bpt.getTextContent());
                        if (save_bodyparts_in_files) {
                            bp.saveBP("bodyparts/ia5.bp");
                        }
                    } else if (bp instanceof BodypartGeneralText) {
                        BodypartGeneralText gtbp = (BodypartGeneralText) bp;
                        System.out.println("Bodypart size = " + gtbp.getSize() + "\n" + gtbp.getStringRepresentation());
                        if (save_bodyparts_in_files) {
                            gtbp.saveBP("D:\\gt.bp");
                        }
                    } else if (bp instanceof BodypartFTBP) {
                        BodypartFTBP ftbp = (BodypartFTBP) bp;
                        System.out.println(ftbp.getStringRepresentation());
                        if (ftbp.getCreationDate() != null) {
                            System.out.println("The creation date of the file is " + ftbp.getCreationDate());
                        }
                        if (save_bodyparts_in_files) {
                            ftbp.saveFTBPInDir("bodyparts");
                        }
                    } else if (bp.getType() == Bodypart_Type.BODYPART_BINARY) {
                        System.out.println("Bodypart size = " + bp.getSize());
                        if (save_bodyparts_in_files) {
                            bp.saveBP("d:\\duc.bin");
                        }
                    } else if (bp.getType() == Bodypart_Type.BODYPART_MESSAGE) {
                        Message fwdMsg = new Message();
                        com.isode.x400api.X400ms.x400_ms_msggetmessagebody(rm, i, fwdMsg);
                        BodypartForwardedMessage fwd = new BodypartForwardedMessage(bp.getBodypartObject());
                        fwd.setFwdMessage(fwdMsg);
                        System.out.println(fwd.getStringRepresentation());
                    }
                }
            } else if (rm.GetType() == X400_att.X400_MSG_REPORT) {
                System.out.println("The DR content is:\n" + rm.getReportContentAsText());
            }

            // finish with the message
            rm.finishWithMessage(0, 0);
        } else {
            System.out.println("Problems reading the message: status = " + status);
        }
    }

    /**
     * List the messages in the P7 Message Store mailbox associated with the
     * bind user
     *
     * There are several ways in which this listing can be modified, the values
     * are described in the code
     *
     * @param connection
     * @throws X400APIException
     */
    public static void listTheMailbox(P7BindSession connection) throws X400APIException {

        // List messages older than the value of "since", or all messages if "since" is null
        // The "since" string is formatted in UTC time, for example, 02 October 2008 at 14:15:16 hours  
        // is 081002141516
        String since = null;

        // The P7 Message Store can store two kinds of messages: submitted messages are the ones
        // that the user submitted to the Message Store (for submission into the MTA)
        // Stored messages are selected by setting entry_class to MS_ENTRY_CLASS_STORED_MESSAGES.
        // Submitted messages are selected by setting entry_class to MS_ENTRY_CLASS_SUBMITTED_MESSAGES.
        // It is not possible to specify a value that return both of them, you have to choose
        P7BindSession.Entry_Class entry_class = P7BindSession.Entry_Class.MS_ENTRY_CLASS_STORED_MESSAGES;

        // Messages in the Message Store can be in one of three status:
        // 1 = new -
        // 2 = listed - 
        // 3 = fetched 
        boolean only_new_messages = false;

        ArrayList<ListResult> list_array = connection.listMailbox(since, entry_class, only_new_messages);

        for (int i = 1; i < list_array.size(); i++) {
            ListResult lm = list_array.get(i);

            if (lm.getType() == X400Msg.X400_Message_Type.MESSAGE_TYPE_MESSAGE) {
                System.out.println("\nEntry number " + i + " is a message (IPM)");
                System.out.println("The message is from '" + lm.getSender() + "'");
                System.out.println("The Subject is '" + lm.getSubject() + "'");
                System.out.println("The Submission time is '" + lm.getSubmissionTime() + "'");
                System.out.println("The Priority is '" + lm.getPriority().toString() + "'");
                System.out.println("The Content length is " + lm.getContLength());
                System.out.println("The Message Identifier is " + lm.getMsgID());
            } else if (lm.getType() == X400Msg.X400_Message_Type.MESSAGE_TYPE_REPORT) {
                System.out.println("\nEntry number " + i + " is a delivery report (DR)");
                System.out.println("The original message Subject Identifier is '" + lm.getSubjectID() + "'");

            } else if (lm.getType() == X400Msg.X400_Message_Type.MESSAGE_TYPE_SUBMITTED) {
                System.out.println("\nEntry number " + i + " is a submitted message");
                System.out.println("The Subject is '" + lm.getSubject() + "'");
            }

        }
    }

    public void listMessages(String since, String until) throws X400APIException {

        final MSListResult messageList = new MSListResult();
        // final String keyword = messageClass == Connection.MSG_TYPE_STORED_MESSAGE ? Common.INBOX_CHECK_POINT : Common.SENT_CHECK_POINT;
        int entryClass = 0;//Connection.MSG_TYPE_STORED_MESSAGE;
//connection.listMessages(begin, end, messageClass);
        SimpleDateFormat DAY_BEGIN_FORMAT = new SimpleDateFormat("yyMMdd000000'Z'");

        String from = DAY_BEGIN_FORMAT.format(new Date());
        String to = DAY_BEGIN_FORMAT.format(new Date());
        final int code = X400ms.x400_ms_listexauxpribefore(connection, since, until, entryClass, -1, X400_att.X400_PRIORITY_ANY, messageList); // ex(session, since, entryClass, messageList);

        if (code != X400_att.X400_E_NOERROR) {

//            disconnect();
            throw new X400APIException(String.format("Fetching message fail (code %s)", code));

        }

//        List<Object> indexes = parse(messageList, entryClass);
//
//        X400ms.x400_ms_listfree(messageList);
//
//        return indexes;
    }

//    private List<Object> parse(MSListResult msListResult, int entryClass) {
//
//        final List<Object> results = new ArrayList<>();
//
//        int code = 0;
//
//        int i;
//
//        for (i = 1;; i++) {
//
//            code = X400ms.x400_ms_listgetintparam(msListResult, X400_att.X400_N_MS_SEQUENCE_NUMBER, i);
//
//            if (code == X400_att.X400_E_NO_MORE_RESULTS) {
//
//                break;
//
//            }
//
//            if (code == X400_att.X400_E_NO_VALUE) {
//
//                continue;
//
//            }
//
//            if (code != X400_att.X400_E_NOERROR) {
//
//                break;
//
//            }
//
//            Index item = new Index(msListResult, i);
//
//            item.setClazz(entryClass);
//
//            results.add(item);
//
//        }
//
//        // X400ms.x400_ms_listfree(msListResult);
//        return results;
//
//    }
//
//    public void Index(MSListResult msListResult, int index) {
//
//        this.submissionTime = getStrParam(msListResult, X400_att.X400_S_MESSAGE_SUBMISSION_TIME, index);
//
//        this.status = getIntParam(msListResult, X400_att.X400_N_MS_ENTRY_STATUS, index);
//
//        this.type = getStrParam(msListResult, X400_att.X400_S_OBJECTTYPE, index);
//
//        this.priority = getIntParam(msListResult, X400_att.X400_N_PRIORITY, index);
//
//        this.subject = getStrParam(msListResult, X400_att.X400_S_SUBJECT, index);
//
//        this.sequence = getIntParam(msListResult, X400_att.X400_N_MS_SEQUENCE_NUMBER, index);
//
//        this.origin = getStrParam(msListResult, X400_att.X400_S_OR_ADDRESS, index);
//
//        this.messageId = getStrParam(msListResult, X400_att.X400_S_MESSAGE_IDENTIFIER, index);
//
//    }
}
