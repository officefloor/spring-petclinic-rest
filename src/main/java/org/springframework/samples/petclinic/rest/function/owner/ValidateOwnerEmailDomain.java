package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.DisposableEmailDomains;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner email whose domain is on the disposable-domain blocklist, but only
 * WHEN PRESENT: an absent email is left untouched so an owner created without one is
 * still accepted. Syntactic validity is enforced upstream by the DTO {@code @Email}
 * constraint; this step adds the domain rule via the model-layer
 * {@link DisposableEmailDomains} (the single source shared with the {@code riskFlag}
 * signal), rejecting a disposable address with 400 through
 * {@link DisposableEmailDomainException}.
 */
public class ValidateOwnerEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        if (DisposableEmailDomains.isDisposable(email)) {
            throw new DisposableEmailDomainException(email, DisposableEmailDomains.domainOf(email));
        }
    }
}
