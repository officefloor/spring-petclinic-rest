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

/**
 * Utility for normalizing telephone numbers submitted when creating an owner.
 * Every non-digit character is stripped and the result must contain exactly ten
 * digits; otherwise an {@link InvalidTelephoneException} is raised so the request
 * is rejected with {@code 400 Bad Request}.
 */
public final class TelephoneNormalizer {

    private static final int REQUIRED_DIGITS = 10;

    private TelephoneNormalizer() {
    }

    /**
     * Normalize the supplied telephone by removing every non-digit character and
     * requiring exactly ten remaining digits.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the normalized 10-digit telephone
     * @throws InvalidTelephoneException if the stripped value is not exactly 10 digits
     */
    public static String normalize(String telephone) {
        String digits = digitsOnly(telephone);
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(
                "telephone must contain exactly " + REQUIRED_DIGITS + " digits after removing non-digit characters");
        }
        return digits;
    }

    /**
     * Strip every non-digit character from the supplied telephone without enforcing a
     * required length. Useful for comparing telephones that may already be stored in a
     * non-normalized form.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the telephone reduced to its digit characters (possibly empty)
     */
    public static String digitsOnly(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
