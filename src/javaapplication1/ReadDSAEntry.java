/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication1;

/* 
 * Copyright (c) 2023, Galadrium Limited, UK 
 * www.galadrium.com
 *  
 * All rights reserved.
 */
import com.isode.dsapi.Attribute;
import com.isode.dsapi.AttributeType;
import com.isode.dsapi.BadAttributeTypeException;
import com.isode.dsapi.DN;
import com.isode.dsapi.DSAPIException;
import com.isode.dsapi.DSapi;
import com.isode.dsapi.DirectorySession;
import com.isode.dsapi.Entry;
import com.isode.dsapi.Indication;
import com.isode.dsapi.NativeLibraryException;
import com.isode.dsapi.NoSuchAttributeException;
import com.isode.dsapi.Selection;

public class ReadDSAEntry {

    // Configurable
    //private static String dsaPresentationAddress = "Internet=localhost+19999"; 
    private static final String dsaPresentationAddress = "Internet=192.168.22.199+19999";
    private static String entryDNToRead = "cn=VVTSMHSA,cn=White Pages,o=messaging";
    
    
    ///CN=EDGGZQZX/OU=EDGG/O=EDGG/PRMD=BELGIUM/ADMD=ICAO/C=XX/
    //private static String entryDNToRead = "cn=White Pages,o=messaging";
    //private static String entryDNToRead = "cn=AYPYANGM,o=AYPY,o=SITA,cn=OPER255,cn=ICAO-MD-Register";
    //private static String entryDNToRead = "cn=LOOOMHSX,o=LOOO,c=AT,ou=Operational,o=European-Directory";

    // Private static variables
    private static DirectorySession ds;

    // Attributes - should be better defined in a separate global class
    // X.400
    public static AttributeType mhsORAddrAttr = null;
    public static AttributeType surnameAttr = null;
    public static AttributeType givenNameAttr = null;
    public static AttributeType commonNameAttr = null;
    public static AttributeType organizationalUnitAttr = null;
    public static AttributeType descriptionAttr = null;
    // Common
    public static AttributeType mhsMaxLengthAttr = null;
    // ATN Directory - Doc 9880 Edition 2 - November 2014
    public static AttributeType atnAFAddressAttr = null;
    public static AttributeType atnPerCertificateAttr = null;
    public static AttributeType atnDerCertificateAttr = null;
    public static AttributeType atnDirectAccessAttr = null;
    public static AttributeType atnFacilityNameAttr = null;
    public static AttributeType atnAircraftIDNameAttr = null;
    public static AttributeType atnVersionAttr = null;
    public static AttributeType atnIPMHeadingExtensionsAttr = null;
    public static AttributeType atnGlobalDomainIdentifierAttr = null;
    public static AttributeType atnICAODesignatorAttr = null;
    public static AttributeType atnNetAttr = null;
    public static AttributeType atnAddressingSchemeAttr = null;
    public static AttributeType atnNamingContextAttr = null;
    public static AttributeType atnMaximumNumberOfBodyPartsAttr = null;
    public static AttributeType atnMaximumTextSizeAttr = null;
    public static AttributeType atnMaximumFileSizeAttr = null;
    public static AttributeType atnUseOfAMHSSecurityAttr = null;
    public static AttributeType atnUseOfDirectoryAttr = null;
    public static AttributeType atnGroupOfAddressesAttr = null;
    public static AttributeType mhsExclusivelyAcceptableEitsAttr = null;
    public static AttributeType mhsDeliverableContentLength = null;
    

    public static void main(String[] args) {

        try {
            // Initialize DSAPI. This is always the first thing to do with DSAPI
            System.out.println("Initializing DSAPI");
            DSapi.initialize();

            // Initialize the attributes
            initATNDirectoryAttributes();

            // Create a Directory session with DAP
            ds = new DirectorySession(dsaPresentationAddress);

            // Bind Anonymously
            System.out.println("Binding anonymously...");
            ds.bind(null);
            System.out.println("Bound");

            shownEntryAttributes(entryDNToRead);

        } catch (DSAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void shownEntryAttributes(String entryDN) throws NullPointerException, DSAPIException {
        // Create a DN object
        DN dn = new DN(entryDN);

        // Read the DN into an Entry				
        System.out.println("Reading the Entry " + entryDNToRead);
        Entry e = getEntry(dn);
        System.out.println("Entry " + entryDNToRead + " successfully read");

        // Show the attributes
        System.out.println("Show some attributes of the entry:\n");
        showSomeAttributes(e);
    }

    /**
     * Initialize the DSA ATN Directory Attributes, which could be used by this
     * program
     *
     * @throws NativeLibraryException
     * @throws BadAttributeTypeException
     */
    public static void initATNDirectoryAttributes() throws BadAttributeTypeException, NativeLibraryException {
        // ATN Directory - Doc 9880 Edition 2 - November 2014
        atnAFAddressAttr = new AttributeType("atn-AF-address");
        atnPerCertificateAttr = new AttributeType("atn-per-certificate");
        atnDerCertificateAttr = new AttributeType("atn-der-certificate");
        atnDirectAccessAttr = new AttributeType("atn-amhs-direct-access");
        atnFacilityNameAttr = new AttributeType("atn-facility-name");
        atnAircraftIDNameAttr = new AttributeType("atn-aircraftIDName");
        atnVersionAttr = new AttributeType("atn-version");
        atnIPMHeadingExtensionsAttr = new AttributeType("atn-ipm-heading-extensions");
        atnGlobalDomainIdentifierAttr = new AttributeType("atn-global-domain-identifier");
        atnICAODesignatorAttr = new AttributeType("atn-icao-designator");
        atnNetAttr = new AttributeType("atn-net");
        atnAddressingSchemeAttr = new AttributeType("atn-amhs-addressing-scheme");
        //atnNamingContextAttr = new AttributeType("atn-amhsMD-naming-context"); 				
        atnMaximumNumberOfBodyPartsAttr = getAttributeType("atn-maximum-number-of-body-parts");
        atnMaximumTextSizeAttr = getAttributeType("atn-maximum-text-size");
        atnMaximumFileSizeAttr = getAttributeType("atn-maximum-file-size");
        atnUseOfAMHSSecurityAttr = getAttributeType("atn-use-of-amhs-security");
        atnUseOfDirectoryAttr = getAttributeType("atn-use-of-directory");
        atnGroupOfAddressesAttr = getAttributeType("atn-group-of-addresses");
         mhsDeliverableContentLength = getAttributeType("mhsDeliverableContentLength");
        System.out.println("Loaded the ATN Directory attributes");
    }

    /**
     * Get the AttributeType for the attrName or return null if it is not found
     *
     * @param attrName
     * @return
     */
    private static AttributeType getAttributeType(String attrName) {
        try {
            return new AttributeType(attrName);
        } catch (BadAttributeTypeException | NativeLibraryException e) {
            return null;
        }
    }

    /**
     *
     * @param en	Entry to inspect
     * @param at	Attribute to search
     *
     * @return	Returns the attribute "at" from the entry "en" or null if the
     * attribute doesn't exist
     * @throws NoSuchAttributeException
     */
    public static Attribute getEntryAttribute(Entry en, AttributeType at) throws NoSuchAttributeException {
        if (en == null || at == null) {
            return null;
        }
        return en.getAttribute(at);
    }

    private static String getAttributeValue(Entry e, AttributeType at, boolean ignore_noattr) throws NativeLibraryException, DSAPIException {
        try {
            Attribute a = getEntryAttribute(e, at);
            if (a == null) {
                return null;
            } else {
                if (a.getValueCount() != 1) {
                    throw new DSAPIException(">>> POTENTIAL PROBLEM WITH ATTRIBUTE VALUES FOR " + at.getAttributeTypeName() + " <<<");
                }
                return a.getValue(0).toString();
            }
        } catch (NoSuchAttributeException e1) {
            if (ignore_noattr) {
                return null;
            } else {
                throw e1;
            }
        }
    }

    /**
     * Read the given DN and return an Entry object.
     *
     * @param dn	DN object to use
     * @return	Returns the Entry object that is associated with the provided DN,
     * if it exist in the DSA
     * @throws DSAPIException
     */
    public static Entry getEntry(DN dn) throws DSAPIException {

        if (ds == null) {
            return null;
        }

        int entry_num;

        Selection sel = new Selection();
        sel.selectAllUser();
        Indication children_ind = null;

        children_ind = ds.read(dn, sel, null);

        if (children_ind == null) {
            return null;
        }

        entry_num = children_ind.getEntryCount();
        if (entry_num == 0) {
            return null;
        } else {
            return children_ind.getEntry(0);
        }
    }

    private static void showSomeAttributes(Entry e) throws DSAPIException {
        
        System.out.println("atnIPMHeadingExtensionsAttr = " + getAttributeValue(e, atnIPMHeadingExtensionsAttr, true));
        
        System.out.println("AFTN Address = " + getAttributeValue(e, atnAFAddressAttr, true));
        String s = getAttributeValue(e, atnIPMHeadingExtensionsAttr,true);
        System.out.println("atnDirectAccessAttr = " + getAttributeValue(e, atnDirectAccessAttr, true));
        
        System.out.println("atnMaximumNumberOfBodyPartsAttr = " + getAttributeValue(e, atnMaximumNumberOfBodyPartsAttr, true));
        System.out.println("atnMaximumTextSizeAttr = " + getAttributeValue(e, atnMaximumTextSizeAttr, true));
        System.out.println("atnMaximumFileSizeAttr = " + getAttributeValue(e, atnMaximumFileSizeAttr, true));
        System.out.println("atnUseOfAMHSSecurityAttr = " + getAttributeValue(e, atnUseOfAMHSSecurityAttr, true));
        System.out.println("atnUseOfDirectoryAttr = " + getAttributeValue(e, atnUseOfDirectoryAttr, true));
        System.out.println("-----------------");
        System.out.println("mshDeliverableContentLength = " + getAttributeValue(e, mhsDeliverableContentLength, true));
    }

}
