package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for what a "normalized" owner email is: lower-cased, so the
 * stored value is canonical regardless of the casing the client sent.
 * Syntactic validity is enforced upstream by the {@code @Email} constraint on the
 * request DTO; this step only canonicalizes an already-valid (or absent) address. The
 * disposable-domain blocklist itself lives in the model-layer
 * {@link org.springframework.samples.petclinic.model.DisposableEmailDomains}, the single
 * source for both the hard rejection (see {@link ValidateOwnerEmailDomain}) and the soft
 * {@code riskFlag} adjacency signal.
 */
final class OwnerEmails {

    private OwnerEmails() {
    }

    static String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
