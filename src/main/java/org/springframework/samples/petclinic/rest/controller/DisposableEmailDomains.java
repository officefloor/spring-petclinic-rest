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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Shared knowledge of disposable-email providers, used both by the hard
 * {@link DisposableEmailDomainValidator} (which rejects an exact disposable domain) and by the
 * soft {@link DisposableEmailDomainEvaluator} (which flags domains that merely belong to the same
 * provider family). Centralising the provider list and the address-parsing here keeps the two
 * checks in agreement.
 */
@Component
public class DisposableEmailDomains {

    /** Fully-qualified domains of known disposable-email providers. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * The registrable labels (the token immediately before the TLD) of the known disposable
     * providers, so sibling TLDs ({@code mailinator.net}) and subdomains ({@code x.mailinator.com})
     * can be recognised as belonging to the same family.
     */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
        .map(DisposableEmailDomains::secondLevelLabel)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());

    /**
     * Extract the lower-cased domain from an email address, or {@code null} when none is present.
     */
    public String domainOf(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Whether {@code domain} is exactly one of the known disposable-email domains.
     */
    public boolean isDisposable(String domain) {
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether {@code domain} belongs to a known disposable-email provider's family: it shares a
     * disposable provider's registrable label, whether as a sibling TLD ({@code mailinator.net}),
     * a subdomain ({@code inbox.mailinator.com}) or the exact domain itself.
     */
    public boolean isDisposableAdjacent(String domain) {
        return domain != null && DISPOSABLE_LABELS.contains(secondLevelLabel(domain));
    }

    /**
     * The token immediately before the top-level domain (e.g. {@code "mailinator"} for both
     * {@code "mailinator.com"} and {@code "inbox.mailinator.co"}), or the whole value when it has
     * no dot.
     */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return domain;
        }
        return labels[labels.length - 2];
    }
}
