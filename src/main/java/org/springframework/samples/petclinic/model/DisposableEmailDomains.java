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
package org.springframework.samples.petclinic.model;

import java.util.Locale;
import java.util.Set;

/**
 * Knowledge of the known disposable-email providers, and how an address relates to them.
 * <p>
 * A domain is <em>disposable</em> when it is exactly one of the known throwaway providers;
 * such addresses are rejected outright and never stored. A domain is
 * <em>disposable-adjacent</em> when it is not itself on the blocklist but shares a known
 * provider's registrable name — a subdomain of it (e.g. {@code inbox.mailinator.com}) or the
 * same second-level label under a different suffix (e.g. {@code tempmail.io}). Such an address
 * passes creation validation yet still resembles a throwaway mailbox, so it is treated as a
 * risk signal rather than a hard rejection.
 */
public final class DisposableEmailDomains {

    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /**
     * @return {@code true} when {@code email}'s domain is exactly a known disposable provider
     */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * @return {@code true} when {@code email}'s domain is not itself a known disposable
     * provider but shares one's registrable name (a subdomain of it, or the same second-level
     * label under a different suffix)
     */
    public static boolean isAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || DISPOSABLE_DOMAINS.contains(domain)) {
            return false;
        }
        String label = secondLevelLabel(domain);
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (label.equals(secondLevelLabel(disposable))) {
                return true;
            }
        }
        return false;
    }

    /** The lower-cased domain part of an address, or {@code null} when there is none. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return domain.isEmpty() ? null : domain;
    }

    /** The label immediately left of the final suffix, e.g. {@code mailinator} for
     *  {@code inbox.mailinator.com}; the whole domain when it carries no suffix. */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length < 2 ? domain : labels[labels.length - 2];
    }
}
