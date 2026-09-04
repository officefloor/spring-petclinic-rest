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

package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalization and comparison of owner email addresses. Kept separate from the owner controller
 * and the {@code Owner} model so the rules for shaping and comparing an email live in one place,
 * as pure functions with no web or persistence dependencies. Mirrors {@link TelephoneNormalizer}
 * and {@link HouseholdNormalizer}, which do the same for telephones and households.
 *
 * <p>A submitted email is reduced to its canonical form — trimmed and lower-cased — which is the
 * value stored and returned. The value is accepted only when it is a syntactically valid address.
 * Two emails denote the same address when their {@linkplain #toComparisonKey(String) comparison
 * keys} — their lower-cased forms — are equal.
 */
public abstract class EmailNormalizer {

    /**
     * Syntactic validation for an owner email: a non-empty local part, an '@', and a dotted domain
     * whose top-level label is at least two letters. Deliberately conservative so plainly malformed
     * input (e.g. a value without an '@') is rejected.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Domains of disposable / throwaway email providers that an owner email is not permitted to use.
     * Kept lower-cased so a submitted address, once reduced to its canonical (lower-cased) form, can be
     * matched against this set directly.
     */
    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Reduce a submitted email to its canonical form — trimmed and lower-cased — which is the value
     * to be stored and returned. The result is accepted only when the trimmed value is a
     * syntactically valid address (see {@link #EMAIL_PATTERN}).
     *
     * @param rawEmail the submitted email (non-null)
     * @return the canonical (trimmed, lower-cased) email, or {@link Optional#empty()} if
     *         {@code rawEmail} is not a syntactically valid address
     */
    public static Optional<String> normalize(String rawEmail) {
        String email = rawEmail.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Optional.empty();
        }
        return Optional.of(email.toLowerCase(Locale.ROOT));
    }

    /**
     * The key used to decide whether two emails denote the same address: the email's lower-cased
     * form, so emails stored in different letter cases still compare equal. A value that is not a
     * syntactically valid address (e.g. legacy data) falls back to its plain lower-cased form.
     *
     * @param email a stored or already-normalized email (non-null)
     * @return the comparison key
     */
    public static String toComparisonKey(String email) {
        return normalize(email).orElseGet(() -> email.toLowerCase(Locale.ROOT));
    }

    /**
     * Whether the given email's domain is on the disposable-domain blocklist (see
     * {@link #DISPOSABLE_DOMAINS}). The domain is the part after the last {@code '@'}, compared
     * case-insensitively. A value with no {@code '@'} is treated as having no blocked domain.
     *
     * @param email a submitted or already-normalized email (non-null)
     * @return {@code true} if the email's domain is on the blocklist
     */
    public static boolean hasDisposableDomain(String email) {
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return DISPOSABLE_DOMAINS.contains(domain);
    }

}
