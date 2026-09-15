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
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Normalizes an optional submitted email to its canonical stored form: a syntactically
 * valid address, lower-cased. An absent (null) email is left as {@code null}.
 */
@Component
public class EmailNormalizer {

    /**
     * A pragmatic syntactic check: a non-empty local part, an {@code @}, and a domain
     * containing at least one dot, with no whitespace or stray {@code @} characters.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * @param email the raw submitted email (may be {@code null})
     * @return {@code null} if the input is {@code null}; otherwise the address lower-cased
     * @throws InvalidEmailException if the value is present but not a syntactically valid address
     */
    public String normalize(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        return email.toLowerCase(Locale.ROOT);
    }
}
