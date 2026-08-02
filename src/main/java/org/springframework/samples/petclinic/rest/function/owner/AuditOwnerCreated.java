package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Records every successful owner creation to the dedicated {@code AUDIT} logger,
 * capturing the authenticated user together with the new owner's id and
 * membership number. Runs after the owner has been saved (and so has an id).
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";
        Integer membershipNumber = OwnerMapper.membershipNumber(owner, ownerRepository.findAll());
        AUDIT.info("owner created user={} id={} membershipNumber={}", user, owner.getId(), membershipNumber);
    }
}
