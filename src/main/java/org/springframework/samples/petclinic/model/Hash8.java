package org.springframework.samples.petclinic.model;

/**
 * Derives the HASH8 segment of an owner's region-and-hash identity: the first 8 upper-case
 * hexadecimal characters of the {@link Sha256} digest over the concatenation of the owner's
 * normalized (E.164) telephone and lastName (e.g. {@code 1A2B3C4D}).
 *
 * <p>This is the one place the segment is computed, so every identity that embeds it (the
 * owner's {@code customerCode} today) reads the same value rather than re-deriving it. A
 * null telephone or lastName contributes an empty string to the concatenation.
 */
public final class Hash8 {

    private Hash8() {
    }

    /** The 8-character upper-case hex HASH8 segment for the given owner. */
    public static String of(Owner owner) {
        String source = nullToEmpty(owner.getTelephone()) + nullToEmpty(owner.getLastName());
        return Sha256.hex(source).substring(0, 8).toUpperCase();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
