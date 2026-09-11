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
 * Single source of truth for the throwaway-email policy an owner's email is judged against. The blocklist of disposable
 * providers ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}) is enforced two ways:
 *
 * <ul>
 *   <li>an email whose domain is <em>exactly</em> a blocklisted domain is rejected outright at create (see
 *       {@link EmailNormalizer#normalize}), so such an address is never stored; and</li>
 *   <li>an email whose domain is not itself blocklisted but is closely related to a blocklisted one is
 *       <em>disposable-adjacent</em> (see {@link #isDisposableAdjacent}) — accepted, but treated as a risk signal.</li>
 * </ul>
 *
 * <p>Kept a static utility (like the mapper's resolvers) so both the create-time rejection and the read-time risk flag
 * derive from one blocklist and can never drift apart. Comparisons are case-insensitive; callers normalize the email to
 * lower case first.
 */
public final class DisposableEmailPolicy {

    /** Domains of throwaway email providers that an owner's email may not use verbatim, and whose look-alikes are risky. */
    static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailPolicy() {
    }

    /**
     * Returns whether {@code domain} is exactly on the disposable-domain blocklist. The comparison is case-insensitive.
     *
     * @param domain the email domain (the part after the final {@code '@'}), may be {@code null}
     * @return {@code true} when the domain is a blocklisted disposable domain
     */
    public static boolean isBlocked(String domain) {
        return domain != null && DISPOSABLE_DOMAINS.contains(domain.toLowerCase());
    }

    /**
     * Returns whether {@code email}'s domain is disposable-adjacent: not itself on the blocklist, but closely related to
     * a blocklisted disposable domain. A domain is disposable-adjacent when it is either
     *
     * <ul>
     *   <li>a subdomain of a blocklisted domain (for example {@code inbox.mailinator.com}); or</li>
     *   <li>a look-alike sharing a blocklisted domain's second-level label under a different top-level domain (for
     *       example {@code mailinator.net} or {@code tempmail.io}).</li>
     * </ul>
     *
     * <p>A missing (blank or {@code null}) email, or one without a domain part, is never disposable-adjacent. The check
     * is case-insensitive.
     *
     * @param email the owner's email, may be {@code null}
     * @return {@code true} when the email's domain is disposable-adjacent
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        if (domain.isEmpty() || DISPOSABLE_DOMAINS.contains(domain)) {
            return false;
        }
        String label = secondLevelLabel(domain);
        for (String blocked : DISPOSABLE_DOMAINS) {
            if (domain.endsWith("." + blocked) || label.equals(secondLevelLabel(blocked))) {
                return true;
            }
        }
        return false;
    }

    /** The label before the first dot of a domain, e.g. {@code "mailinator"} for both {@code mailinator.com} and {@code mailinator.net}. */
    private static String secondLevelLabel(String domain) {
        int dot = domain.indexOf('.');
        return dot < 0 ? domain : domain.substring(0, dot);
    }
}
