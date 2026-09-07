/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SendIPN;



import com.isode.x400api.X400_att;
import com.isode.x400mtapi.MTMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ANDH
 */
public class MtCommon {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd.HHmmss");
    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyMMddHHmmss");
    private static final SimpleDateFormat filingTime = new SimpleDateFormat("ddHHmm");
    private static final SimpleDateFormat ipmDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");

    public static int getInt(MTMessage message, int att, Integer mode) {

        int status = com.isode.x400mtapi.X400mt.x400_mt_msggetintparam(message, att);

        if (status == X400_att.X400_E_NO_VALUE || status != X400_att.X400_E_NOERROR) {
            return 0;
        }

        return message.GetIntValue();
    }

    public static int getInt(MTMessage message, int att) {
        return getInt(message, att, 0);
    }

    public static List<List<String>> buildupAftnAddresses(List<String> addresses) {
        final List<List<String>> splitedAddresses = new ArrayList<>();
        List<String> adds = new ArrayList<>();
        int index = 0;
        for (String address : addresses) {
            if (address.isEmpty()) {
                continue;
            }
            // divide each batch contains 21 address
            if (index == 21) {
                splitedAddresses.add(adds);
                // create new address list
                adds = new ArrayList<>();
                adds.add(address);
                index = 1;
                continue;
            }

            adds.add(address);
            index++;
        }

        if (adds.size() > 0) {
            splitedAddresses.add(adds);
        }
        return splitedAddresses;
    }

    public static int priorityMapping(String priority) {
        if (priority == null) {
            return 4;
        }
        switch (priority) {
            case "SS":
                return 0;
            case "DD":
                return 1;
            case "FF":
                return 2;
            case "GG":
                return 3;
            case "KK":
                return 4;
            default:
                return 4;
        }
    }

    public static String generateMessageId() {
      //  final String hostname = Config.instance.getHostName();
      //  final String domain = Config.instance.getGlobalDomainID();
        final String hostname = "Config.instance.getHostName()";
       final String domain = "Config.instance.getGlobalDomainID()";
      
      Date date = new Date();
        int n = (int) (date.getTime() % 1000);
        return String.format("[%s;%s.%s-%s]", domain, hostname, n, dateFormat.format(date));
    }

    public static String generateIpmId(String originator) {
        final String time = ipmDateFormat.format(new Date());
        // String ipmFormat = "%s;%sZ*";
        final String ipmFormat = "%sZ*%s";
        return String.format(ipmFormat, time, originator);
    }

    public static String getReceiptTimeFromFilingTime(String filingTime) {
        final String patern = "yyMM" + filingTime + "00";
        final SimpleDateFormat frmt = new SimpleDateFormat(patern);
        return frmt.format(new Date()) + "Z";
    }

    public static String getAuthorizedTimeFromFilingTime(String filingTime) {
        final String patern = "yyyyMM" + filingTime + "00";
        final SimpleDateFormat frmt = new SimpleDateFormat(patern);
        return frmt.format(new Date()) + "Z";
    }

    public static String generateFilingTime(Date date) {
        return filingTime.format(date);
    }

    public static String generateFilingTime() {
        return generateFilingTime(new Date());
    }

    public static String generateArrivalTime() {
        return datetimeFormat.format(new Date()) + "Z";
    }

    private static String buildAddressIndicator(List<String> addresses) {
        final StringBuilder builder = new StringBuilder();
        for (String addresse : addresses) {
            builder.append(addresse).append(" ");
        }
        return builder.toString().trim();
    }
}
