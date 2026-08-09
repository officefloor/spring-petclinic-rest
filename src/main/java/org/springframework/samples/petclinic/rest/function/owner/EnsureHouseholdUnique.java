package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner that would silently join an existing household at the same residence.
 * The {@code householdId} is deterministic — computed from {@code (normalizedLastName, postcode)} by
 * {@link AssignHouseholdId} — but a postcode alone can be shared coincidentally by unrelated cities
 * (an unknown-region city accepts any 4-digit postcode), so a shared {@code householdId} on its own
 * does not prove a shared residence. A second owner is treated as a household duplicate only when it
 * shares both the {@code householdId} <em>and</em> the same city as an existing member — same surname,
 * same postcode, same city — and is then rejected with 409 (Conflict) via
 * {@link DuplicateHouseholdException}. Same surname and postcode but a different city are distinct
 * residences and are admitted (their {@code membershipLevel} is then capped against any true household
 * member by {@link CapMembershipLevel}).
 *
 * <p>The request may acknowledge the shared household by setting {@code sharesHousehold} true; that
 * bypasses this block and the owner is created as a declared household member (and, being declared, is
 * not flagged as a possible duplicate by {@link AssignPossibleDuplicate}). Owners with no postcode have
 * no {@code householdId} and so are never household duplicates.
 *
 * <p>This is distinct from {@link EnsureOwnerIdentityUnique}, which rejects an exact identity match
 * (same telephone, email and household) regardless of {@code sharesHousehold}.
 *
 * <p>Runs after {@link AssignHouseholdId} (so the {@code householdId} is populated) and before
 * {@link SaveOwner}, so a duplicate is a 409 rather than a persisted row.
 */
public class EnsureHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // acknowledged: create as a declared household member
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return; // no household to clash with
        }
        String city = normalizeCity(owner.getCity());
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is treated as absent
            }
            if (householdId.equals(existing.getHouseholdId())
                    && city.equals(normalizeCity(existing.getCity()))) {
                throw new DuplicateHouseholdException(householdId);
            }
        }
    }

    /** Trim and lower-case the city so residence comparison ignores casing and surrounding space. */
    private static String normalizeCity(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
