package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneConflictException;

/**
 * Runs after {@link ValidateOwnerFields} (whose normalized telephone this step reads via {@code @Val})
 * and before {@link BuildOwner}: rejects the request with 409 when any existing owner already uses the
 * same normalized telephone. Every stored owner's telephone is normalized the same way (non-digits
 * stripped) before comparison, so differently-formatted duplicates are still caught.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneConflictException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new OwnerTelephoneConflictException(
                        "An owner with telephone " + telephone + " already exists");
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
