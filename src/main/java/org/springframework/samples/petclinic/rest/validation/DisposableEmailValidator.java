/*
 * Copyright 2016 the original author or authors.
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

import java.util.Locale;
import java.util.Set;

/**
 * Checks an owner's email address against a blocklist of disposable-email domains. Owners are
 * expected to supply a durable address, so throwaway providers are refused.
 */
public final class DisposableEmailValidator {

    /** The disposable-email domains that are not accepted for an owner. */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailValidator() {
    }

    /**
     * Returns whether the supplied email's domain is on the disposable-domain blocklist. The
     * comparison is case-insensitive; a {@code null}, blank or address-less value is not blocked.
     *
     * @param email the email as submitted, possibly {@code null} or blank
     * @return {@code true} when the email's domain is blocklisted, {@code false} otherwise
     */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Returns whether the supplied email's domain is <em>disposable-adjacent</em>: a subdomain of a
     * blocklisted disposable provider (for example {@code inbox.mailinator.com}) rather than the
     * blocked domain itself. Such an address is not refused outright at creation yet still belongs to
     * a throwaway provider, so it is worth surfacing as a soft risk signal. The comparison is
     * case-insensitive; a {@code null}, blank or address-less value is not adjacent.
     *
     * @param email the email as submitted, possibly {@code null} or blank
     * @return {@code true} when the email's domain is a subdomain of a blocklisted domain
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        return BLOCKED_DOMAINS.stream().anyMatch(blocked -> domain.endsWith("." + blocked));
    }

    /**
     * Extracts the lower-cased domain part of an email, or {@code null} when the value is
     * {@code null} or carries no {@code '@'} separator.
     */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
    }
}
