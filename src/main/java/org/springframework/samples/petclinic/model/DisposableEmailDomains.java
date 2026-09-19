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
 * The single source of truth about disposable (throwaway) email providers, classifying a domain as
 * either {@link #isDisposable(String) disposable} (exactly a known throwaway provider) or
 * {@link #isDisposableAdjacent(String) disposable-adjacent} (closely related to one without being
 * one itself). Owner creation rejects disposable domains outright, while the softer adjacent band
 * is only a risk signal, so both decisions read the same blocklist from here rather than each
 * holding its own copy.
 */
public final class DisposableEmailDomains {

    /** Domains that provide throwaway, disposable mailboxes. */
    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /**
     * The lower-cased domain portion of an email address.
     *
     * @param email the address to inspect, or {@code null} when none was supplied
     * @return the part after the last {@code '@'} lower-cased, or {@code null} when the email is
     *         absent or has no {@code '@'}
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
     * Whether the domain is exactly a known disposable-email provider.
     *
     * @param domain the domain to test, or {@code null}
     * @return {@code true} when the domain is on the disposable blocklist
     */
    public static boolean isDisposable(String domain) {
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Whether the domain is disposable-adjacent: closely related to a blocked disposable provider
     * without being one itself. A domain qualifies when it is a subdomain of a blocked domain (e.g.
     * {@code inbox.mailinator.com}) or shares a blocked domain's registrable label under a different
     * suffix (e.g. {@code mailinator.net} or bare {@code mailinator}). Domains that are themselves
     * disposable are not adjacent.
     *
     * @param domain the domain to test, or {@code null}
     * @return {@code true} when the domain is disposable-adjacent
     */
    public static boolean isDisposableAdjacent(String domain) {
        if (domain == null || isDisposable(domain)) {
            return false;
        }
        for (String blocked : BLOCKED_DOMAINS) {
            if (domain.endsWith("." + blocked)) {
                return true;
            }
            String label = blocked.substring(0, blocked.indexOf('.'));
            if (domain.equals(label) || domain.startsWith(label + ".")) {
                return true;
            }
        }
        return false;
    }
}
