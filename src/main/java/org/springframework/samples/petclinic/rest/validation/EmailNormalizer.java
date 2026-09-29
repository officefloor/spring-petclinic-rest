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

import java.util.regex.Pattern;

/**
 * Utility for normalizing the optional e-mail address submitted when creating or
 * updating an owner. A {@code null} value is left untouched (the field is optional),
 * but when a value is supplied it must be a syntactically valid address; otherwise an
 * {@link InvalidEmailException} is raised so the request is rejected with
 * {@code 400 Bad Request}. Valid values are returned lower-cased for consistent
 * storage and retrieval.
 */
public final class EmailNormalizer {

    /**
     * Pragmatic syntactic check for an e-mail address: a non-empty local part, a single
     * {@code @}, and a dotted domain whose top-level label is alphabetic.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailNormalizer() {
    }

    /**
     * Normalize the supplied e-mail address.
     *
     * @param email the raw e-mail value (may be {@code null} because the field is optional)
     * @return {@code null} when the input is {@code null}, otherwise the address lower-cased
     * @throws InvalidEmailException if a value is supplied but is not syntactically valid
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("email must be a syntactically valid address");
        }
        return email.toLowerCase();
    }
}
