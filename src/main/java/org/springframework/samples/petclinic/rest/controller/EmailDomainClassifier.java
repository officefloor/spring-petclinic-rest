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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Classifies an owner's email by its domain against the fixed table of known disposable
 * domains.
 *
 * <p>Keeping this in one place means every rule keyed on an owner's email domain reads the
 * same disposable-domain table and derives the domain the one way, so they can never drift
 * apart: the domain an email yields here is the domain every domain-keyed rule sees for it.
 */
@Component
public class EmailDomainClassifier {

    /** Fixed table of disposable email domains; an email whose domain matches one of these
     *  (case-insensitively) is a disposable-domain email. */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The lower-cased domain of an email address: the part after its single '@'. Returns
     * {@code null} when {@code email} is {@code null} or carries no '@'.
     *
     * @param email an email address, expected to carry a single '@' (as produced by the
     *              create endpoint's syntactic email check)
     * @return the lower-cased domain, or {@code null} when the email has none
     */
    public String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Whether an email's {@link #domainOf domain} is one of the known disposable domains
     * (compared case-insensitively). This is the single disposable-domain check on create,
     * so a disposable email is recognised the one way wherever the rule is applied.
     *
     * @param email an email address, expected to carry a single '@'
     * @return {@code true} if the email's domain is a known disposable domain, {@code false}
     *         otherwise (including when the email has no domain)
     */
    public boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_EMAIL_DOMAINS.contains(domain);
    }
}
