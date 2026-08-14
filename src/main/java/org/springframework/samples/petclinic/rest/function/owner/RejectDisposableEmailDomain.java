package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Runs in the create-owner pipeline after the email is normalized. An absent email passes through;
 * a present email whose domain is on the disposable-domain blocklist (e.g. mailinator.com) is
 * rejected with a 400. Reads the already lower-cased value so the domain comparison is exact.
 */
public class RejectDisposableEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        OwnerEmails.rejectDisposableDomain(request.getEmail());
    }
}
