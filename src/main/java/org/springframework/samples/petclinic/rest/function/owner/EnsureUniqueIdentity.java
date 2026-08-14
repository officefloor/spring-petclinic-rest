package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate-detection step in the create-owner pipeline, consolidating what used to be
 * separate telephone, email and household checks. Runs after the telephone and email are normalized
 * and after {@link DeriveHouseholdId} has resolved any shared {@code householdId}.
 *
 * <p>It applies two 409 rules against the already-stored owners:
 * <ul>
 * <li><b>Identity duplicate</b> — the request's {@code identityKey}, derived exactly as
 * {@link Owner#getIdentityKey()} does ({@code normalizedTelephone|email|householdId}, email and
 * householdId empty when absent), equals an existing owner's whole key. This always rejects, even
 * with {@code sharesHousehold}.</li>
 * <li><b>Household duplicate</b> — the request's computed {@code householdId} (from lastName and
 * postcode) matches an existing owner's. Because the household is keyed on {@code (lastName,
 * postcode)}, a second owner in that household is a duplicate <em>unless</em> it declares
 * {@code sharesHousehold}, which admits it as a member.</li>
 * </ul>
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val HouseholdId householdId,
            OwnerRepository ownerRepository) throws DuplicateIdentityException {
        String householdIdValue = (householdId == null) ? null : householdId.value();
        String identityKey = identityKey(request, householdIdValue);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners no longer block a create
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
            // A household of one (no postcode) shares with no one; sharesHousehold admits a member.
            if (!sharesHousehold && householdIdValue != null
                    && householdIdValue.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdIdValue);
            }
        }
    }

    /** Derive the key for the owner being created, matching {@link Owner#getIdentityKey()} exactly. */
    private static String identityKey(OwnerFieldsDto request, String householdId) {
        String tel = request.getTelephone() == null ? "" : request.getTelephone();
        String email = request.getEmail();
        String mail = (email == null || email.isBlank()) ? "" : email;
        String household = (householdId == null || householdId.isBlank()) ? "" : householdId;
        return tel + "|" + mail + "|" + household;
    }
}
