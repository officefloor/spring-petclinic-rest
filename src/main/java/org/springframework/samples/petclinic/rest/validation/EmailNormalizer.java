/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.rest.advice.DisposableEmailException;
import org.springframework.stereotype.Component;

/**
 * Turns owner emails into the single canonical form that gets stored, returned and compared, so the REST controllers do
 * not have to carry the email rules themselves.
 *
 * <p>A syntactically valid address is already enforced by Bean Validation on the request payload, so normalization only
 * canonicalizes the value by lower-casing it. So {@code "Owner@Example.TEST"} becomes {@code "owner@example.test"}.
 *
 * <p>Normalization also rejects any address whose domain is on the disposable-domain blocklist
 * ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}), since such throwaway addresses are not
 * acceptable owner contacts. The comparison is case-insensitive because the value is canonicalized (lower-cased) first.
 *
 * <p>Unlike the address and telephone, an email is optional: a missing (blank or {@code null}) value is left untouched,
 * so callers can uniformly treat a blank result as a missing email. The same canonical form is used both for the value
 * that is stored and returned and for every email comparison (such as duplicate detection across existing owners), so
 * incidental case differences never affect the outcome.
 */
@Component
public class EmailNormalizer {

    /**
     * Normalizes an owner email into its canonical stored form by lower-casing it. A missing (blank or {@code null})
     * email is returned untouched, since the field is optional. The normalized value is what gets stored and returned as
     * {@code email}, and the same form is used when comparing an owner's email against those of existing owners. An
     * email whose domain is on the disposable-domain blocklist is rejected.
     *
     * @param email the raw email value from the request, may be {@code null}
     * @return the lower-cased email, or the original value when none was supplied
     * @throws DisposableEmailException if the email's domain is on the disposable-domain blocklist
     */
    public String normalize(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        String normalized = email.toLowerCase();
        rejectDisposableDomain(normalized);
        return normalized;
    }

    /**
     * Rejects a normalized email whose domain (the part after the final {@code '@'}) is on the disposable-domain
     * blocklist. An address without a domain part is left to Bean Validation's syntactic check and passes here.
     *
     * @param normalized the lower-cased email
     * @throws DisposableEmailException if the domain is on the disposable-domain blocklist
     */
    private void rejectDisposableDomain(String normalized) {
        int at = normalized.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = normalized.substring(at + 1);
        if (DisposableEmailPolicy.isBlocked(domain)) {
            throw new DisposableEmailException(
                "email domain " + domain + " is on the disposable-domain blocklist");
        }
    }
}
