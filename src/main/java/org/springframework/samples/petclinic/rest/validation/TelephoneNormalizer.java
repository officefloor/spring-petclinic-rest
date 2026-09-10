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

import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.stereotype.Component;

/**
 * Turns owner telephone numbers into the single canonical form that gets stored, returned and compared, so the REST
 * controllers do not have to carry the telephone rules themselves.
 *
 * <p>Two entry points share one notion of "canonical": {@link #normalize(String)} validates a raw value supplied on a
 * request and rejects anything that cannot form a valid number, while {@link #canonicalize(String)} reduces an
 * already-stored value to the same form for duplicate detection without rejecting it.
 */
@Component
public class TelephoneNormalizer {

    /**
     * Normalizes a raw request telephone into its canonical stored form. Every non-digit character is removed, then the
     * result is required to be exactly ten digits. The normalized 10-digit value is what gets stored and returned as
     * {@code telephone}.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized canonical telephone
     * @throws InvalidTelephoneException if the value is not exactly ten digits after non-digit characters are stripped
     */
    public String normalize(String telephone) {
        String digits = digitsOf(telephone);
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(
                "telephone must contain exactly 10 digits after removing non-digit characters");
        }
        return digits;
    }

    /**
     * Reduces an already-stored telephone to the canonical form used for duplicate detection. Unlike
     * {@link #normalize(String)} this never rejects its input, so existing owners are compared regardless of how their
     * number was originally formatted.
     *
     * @param telephone an owner's stored telephone, may be {@code null}
     * @return the canonical form used for comparison
     */
    public String canonicalize(String telephone) {
        return digitsOf(telephone);
    }

    private String digitsOf(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
