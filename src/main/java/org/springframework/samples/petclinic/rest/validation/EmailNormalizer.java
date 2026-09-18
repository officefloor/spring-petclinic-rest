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

/**
 * Normalizes an owner's optional email address to its canonical lower-cased form. Syntactic
 * validation is handled declaratively by the {@code @Email} constraint on the request DTO;
 * this normalizer only applies the case transformation once the value is known to be valid.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    /**
     * Returns the supplied email lower-cased, or {@code null} when no email was provided.
     *
     * @param email the email as submitted, possibly {@code null} or blank
     * @return the lower-cased email, or {@code null} when the input is {@code null} or blank
     */
    public static String normalize(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
