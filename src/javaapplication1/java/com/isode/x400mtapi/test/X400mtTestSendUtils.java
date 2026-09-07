/*  Copyright (c) 2008-2011, Isode Limited, London, England.
 *  All rights reserved.
 *                                                                       
 *  Acquisition and use of this software and related materials for any      
 *  purpose requires a written licence agreement from Isode Limited,
 *  or a written licence from an organisation licenced by Isode Limited
 *  to grant such a licence.
 *
 */


// 
/**
 ** Set of functions which submit a message into a P7 messge store.
 */


package com.isode.x400mtapi.test;

import com.isode.x400api.X400_att;
import com.isode.x400api.Message;
import com.isode.x400api.Recip;
import com.isode.x400api.Session;
import com.isode.x400api.DLExpHist;
import com.isode.x400api.Traceinfo;
import com.isode.x400api.InternalTraceinfo;
import com.isode.x400api.RediHist;
import com.isode.x400api.ORandDL;
import com.isode.x400api.PSS;
import com.isode.x400api.ALI;
import com.isode.x400api.OtherRecip;
import com.isode.x400api.ACP127Resp;
import com.isode.x400api.DistField;
import com.isode.x400api.BodyPart;

import com.isode.x400mtapi.MTMessage;

import com.isode.x400api.test.config;
import com.isode.x400api.test.X400BuildFwdMsg;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class X400mtTestSendUtils
{
    /* Cheap noddy way of reading in sample binary file */
    private static byte[] read_bin_file (
        String filename
    )
    {
        byte[] bytes;
        long file_length;
        try {
            File file = new File(config.sharedir, filename);
            file_length = file.length();
            
            FileInputStream filestream = new FileInputStream(file);
            bytes = new byte[(int)file_length];

            System.out.println("Reading in " + file_length + " bytes ");
            filestream.read(bytes,0,(int)file_length);
            filestream.close();
            System.out.println("Finished read_bin_file: ");
            return bytes;
           
            
        } catch (IOException obj) {
            System.out.println("Failed to add bin: "+obj.getMessage());
        }
        System.out.println("Finished read_bin_file: ");
        return null;
    }


    private static int add_fwd_enc (MTMessage mtmessage_obj) {
        int status;
        byte [] fwd_enc_bytes;
        Message fwd_enc_message_obj = new Message();
        System.out.println("Adding P772 Forwarded encrypted bodypart ");
        /* NB: We are just re-using the pilot_fwd_info BER as forwarded
         * encrypted ber
         */
        fwd_enc_bytes = read_bin_file("pilot_fwd_info.ber"); 

        
        /* Add Envelope information to this forwarded encrypted
         * message */
        status = X400BuildFwdMsg.build_fwd_msg(fwd_enc_message_obj,
                                               config.fwd_recip_oraddress);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("build_fwd_msg_env failed " + status);
	    return status;
	}
        
        /* Set the content type to be p772 */
        status = com.isode.x400api.X400.x400_msgaddstrparam(fwd_enc_message_obj, 
        X400_att.X400_S_EXTERNAL_CONTENT_TYPE, config.mt_msg_external_type,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

        status = com.isode.x400api.X400.x400_msgaddbyteparam(fwd_enc_message_obj, 
        X400_att.X400_S_ENCRYPTED_DATA,fwd_enc_bytes);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_msgaddbyteparam failed " + status);
	    return status;
	}

        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddmessagebodywtype(mtmessage_obj,fwd_enc_message_obj,X400_att.X400_T_FWDENC);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        
        
        return X400_att.X400_E_NOERROR;
    }
    
    
    private static int add_fwd_MM (MTMessage mtmessage_obj) {
        int status;
        Message fwd_MM_obj = new Message();
        System.out.println("Adding P772 Forwarded Military Message ");
        /* The build_fwd_msg will create suitable envelope and message content
         */
        status = X400BuildFwdMsg.build_fwd_msg(fwd_MM_obj,
                                               config.fwd_recip_oraddress);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("build_fwd_msg failed " + status);
	    return status;
	}
        
        /* Set the content type to be p772 */
        status = com.isode.x400api.X400.x400_msgaddstrparam(fwd_MM_obj, 
        X400_att.X400_S_EXTERNAL_CONTENT_TYPE, config.mt_msg_external_type,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        
         status = com.isode.x400mtapi.X400mt.x400_mt_msgaddmessagebodywtype(mtmessage_obj,fwd_MM_obj,X400_att.X400_T_MM);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
                
        return X400_att.X400_E_NOERROR;
    }


    private static int add_x420_fwd_content (MTMessage mtmessage_obj) {
        int status;
        byte [] fwd_cnt_bytes;
        Message fwd_cnt_message_obj = new Message();
        System.out.println("Adding Forwarded content bodypart ");
        
        fwd_cnt_bytes = read_bin_file("example_fwd_content.ber"); 
        
        /* Add Envelope information to this forwarded encrypted
         * message */
        status = X400BuildFwdMsg.build_fwd_msg(fwd_cnt_message_obj,
                                               config.fwd_recip_oraddress);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("build_fwd_msg_env failed " + status);
	    return status;
	}
        
        /* Set the content type to be p772
         * NB: This doesn't actually have to be P772, but it doesn't hurt*/
        status = com.isode.x400api.X400.x400_msgaddstrparam(fwd_cnt_message_obj, 
        X400_att.X400_S_EXTERNAL_CONTENT_TYPE, config.mt_msg_external_type,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

        status = com.isode.x400api.X400.x400_msgaddbyteparam(fwd_cnt_message_obj, 
        X400_att.X400_S_FWD_CONTENT_STRING,fwd_cnt_bytes);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_msgaddbyteparam failed " + status);
	    return status;
	}

        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddmessagebodywtype(mtmessage_obj,fwd_cnt_message_obj,X400_att.X400_T_FWD_CONTENT);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        
        
        return X400_att.X400_E_NOERROR;
    }
    
    
    private static int add_adatp3 (MTMessage mtmessage_obj) {
        int status;
        BodyPart bp = new BodyPart();
        
        System.out.println("Adding P772 adatp3 ");
        
        status = com.isode.x400api.X400.x400_bodypartnew(X400_att.X400_T_ADATP3,bp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartnew failed " + status);
            return status;
        }
        
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbodypart(mtmessage_obj,bp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddbodypart failed " + status);
            return status;
        }
        
        status = com.isode.x400api.X400.x400_bodypartaddintparam(
            bp,
            X400_att.X400_N_ADATP3_PARM,
            1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddintparam failed " + status);
            return status;
        }
        
        
        status = com.isode.x400api.X400.x400_bodypartaddintparam(
            bp,
            X400_att.X400_N_ADATP3_CHOICE,
            1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddintparam failed " + status);
            return status;
        }
                
        status = com.isode.x400api.X400.x400_bodypartaddstrparam(
            bp,
            X400_att.X400_S_ADATP3_DATA,
            config.adatp3_line1, -1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddstrparam failed " + status);
            return status;
        }
        
            
        return X400_att.X400_E_NOERROR;
    }
    
    private static int add_corrections (MTMessage mtmessage_obj) {
        int status;
        BodyPart bp = new BodyPart();
        System.out.println("Adding P772 correction ");
        status = com.isode.x400api.X400.x400_bodypartnew(X400_att.X400_T_CORRECTIONS,bp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartnew failed " + status);
            return status;
        }
        
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbodypart(mtmessage_obj,bp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddbodypart failed " + status);
            return status;
        }
        
        status = com.isode.x400api.X400.x400_bodypartaddintparam(
            bp,
            X400_att.X400_N_CORREC_PARM,
            1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddintparam failed " + status);
            return status;
        }
        
        
        status = com.isode.x400api.X400.x400_bodypartaddstrparam(
            bp,
            X400_att.X400_S_CORREC_DATA,
            config.mt_correction, -1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddstrparam failed " + status);
            return status;
        }
        
            
        return X400_att.X400_E_NOERROR;
    }
    
    private static int add_acp127data (MTMessage mtmessage_obj) {
        int status;
        BodyPart bp = new BodyPart();
        System.out.println("Adding P772 ACP127Data ");
        status = com.isode.x400api.X400.x400_bodypartnew(X400_att.X400_T_ACP127DATA,bp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartnew failed " + status);
            return status;
        }
        
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbodypart(mtmessage_obj,bp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddbodypart failed " + status);
            return status;
        }
        
        status = com.isode.x400api.X400.x400_bodypartaddintparam(
            bp,
            X400_att.X400_N_ACP127DATA_PARM,
            1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddintparam failed " + status);
            return status;
        }
        
        
        status = com.isode.x400api.X400.x400_bodypartaddstrparam(
            bp,
            X400_att.X400_S_ACP127_DATA,
            config.mt_acp127data, -1
        );
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_bodypartaddstrparam failed " + status);
            return status;
        }
        
            
        return X400_att.X400_E_NOERROR;
    }
    
    private static int add_acp127resp(MTMessage message_obj){
        int status;
        ACP127Resp resp_obj = new ACP127Resp();
        
        System.out.println("Adding acp127 resp");
        status = com.isode.x400mtapi.X400mt.x400_mt_acp127respnew(message_obj,
								resp_obj);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("add_acp127resp failed " + status);
            return status;    
        }

        status  = com.isode.x400api.X400.x400_acp127respaddintparam(
            resp_obj,X400_att.X400_ACP127_NOTI_TYPE_NEG);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127respaddintparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_acp127respaddintparam(
            resp_obj,X400_att.X400_ACP127_NOTI_TYPE_POS);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127respaddintparamfailed " + status);
            return status;
        }
        

        status  = com.isode.x400api.X400.x400_acp127respaddintparam(
            resp_obj,X400_att.X400_ACP127_NOTI_TYPE_TRANS);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127respaddintparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_acp127addstrparam(
            resp_obj,
            X400_att.X400_S_ACP127_NOTI_RESP_TIME,
            config.mt_acp127resp_resp_time);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127addstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_acp127addstrparam(
            resp_obj,
            X400_att.X400_S_ACP127_NOTI_RESP_RECIPIENT,
            config.mt_acp127resp_resp_recip);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127addstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_acp127addstrparam(
            resp_obj,
            X400_att.X400_S_ACP127_NOTI_RESP_SUPP_INFO,
            config.mt_acp127resp_supp_info);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127addstrparam failed " + status);
            return status;
        }
        
        /* Now add an ALI */
        ALI ali = new ALI();
        System.out.println("Adding acp127resp ALI");
        status = com.isode.x400api.X400.x400_acp127respalinew(resp_obj,
                                                              ali);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_acp127respalinew failed " + status);
            return status;    
        }
         status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_OR_ADDRESS, config.mt_ali_or1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }
          
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_DN_ADDRESS, config.mt_ali_dn1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_FREE_FORM_NAME, config.mt_ali_free_form_name1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_TEL, config.mt_ali_tel1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_aliaddintparam(ali,X400_att.X400_N_ALI_TYPE, config.mt_ali_type1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddintparam failed " + status);
            return status;
        }


        status  = com.isode.x400api.X400.x400_aliaddintparam(ali,X400_att.X400_N_ALI_NOTIFICATION_REQUEST, config.mt_ali_noti_request1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddintparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_aliaddintparam(ali,X400_att.X400_N_ALI_REPLY_REQUEST, config.mt_ali_reply_request1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddintparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;
        
    }
     private static int add_otherrecip(MTMessage mtmessage_obj) {
        int status;
        OtherRecip otherrecip = new OtherRecip();
        status = com.isode.x400mtapi.X400mt.x400_mt_otherrecipnew(mtmessage_obj,otherrecip);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_otherrecip failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_otherrecipaddstrparam(otherrecip, config.mt_otherrecip_str);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_otherrecipaddstrparam failed " + status);
            return status;
        }
        
         status  = com.isode.x400api.X400.x400_otherrecipaddintparam(otherrecip,
                                                                     config.mt_otherrecip_int);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_otherrecipaddintparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;
     }
    
    private static int add_ali(MTMessage mtmessage_obj) {
        int status;
        ALI ali = new ALI();
        status = com.isode.x400mtapi.X400mt.x400_mt_alinew(mtmessage_obj,ali);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_handling_instructions failed " + status);
	    return status;
	}
        
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_OR_ADDRESS, config.mt_ali_or1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }
          
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_DN_ADDRESS, config.mt_ali_dn1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_FREE_FORM_NAME, config.mt_ali_free_form_name1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_aliaddstrparam(ali,X400_att.X400_S_IOB_TEL, config.mt_ali_tel1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_aliaddintparam(ali,X400_att.X400_N_ALI_TYPE, config.mt_ali_type1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddintparam failed " + status);
            return status;
        }


        status  = com.isode.x400api.X400.x400_aliaddintparam(ali,X400_att.X400_N_ALI_NOTIFICATION_REQUEST, config.mt_ali_noti_request1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddintparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_aliaddintparam(ali,X400_att.X400_N_ALI_REPLY_REQUEST, config.mt_ali_reply_request1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_aliaddintparam failed " + status);
            return status;
        }
        
        
        return X400_att.X400_E_NOERROR;
    }



    private static int add_handling_instructions(MTMessage mtmessage_obj) {
        int status;
        PSS hi = new PSS();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssnew(mtmessage_obj,hi,X400_att.X400_S_HANDLING_INSTRUCTIONS);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_handling_instructions failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_pssaddstrparam(hi, config.mt_handling_instruction1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_pssaddstrparam failed " + status);
            return status;
        }

         PSS hi2 = new PSS();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssnew(mtmessage_obj,hi2,X400_att.X400_S_HANDLING_INSTRUCTIONS);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_handling_instructions failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_pssaddstrparam(hi2,config.mt_handling_instruction2);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_pssaddstrparam failed " + status);
            return status;
        }

        return X400_att.X400_E_NOERROR;
    }
 private static int add_messaging_instructions(MTMessage mtmessage_obj) {
        int status;
        PSS mi = new PSS();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssnew(mtmessage_obj,mi,X400_att.X400_S_MESSAGE_INSTRUCTIONS);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_message_instructions failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_pssaddstrparam(mi,config.mt_message_instruction1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_pssaddstrparam failed " + status);
            return status;
        }

         PSS mi2 = new PSS();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssnew(mtmessage_obj,mi2,X400_att.X400_S_MESSAGE_INSTRUCTIONS);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_message_instructions failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_pssaddstrparam(mi2,config.mt_message_instruction2);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_pssstrparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;
 }
    private static int add_dist_codes(MTMessage mtmessage_obj) {
        int status;
        PSS dc = new PSS();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssnew(mtmessage_obj,dc,X400_att.X400_S_DIST_CODES_SIC);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_dist_codes failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_pssaddstrparam(dc,config.mt_distcode1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_pssaddstrparam failed " + status);
            return status;
        }

        PSS dc2 = new PSS();
        status = com.isode.x400mtapi.X400mt.x400_mt_pssnew(mtmessage_obj,dc2,X400_att.X400_S_DIST_CODES_SIC);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_dist_codes failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_pssaddstrparam(dc2,config.mt_distcode2);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_pssstrparam failed " + status);
            return status;
        }

        /* Now add distcode fields */
        DistField distfield = new DistField();
        System.out.println("Adding dist fields ");
        status = com.isode.x400mtapi.X400mt.x400_mt_distfieldnew(mtmessage_obj,distfield);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_dist_codes failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_distfieldaddstrparam(distfield,X400_att.X400_S_DIST_CODES_EXT_OID,config.mt_distfieldoid1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_distfieldaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_distfieldaddbyteparam(distfield,X400_att.X400_S_DIST_CODES_EXT_VALUE,config.mt_distfieldvalue1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_distfieldaddstrparam failed " + status);
            return status;
        }


        DistField distfield2 = new DistField();
        status = com.isode.x400mtapi.X400mt.x400_mt_distfieldnew(mtmessage_obj,distfield2);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_dist_codes failed " + status);
	    return status;
	}

        status  = com.isode.x400api.X400.x400_distfieldaddstrparam(distfield2,X400_att.X400_S_DIST_CODES_EXT_OID,config.mt_distfieldoid2);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_distfieldaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_distfieldaddbyteparam(distfield2,X400_att.X400_S_DIST_CODES_EXT_VALUE,config.mt_distfieldvalue2);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_distfieldaddstrparam failed " + status);
            return status;
        }
        
        
        return X400_att.X400_E_NOERROR;
    }

    
    private static int add_exempt_address(MTMessage mtmessage_obj) {
        int status;
        Recip recip_obj = new Recip();
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipnew(mtmessage_obj, X400_att.X400_EXEMPTED_ADDRESS, recip_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipnew failed " + status);
	    return status;
	}

        status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_IOB_OR_ADDRESS, config.mt_ea_or_address, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

        status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_IOB_DN_ADDRESS, config.mt_ea_dn_address, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

        status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_IOB_FREE_FORM_NAME, config.mt_ea_ffn, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

        status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_IOB_TEL, config.mt_ea_tel, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

        return X400_att.X400_E_NOERROR;
    }

    
   

    
   private static int add_oranddl (MTMessage mtmessage_obj)
    {
        int status;
        ORandDL oranddl_obj = new ORandDL();
        System.out.println("Adding Originator and dl expansion history ");
        status = com.isode.x400mtapi.X400mt.x400_mt_oranddlnew(mtmessage_obj,oranddl_obj);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("add_oranddl failed " + status);
	    return status;
    
        }
       
        status  = com.isode.x400api.X400.x400_oranddladdstrparam(
            oranddl_obj,X400_att.X400_S_ORIG_OR_EXAP_TIME,
            config.mt_rep_oranddl_exap_time1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("oranddladdstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_oranddladdstrparam(
            oranddl_obj,X400_att.X400_S_OR_ADDRESS,
            config.mt_rep_oranddl_exap_or1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("oranddladdstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_oranddladdstrparam(
            oranddl_obj,X400_att.X400_S_DIRECTORY_NAME,
            config.mt_rep_oranddl_exap_dn1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("oranddladdstrparam failed " + status);
            return status;
        }
        
        
        return X400_att.X400_E_NOERROR;   
    }


    /* Redirection histories are normally per recipient.
     * However when dealing with reports, it's possible for the actually
     * report to contain a redirection history in it's envelope.*/
    private static int add_redihist (
        MTMessage mtmessage_obj,
        Recip recip_obj,
        String redi_time,
        String redi_or,
        String redi_dn,
        int redi_reason
    )
    {
        int status;
        RediHist redihist_obj = new RediHist();
        System.out.println("Adding Redirection history ");
        if (mtmessage_obj == null) {
            status = com.isode.x400api.X400.x400_redihistnew(recip_obj,
                                                             redihist_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("add_redihist failed " + status);
                return status;
                
            }
        } else {
            status = com.isode.x400mtapi.X400mt.x400_mt_redihistnewenv(mtmessage_obj,
                                                                redihist_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("add_redihist failed " + status);
                return status;
                
            }
        }
        
        System.out.println("Adding redihist X400_S_REDIRECTION_TIME");
        status  = com.isode.x400api.X400.x400_redihistaddstrparam(
            redihist_obj,X400_att.X400_S_REDIRECTION_TIME,redi_time,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("redihistaddstrparam failed " + status);
            return status;
        }
        
        System.out.println("Adding redihist X400_S_OR_ADDRESS");
        status  = com.isode.x400api.X400.x400_redihistaddstrparam(
            redihist_obj,X400_att.X400_S_OR_ADDRESS, redi_or,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("redihistaddstrparam failed " + status);
            return status;
        }
        
        System.out.println("Adding redihist X400_S_DIRECTORY_NAME");
        status  = com.isode.x400api.X400.x400_redihistaddstrparam(
            redihist_obj,X400_att.X400_S_DIRECTORY_NAME,redi_dn,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("redihistaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_redihistaddintparam(
            redihist_obj,X400_att.X400_N_REDIRECTION_REASON, redi_reason);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("redihistaddstrparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;
    }
    
    private static int add_internal_trace_info (MTMessage mtmessage_obj)
    {
        int status;
        InternalTraceinfo traceinfo_obj = new InternalTraceinfo();
        System.out.println("Adding Internal Traceinfo ");
        status = com.isode.x400mtapi.X400mt.x400_mt_internaltraceinfonew(mtmessage_obj,traceinfo_obj);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("add_internal_traceinfo failed " + status);
	    return status;
    
        }
        
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_GLOBAL_DOMAIN_ID, config.mt_msg_inttraceinfo1_GDI,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_MTA_NAME, config.mt_msg_inttraceinfo1_mtaname,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }

         status  = com.isode.x400api.X400.x400_internaltraceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_MTA_SI_TIME, config.mt_msg_inttraceinfo1_SItime,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_MTA_SI_ROUTING_ACTION,
            X400_att.X400_MTA_SI_ROUTING_ACTION_RELAYED);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }

        /* Put the attempted action, as attempted MTA */
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_MTA_SI_ATTEMPTED_ACTION,
            X400_att.X400_MTA_SI_RA_MTA);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_MTA_SI_ATTEMPTED_MTA, config.mt_msg_inttraceinfo1_SIattempted_MTA,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_MTA_SI_DEFERRED_TIME, config.mt_msg_inttraceinfo1_SIdeferred_time,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_internaltraceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_MTA_SI_CEIT, config.mt_msg_inttraceinfo1_SICEIT,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_internaltraceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_MTA_SI_OTHER_ACTIONS,
            X400_att.X400_MTA_SI_OTHER_ACTION_REDIRECTED);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }

         status  = com.isode.x400api.X400.x400_internaltraceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_MTA_SI_OTHER_ACTIONS,
            X400_att.X400_MTA_SI_OTHER_ACTION_DLOPERATION);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("InternalTraceinfoaddstrparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;   
    }

    private static int add_trace_info (MTMessage mtmessage_obj, int type)
    {
        int status;
        Traceinfo traceinfo_obj = new Traceinfo();
        System.out.println("Adding Traceinfo ");
        status = com.isode.x400mtapi.X400mt.x400_mt_traceinfonew(mtmessage_obj,
                                          traceinfo_obj,type);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("add_traceinfo failed " + status);
	    return status;
    
        }
        
        System.out.println("Traceinfo add  X400_S_GLOBAL_DOMAIN_ID");
        status  = com.isode.x400api.X400.x400_traceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_GLOBAL_DOMAIN_ID, config.mt_msg_traceinfo1_GDI,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }
        
        System.out.println("Traceinfo add  X400_S_DSI_ARRIVAL_TIME");
        status  = com.isode.x400api.X400.x400_traceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_DSI_ARRIVAL_TIME, config.mt_msg_traceinfo1_AT,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }
        
        /* optional */
        status  = com.isode.x400api.X400.x400_traceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_DSI_ATTEMPTED_DOMAIN, config.mt_msg_traceinfo1_AD,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }

        /* optional */
        status  = com.isode.x400api.X400.x400_traceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_DSI_AA_DEF_TIME, config.mt_msg_traceinfo1_AA_DEF_TIME,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }
        
        /* optional */
        status  = com.isode.x400api.X400.x400_traceinfoaddstrparam(
            traceinfo_obj,X400_att.X400_S_DSI_AA_CEIT, config.mt_msg_traceinfo1_AA_CEIT,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_traceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_DSI_ROUTING_ACTION, config.mt_msg_traceinfo1_DSI_RA);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }

        status  = com.isode.x400api.X400.x400_traceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_DSI_AA_REDIRECTED, config.mt_msg_traceinfo1_DSI_redirected);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_traceinfoaddintparam(
            traceinfo_obj,X400_att.X400_N_DSI_AA_DLOPERATION, config.mt_msg_traceinfo1_DSI_AA_DLOP);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Traceinfoaddstrparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;
    }
    
    private static int build_p772 (MTMessage mtmessage_obj)
    {
        int status;
        
        if (config.mt_use_p772 == false)
            return X400_att.X400_E_NOERROR;

        /* STANAG 4406 A1.6 Exempted Address */
        System.out.println("Adding P772 exempted address ");
        status = add_exempt_address(mtmessage_obj);
        if ( status != X400_att.X400_E_NOERROR ) {
            return status;
        }


        /*STANAG 4406 A1.3	Distribution codes */
        System.out.println("Adding P772 Distribution codes ");
        status = add_dist_codes(mtmessage_obj);
        if ( status != X400_att.X400_E_NOERROR ) {
            return status;
        }
        
        
        /* STANAG 4406 A1.4	Handling instructions */
        System.out.println("Adding P772 Handling instructions ");
        status = add_handling_instructions(mtmessage_obj);
        if ( status != X400_att.X400_E_NOERROR ) {
            return status;
        }

        /* STANAG 4406 A1.5	Message instructions */
        System.out.println("Adding P772 Message instructions ");
        status = add_messaging_instructions(mtmessage_obj);
        if ( status != X400_att.X400_E_NOERROR ) {
            return status;
        }
        
        
        System.out.println("Adding P772 Codress ");
        /* STANAG 4406 A1.6	Codress message */

        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                          X400_att.X400_N_EXT_CODRESS, config.mt_codress);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddintparam failed " + status);
            return status;
        }

        System.out.println("Adding P772 originator reference");
        /*STANAG 4406 A1.7	Originator reference*/
         status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_ORIG_REF, config.mt_orig_ref,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        
        System.out.println("Adding P772 primary precedence ");
        /* STANAG 4406 A1.8	Primary precedence */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                          X400_att.X400_N_EXT_PRIM_PREC, config.mt_prim_prec);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddintparam failed " + status);
            return status;
        }

        System.out.println("Adding P772 Copy precedence ");
        /* STANAG 4406 A1.9    Copy precedence */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                          X400_att.X400_N_EXT_COPY_PREC, config.mt_copy_prec);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddintparam failed " + status);
            return status;
        }

        System.out.println("Adding P772 message type ");
        /* STANAG 4406 A1.10	Message type */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                          X400_att.X400_N_EXT_MSG_TYPE, config.mt_msg_type);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddintparam failed " + status);
            return status;
        }

        System.out.println("Adding P772 Address list indicator");
        /* STANAG 4406 A1.11	Address list indicator */
        status = add_ali(mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to add ALI" + status);
	    return status;
	}


        System.out.println("Adding P772 Other recipient ");
        /* STANAG 4406 A1.12	Other recipients indicator */
        status = add_otherrecip(mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("Failed to add Other Recip" + status);
	    return status;
	}
        

        
        
        System.out.println("Adding P772 Ext Auth info ");
        /*STANAG 4406 A1.2	Extended authorisation information */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_EXT_AUTH_INFO, config.mt_ext_auth_info,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

        System.out.println("Adding P772 ACP127 Msg ID ");
        /* STANAG 4406 A1.14	ACP127 message identifier */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_ACP127_MSG_ID, config.mt_acp127_msg_id,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        System.out.println("Adding P772 Orig PLAD ");
        /* STANAG 4406 A1.15	Originator PLAD */
         status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_ORIG_PLAD, config.mt_orig_plad,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

        /* STANAG 4406 A1.13	Pilot forwarding information */
        
        byte [] pilot_bytes;
        
        System.out.println("Adding Pilot forwarding info ");
        pilot_bytes = read_bin_file("pilot_fwd_info.ber");
        
        if (pilot_bytes != null) {
            System.out.println("Adding pilot forwarding info bytes");
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbyteparam(
                mtmessage_obj, 
                X400_att.X400_S_PILOT_FWD_INFO,
                pilot_bytes);
        }
        
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Adding pilot forwarding info failed " + status);
            return status;
        }
        
        /* STANAG 4406 A1.16	Security Information Labels */
        byte [] sec_label_bytes;
        
        System.out.println("Adding security info label ");
        sec_label_bytes = read_bin_file("info_sec_label.ber");
        
        if (sec_label_bytes != null) {
            System.out.println("Adding security info label bytes");
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbyteparam(
                mtmessage_obj, 
                X400_att.X400_S_INFO_SEC_LABEL,
                sec_label_bytes);
        }
        
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Adding pilot forwarding info failed " + status);
            return status;
        } 
        
        
/*
STANAG 4406 A2.1	ACP127 notification request
STANAG 4406 A3.1	ACP127 notification response
	
STANAG 4406 B1.1	ADatP3
STANAG 4406 B1.2	Corrections 
STANAG 4406 B1.3	Forwarded Encrypted
STANAG 4406 B1.4	MM Message
STANAG 4406 B1.5	ACP127Data
*/	
	

        return X400_att.X400_E_NOERROR;
    }

    private static int add_dlexp_hist (MTMessage mtmessage_obj)
    {
        int status;
        DLExpHist dleh_obj = new DLExpHist();
        System.out.println("Adding DL Exp History ");
        status = com.isode.x400mtapi.X400mt.x400_mt_DLexphistnew(mtmessage_obj,dleh_obj);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_dlexp_hist failed " + status);
	    return status;
	}

        
        status  = com.isode.x400api.X400.x400_DLaddstrparam(dleh_obj,X400_att.X400_S_OR_ADDRESS,config.mt_dlexp_or1,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_DLaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_DLaddstrparam(dleh_obj,
                                     X400_att.X400_S_DIRECTORY_NAME,
                                     config.mt_dlexp_dn1,
                                     -1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_DLaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_DLaddstrparam(dleh_obj,
                                     X400_att.X400_S_DLEXP_TIME,
                                     config.mt_dlexp_exp_time1,
                                     -1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_DLaddstrparam failed " + status);
            return status;
        }
        
        /* add second dl exp hist */
        DLExpHist dleh_obj2 = new DLExpHist();
        System.out.println("Adding DL Exp History ");
        status = com.isode.x400mtapi.X400mt.x400_mt_DLexphistnew(mtmessage_obj,dleh_obj2);
       	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_dlexp_hist failed " + status);
	    return status;
	}

        
        status  = com.isode.x400api.X400.x400_DLaddstrparam(dleh_obj2,X400_att.X400_S_OR_ADDRESS,config.mt_dlexp_or2,-1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_DLaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_DLaddstrparam(dleh_obj2,
                                     X400_att.X400_S_DIRECTORY_NAME,
                                     config.mt_dlexp_dn2,
                                     -1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_DLaddstrparam failed " + status);
            return status;
        }
        
        status  = com.isode.x400api.X400.x400_DLaddstrparam(dleh_obj2,
                                     X400_att.X400_S_DLEXP_TIME,
                                     config.mt_dlexp_exp_time2,
                                     -1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_DLaddstrparam failed " + status);
            return status;
        }
        
        return X400_att.X400_E_NOERROR;
    }
    
    private static int build_probe_env (MTMessage mtmessage_obj)
    {
        int rno = 1; /* recipient number */
        int status;
        
        System.out.println("Adding probe env");
        
        /*X.411 Probe-identifier 12.2.1.2.1.1 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_MESSAGE_IDENTIFIER, config.mt_prb_id,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        
        /*X.411 Per-domain-bilateral-information 12.2.1.1.1.2 NYI*/

        
        /* X.411 Trace-information 12.2.1.1.1.3 */
        /* X.411 Internal-trace-information 12.2.1.1.1.4 */

        
        /* X.411 DL-expansion-history 18.3.1.1.1.7 */
        System.out.println("Adding dl expansion history");
        status = add_dlexp_hist(mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
            return status;
        }
        System.out.println("Adding probe orig name");
        /*  X.411 Originator-name 8.2.1.1.1.1 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                           X400_att.X400_S_OR_ADDRESS,
                           config.mt_sendoraddr, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

        // header
        System.out.println("Adding probe orig header");
	status = add_mt_recip(mtmessage_obj, config.mt_sendoraddr, 
	    X400_att.X400_ORIGINATOR, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}

        
        System.out.println("Adding probe recipient");
        /* Add a recipient and per recipient atttributes*/
        {
            Recip recip_obj = new Recip();
            status = com.isode.x400mtapi.X400mt.x400_mt_recipnew(mtmessage_obj, 
                                       X400_att.X400_RECIP_STANDARD, recip_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipnew failed " + status);
                return status;
            }
            
            System.out.println("Adding probe recipient name");
            /* X.411 Recipient-name 8.2.1.1.1.2 */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_OR_ADDRESS, config.mt_rcvoraddr, -1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddstrparam failed " + status);
                return status;
            }

            System.out.println("Adding probe recip number");
            /*X.411 Originally-specified-recipient-number 12.2.1.1.1.5*/
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, rno++);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }

            System.out.println("Adding probe recipient responsiblity");
            /* X.411 Responsibility 12.2.1.1.1.6 (1 = responsible)*/
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_RESPONSIBILITY, config.mt_prb_resp);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }

          
            System.out.println("Adding probe orig requested alt recip");
            /* X.411 Originator-requested-alternate-recipient 8.2.1.1.1.5 */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT, 
	    	config.mt_sendaltrecip, -1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddstrparam failed " + status);
                return status;
            }


            System.out.println("Adding probe orig report request");
            /* X.411 Originator-report-request 8.2.1.1.1.22
             * no report           0
             * non-delivery-report 1
             * report              2
             */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_REPORT_REQUEST, config.mt_prb_rep_req);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }


            System.out.println("Adding probe recipient mta report request");
            /* X.411 Originating-MTA-report-request 12.2.1.1.1.8
             * non-delivery-report 1
             * report              2
             * audited-report      3
             */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                X400_att.X400_N_MTA_REPORT_REQUEST, config.mt_prb_mta_rep_req);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }
            
        }
        System.out.println("Adding probe dl expansion prohibited");
        /* X.411 DL-expansion-prohibited 8.2.1.1.1.6 (0 = not prohibited) */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                                                                   X400_att.X400_N_DL_EXPANSION_PROHIBITED,config.mt_prb_dlexp);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddintparam failed " + status);
            return status;
            }
            
        System.out.println("Adding probe alt recipient allowed");
        /* X.411 Alternate-recipient-allowed 8.2.1.1.1.3 (1 = yes) */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
            X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED,
                                      config.mt_prb_alt_recip_allowed);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}
        
        System.out.println("Adding probe reassignment prohibited");
        /* X.411 Recipient-reassignment-prohibited 8.2.1.1.1.4 (0 = no)*/
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                 X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED,
                 config.mt_prb_recip_res_prohib);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}
       
   
        
        /*
          X.411 Intended-recipient-name / redirection history 8.3.1.1.1.5
          X.411 Redirection-reason
        */

        System.out.println("Adding probe implicit conversion prohobited");
        /* X.411 Implicit-conversion-prohibited 8.2.1.1.1.9 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED,
                                              config.mt_prb_imp_conv_prohib);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	} 

        System.out.println("Adding probe conversion with loss prohibited");
        
        /* X.411 Conversion-with-loss-prohibited 8.2.1.1.1.10 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                              X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED,
                              config.mt_prb_conv_with_loss_prohib);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
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
        
        System.out.println("Adding probe conversion OEIT");
        /* X.411 Original-encoded-information-types 8.2.1.1.1.33 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES,
                 config.mt_prb_OEITS, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	} 

        return X400_att.X400_E_NOERROR;
    }

    private static int build_probe_content (MTMessage mtmessage_obj)
    {
        int status;
        
        /* X.411 Content-type 8.2.1.1.1.34
         * Normally content type will be P2 or P22
         * However if your are manipulating a P772 message,
         * then you need to use an externally defined content type
         */
        System.out.println("Building probe content ");
        if (config.mt_prb_external_type == null ||
            config.mt_prb_external_type.equals("")) {
            
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                                  X400_att.X400_N_CONTENT_TYPE,
                                  config.mt_prb_cont_type);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_msgaddintparam failed " + status);
                return status;
            }
            
        } else {
             status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_EXTERNAL_CONTENT_TYPE,
                 config.mt_prb_external_type, -1);
             if (status != X400_att.X400_E_NOERROR) {
                 System.out.println("x400_mt_msgaddstrparam failed " + status);
                 return status;
             } 
            
        }
        
        System.out.println("Adding probe content id ");
        /* X.411 Content-identifier  8.2.1.1.1.35 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_CONTENT_IDENTIFIER, config.mt_prb_cont_id, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
        
        System.out.println("Adding probe content correlator ");
        /* X.411 Content-correlator 8.2.1.1.1.36
         * NB: In this example the content correlator is an IA5text string
         * You could however add a correctly formatted series of bytes
         * using the msgaddbyteparam and
         * X400_S_CONTENT_CORRELATOR_OCTET_STRING parameter value.
         */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_CONTENT_CORRELATOR_IA5_STRING, config.mt_prb_cont_corr, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	} 
        
        
        System.out.println("Adding probe content length ");
        /* X.411 Content-length 8.2.1.2.1.2 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                 X400_att.X400_N_CONTENT_LENGTH, config.mt_prb_cont_length);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddintparam failed " + status);
            return status;
        }
        
        
        /*
          X.411 Notification-type  8.2.1.1.1.38
          X.411 Service-message 8.2.1.1.1.39
        */
        
        return X400_att.X400_E_NOERROR;
    }
    
    private static int build_probe (MTMessage mtmessage_obj)
    {
      
        int status;
          
        status = build_probe_env (mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
            return status;
        }

        status = build_probe_content(mtmessage_obj);
        return status;
    }


    private static int build_rep_env (MTMessage mtmessage_obj, int rno)
    {

        int status;

        /* Report transfer envelope: */
        System.out.println("Adding report env ");
        System.out.println("Adding report dest name to env" + config.mt_rep_dest_name);
        /*X.411 Report-destination-name 12.2.1.3.1.2*/
        status = add_mt_recip(mtmessage_obj, config.mt_rep_dest_name, 
                              X400_att.X400_RECIP_ENVELOPE, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}
        
        System.out.println("Adding report identifier");
        /* X.411 Report-identifier 12.2.1.3.1.1 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_MESSAGE_IDENTIFIER, config.mt_rep_id,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}
       
        
        /* Report transfer envelope extensions: */

        
        /* NYI: X.411 Message-security-label  8.2.1.1.1.30 
       
        */
        
        /* X.411 redirection history 8.3.1.2.1.5
         * NB this is the redirection history of the report.
         */
        System.out.println("Add redirection env ");
        status =  add_redihist ( mtmessage_obj,
                                 null,
                                 config.mt_redi_time1,
                                 config.mt_redi_or1,
                                 config.mt_redi_dn1,
                                 X400_att.X400_RR_ALIAS);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add redirection env history failed " + status);
	    return status;
	}
        
        /* X.411 Trace-information 12.2.1.1.1.3 */
        System.out.println("Adding trace info");
        status = add_trace_info (mtmessage_obj,X400_att.X400_TRACE_INFO);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add trace info failed " + status);
            return status;
	}
        
        System.out.println("Adding internal trace info");
        /* X.411 Internal-trace-information 12.2.1.1.1.4 */
        status = add_internal_trace_info  (mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add internal traceinfo failed " + status);
            return status;
	}

        
        /*X.411 Originator-and-DL-expansion-history 8.3.1.2.1.3 */
        status = add_oranddl (mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add ORandDL failed " + status);
            return status;
	}
        
        
        
        /*
          X.411 Reporting-DL-name  18.3.1.2.1.4
          There are some rules as to when you should include a reporting
          dl name. So this is commented out.
        status = add_mt_recip(mtmessage_obj, config.mt_rep_dest_name, 
                              X400_att.X400_REPORTING_DL_NAME, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}
        */

        /* NYI:
           X.411 Reporting-MTA-certificate  8.3.1.2.1.12 
           X.411 Report-origin-authentication-check  8.3.1.2.1.13 */

        
        
        /* NYI: X.411 Reporting-MTA-name  8.3.1.2.1.17 */
        
       
           

        return X400_att.X400_E_NOERROR;
    }

    private static int build_rep_content (MTMessage mtmessage_obj,int rno)
    {
        int status;
        /* Report content */
        
        System.out.println("Adding report subject identifier");
        /* X.411 Subject-identifier 12.2.1.3.1.3 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                       X400_att.X400_S_SUBJECT_IDENTIFIER,
                       config.mt_rep_sub_id,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

        /*
          X.411 Subject-intermediate-trace-information  12.2.1.3.1.4
        */
        System.out.println("Adding subject trace info");
        status = add_trace_info (mtmessage_obj,X400_att.X400_SUBJECT_TRACE_INFO);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add subject trace info failed " + status);
            return status;
	}
        
         System.out.println("Adding report original OEIT");
        /* X.411 Original-encoded-information-types  8.2.1.1.1.33 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                       X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES,
                       config.mt_rep_OEIT,-1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

         /* X.411 Content-type 8.2.1.1.1.34
         * Normally content type will be P2 or P22
         * However if your are manipulating a P772 message,
         * then you need to use an externally defined content type
         */
        System.out.println("Adding report content type");
        if (config.mt_rep_external_type == null ||
            config.mt_rep_external_type.equals("")) {
        
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
                                  X400_att.X400_N_CONTENT_TYPE,
                                  config.mt_rep_cont_type);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_msgaddintparam failed " + status);
                return status;
            }
            
        } else {
             status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_EXTERNAL_CONTENT_TYPE,
                 config.mt_rep_external_type, -1);
             if (status != X400_att.X400_E_NOERROR) {
                 System.out.println("x400_mt_msgaddstrparam failed " + status);
                 return status;
             } 
            
        }

        System.out.println("Adding report content identifier");
        /* X.411 Content-identifier  8.2.1.1.1.35 */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_CONTENT_IDENTIFIER,
                 config.mt_rep_cont_id, -1);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddstrparam failed " + status);
            return status;
        } 

        /* NYI: X.411 Returned-content 8.3.1.2.1.14 */
        /* NYI: X.411 Additional-information  12.2.1.3.1.6 */

        /* Report transfer content extensions */

                
        System.out.println("Adding report content correlator");
        /* X.411 Content-correlator  8.2.1.1.1.36
         * NB: In this example the content correlator is an IA5text string
         * You could however add a correctly formatted series of bytes
         * using the msgaddbyteparam and
         * X400_S_CONTENT_CORRELATOR_OCTET_STRING parameter value.
         * */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_CONTENT_CORRELATOR_IA5_STRING, config.mt_prb_cont_corr, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	} 
        
       
        /* The envelope contains the report destination name
         * However we need to be able to set the per recipient transfer
         * fields, which we do with an X400_RECIP_REPORT */
        {
            Recip recip_obj = new Recip();
            
            System.out.println("Adding report new recip ");
            status = com.isode.x400mtapi.X400mt.x400_mt_recipnew(mtmessage_obj, 
                                       X400_att.X400_RECIP_REPORT, recip_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipnew failed " + status);
                return status;
            }

            System.out.println("Adding report recip name ");
            /* X.411 Recipient-name 8.2.1.1.1.2 */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_OR_ADDRESS, config.mt_rep_dest_name, -1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddstrparam failed " + status);
                return status;
            }
            
            System.out.println("Adding report recip number");
            /*X.411 Originally-specified-recipient-number 12.2.1.1.1.5*/
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, rno++);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }
            
            /* Now specify the per recipient indicators:      */
            System.out.println("Adding responsibility");
            /* Per recipient indicators: responsibility */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_RESPONSIBILITY, 1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }
            
            System.out.println("Adding Per recipient indicator report request ");
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_REPORT_REQUEST, 2);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }

            System.out.println("Adding Per recipient indicator MTA report request ");
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                     X400_att.X400_N_MTA_REPORT_REQUEST, 3);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }

            /* End of per recipient indicators */

            /* Last trace information: */
            
            System.out.println("Adding report arrival time ");
            /* X.411 Arrival-time  12.2.1.3.1.5 */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_ARRIVAL_TIME, config.mt_rep_arr_time, -1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddstrparam failed " + status);
                return status;
            }

            
            System.out.println("Adding report CEITs ");
            /* X.411 Converted-encoded-information-types 8.3.1.2.1.5 */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_CONVERTED_ENCODED_INFORMATION_TYPES,
            config.mt_rep_CEIT, -1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddstrparam failed " + status);
                return status;
            }

            /* NYI: X.411 Originally-intended-recipient-name 8.3.1.1.1.4 */
            
            System.out.println("Adding report supplementary information");
            /* X.411 Supplementary-information  8.3.1.2.1.6 */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_SUPPLEMENTARY_INFO,
            config.mt_rep_sup_info, -1);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddstrparam failed " + status);
                return status;
            }

            if (config.mt_rep_dr == 1) {
                /* we are doing a delivery report */
                System.out.println("Adding report Message delivery time");
                /* X.411 Message-delivery-time 8.3.1.2.1.8 */
                status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
                  X400_att.X400_S_MESSAGE_DELIVERY_TIME,
                  config.mt_rep_msg_del_time, -1);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("x400_mt_msgaddintparam failed " + status);
                    return status;
                }
                
                System.out.println("Adding report MTS user type");
                /*  X.411 Type-of-MTS-user  8.3.1.2.1.9 */
                status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                X400_att.X400_N_TYPE_OF_USER, 0);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam failed " + status);
                return status;
            }

            }  else {
                /* we are doing a non-delivery report */

                System.out.println("Adding report non delivery reason");
                /*  X.411 Non-delivery-reason-code 8.3.1.2.1.10 */
                status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                 X400_att.X400_N_NON_DELIVERY_REASON, config.mt_rep_non_del_reason);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("x400_mt_recipaddintparam failed " + status);
                    return status;
                }
                
                
                System.out.println("Adding report non delivery diagnostic");
                /* X.411 Non-delivery-diagnostic-code 8.3.1.2.1.11 */
                status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                 X400_att.X400_N_NON_DELIVERY_DIAGNOSTIC, config.mt_rep_non_del_diag);
                if (status != X400_att.X400_E_NOERROR) {
                    System.out.println("x400_mt_recipaddintparam failed " + status);
                    return status;
                }

            }

            
            /* per recipient redirection history */
            System.out.println("add redirection history for this recipient ");
            status =  add_redihist (null,
                                    recip_obj,
                                    config.mt_redi_time1,
                                    config.mt_redi_or1,
                                    config.mt_redi_dn1,
                                    X400_att.X400_RR_ALIAS);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add redirection history failed " + status);
	    return status;
	}
            
            
        }


        /* NYI:Adding message content */
        
        return X400_att.X400_E_NOERROR;
    }
    
    private static int build_report (MTMessage mtmessage_obj)
    {
        int status;
        int rno = 1;
        status = build_rep_env (mtmessage_obj,rno);
        if (status != X400_att.X400_E_NOERROR) {
            return status;
        }

        status = build_rep_content(mtmessage_obj,rno);
        return status;
    }

    /**
     * Transfer a message into the MTA.
     */
    public static void send_msg(String[] args, int type)
    {
	int status;

	// instantiate a message object, and make it an API object 
	// by opening an API session 
	Session session_obj = new Session();

     
        
	// Open the session using the instance field values
        status = com.isode.x400mtapi.X400mt.x400_mt_open(config.x400mt_chan_name,
                                                       session_obj);
                          

	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_open failed " + status);
	    return;
	}
	System.out.println("Opened MT session successfully");

	// turn on all logging for this session
    	status = com.isode.x400mtapi.X400mt.x400_mt_setstrdefault(session_obj, 
	    X400_att.X400_S_LOG_CONFIGURATION_FILE, "x400api.xml", -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_setstrdefault failed " + status);
	    return;
	}

	// instantiate a message object, and make it an API object 
	MTMessage mtmessage_obj = new MTMessage();
        
        status = com.isode.x400mtapi.X400mt.x400_mt_msgnew(session_obj, 
	    type, mtmessage_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_mtgnew failed " + status);
	    return;
	}

        switch (type) {
        case X400_att.X400_MSG_PROBE:
            status = build_probe (mtmessage_obj);
            break;
        case X400_att.X400_MSG_REPORT:
            status = build_report (mtmessage_obj);
            break;
        case X400_att.X400_MSG_MESSAGE:
        default:
            if (config.building_simple_message) {
                status = build_simple_mtmsg(session_obj, mtmessage_obj);
            } else {
                status = build_mtmsg(session_obj, mtmessage_obj);
            }  
        }
        
        if (status != X400_att.X400_E_NOERROR) {
             System.out.println("x400_mt build message failed " + status);
            return;
        }
	// message all assembled - submit it 
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgsend(mtmessage_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgsend failed " + status);
	    return;
	}
	System.out.println("Submitted message successfully");

	// close the API session 
    	status = com.isode.x400mtapi.X400mt.x400_mt_close(session_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_close failed " + status);
	    return;
	}
	System.out.println("Closed MT Session successfully\n");
	return;
    }

    /**
     * Build a message to submit into the Message Store.
     */
    private static int build_simple_mtmsg(Session session_obj, MTMessage mtmessage_obj)
    {
	return X400_att.X400_E_NYI;
    }

    /**
     * Build a message to submit into the Message Store.
     */
    private static int build_mtmsg(Session session_obj,
                                   MTMessage mtmessage_obj)
    {
	int status;
	int rno = 1;
        String unknown_recip
            = "/CN=invalid/O=GatewayMTA/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";
        String orig = config.mt_sendoraddr;



	System.out.println("Building message envelope");
	status = build_mt_env(mtmessage_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}

	System.out.println("Building message content");
	status = build_mt_content(session_obj, mtmessage_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}


        if (config.mt_use_p772 == true) {
            System.out.println("Adding P772 attributes");
            status = build_p772(mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_msgnew failed " + status);
                return status;
            }            
        }

        
        
	System.out.println("Adding originator");
	// envelope 
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_OR_ADDRESS, config.mt_sendoraddr, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
	// header 
	status = add_mt_recip(mtmessage_obj, config.mt_sendoraddr, 
	    X400_att.X400_ORIGINATOR, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}



	System.out.println("Adding recipients");

	// add recip to msg 
	status = add_mt_recip(mtmessage_obj, config.mt_rcvoraddr, 
	    X400_att.X400_RECIP_STANDARD, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}

	// add unknown (unroutable) recip to message
	status = add_mt_recip(mtmessage_obj, unknown_recip, 
	    X400_att.X400_RECIP_STANDARD, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}

        /* Add dl exempted recipient */

        status = add_mt_recip(mtmessage_obj, config.mt_msg_dl_exempted_addr, 
	    X400_att.X400_DL_EXEMPTED_RECIP, rno++);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgnew failed " + status);
	    return status;
	}
        
        
	return status;
    }

    /**
     * Add a recipient into a message for message transfer.
     */
    private static int add_mt_recip(MTMessage mtmessage_obj, 
	String recip, int type, int rno)
    {
	int status;

	// instantiate a recip object, and make it an API object 
	Recip recip_obj = new Recip();
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipnew(mtmessage_obj, 
	    type, recip_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipnew failed " + status);
	    return status;
	}

        /* Add redi hists */
        /* Add redirection history 1 */
        
        status =  add_redihist (null,
                                recip_obj,
                                config.mt_redi_time1,
                                config.mt_redi_or1,
                                config.mt_redi_dn1,
                                X400_att.X400_RR_ALIAS);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add redirection history failed " + status);
	    return status;
	}

        
        
        /* Add redirection history 2 */
        
        status =  add_redihist (null,
                                recip_obj,
                                config.mt_redi_time2,
                                config.mt_redi_or2,
                                config.mt_redi_dn2,
                                X400_att.X400_RR_RECIP_MD_ASSIGNED_ALT_RECIP);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add redirection history failed " + status);
	    return status;
	}
        
        
	// add some X.400 envelope attributes into the recipient 

	// use length of -1 to indicate null terminated 
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_OR_ADDRESS, recip, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
	    X400_att.X400_N_MTA_REPORT_REQUEST, 3);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddintparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
	    X400_att.X400_N_REPORT_REQUEST, 2);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddintparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
	    X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER, rno);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddintparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT, 
	    	config.mt_sendaltrecip, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

	// add some X.400 recip attributes into the recipient 
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
	    X400_att.X400_N_NOTIFICATION_REQUEST, 7);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
	    X400_att.X400_N_REPLY_REQUESTED, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_FREE_FORM_NAME, recip, recip.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}
	
    	status = com.isode.x400mtapi.X400mt.x400_mt_recipaddstrparam(recip_obj, 
	    X400_att.X400_S_TELEPHONE_NUMBER, config.mt_phone_num, config.mt_phone_num.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_recipaddstrparam failed " + status);
	    return status;
	}

        if (config.mt_use_p772 == true) {
            /* Add P772 per-recipient extensions */
             System.out.println("Adding STANAG 4406 A2.1 ACP127 notification request");
             
             /* STANAG 4406 A2.1 ACP127 notification request */
             status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                      X400_att.X400_N_ACP127_NOTI_TYPE,
                      X400_att.X400_ACP127_NOTI_TYPE_NEG);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam X400_N_ACP127_NOTI_TYPE failed " + status);
                return status;
            }
            
            /* STANAG 4406 A2.1 ACP127 notification request */
             status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                      X400_att.X400_N_ACP127_NOTI_TYPE,
                      X400_att.X400_ACP127_NOTI_TYPE_POS);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam X400_N_ACP127_NOTI_TYPE failed " + status);
                return status;
            }

            /* STANAG 4406 A2.1 ACP127 notification request */
            status = com.isode.x400mtapi.X400mt.x400_mt_recipaddintparam(recip_obj, 
                      X400_att.X400_N_ACP127_NOTI_TYPE,
                      X400_att.X400_ACP127_NOTI_TYPE_TRANS);
            if (status != X400_att.X400_E_NOERROR) {
                System.out.println("x400_mt_recipaddintparam X400_N_ACP127_NOTI_TYPE failed " + status);
                return status;
            }

        }
        

        
	System.out.println("Added recipient " + recip + "to MT Message");
	return status;
    }

    /**
     * Build the envelope for a message for transfer into an MTA
     */
    private static int build_mt_env(MTMessage mtmessage_obj)
    {
	int status;

        /* Add trace infomation */

        status =  add_trace_info (mtmessage_obj,X400_att.X400_TRACE_INFO);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add trace info failed " + status);
	    return status;
	}
        
        /* Add internal trace infomation */
        status =  add_internal_trace_info (mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add trace info failed " + status);
	    return status;
	}
        
	// Priority: 0 - normal, 1 - non-urgent, 2 - urgent 
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_PRIORITY, 2);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Disclosure of recipients: 0 - no, 1 - yes 
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_DISCLOSURE, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	//  Implicit conversion prohibited: 0 - no, 1 - yes
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Alternate recipient allowed: 0 - no, 1 - yes
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Content return request: 0 - no, 1 - yes
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_CONTENT_RETURN_REQUEST, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Recipient reassignment prohibited: 0 - no, 1 - yes
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Distribution List expansion prohibited: 0 - no, 1 - yes
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_DL_EXPANSION_PROHIBITED, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Conversion with loss prohibited: 0 - no, 1 - yes
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED, 1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Message Identifier. In RFC 2156 String form
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_MESSAGE_IDENTIFIER, config.mt_msg_id, config.mt_msg_id.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// Content Identifier
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_CONTENT_IDENTIFIER, config.mt_msg_content_id, 
	    config.mt_msg_content_id.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

        
        //  Deferred Delivery Time: UTCTime format YYMMDDHHMMSS<zone>
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_DEFERRED_DELIVERY_TIME, config.mt_msg_def_del_time, 
	    config.mt_msg_latest_del_time.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}
	//  Latest Delivery Time: UTCTime format YYMMDDHHMMSS<zone>
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_LATEST_DELIVERY_TIME, config.mt_msg_latest_del_time, 
	    config.mt_msg_latest_del_time.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	//  Originator Return Address (X.400 String format)
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_ORIGINATOR_RETURN_ADDRESS, config.mt_origretaddr, 
	    config.mt_origretaddr.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}


        System.out.println("Adding dl expansion history");
        status = add_dlexp_hist(mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR) {
            return status;
        }
        
        System.out.println("Adding content correlator ");
        /* X.411 Content-correlator  8.2.1.1.1.36
         * NB: In this example the content correlator is an IA5text string
         * You could however add a correctly formatted series of bytes
         * using the msgaddbyteparam and
         * X400_S_CONTENT_CORRELATOR_OCTET_STRING parameter value.
         * */
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                 X400_att.X400_S_CONTENT_CORRELATOR_IA5_STRING, config.mt_msg_cont_corr, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	} 
        
        /* Add in binary file stuff */
        /* Add MOAC */
        
        byte [] moac_bytes;
        
        System.out.println("Adding MOAC ");
        moac_bytes = read_bin_file("MOAC.ber");
        
        if (moac_bytes != null) {
            System.out.println("Adding MOAC bytes");
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbyteparam(
                mtmessage_obj, 
                X400_att.X400_S_MOAC,
                moac_bytes);
        }
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Adding MOAC failed " + status);
            return status;
        } 
        
        byte [] orig_cert_bytes;
        /* Add in orignator cert*/
        System.out.println("Adding originator cert ");
        orig_cert_bytes = read_bin_file("1137492922.p12");
        
        if (orig_cert_bytes != null) {
            System.out.println("Adding originator certificate bytes");
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbyteparam(
                mtmessage_obj, 
                X400_att.X400_S_ORIG_CERT,
                orig_cert_bytes);
        }
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Adding originator certificate failed" + status);
            return status;
        } 
        
        byte [] sec_label_bytes;
        /* Add in a secrurity label */
        System.out.println("Adding X.411 security label");
        sec_label_bytes = read_bin_file("x411seclabel.ber");
        
        if (sec_label_bytes != null) {
            System.out.println("Adding X.411 security label");
            status = com.isode.x400mtapi.X400mt.x400_mt_msgaddbyteparam(
                mtmessage_obj, 
                X400_att.X400_S_SECURITY_LABEL,
                sec_label_bytes);
        }
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("Adding X.411 security label failed" + status);
            return status;
        } 
        

	return X400_att.X400_E_NOERROR;
    }

    /**
     * Build the content for a message for transfer into the MTA.
     */
    private static int build_mt_content(Session session_obj,
                                        MTMessage mtmessage_obj)
    {
	int status;
	int num_atts = 0;
            
	int autoforwarded = 1;

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_IPM_IDENTIFIER, config.mt_msg_ipm_id, config.mt_msg_ipm_id.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_SUBJECT, config.mt_msg_subj, -1);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_REPLIED_TO_IDENTIFIER,  config.mt_msg_ipm_id, 
	    config.mt_msg_ipm_id.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_OBSOLETED_IPMS, config.mt_msg_ipm_obs_id, 
	    config.mt_msg_ipm_obs_id.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_RELATED_IPMS,
            config.mt_msg_ipm_rel_id,
            config.mt_msg_ipm_rel_id.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_S_EXPIRY_TIME, config.mt_msg_def_utc, config.mt_msg_def_utc.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
                                                                 X400_att.X400_S_REPLY_TIME, config.mt_msg_def_utc, config.mt_msg_def_utc.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_IMPORTANCE, config.mt_msg_importance);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_SENSITIVITY, config.mt_msg_sensitivity);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_AUTOFORWARDED, config.mt_msg_autoforwarded);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	// now add the attachments/bodyparts

	// Add an IA5 attachment using AddStrParam
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddstrparam(mtmessage_obj, 
	    X400_att.X400_T_IA5TEXT,
            config.mt_msg_cnt_ia5,
            config.mt_msg_cnt_ia5.length());
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddstrparam failed " + status);
	    return status;
	}
	num_atts++;

	// Add an IA5 attachment
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddattachment(
	    mtmessage_obj, X400_att.X400_T_IA5TEXT, config.mt_msg_cnt_ia5_att, 
	    config.mt_msg_cnt_ia5_att.length(), config.mt_msg_cnt_emptybinarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddattachment failed " + status);
	    return status;
	}
	num_atts++;

	// Add an 8859-1 attachment
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddattachment(
	    mtmessage_obj, X400_att.X400_T_ISO8859_1, config.mt_msg_cnt_8859_1, 
	    config.mt_msg_cnt_8859_1.length(), config.mt_msg_cnt_emptybinarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddattachment failed " + status);
	    return status;
	}
	num_atts++;

	// Add an 8859-2 attachment
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddattachment(mtmessage_obj, 
	    X400_att.X400_T_ISO8859_1,
            config.mt_msg_cnt_8859_2,
            config.mt_msg_cnt_8859_2.length(), config.mt_msg_cnt_emptybinarydata);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddattachment failed " + status);
	    return status;
	}
	num_atts++;

	// add a forwarded msg attachment
	// instantiate a message object putting it into an API object 
	Message fwd_message_obj = new Message();
	String fwd_recip_oraddress = config.fwd_recip_oraddress;
	//"/CN=lppt2/OU=lppt/O=" 
	    //+ config.hostname + "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";

	System.out.println("Building message for message bodypart");
	status = X400BuildFwdMsg.build_fwd_msg(fwd_message_obj, 
	    fwd_recip_oraddress);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("build_fwd_msg failed " + status);
	    return status;
	}

	// add the X400Message as a bodypart to the X400mtMessage
	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddmessagebody(
	    mtmessage_obj, fwd_message_obj); 
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_addmessagebody failed " + status);
	    return status;
	} 
	num_atts++;

        // Add an bin attachment
        status = com.isode.x400mtapi.X400mt.x400_mt_msgaddattachment(
            mtmessage_obj, X400_att.X400_T_BINARY, config.mt_msg_cnt_bin,
            config.mt_msg_cnt_bin.length(), config.mt_msg_cnt_binarydata);
        if (status != X400_att.X400_E_NOERROR) {
            System.out.println("x400_mt_msgaddattachment failed (bin)" + status);
            return status;
        }
        num_atts++;


        /* Now add P772 bodyparts */
        if (config.mt_use_p772 == true) {
            status = add_adatp3 (mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            num_atts++;

            status = add_corrections (mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            num_atts++;
            
            status = add_acp127data (mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            num_atts++;

            status = add_fwd_enc (mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            num_atts++;
            
            
            status = add_fwd_MM (mtmessage_obj);
            if (status != X400_att.X400_E_NOERROR)
                return status;
            num_atts++;
            
        }

        status = add_x420_fwd_content (mtmessage_obj);
        if (status != X400_att.X400_E_NOERROR)
            return status;
        num_atts++;

        
	// record number of attachments
    	status = com.isode.x400mtapi.X400mt.x400_mt_msgaddintparam(mtmessage_obj, 
	    X400_att.X400_N_NUM_ATTACHMENTS, num_atts);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_mt_msgaddintparam failed " + status);
	    return status;
	}

	/************* The ACP127 Notification Response is only present in a Military Notification ********
	status = add_acp127resp(mtmessage_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("add_acp127resp " + status);
	    return status;
	}
        ***************************/

	return status;

    }
}

