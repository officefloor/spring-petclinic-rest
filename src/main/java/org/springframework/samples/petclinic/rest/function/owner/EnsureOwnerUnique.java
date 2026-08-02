package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerConflictException;
import org.springframework.util.StringUtils;

/**
 * Rejects creating an owner that clashes with an existing one - with a 409 via
 * {@link OwnerConflictException}. An owner clashes when its telephone number is
 * already used by another owner, or (when an email address is provided) when that
 * email is already used by another owner.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws OwnerConflictException {
        boolean hasEmail = StringUtils.hasText(owner.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new OwnerConflictException(
                        "An owner with the same telephone already exists");
            }
            if (hasEmail && Objects.equals(existing.getEmail(), owner.getEmail())) {
                throw new OwnerConflictException(
                        "An owner with the same email already exists");
            }
        }
    }
}
