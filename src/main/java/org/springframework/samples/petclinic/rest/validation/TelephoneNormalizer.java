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

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Normalizes a submitted telephone to its canonical stored form: every non-digit
 * character is removed and the result must be exactly ten digits.
 */
@Component
public class TelephoneNormalizer {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private static final int REQUIRED_DIGITS = 10;

    /**
     * @param telephone the raw submitted telephone (may be {@code null})
     * @return the telephone reduced to exactly ten digits
     * @throws InvalidTelephoneException if the value does not contain exactly ten digits
     *         once every non-digit character has been stripped
     */
    public String normalize(String telephone) {
        String digits = telephone == null ? "" : NON_DIGIT.matcher(telephone).replaceAll("");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(telephone);
        }
        return digits;
    }
}
