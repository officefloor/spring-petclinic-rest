package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey} — a derived value returned on the
 * owner DTO and used for duplicate detection. The key is the lower-case hex SHA-256 (see
 * {@link Hashing}) of {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 *
 * <p>The inputs are normalized the same way for every owner so stored owners and the owner being
 * created compare consistently: the telephone to its E.164 form (see {@link OwnerTelephones}, so a
 * stored value that predates the pipeline still compares correctly), the email trimmed and
 * lower-cased (empty when absent), and the last name reduced to its {@link Soundex} code. Hard
 * duplicate detection (409) compares this key (see {@link RejectDuplicateIdentity}); the soft
 * signal keys on the weaker {@code soundex(lastName)} + {@code postcode} pair (see
 * {@link FlagPossibleDuplicate}). The key is exposed by the owner mapper.
 *
 * <p>Because the telephone is part of the key, two owners sharing a last name and postcode but
 * carrying different telephones produce <em>different</em> identity keys — they are a soft match,
 * not a hard duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's duplicate-detection identity key: SHA-256 hex over the normalized inputs. */
    public static String identityKey(Owner owner) {
        String canonical = normalizedTelephone(owner) + "|" + lowerEmail(owner) + "|" + Soundex.of(owner.getLastName());
        return Hashing.sha256Hex(canonical);
    }

    /**
     * The owner's telephone in its normalized E.164 form (see {@link OwnerTelephones#toE164}),
     * falling back to the stored raw value (or empty) when it cannot be normalized.
     */
    static String normalizedTelephone(Owner owner) {
        String telephone = OwnerTelephones.toE164(owner.getTelephone());
        if (telephone == null) {
            telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        return telephone;
    }

    /** The owner's email trimmed and lower-cased, or empty when absent. */
    static String lowerEmail(Owner owner) {
        String email = owner.getEmail();
        return (email == null || email.isBlank()) ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
