package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Duplicate-detection step of {@code POST /api/owners}: rejects the request 409 when the new owner
 * duplicates an existing one. Duplicate detection is now expressed through the single
 * {@code identityKey} &mdash; the lower-case hex SHA-256 of
 * {@code regionCode + '|' + normalizedTelephone + '|' + (email or empty) + '|' + soundex(lastName)}
 * (see {@link OwnerIdentity#key(String, String, String, String)}), where {@code regionCode} folds in
 * the version-2 {@code 'V2'} tag. The separate household-duplicate 409 that was derived from the
 * computed {@code householdId} no longer applies.
 *
 * <p>Only an <b>exact identity</b> conflict is rejected: the whole {@code identityKey} matches an
 * existing owner. Because the telephone is part of the hashed input, two owners sharing a last name
 * and postcode but with different telephones have different keys and are both admitted &mdash; the
 * second is recorded downstream as a soft match by {@link FlagPossibleDuplicate} rather than
 * rejected here.
 *
 * <p>Runs after the normalize and email-domain steps (so telephone is E.164, email lower-cased, and
 * disposable domains already blocked) and before {@link BuildOwner}, so no owner is persisted on
 * conflict. Soft-deleted owners are ignored.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(Locality.of(request.getCity(), request.getPostcode()),
                request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new registration
            }
            String existingKey = OwnerIdentity.key(
                    Locality.of(existing.getCity(), existing.getPostcode()),
                    existing.getTelephone(), existing.getEmail(), existing.getLastName());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
