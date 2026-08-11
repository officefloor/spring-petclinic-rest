package org.springframework.samples.petclinic.mapper;

/**
 * American Soundex phonetic code of a surname, used by the owner identity key and the soft
 * duplicate match. Two surnames that sound alike share a code, so duplicate detection groups
 * likely-same households regardless of spelling. Kept out of {@link OwnerMapper} so MapStruct
 * does not mistake the helper for an implicit mapping method.
 *
 * <p>Delegates to Apache Commons Codec's US-English {@code Soundex}, returning an empty string
 * for a null/blank name or one with no encodable letters (rather than throwing), so callers can
 * treat it as a plain segment.
 */
public final class Soundex {

    private static final org.apache.commons.codec.language.Soundex ENCODER =
            org.apache.commons.codec.language.Soundex.US_ENGLISH;

    private Soundex() {
    }

    /** The American Soundex code of {@code lastName}, or an empty string when it has no letters. */
    public static String of(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return "";
        }
        try {
            return ENCODER.soundex(lastName);
        }
        catch (IllegalArgumentException ex) {
            return ""; // no encodable letters — treat as an empty phonetic segment
        }
    }
}
