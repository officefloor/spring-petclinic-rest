package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is now
 * expressed through: the lower-case hex SHA-256 of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 *
 * <p>The components are taken from the owner as already canonicalised earlier in the create
 * pipeline: the telephone in E.164 form (see {@link TelephoneE164}), the email lower-cased, and the
 * surname reduced to its {@link Soundex} code. Because the telephone is part of the key, two members
 * of the same household (same {@code soundex(lastName)} and postcode) with different telephones have
 * different identity keys; only an exact whole-key match is a duplicate. The computed
 * {@code householdId} is deliberately no longer part of the key — duplicate detection is this single
 * identity key, and a same-surname/same-postcode owner with a different telephone is a soft match
 * (see {@link AssignPossibleDuplicate}), not a hard household duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = (owner.getEmail() == null || owner.getEmail().isBlank())
                ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String soundex = Soundex.of(owner.getLastName());
        return sha256hex(telephone + "|" + email + "|" + soundex);
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
