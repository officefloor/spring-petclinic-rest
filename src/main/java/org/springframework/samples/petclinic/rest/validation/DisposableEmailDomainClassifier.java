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

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Single source of truth for classifying an email address by the disposability of its domain.
 *
 * <p>A domain is <em>blocked</em> when it exactly matches a known disposable-email provider; such
 * addresses are rejected at creation (see {@link DisposableEmailDomainValidator}). A domain is
 * <em>disposable-adjacent</em> when it is not itself blocked yet shares its registrable name with a
 * blocked provider &mdash; for example {@code mailinator.net} or {@code x.tempmail.org}, which sit
 * next to a blocked domain on a different top-level domain or as a subdomain. Such addresses are
 * accepted but warrant a closer look.
 *
 * <p>All inputs are expected to be the already-normalized (trimmed, lower-cased) email produced by
 * {@link EmailNormalizer}; an absent email is classified as neither blocked nor adjacent.
 */
@Component
public class DisposableEmailDomainClassifier {

    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The registrable labels (e.g. {@code mailinator}) of the blocked domains. */
    private static final Set<String> DISPOSABLE_BRANDS = BLOCKED_DOMAINS.stream()
        .map(DisposableEmailDomainClassifier::registrableLabel)
        .collect(Collectors.toUnmodifiableSet());

    /**
     * @param email the normalized email address, or {@code null}/blank when none was supplied
     * @return {@code true} when the email's domain is on the disposable-provider blocklist
     */
    public boolean isBlocked(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * @param email the normalized email address, or {@code null}/blank when none was supplied
     * @return {@code true} when the email's domain is not itself blocked but shares its registrable
     * name with a blocked disposable-email provider (a different TLD or a subdomain of one)
     */
    public boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || BLOCKED_DOMAINS.contains(domain)) {
            return false;
        }
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_BRANDS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    private static String domainOf(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** The label immediately left of the top-level domain (e.g. {@code mailinator} in {@code mailinator.com}). */
    private static String registrableLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}
