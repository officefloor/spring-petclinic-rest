package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for {@code POST /api/owners}: rejects the request only when the new
 * owner's whole {@code identityKey} equals an existing owner's identityKey (see {@link OwnerIdentity}).
 * This subsumes the former separate telephone, email and household checks — because the telephone is
 * part of the key, two members of the same household with different telephones are both allowed; only
 * an exact full-key match is a duplicate. On a match it throws {@link DuplicateIdentityException},
 * handled globally as 409.
 */
public class EnsureIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String lastName = OwnerIdentity.normalizeName(request.getLastName());
        String address = OwnerAddress.normalize(request.getAddress());

        // The household the new owner would join: any existing owner sharing the normalized lastName
        // and address. When present, both this owner and those peers carry the same derived household
        // id; when absent, the household component is empty (a solo owner).
        boolean hasPeer = false;
        for (Owner existing : ownerRepository.findAll()) {
            if (sharesHousehold(lastName, address, existing)) {
                hasPeer = true;
                break;
            }
        }
        String newHousehold = hasPeer ? OwnerIdentity.deriveHouseholdId(lastName, address) : "";
        String newKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), newHousehold);

        for (Owner existing : ownerRepository.findAll()) {
            // A household peer shares the new owner's derived id even if it predates the household and
            // has not been back-filled yet; any other owner is compared on its own stored household.
            String household = sharesHousehold(lastName, address, existing)
                    ? newHousehold
                    : (existing.getHouseholdId() == null ? "" : existing.getHouseholdId());
            String existingKey = OwnerIdentity.key(existing.getTelephone(), existing.getEmail(), household);
            if (newKey.equals(existingKey)) {
                throw new DuplicateIdentityException(newKey);
            }
        }
    }

    private static boolean sharesHousehold(String lastName, String address, Owner existing) {
        return lastName.equals(OwnerIdentity.normalizeName(existing.getLastName()))
                && address.equals(OwnerAddress.normalize(existing.getAddress()));
    }
}
