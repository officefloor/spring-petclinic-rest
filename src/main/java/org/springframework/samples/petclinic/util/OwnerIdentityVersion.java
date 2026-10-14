package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.rest.validation.OwnerPostcode;

/**
 * Version 2 of the owner identity. The identifiers (member id, household id and identity key) are derived
 * from the version-2 region code '&lt;VERSION_TAG&gt;&lt;REGION&gt;', e.g. 'V2NSW', so none of them repeats a
 * version-1 value. The tag appears only inside the identifiers; the plain region (e.g. 'NSW') is used for
 * the user-facing locality, timezone and owner segment.
 */
public final class OwnerIdentityVersion {

    /** The owner identity (and owner representation) version. */
    public static final int VERSION = 2;

    /** The fixed version tag mixed into the region code of every identifier. */
    public static final String VERSION_TAG = "V2";

    private OwnerIdentityVersion() {
    }

    /**
     * The version-2 region code of a plain region code, e.g. 'NSW' -&gt; 'V2NSW'.
     */
    public static String regionCode(String region) {
        return VERSION_TAG + region;
    }

    /**
     * The version-2 region code of the region derived by {@link OwnerPostcode#regionOf(String, String)}.
     */
    public static String regionCode(String postcode, String city) {
        return regionCode(OwnerPostcode.regionOf(postcode, city));
    }
}
