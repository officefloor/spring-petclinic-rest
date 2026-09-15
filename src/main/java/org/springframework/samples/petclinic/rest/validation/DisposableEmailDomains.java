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
import java.util.stream.Collectors;

/**
 * Single source of truth for the disposable-email providers the application recognises, and the
 * small set of pure predicates over an email's domain that others reuse:
 *
 * <ul>
 *   <li>{@link #isBlocked(String)} &mdash; the domain is exactly one of the known disposable
 *       providers. Such addresses are rejected on create by {@link DisposableEmailDomainValidator}
 *       and so never reach persistence.</li>
 *   <li>{@link #isAdjacent(String)} &mdash; the domain is not itself blocked, but is closely
 *       related to a blocked provider: it shares that provider's second-level name. This covers a
 *       subdomain of a blocked domain (e.g. {@code x.mailinator.com}) and a look-alike under a
 *       different top-level domain (e.g. {@code mailinator.net}). These slip past the exact-match
 *       blocklist yet still warrant flagging.</li>
 * </ul>
 */
public final class DisposableEmailDomains {

    /** Domains of disposable-email providers whose addresses are not accepted. */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Second-level names of the blocked providers (e.g. 'mailinator' from 'mailinator.com'). */
    private static final Set<String> BLOCKED_SECOND_LEVEL_NAMES = BLOCKED_DOMAINS.stream()
        .map(DisposableEmailDomains::secondLevelName)
        .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailDomains() {
    }

    /**
     * Extracts the lower-cased domain from an email, i.e. the part after its last {@code @}.
     *
     * @param email the email (may be {@code null}); expected already normalized
     * @return the lower-cased domain, or {@code null} when the email is absent or has no {@code @}
     */
    public static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * @return {@code true} when the email's domain is exactly a known disposable provider.
     */
    public static boolean isBlocked(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * @return {@code true} when the email's domain is not itself blocked but shares a blocked
     *         provider's second-level name (a subdomain or a different-TLD look-alike)
     */
    public static boolean isAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || BLOCKED_DOMAINS.contains(domain)) {
            return false;
        }
        return BLOCKED_SECOND_LEVEL_NAMES.contains(secondLevelName(domain));
    }

    /**
     * The second-level name of a domain: the label immediately left of the top-level domain
     * (e.g. {@code 'mailinator'} for both {@code 'mailinator.com'} and {@code 'x.mailinator.com'}).
     * A domain with no dot is returned unchanged.
     */
    private static String secondLevelName(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length < 2 ? domain : labels[labels.length - 2];
    }
}
