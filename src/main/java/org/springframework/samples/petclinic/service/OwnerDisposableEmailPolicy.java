package org.springframework.samples.petclinic.service;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: an owner's email must not use a disposable-email provider. The domain (the part
 * after the last {@code @}, compared case-insensitively) is rejected when it appears on a fixed
 * blocklist (mailinator.com, tempmail.com, guerrillamail.com). Email is optional: an owner created
 * without one, or with no {@code @}, passes unchanged. Kept as a small, self-contained unit so the
 * rule can be enforced from the create flow without adding complexity to the controller or service.
 */
public final class OwnerDisposableEmailPolicy {

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerDisposableEmailPolicy() {
    }

    /**
     * Reject the owner when its email domain is on the disposable-domain blocklist.
     *
     * @param owner the owner being created
     * @throws DisposableEmailException if the email domain is blocklisted
     */
    public static void rejectDisposableDomain(Owner owner) {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase();
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new DisposableEmailException();
        }
    }

    /** Thrown when an owner's email domain is on the disposable-domain blocklist. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class DisposableEmailException extends RuntimeException {
    }
}
