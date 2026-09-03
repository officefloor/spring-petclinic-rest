package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create whose whole {@code identityKey} already belongs to another owner,
 * before {@link SaveOwner} runs. Consolidates the former telephone, email and household
 * duplicate checks: only an exact full-key match is a duplicate, so two members of the
 * same household with different telephones are both allowed. Handled with 409 by
 * {@code DuplicateIdentityHandler}. Runs after {@link AssignHouseholdId} so the key can
 * include the household part.
 */
public class CheckUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String key = identityKey(owner);
        for (Owner other : ownerRepository.findAll()) {
            if (!other.isDeleted() && !other.getId().equals(owner.getId())
                    && key.equals(identityKey(other))) {
                throw new DuplicateIdentityException(key);
            }
        }
    }

    /** SHA-256 hex over version-2 region + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName). */
    public static String identityKey(Owner owner) {
        String raw = IdentityRegion.code(owner) + "|" + normalizeTelephone(owner.getTelephone())
                + "|" + lower(owner.getEmail()) + "|" + Soundex.code(owner.getLastName());
        return sha256Hex(raw);
    }

    private static String normalizeTelephone(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
