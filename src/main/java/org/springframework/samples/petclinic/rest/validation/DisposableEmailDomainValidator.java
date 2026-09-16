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

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist. Disposable
 * addresses are throw-away mailboxes that cannot be relied on for contacting an owner, so
 * a present email must not use one of these domains.
 *
 * <p>This is a policy check distinct from {@link EmailNormalizer}'s syntactic validation: it
 * operates on an already-normalized (lower-cased, syntactically valid) address.
 */
public final class DisposableEmailDomainValidator {

    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomainValidator() {
    }

    /**
     * Rejects a normalized email whose domain is on the disposable-domain blocklist. Email is
     * optional, so a {@code null} value is accepted unchanged.
     *
     * @param email the normalized (lower-cased, syntactically valid) email, or {@code null} if none
     * @throws DisposableEmailDomainException if the email's domain is on the blocklist
     */
    public static void validate(String email) {
        if (email == null) {
            return;
        }
        String domain = email.substring(email.indexOf('@') + 1);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(
                "Email domain '" + domain + "' is a disposable-domain and is not allowed");
        }
    }

    /**
     * Reports whether a normalized email's domain is <em>disposable-adjacent</em>: a subdomain of one
     * of the blocked disposable domains (e.g. {@code inbox.mailinator.com}). Such an address slips
     * past {@link #validate(String)} — which only rejects an exact blocklist match — yet still points
     * at a throw-away mail provider, so it is worth flagging for review.
     *
     * @param email the normalized (lower-cased, syntactically valid) email, or {@code null} if none
     * @return {@code true} when the email's domain is a subdomain of a blocked disposable domain
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1);
        return BLOCKED_DOMAINS.stream().anyMatch(blocked -> domain.endsWith("." + blocked));
    }
}
