package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Response flag: true when this owner is a possible duplicate, its email domain is
 * disposable-adjacent, or its city is over its soft capacity; otherwise false. Recomputed at
 * response time from the same sources the individual create-time rules use.
 */
final class RiskFlag {

    /** Base labels of the disposable domains {@link EnsureOwnerEmailNotDisposable} blocks; a domain
     *  carrying one of these labels (e.g. a different TLD or a subdomain) is disposable-adjacent. */
    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    /** Soft city limit: below the hard capacity of 50 the per-city rule rejects at. */
    private static final int SOFT_CAPACITY = 40;

    private RiskFlag() {
    }

    static boolean isActive(Owner owner, OwnerRepository ownerRepository) {
        return PossibleDuplicate.of(owner, ownerRepository) != null
                || hasDisposableAdjacentEmail(owner)
                || cityIsOverSoftCapacity(owner, ownerRepository);
    }

    private static boolean cityIsOverSoftCapacity(Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count >= SOFT_CAPACITY;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static boolean hasDisposableAdjacentEmail(Owner owner) {
        String email = owner.getEmail();
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        for (String label : email.substring(at + 1).toLowerCase().split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
