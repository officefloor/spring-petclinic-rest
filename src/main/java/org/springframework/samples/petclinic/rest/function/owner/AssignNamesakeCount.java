package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that counts how many existing owners already share this owner's
 * {@code firstName} and {@code lastName} — compared case-insensitively — and stores the total on the
 * owner as {@code namesakeCount}.
 *
 * <p>Runs after {@link BuildOwner} so the owner carries its names, and before {@link SaveOwner} so
 * the count reflects only owners that existed before this create: the new owner is not yet persisted
 * and so is never counted among its own namesakes. {@code @Val} yields the built owner, mutated in
 * place and persisted by the save step.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = normalize(owner.getFirstName());
        String lastName = normalize(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(normalize(existing.getFirstName()))
                    && lastName.equals(normalize(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    /** Lower-case for a case-insensitive comparison; a null name normalizes to empty. */
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
