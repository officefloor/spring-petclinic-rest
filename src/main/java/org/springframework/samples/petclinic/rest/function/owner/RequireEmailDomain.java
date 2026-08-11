package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Step of {@code POST /api/owners}: rejects an owner whose {@code email} domain is on the
 * disposable-domain blocklist. Email is optional, so a null/blank value is left untouched (it runs
 * after {@link NormalizeOwnerEmail}, which has already trimmed, lower-cased and syntactically
 * validated any present value). A present email whose domain matches a blocked domain is rejected
 * 400 via {@link DisposableEmailDomainException}.
 */
public class RequireEmailDomain {

    /** Disposable email domains that are not accepted for owner registration. */
    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // optional when absent
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return; // syntax already validated upstream; nothing to classify
        }
        String domain = email.substring(at + 1).toLowerCase();
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
