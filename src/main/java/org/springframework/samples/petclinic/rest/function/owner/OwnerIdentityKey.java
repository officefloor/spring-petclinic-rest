package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived identity of an owner used for duplicate detection.
 *
 * <p>An owner's {@code identityKey} is the lower-case hex {@code SHA-256} of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where the telephone is
 * its E.164 form, the email is its lower-cased form (empty when absent or blank) and the last name
 * is reduced to its {@link Soundex} code. Two owners are duplicates only when their whole identity
 * keys are equal — so, for example, two people with the same (phonetic) last name and postcode but
 * different telephones have different keys and are both allowed, surfacing instead as a soft
 * possible-duplicate.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /**
     * Builds an identity key from its already-normalized parts.
     *
     * @param telephoneE164 the E.164 telephone (never blank for a valid owner)
     * @param email the email, or {@code null}/blank when none
     * @param lastName the owner's last name, reduced to its Soundex code
     * @return the 64-character lower-case hex SHA-256 identity key
     */
    public static String of(String telephoneE164, String email, String lastName) {
        String tel = telephoneE164 == null ? "" : telephoneE164;
        String mail = (email == null || email.trim().isEmpty()) ? ""
                : email.trim().toLowerCase(Locale.ROOT);
        String seed = tel + "|" + mail + "|" + Soundex.encode(lastName);
        return sha256hex(seed);
    }

    /**
     * Derives the identity key of a stored owner, normalizing its telephone to E.164 and its
     * email to lower case exactly as the create pipeline normalizes an incoming request, so a
     * new owner and an existing owner are compared on the same footing.
     */
    public static String forOwner(Owner owner) {
        return of(TelephoneNormalizer.toE164(owner.getTelephone()), owner.getEmail(),
                owner.getLastName());
    }

    private static String sha256hex(String seed) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
