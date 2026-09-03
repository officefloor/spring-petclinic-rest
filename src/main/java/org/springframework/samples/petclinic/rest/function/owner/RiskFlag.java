package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Read-time risk indicator for an owner. {@link #of} is true when any of these hold:
 * the owner is a possible duplicate, its email domain is disposable-adjacent, or its
 * city is over the soft capacity that drives the {@link CityCapacity#approaching
 * capacity warning}; otherwise false.
 */
public final class RiskFlag {

    /** Substrings of well-known disposable mail providers; a domain containing any is disposable-adjacent. */
    private static final Set<String> DISPOSABLE_TOKENS = Set.of(
            "mailinator", "guerrillamail", "yopmail", "10minutemail", "tempmail",
            "throwaway", "trashmail", "getnada", "sharklasers", "dispostable", "maildrop");

    private RiskFlag() {
    }

    public static boolean of(Owner owner, OwnerRepository ownerRepository) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || disposableAdjacent(owner.getEmail())
                || CityCapacity.approaching(owner.getCity(), ownerRepository);
    }

    private static boolean disposableAdjacent(String email) {
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return DISPOSABLE_TOKENS.stream().anyMatch(domain::contains);
    }
}
