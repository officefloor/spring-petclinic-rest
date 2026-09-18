package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Runs in {@code POST /api/owners} after {@link ValidateOwnerFields}, reading the
 * already-normalized body as a variable. Consolidates the former separate telephone, email and
 * household duplicate checks into one: it derives the request's
 * {@link OwnerIdentity#key identity key} and rejects the request with a 409 only when that
 * <em>whole</em> key equals an existing owner's, before any entity is built or persisted.
 *
 * <p>Because the telephone is part of the key, two members of one household (same household id)
 * with different telephones have different keys and are both allowed; only an exact full-key
 * match is a duplicate. Reads the same transaction as the writes.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {

        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(),
                requestHouseholdId(request));

        for (Owner existing : ownerRepository.findAll()) {
            String existingKey = OwnerIdentity.key(existing.getTelephone(), existing.getEmail(),
                    existing.getHouseholdId());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }

    /**
     * The household id the request's owner will be assigned: the stable
     * {@link Household#id(String, String) household id} for its lastName and address when it
     * opts into a shared household, otherwise none (so a solo owner contributes no household
     * component to its key).
     */
    private static String requestHouseholdId(OwnerFieldsDto request) {
        return Boolean.TRUE.equals(request.getSharesHousehold())
                ? Household.id(request.getLastName(), request.getAddress()) : null;
    }
}
