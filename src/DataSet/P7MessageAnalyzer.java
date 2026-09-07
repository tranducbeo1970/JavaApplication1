/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataSet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Paths;

/**
 * Experimental P7/AMHS stored-message analyzer.
 *
 * No third-party libraries are required.
 * Usage:
 *   javac P7MessageAnalyzer.java
 *   java P7MessageAnalyzer <binary-file>
 */
public final class P7MessageAnalyzer {

    private static final Pattern ATS_ADDRESS = Pattern.compile("^[A-Z]{8}$");
    private static final Pattern PRI_PATTERN = Pattern.compile("(?:^|\\r?\\n)PRI:\\s*([A-Z]{2})(?:\\r?\\n|$)");
    private static final Pattern FT_PATTERN = Pattern.compile("(?:^|\\r?\\n)FT:\\s*([0-9]{6})(?:\\r?\\n|$)");
    private static final Pattern OHI_PATTERN = Pattern.compile("(?:^|\\r?\\n)OHI:\\s*(.*?)(?:\\r?\\n|$)");

    private P7MessageAnalyzer() {}

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java P7MessageAnalyzer <p7-binary-file>");
            System.exit(2);
        }

        try {
            Analysis a = analyze(Paths.get(args[0]));
            print(a);
        } catch (Exception e) {
            System.err.println("Cannot analyze file: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static Analysis analyze(Path file) throws IOException {
        byte[] data = Files.readAllBytes(file);
        BerNode root = BerReader.parseOne(data, 0, data.length);

        if (!root.isUniversal(16) || !root.constructed) {
            throw new IllegalArgumentException("Root object is not ASN.1 SEQUENCE");
        }
        if (root.children.size() < 3) {
            throw new IllegalArgumentException("Unexpected stored-entry structure");
        }

        Analysis out = new Analysis();
        out.fileName = file.getFileName().toString();
        out.fileSize = data.length;

        BerNode seq = root.children.get(0);
        if (seq.isUniversal(2)) {
            out.sequenceNumber = decodeInteger(seq.value);
        }

        // Observed stored-entry discriminator:
        // [0] constructed = delivered message
        // [1] constructed = delivered report
        // [2] constructed = submitted message
        BerNode entryNode = null;
        BerNode encodedContentNode = null;
        for (int i = 2; i < root.children.size(); i++) {
            BerNode n = root.children.get(i);
            if (n.tagClass == TagClass.CONTEXT && n.constructed) {
                if (entryNode == null) entryNode = n;
            } else if (n.tagClass == TagClass.CONTEXT && !n.constructed && n.tagNumber == 2) {
                encodedContentNode = n;
            }
        }

        if (entryNode == null) {
            out.entryType = "UNKNOWN";
        } else if (entryNode.tagNumber == 0) {
            out.entryType = "DELIVERED";
            out.thisRecipient = extractDeliveredThisRecipient(entryNode);
        } else if (entryNode.tagNumber == 1) {
            out.entryType = "REPORT";
        } else if (entryNode.tagNumber == 2) {
            out.entryType = "SUBMITTED";
        } else {
            out.entryType = "UNKNOWN_CONTEXT_" + entryNode.tagNumber;
        }

        // Message content is stored as context-specific primitive [2]. Its value
        // is itself a BER-encoded X.420 InformationObject.
        if (encodedContentNode != null && encodedContentNode.value.length > 0) {
            try {
                BerNode informationObject = BerReader.parseOne(
                        encodedContentNode.value, 0, encodedContentNode.value.length);
                extractX420(informationObject, out);
            } catch (RuntimeException e) {
                out.warnings.add("Content [2] exists but nested ASN.1 could not be parsed: " + e.getMessage());
            }
        }

        if (out.atsRawBody != null) {
            parseBasicAtsBody(out);
        }

        if ("DELIVERED".equals(out.entryType) && out.thisRecipient == null) {
            out.warnings.add("Delivered entry detected but this-recipient could not be extracted");
        }
        if (out.originator == null && !"REPORT".equals(out.entryType)) {
            out.warnings.add("Originator not found in X.420 heading");
        }
        if (out.recipients.isEmpty() && !"REPORT".equals(out.entryType)) {
            out.warnings.add("No primary recipients found in X.420 heading");
        }

        return out;
    }

    private static void extractX420(BerNode informationObject, Analysis out) {
        // Observed X.420 InformationObject ::= CHOICE { ipm [0] IPM, ... }
        if (!(informationObject.tagClass == TagClass.CONTEXT
                && informationObject.tagNumber == 0
                && informationObject.constructed)) {
            out.warnings.add("Nested content is not an observed X.420 IPM [0]");
            return;
        }

        BerNode heading = firstDirectChild(informationObject, TagClass.UNIVERSAL, 17, true); // SET
        BerNode body = firstDirectChild(informationObject, TagClass.UNIVERSAL, 16, true);    // SEQUENCE

        if (heading != null) {
            BerNode originatorField = firstDirectChild(heading, TagClass.CONTEXT, 0, true);
            out.originator = firstAtsAddress(originatorField);

            BerNode primaryRecipients = firstDirectChild(heading, TagClass.CONTEXT, 2, true);
            if (primaryRecipients != null) {
                LinkedHashSet<String> addresses = new LinkedHashSet<>();
                collectAtsAddresses(primaryRecipients, addresses);
                out.recipients.addAll(addresses);
            }

            // In our current corpus originators-reference is carried as a heading
            // extension under context [15], with a BMPString value.
            BerNode extensions = firstDirectChild(heading, TagClass.CONTEXT, 15, true);
            if (extensions != null) {
                BerNode bmp = firstDescendant(extensions, TagClass.UNIVERSAL, 30); // BMPString
                if (bmp != null) {
                    out.originatorsReference = decodeBmpString(bmp.value);
                }
            }
        }

        if (body != null) {
            BerNode ia5 = firstDescendant(body, TagClass.UNIVERSAL, 22); // IA5String
            if (ia5 != null) {
                out.atsRawBody = new String(ia5.value, StandardCharsets.US_ASCII);
            }
        }
    }

    private static String extractDeliveredThisRecipient(BerNode deliveryEnvelope) {
        // Observed X.411 MessageDeliveryEnvelope/OtherMessageDeliveryFields:
        // direct context [4] carries this-recipient-name.
        BerNode n = firstDescendantAtShallowest(deliveryEnvelope, TagClass.CONTEXT, 4, true);
        return firstAtsAddress(n);
    }

    private static void parseBasicAtsBody(Analysis out) {
        String raw = out.atsRawBody;
        String visible = stripLeadingControl(raw, '\u0001'); // SOH

        Matcher pri = PRI_PATTERN.matcher(visible);
        Matcher ft = FT_PATTERN.matcher(visible);
        Matcher ohi = OHI_PATTERN.matcher(visible);

        if (pri.find()) {
            out.serviceLevel = "BASIC";
            out.atsPriority = pri.group(1);
        } else {
            // We intentionally do not claim EXTENDED until an Extended corpus is tested.
            out.serviceLevel = "UNKNOWN";
        }

        if (ft.find()) out.filingTime = ft.group(1);
        if (ohi.find()) out.optionalHeadingInfo = ohi.group(1);

        int stx = raw.indexOf('\u0002');
        if (stx >= 0) {
            out.content = raw.substring(stx + 1);
            while (out.content.startsWith("\r") || out.content.startsWith("\n")) {
                out.content = out.content.substring(1);
            }
        } else {
            out.content = raw;
        }
    }

    private static String stripLeadingControl(String s, char c) {
        return !s.isEmpty() && s.charAt(0) == c ? s.substring(1) : s;
    }

    private static String firstAtsAddress(BerNode node) {
        if (node == null) return null;
        List<String> strings = new ArrayList<>();
        collectPrintableStrings(node, strings);
        for (String s : strings) {
            if (ATS_ADDRESS.matcher(s).matches()) return s;
        }
        return null;
    }

    private static void collectAtsAddresses(BerNode node, Set<String> out) {
        if (node == null) return;
        if (node.isUniversal(19)) { // PrintableString
            String s = new String(node.value, StandardCharsets.US_ASCII);
            if (ATS_ADDRESS.matcher(s).matches()) out.add(s);
        }
        for (BerNode child : node.children) collectAtsAddresses(child, out);
    }

    private static void collectPrintableStrings(BerNode node, List<String> out) {
        if (node == null) return;
        if (node.isUniversal(19)) {
            out.add(new String(node.value, StandardCharsets.US_ASCII));
        }
        for (BerNode child : node.children) collectPrintableStrings(child, out);
    }

    private static BerNode firstDirectChild(BerNode n, TagClass cls, int tag, boolean constructed) {
        if (n == null) return null;
        for (BerNode c : n.children) {
            if (c.tagClass == cls && c.tagNumber == tag && c.constructed == constructed) return c;
        }
        return null;
    }

    private static BerNode firstDescendant(BerNode n, TagClass cls, int tag) {
        if (n == null) return null;
        if (n.tagClass == cls && n.tagNumber == tag) return n;
        for (BerNode c : n.children) {
            BerNode x = firstDescendant(c, cls, tag);
            if (x != null) return x;
        }
        return null;
    }

    private static BerNode firstDescendantAtShallowest(BerNode n, TagClass cls, int tag, boolean constructed) {
        if (n == null) return null;
        List<BerNode> level = new ArrayList<>();
        level.add(n);
        while (!level.isEmpty()) {
            List<BerNode> next = new ArrayList<>();
            for (BerNode x : level) {
                if (x != n && x.tagClass == cls && x.tagNumber == tag && x.constructed == constructed) {
                    return x;
                }
                next.addAll(x.children);
            }
            level = next;
        }
        return null;
    }

    private static long decodeInteger(byte[] v) {
        if (v.length == 0 || v.length > 8) return -1;
        long x = (v[0] & 0x80) != 0 ? -1L : 0L;
        for (byte b : v) x = (x << 8) | (b & 0xffL);
        return x;
    }

    private static String decodeBmpString(byte[] v) {
        if ((v.length & 1) != 0) return "<invalid BMPString>";
        return new String(v, StandardCharsets.UTF_16BE);
    }

    private static String escapeControls(String s) {
        if (s == null) return null;
        return s.replace("\u0001", "<SOH>")
                .replace("\u0002", "<STX>")
                .replace("\r", "\\r")
                .replace("\n", "\\n\n");
    }

    private static void print(Analysis a) {
        System.out.println("============================================");
        System.out.println("P7 / AMHS STORED MESSAGE ANALYZER");
        System.out.println("============================================");
        System.out.println("File             : " + a.fileName);
        System.out.println("File size        : " + a.fileSize + " bytes");
        System.out.println("Sequence number  : " + value(a.sequenceNumber));
        System.out.println("Entry type       : " + value(a.entryType));
        System.out.println("Service level    : " + value(a.serviceLevel));
        System.out.println();
        System.out.println("Originator       : " + value(a.originator));
        System.out.println("Recipients       : " + (a.recipients.isEmpty() ? "<not found>" : String.join(", ", a.recipients)));
        System.out.println("This recipient   : " + value(a.thisRecipient));
        System.out.println();
        System.out.println("ATS priority     : " + value(a.atsPriority));
        System.out.println("Filing time      : " + value(a.filingTime));
        System.out.println("OHI              : " + value(a.optionalHeadingInfo));
        System.out.println("Originator ref   : " + value(a.originatorsReference));
        System.out.println();
        System.out.println("ATS content:");
        System.out.println("--------------------------------------------");
        System.out.println(a.content == null ? "<not found>" : a.content);
        System.out.println("--------------------------------------------");

        if (!a.warnings.isEmpty()) {
            System.out.println();
            System.out.println("Warnings:");
            for (String w : a.warnings) System.out.println(" - " + w);
        }
    }

    private static String value(Object x) {
        return x == null ? "<not found>" : String.valueOf(x);
    }

    public static final class Analysis {
        public String fileName;
        public int fileSize;
        public long sequenceNumber = -1;
        public String entryType;
        public String serviceLevel;
        public String originator;
        public final List<String> recipients = new ArrayList<>();
        public String thisRecipient;
        public String atsPriority;
        public String filingTime;
        public String optionalHeadingInfo;
        public String originatorsReference;
        public String atsRawBody;
        public String content;
        public final List<String> warnings = new ArrayList<>();
    }

    private enum TagClass { UNIVERSAL, APPLICATION, CONTEXT, PRIVATE }

    private static final class BerNode {
        final TagClass tagClass;
        final boolean constructed;
        final int tagNumber;
        final int offset;
        final int headerLength;
        final int contentLength;
        final byte[] value;
        final List<BerNode> children;

        BerNode(TagClass tagClass, boolean constructed, int tagNumber,
                int offset, int headerLength, int contentLength,
                byte[] value, List<BerNode> children) {
            this.tagClass = tagClass;
            this.constructed = constructed;
            this.tagNumber = tagNumber;
            this.offset = offset;
            this.headerLength = headerLength;
            this.contentLength = contentLength;
            this.value = value;
            this.children = children;
        }

        boolean isUniversal(int tag) {
            return tagClass == TagClass.UNIVERSAL && tagNumber == tag;
        }
    }

    private static final class BerReader {
        static BerNode parseOne(byte[] data, int start, int available) {
            ParseResult r = parse(data, start, start + available);
            if (r.nextOffset > start + available) {
                throw new IllegalArgumentException("BER object exceeds available data");
            }
            return r.node;
        }

        private static ParseResult parse(byte[] data, int p, int limit) {
            int start = p;
            if (p >= limit) throw new IllegalArgumentException("Missing BER tag");

            int first = data[p++] & 0xff;
            TagClass cls = TagClass.values()[(first >>> 6) & 0x03];
            boolean constructed = (first & 0x20) != 0;
            int tag = first & 0x1f;

            if (tag == 0x1f) {
                tag = 0;
                int count = 0;
                int b;
                do {
                    if (p >= limit) throw new IllegalArgumentException("Truncated high-tag-number");
                    b = data[p++] & 0xff;
                    if (count++ > 4) throw new IllegalArgumentException("Tag number too large");
                    tag = (tag << 7) | (b & 0x7f);
                } while ((b & 0x80) != 0);
            }

            if (p >= limit) throw new IllegalArgumentException("Missing BER length");
            int lb = data[p++] & 0xff;
            int len;
            if ((lb & 0x80) == 0) {
                len = lb;
            } else {
                int n = lb & 0x7f;
                if (n == 0) throw new IllegalArgumentException("Indefinite BER length not supported yet");
                if (n > 4 || p + n > limit) throw new IllegalArgumentException("Invalid BER length");
                len = 0;
                for (int i = 0; i < n; i++) len = (len << 8) | (data[p++] & 0xff);
            }

            int headerLen = p - start;
            int valueStart = p;
            int valueEnd = valueStart + len;
            if (valueEnd < valueStart || valueEnd > limit) {
                throw new IllegalArgumentException("BER value exceeds enclosing object at offset " + start);
            }

            byte[] value = new byte[len];
            System.arraycopy(data, valueStart, value, 0, len);

            List<BerNode> children = new ArrayList<>();
            if (constructed) {
                int q = valueStart;
                while (q < valueEnd) {
                    ParseResult child = parse(data, q, valueEnd);
                    if (child.nextOffset <= q) throw new IllegalStateException("BER parser made no progress");
                    children.add(child.node);
                    q = child.nextOffset;
                }
                if (q != valueEnd) throw new IllegalArgumentException("Constructed BER length mismatch");
            }

            BerNode node = new BerNode(cls, constructed, tag, start, headerLen, len, value, children);
            return new ParseResult(node, valueEnd);
        }
    }

    private static final class ParseResult {
        final BerNode node;
        final int nextOffset;
        ParseResult(BerNode node, int nextOffset) {
            this.node = node;
            this.nextOffset = nextOffset;
        }
    }
}
