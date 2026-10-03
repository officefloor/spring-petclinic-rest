/*
 * Copyright 2002-2017 the original author or authors.
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

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Single source of truth for the known disposable email domains and the two checks that key off
 * them.
 *
 * <p>The disposable domains themselves ({@code mailinator.com}, {@code tempmail.com},
 * {@code guerrillamail.com}) are rejected outright by {@code EmailDomainValidation} at create time,
 * via {@link #isBlocked(String)}. The softer {@link #isDisposableAdjacent(String)} check
 * additionally recognises the ring of lookalike domains around them — the same registrable name
 * under a different subdomain or top-level domain (e.g. {@code inbox.mailinator.com} or
 * {@code mailinator.net}) — which slip past the exact-match block yet still signal a throwaway
 * address, and so raise an owner's {@code riskFlag}.
 */
public final class DisposableDomains {

    private DisposableDomains() {
    }

    /** Exact disposable domains, lower-cased; rejected at create by {@code EmailDomainValidation}. */
    public static final Set<String> BLOCKED = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Registrable labels of the blocked domains (e.g. {@code 'mailinator'}); the basis of adjacency. */
    private static final Set<String> BLOCKED_LABELS = BLOCKED.stream()
        .map(DisposableDomains::registrableLabel)
        .collect(Collectors.toUnmodifiableSet());

    /**
     * Whether {@code domain} (the part of an email after the last {@code '@'}) is exactly one of the
     * blocked disposable domains, compared case-insensitively. A {@code null} domain is not blocked.
     */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain.trim().toLowerCase());
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: its registrable label — the label
     * immediately before the public suffix, e.g. {@code 'mailinator'} in both {@code 'mailinator.net'}
     * and {@code 'inbox.mailinator.com'} — matches the registrable label of a known disposable domain.
     * Returns {@code false} for a {@code null}/blank email or one carrying no domain part.
     */
    public static boolean isDisposableAdjacent(String email) {
        String label = registrableLabel(domainOf(email));
        return label != null && BLOCKED_LABELS.contains(label);
    }

    /** The domain part of an email (after the last {@code '@'}), trimmed and lower-cased, or {@code null}. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        return domain.isEmpty() ? null : domain;
    }

    /**
     * The registrable label of a domain: the second-to-last dot-separated segment (e.g.
     * {@code 'mailinator'} for both {@code 'mailinator.com'} and {@code 'inbox.mailinator.com'}), or
     * the whole single segment when the domain carries no dot. {@code null} when {@code domain} is
     * {@code null} or empty.
     */
    private static String registrableLabel(String domain) {
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        String[] parts = domain.split("\\.");
        String label = parts.length == 1 ? parts[0] : parts[parts.length - 2];
        return label.isEmpty() ? null : label;
    }
}
