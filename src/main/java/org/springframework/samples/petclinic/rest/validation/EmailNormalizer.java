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
 * Normalizes an owner's optional email to its canonical storage form. The email is lower-cased,
 * and a value that is present must be a syntactically valid address.
 */
public final class EmailNormalizer {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailNormalizer() {
    }

    /**
     * Returns the canonical, lower-cased form of the submitted email. Email is optional, so a
     * {@code null} value is returned unchanged; any present value must be syntactically valid.
     *
     * @param email the submitted email, or {@code null} if none was provided
     * @return the lower-cased email, or {@code null} if none was provided
     * @throws InvalidEmailException if a value is present but not a syntactically valid address
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        return email.toLowerCase();
    }
}
