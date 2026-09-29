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

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility for normalizing the optional e-mail address submitted when creating or
 * updating an owner. A {@code null} value is left untouched (the field is optional),
 * but when a value is supplied it must be a syntactically valid address whose domain is
 * not on the disposable-domain blocklist; otherwise an {@link InvalidEmailException} is
 * raised so the request is rejected with {@code 400 Bad Request}. Valid values are
 * returned lower-cased for consistent storage and retrieval.
 */
public final class EmailNormalizer {

    /**
     * Pragmatic syntactic check for an e-mail address: a non-empty local part, a single
     * {@code @}, and a dotted domain whose top-level label is alphabetic.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Domains of disposable / throwaway e-mail providers that are not accepted for an owner.
     */
    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private EmailNormalizer() {
    }

    /**
     * Normalize the supplied e-mail address.
     *
     * @param email the raw e-mail value (may be {@code null} because the field is optional)
     * @return {@code null} when the input is {@code null}, otherwise the address lower-cased
     * @throws InvalidEmailException if a value is supplied but is not syntactically valid,
     *     or its domain is on the disposable-domain blocklist
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("email must be a syntactically valid address");
        }
        String normalized = email.toLowerCase();
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("email domain is not accepted");
        }
        return normalized;
    }

    /**
     * Whether the supplied e-mail address is <em>disposable-adjacent</em>: a soft signal that its
     * domain resembles a known throwaway provider without necessarily being on the outright
     * {@link #DISPOSABLE_DOMAINS blocklist} (blocklisted domains are already rejected at create
     * time by {@link #normalize(String)}). A domain qualifies when it is, or is a subdomain of, a
     * blocklisted disposable domain (e.g. {@code mail.mailinator.com}), or when it shares the
     * registrable second-level label of one but uses a different top-level domain (e.g.
     * {@code mailinator.net} for the blocklisted {@code mailinator.com}).
     *
     * <p>Unlike {@link #normalize(String)} this never throws; a {@code null}, blank or
     * domain-less value is simply not adjacent.
     *
     * @param email the raw or stored e-mail value (may be {@code null})
     * @return {@code true} when the address's domain is disposable-adjacent
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (domain.equals(disposable) || domain.endsWith("." + disposable)) {
                return true;
            }
        }
        String secondLevel = registrableLabel(domain);
        if (secondLevel == null) {
            return false;
        }
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (secondLevel.equals(registrableLabel(disposable))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The registrable second-level label of a domain — the dot-separated label immediately before
     * the final (top-level) label, e.g. {@code mailinator} for both {@code mailinator.com} and
     * {@code mail.mailinator.com}. Returns {@code null} when the domain has no such label.
     */
    private static String registrableLabel(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return null;
        }
        return labels[labels.length - 2];
    }
}
