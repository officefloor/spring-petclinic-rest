package org.springframework.samples.petclinic.mapper;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: true when the owner warrants review because any single
 * risk signal holds, otherwise false. The signals are:
 *
 * <ul>
 * <li>possible duplicate — {@code possibleDuplicate} true (a soft match on lastName + postcode,
 * see {@code AssignPossibleDuplicate});</li>
 * <li>disposable-adjacent email — the email domain shares a second-level name with a known
 * disposable-email provider (see {@link #DISPOSABLE_BASES}) without being on the hard blocklist
 * that {@code ValidateOwnerFields} rejects outright. This catches variants that slip past the
 * blocklist, e.g. a different TLD ({@code mailinator.net}) or a subdomain of a listed provider
 * ({@code mail.mailinator.com});</li>
 * <li>city over soft capacity — {@code capacityWarning} true (the city already held 40-49 owners,
 * i.e. it is at or past the soft threshold of 40 that precedes the hard capacity of 50, see
 * {@code AssignCapacityWarning} / {@code EnsureCityCapacity}).</li>
 * </ul>
 *
 * <p>Kept as a plain static helper (not a mapper method) so MapStruct does not treat it as an
 * implicit mapping method.
 */
public final class OwnerRisk {

    /**
     * Second-level names of the disposable-email providers on the create blocklist. An email whose
     * domain carries one of these as its second-level label is disposable-adjacent.
     */
    private static final Set<String> DISPOSABLE_BASES = Set.of("mailinator", "tempmail", "guerrillamail");

    private OwnerRisk() {
    }

    /** True when any risk signal holds for the owner. */
    public static boolean flag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || isDisposableAdjacent(owner.getEmail());
    }

    /** True when the email's domain shares a second-level name with a known disposable provider. */
    static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false; // no dotted domain, so no second-level name to compare
        }
        String secondLevel = labels[labels.length - 2];
        return DISPOSABLE_BASES.contains(secondLevel);
    }
}
