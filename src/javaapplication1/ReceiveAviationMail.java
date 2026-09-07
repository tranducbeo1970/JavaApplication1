/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

/* 
 * Copyright (c) 2008-2010, Isode Limited, London, England.
 * All rights reserved.
 * 
 * Acquisition and use of this software and related materials for any
 * purpose requires a written licence agreement from Isode Limited,
 * or a written licence from an organisation licenced by Isode Limited
 * to grant such a licence.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.isode.rbac.swig.JRSecurityLabel;

import com.isode.x400.highlevel.Bodypart;
import com.isode.x400.highlevel.BodypartFTBP;
import com.isode.x400.highlevel.BodypartForwardedMessage;
import com.isode.x400.highlevel.BodypartGeneralText;
import com.isode.x400.highlevel.BodypartIA5Text;
import com.isode.x400.highlevel.FileException;
import com.isode.x400.highlevel.ListResult;
import com.isode.x400.highlevel.P7BindSession;
import com.isode.x400.highlevel.X400Msg;
import com.isode.x400.highlevel.ReceiveMsg;
import com.isode.x400.highlevel.Bodypart.Bodypart_Type;
import com.isode.x400.highlevel.X400APIException;
import com.isode.x400api.AMHS_att;
import com.isode.x400api.Message;
import com.isode.x400api.X400_att;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test program that connects to an P7 Message Store, checks if a message is
 * ready to be read, and if it is, reads it and displays some values on the
 * standard output.
 *
 */
public class ReceiveAviationMail {

    // Mandatory P7 configuration settings. You must change these values for this program to work with P7
    //private static String p7_message_store_presentation_address = "\"3001\"/Internet=Seventeen+3001";
    private static String p7_message_store_presentation_address = "\"3001\"/Internet=192.168.22.186+3001";
    //private static String p7_user_or_address = "/OU=EGKKZPZX/O=AFTN/PRMD=EG/ADMD=ICAO/C=XX/";
    private static String p7_user_or_address = "/CN=VVVVYMYX/OU=VVVV/O=VVTS/PRMD=VIETNAM/ADMD=ICAO/C=XX/";
    private static String p7_user_password = "amhs";

    // Optional configuration values
    private static boolean delete_p7_messages_after_reading_them = false;
    private static boolean save_bodyparts_in_files = false;
    
    private static P7BindSession connection;
    
    private static int seq = 0;

    public static void main(String[] args) {

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
        
        try {
            
            System.out.println("Connecting to the P7 Message Store");

            // Create a new P7 bind session object with the session values necessary for the bind
            connection = new P7BindSession(p7_message_store_presentation_address,p7_user_or_address, p7_user_password);

            // It is possible to request a the number of unread messages at the time of bind (SUMMARIZE)
            // If you are interested in this result, set SetSummarizeOnBind(true), otherwise
            // don't set it, as the operation isn't needed.
            // connection.getSession().SetSummarizeOnBind(true);
            // Bind to the P7 Message Store
            connection.bind();

            // turn on all logging for this session
            int status = com.isode.x400api.X400ms.x400_ms_setstrdefault(connection.getSession(),
                    X400_att.X400_S_LOG_CONFIGURATION_FILE, "D:\\debug-x400.xml", -1);
            if (status != X400_att.X400_E_NOERROR) {
                throw new X400APIException("Failed to set the log", status);
            } else {
                System.out.println("Log set OK");
            }
            
            byte[] data = new byte[10];
            
         //   ReceiveMsg rm1 = new ReceiveMsg(connection, -1, data);
            
            ArrayList<ListResult> list_array = connection.listMailbox(since, entry_class, only_new_messages);

            // Since the bind was successful, try to read the messages from the Message Store
            int num_of_msgs = connection.getRefreshNumberOfMessages();

            
//            for (int i = 0; i <= num_of_msgs; i++) {
//                ReceiveMsg rm = new ReceiveMsg(connection,-1);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(ReceiveAviationMail.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            
            // If there are any existing messages, display them.
            if (num_of_msgs == 0) {
                System.out.println("\nThere are no existing messages ready to display");
            } else {
                System.out.println("\nThere are " + num_of_msgs + " messages");
                
                for (int i = 1; i <= num_of_msgs; i++) {
                    System.out.println("\nShowing message number " + i);
                    //ReceiveMsg rm = connection.receiveNextAvailableMessage();

                    //ReceiveMsg rm = new ReceiveMsg(connection, seq);
                    // Display information about the message in stdout 
                    //ReceivedMessage1 receivedMessage = connection.get(seq,extract_attachment_mode);      
                    seq = 0;
                    //  Instant start = Instant.now();
                    ListResult lm = list_array.get(i);
                    int sss = lm.getSequenceNumber();
                    System.out.println("Get message seq num = " + Integer.toString(sss));
                    
                    ReceiveMsg rm = new ReceiveMsg(connection,sss);

                    //Instant end = Instant.now();
                    //long micros = Duration.between(start, end).toNanos() / 1_000;
                    //System.out.println("Elapsed: " + micros + " µs");
                    //ReceiveMsg rm = connection.get(seq,0);        
                    showMessage(rm, i);
                                
                    // Optionally delete the message from the mailbox after reading it
                    if (delete_p7_messages_after_reading_them) {
                        connection.deleteMessage(rm);
                    }
                  
                    rm = null;
                }
                
                System.out.println("\nFinished displaying existing messages");
            }

            // Now wait 10 seconds for new messages to be available  
            while (true) {
                int wait = 100000;
                System.out.println("\n>>> Waiting " + wait + " seconds for a new message");
                status = connection.waitForNewMessages(wait);

                switch (status) {
                    case X400_att.X400_E_NOERROR:
                        System.out.println("\nFound a new message, reading it");
                        ReceiveMsg rm = new ReceiveMsg(connection);
                        showMessage(rm, num_of_msgs + 1);
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
           // connection.unbind();
            
        } catch (X400APIException e) {

            // Catch all exceptions, report it and exit the program
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Show the message that was received, by printing some of the attributes in
     * the stdout If the sequence number if provided, and it's not zero, save
     * the content of the message in the file /tmp/msg-<seq_number>
     *
     * @param rm
     */
    public static void showMessage(ReceiveMsg rm, int seq) throws X400APIException {
        // Here to simulate what would happen when things go wrong and the message can't be read
        // Presume that things work.
        int status = X400_att.X400_E_NOERROR;
        
        if (status == X400_att.X400_E_NOERROR) {
            
            System.out.println("The type is '" + rm.getTypeAsString() + "'");
            
            if (rm.GetType() == X400_att.X400_MSG_MESSAGE || rm.GetType() == 0) {
                
                System.out.println("The message is from '" + rm.getFrom() + "'");
                System.out.println("The message primary recipients are '" + rm.getToRecipients() + "'");
                if (rm.getCcRecipients() != null) {
                    System.out.println("The message copy recipients are '" + rm.getCcRecipients() + "'");
                }
                System.out.println("The Subject is '" + rm.getSubject() + "'");
                System.out.println("The Date is '" + rm.getMessageSubmissionTimeAsDate().toString() + "'");
                System.out.println("The Authorization Time is '" + rm.getStringParam(X400_att.X400_S_AUTHORIZATION_TIME) + "'");
                System.out.println("The ATS Filing Time is '" + rm.getStringParam(AMHS_att.ATS_S_FILING_TIME) + "'");
                System.out.println("The ATS Priority is '" + rm.getStringParam(AMHS_att.ATS_S_PRIORITY_INDICATOR) + "'");
                System.out.println("The ATS OHI is '" + rm.getStringParam(AMHS_att.ATS_S_OPTIONAL_HEADING_INFO) + "'");
                System.out.println("Extended is '" + rm.getIntParam(AMHS_att.ATS_N_EXTENDED) + "'");

                // X.411 Security Labels are optional 
                JRSecurityLabel securityLabel = rm.getJRSecurityLabel();
                if (securityLabel != null) {
                    System.out.println("The Security Label is '" + securityLabel.getXML() + "'");
                }

                // Show information about the bodyparts, and their contents when possible
                int numOfBodyparts = rm.getNumberOfBodyparts();
                if (numOfBodyparts == 0) {
                    System.out.println("There are no bodyparts");
                } else if (numOfBodyparts == 1) {
                    System.out.println("There is one bodypart");
                } else {
                    System.out.println("There are " + numOfBodyparts + " bodyparts");
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
                            gtbp.saveBP("bodyparts/gt.bp");
                        }
                    } else if (bp instanceof BodypartFTBP) {
                        BodypartFTBP ftbp = (BodypartFTBP) bp;
                        System.out.println(ftbp.getStringRepresentation());
                        //ftbp.get
                        if (ftbp.getCreationDate() != null) {
                            System.out.println("The creation date of the file is " + ftbp.getCreationDate());
                        }
                        if (save_bodyparts_in_files) {
                            ftbp.saveFTBPInDir("bodyparts");
                        }
                    } else if (bp.getType() == Bodypart_Type.BODYPART_BINARY) {                        
                        System.out.println("Bodypart size = " + bp.getSize());
                        if (save_bodyparts_in_files) {
                            bp.saveBP("bodyparts/binary");
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

    /**
     * Read the X.400 message content from a file, and return a ReceiveMsg
     *
     * @param filename : the file where to read the message
     */    
    public static ReceiveMsg readMessageContent(P7BindSession connection, String filename) throws X400APIException {
        
        byte[] messageContent;
        File f = new File(filename);
        
        int fileLength = (int) f.length();
        messageContent = new byte[fileLength];
        
        try {
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            
            try {
                in.read(messageContent, 0, fileLength);
                in.close();
            } catch (Exception e) {
                throw new FileException("readMessageContent failed, couldn't read file "
                        + filename + "\n" + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            throw new FileException("readMessageContent failed, couldn't read file "
                    + filename + "\n" + e.getLocalizedMessage());
        }
        
        ReceiveMsg rm = new ReceiveMsg(connection, X400_att.X400_MSG_MESSAGE, messageContent);
        
        return rm;
    }

    /**
     * Save the X.400 message provided to a file.
     *
     * @param filename : the file where to save the message
     * @param rm : the X.400 message to save
     */    
    public static void saveMessageContent(String filename, ReceiveMsg rm) throws X400APIException {
        int status = 0;
        int bp_len = 0;
        byte[] bp_data = new byte[32000];

        // Read the contents of the message into the byte array
        status = com.isode.x400api.X400ms.x400_ms_msggetbyteparam(rm, X400_att.X400_S_CONTENT_STRING, bp_data);
        bp_len = rm.GetAttLen();
        
        if (status == X400_att.X400_E_NO_VALUE) {
            // If no value, return
            System.out.println("The content of the message was empty");
            return;            
        } else if (status == X400_att.X400_E_NOERROR) {
            // If no error, continue
        } else if (status == X400_att.X400_E_NOSPACE) {
            // The problem was lack of space, try again, with sufficiently large array
            bp_data = new byte[rm.GetAttLen()];
            status = com.isode.x400api.X400ms.x400_ms_msggetbyteparam(rm, X400_att.X400_S_CONTENT_STRING, bp_data);
            if (status != X400_att.X400_E_NOERROR) {
                throw new X400APIException("saveContent failed", status);
            }
        } else {
            throw new X400APIException("saveContent failed", status);
        }

        // And write it to the file
        try {
            FileOutputStream fstream = new FileOutputStream(filename);
            DataOutputStream out = new DataOutputStream(fstream);
            try {
                out.write(bp_data, 0, bp_len);
                out.close();
            } catch (Exception e) {
                throw new FileException("Couldn't write file " + filename + "\n" + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            throw new FileException("Couldn't open file " + filename + "\n" + e.getLocalizedMessage());
        }
    }
}
