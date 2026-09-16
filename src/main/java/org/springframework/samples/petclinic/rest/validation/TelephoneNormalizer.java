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
 * Normalizes an owner's telephone to its canonical storage form: every non-digit character is
 * removed and the result must contain exactly 10 digits.
 */
public final class TelephoneNormalizer {

    private static final int REQUIRED_DIGITS = 10;

    private TelephoneNormalizer() {
    }

    /**
     * Strips every non-digit character from the submitted telephone and returns the resulting
     * 10-digit value.
     *
     * @param telephone the submitted telephone, possibly containing formatting characters
     * @return the normalized 10-digit telephone
     * @throws InvalidTelephoneException if the value does not contain exactly 10 digits once stripped
     */
    public static String normalize(String telephone) {
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(
                "Telephone must contain exactly " + REQUIRED_DIGITS + " digits after removing non-digit characters");
        }
        return digits;
    }
}
