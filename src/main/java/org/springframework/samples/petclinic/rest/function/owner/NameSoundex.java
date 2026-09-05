package org.springframework.samples.petclinic.rest.function.owner;

import org.apache.commons.codec.language.Soundex;

/**
 * Computes the Soundex code of a name: the standard four-character phonetic encoding (an
 * initial letter followed by three digits) that maps similarly sounding surnames to the same
 * value. The owner {@code identityKey} folds {@code soundex(lastName)} into its hash (see
 * {@link OwnerIdentity}) and the soft-duplicate check compares surnames by this code (see
 * {@link AssignPossibleDuplicate}); centralising it here keeps that one primitive in one place.
 * It delegates to Apache Commons Codec's {@link Soundex} rather than re-implementing the
 * encoding. A blank or unencodable name yields the empty string.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class NameSoundex {

    private static final Soundex SOUNDEX = new Soundex();

    private NameSoundex() {
    }

    /**
     * @param name the name to encode; leading/trailing whitespace is ignored.
     * @return the four-character Soundex code, or the empty string when {@code name} is null,
     *         blank or contains no encodable letters.
     */
    public static String of(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        try {
            return SOUNDEX.soundex(name.trim());
        }
        catch (IllegalArgumentException ex) {
            return ""; // a name with no encodable letters contributes no phonetic segment
        }
    }
}
