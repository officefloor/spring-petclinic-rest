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
 * <p>It derives the request's {@code identityKey} exactly as {@link Owner#getIdentityKey()} does —
 * {@code normalizedTelephone|email|householdId} (email and householdId empty when absent) — and
 * rejects with a 409 only when the WHOLE key equals an existing owner's. Because the telephone is
 * part of the key, two members of the same household (same {@code householdId}) with different
 * telephones have different identityKeys and are both allowed; only an exact full-key match is a
 * duplicate.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val HouseholdId householdId,
            OwnerRepository ownerRepository) throws DuplicateIdentityException {
        String householdIdValue = (householdId == null) ? null : householdId.value();
        String identityKey = identityKey(request, householdIdValue);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
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
