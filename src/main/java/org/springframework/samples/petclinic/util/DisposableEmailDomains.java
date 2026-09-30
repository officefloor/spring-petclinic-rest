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
import java.util.stream.Collectors;

/**
 * Single source of truth for the disposable-email providers the application knows about. Both the
 * create-time email validation (in the owner controller, which rejects an owner whose whole email
 * domain is on {@link #BLOCKLIST}) and the read-time risk-flag derivation (in the owner mapper, which
 * raises the softer "disposable-adjacent" signal from {@link #BASE_LABELS}) consult this class, so the
 * set of disposable providers has one owner: add a domain to {@link #BLOCKLIST} here and both paths
 * pick it up — the base-label view is derived from it rather than hand-restated.
 */
public final class DisposableEmailDomains {

    /**
     * The disposable-email domains not accepted for an owner. An email whose whole domain (the part
     * after the {@code @}) is on this blocklist is rejected at creation. Entries are lower-case and
     * matched case-insensitively.
     */
    public static final Set<String> BLOCKLIST =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level labels of the {@link #BLOCKLIST} domains — each domain's label immediately
     * before its final TLD (e.g. {@code "mailinator"} from {@code "mailinator.com"}). Derived from the
     * blocklist rather than maintained separately, so it always tracks it. Used for the read-time
     * "disposable-adjacent" signal: a domain sharing one of these labels (a subdomain of a disposable
     * provider, or the same base under a different TLD) is disposable-adjacent.
     */
    public static final Set<String> BASE_LABELS = BLOCKLIST.stream()
        .map(DisposableEmailDomains::baseLabel)
        .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailDomains() {
    }

    /**
     * The second-level label of a domain: the dot-separated label immediately before its final TLD
     * (e.g. {@code "mailinator"} from {@code "mailinator.com"}), or the whole value when it carries no
     * dot. Lower-cased.
     *
     * @param domain the domain to reduce to its base label
     * @return the domain's lower-cased second-level label
     */
    private static String baseLabel(String domain) {
        String lower = domain.toLowerCase(Locale.ROOT);
        int lastDot = lower.lastIndexOf('.');
        if (lastDot < 0) {
            return lower;
        }
        int prevDot = lower.lastIndexOf('.', lastDot - 1);
        return lower.substring(prevDot + 1, lastDot);
    }
}
