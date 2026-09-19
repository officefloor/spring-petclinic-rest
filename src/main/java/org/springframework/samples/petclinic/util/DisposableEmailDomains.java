/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * Recognizes email addresses whose domain belongs to a known disposable-email provider, so an
 * owner cannot register with a throw-away address.
 */
public final class DisposableEmailDomains {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /**
     * Whether the given email's domain is on the disposable-domain blocklist.
     *
     * @param email the email value (may be {@code null})
     * @return {@code true} when {@code email} has a domain listed as disposable, {@code false}
     * when it is {@code null}, has no domain part or the domain is not blocked
     */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Whether the given email's domain is <em>disposable-adjacent</em>: not itself on the blocklist
     * (such addresses are rejected outright), but a subdomain of a blocked disposable domain, e.g.
     * {@code inbox.mailinator.com}. These slip past {@link #isDisposable(String)} yet still resemble a
     * throw-away provider closely enough to warrant a risk flag.
     *
     * @param email the email value (may be {@code null})
     * @return {@code true} when {@code email}'s domain is a subdomain of a blocked disposable domain,
     * {@code false} otherwise (including when it is {@code null}, has no domain part, or is itself
     * exactly a blocked domain)
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || BLOCKED_DOMAINS.contains(domain)) {
            return false;
        }
        return BLOCKED_DOMAINS.stream().anyMatch(blocked -> domain.endsWith("." + blocked));
    }

    /**
     * The lower-cased domain part of {@code email}, or {@code null} when it is {@code null} or has no
     * {@code '@'} separator.
     */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }
}
