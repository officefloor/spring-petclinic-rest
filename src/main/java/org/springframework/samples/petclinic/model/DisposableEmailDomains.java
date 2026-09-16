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
package org.springframework.samples.petclinic.model;

import java.util.Locale;
import java.util.Set;

/**
 * The set of known disposable/throwaway email domains, and the rules for judging an email
 * address against them. Owns this knowledge in one place so both the create/update
 * validation (which rejects an outright disposable address) and the owner risk flag (which
 * treats a merely disposable-<em>adjacent</em> address as a soft signal) read the same list.
 */
public final class DisposableEmailDomains {

    /** Domains of disposable/throwaway email providers. */
    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /**
     * @param email an email value (typically already normalized)
     * @return {@code true} if the email's domain is exactly one of the known disposable
     *         domains
     */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether the email's domain is disposable-adjacent: either a known disposable domain,
     * a subdomain of one, or a domain sharing its registrable base label (e.g. the same
     * name under a different top-level domain). This catches the near-misses that slip past
     * the exact {@link #isDisposable(String) disposable} blocklist.
     *
     * @param email an email value (typically already normalized)
     * @return {@code true} if the email's domain is a known disposable domain or a close
     *         relative of one
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        String base = baseLabel(domain);
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (domain.equals(disposable) || base.equals(baseLabel(disposable))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The lower-cased domain part of an email address, or {@code null} when {@code email} is
     * {@code null} or carries no {@code '@'}.
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

    /**
     * The registrable base label of a domain: the label immediately before its top-level
     * domain (e.g. {@code "mailinator"} for {@code "mail.mailinator.com"}), or the whole
     * domain when it has no dot.
     */
    private static String baseLabel(String domain) {
        String[] parts = domain.split("\\.");
        return parts.length >= 2 ? parts[parts.length - 2] : domain;
    }
}
