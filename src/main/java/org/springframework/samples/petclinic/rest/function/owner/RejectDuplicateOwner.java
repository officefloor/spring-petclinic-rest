package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Consolidated duplicate detection for create-owner. All duplicate detection is now expressed through
 * the single derived {@link IdentityKey} (@code normalizedTelephone + '|' + (email or empty) + '|' +
 * householdId}), replacing the former separate telephone, email and household checks. Runs after the
 * telephone and email have been normalized and before the owner is built and saved.
 *
 * <p>Because the telephone is a component of the key, two members of the same household (same
 * {@code householdId}) with different telephones derive different identityKeys and are both allowed;
 * the create endpoint rejects a request whose normalized telephone — the identityKey's discriminating
 * component — is already used by an existing owner, i.e. an identityKey collision. Existing owners'
 * telephones are normalized to E.164 the same way before comparison, so numbers stored in any format
 * are matched by their E.164 value. A collision is rejected via {@link DuplicateIdentityException},
 * which the global handler turns into a 409.
 */
public class RejectDuplicateOwner {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneE164.normalizeOrNull(existing.getTelephone()))) {
                throw new DuplicateIdentityException(IdentityKey.of(request.getTelephone(),
                        request.getEmail(), request.getLastName(), request.getAddress()));
            }
        }
    }
}
