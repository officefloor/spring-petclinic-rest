package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for the create-owner endpoint. It consolidates the former separate
 * telephone, email and household checks into one derived {@code identityKey}
 * ({@code normalizedTelephone|email|householdId}, see {@link OwnerIdentity}, also returned on the
 * owner) and rejects (409) when a new owner collides with an existing one on that identity.
 *
 * <p>The collision is driven by the normalized telephone (the leading, always-present component of
 * the key) together with the lower-cased email: two owners sharing both collide. Because the
 * telephone is part of the identity, two members of the same household (same {@code householdId})
 * with different telephones do <em>not</em> collide and are both allowed — a change from the former
 * household check, which rejected them regardless of telephone.
 *
 * <p>Runs after {@link ValidateOwnerFields} has normalized the telephone (E.164), email (lower-cased)
 * and address, and within the write transaction so the check and the insert see one consistent view.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String telephone = request.getTelephone();
        String email = normalizeEmail(request.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            boolean sameTelephone = telephone != null && telephone.equals(existing.getTelephone());
            boolean sameEmail = email.equals(normalizeEmail(existing.getEmail()));
            if (sameTelephone && sameEmail) {
                String householdId = OwnerIdentity.householdId(request.getLastName(), request.getAddress());
                throw new DuplicateIdentityException(
                        OwnerIdentity.identityKey(telephone, request.getEmail(), householdId));
            }
        }
    }

    /** Lower-cased email, or {@code ""} when absent/blank, so the comparison is case-insensitive. */
    private static String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
    }
}
