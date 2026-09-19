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

/**
 * Normalizes email addresses to their canonical lower-cased form, so an owner's email is
 * always stored and returned the same way regardless of the casing the client supplied.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    /**
     * Lower-case the given email value.
     *
     * @param email the raw email value (may be {@code null})
     * @return the lower-cased email, or {@code null} when {@code email} is {@code null}
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.toLowerCase(Locale.ROOT);
    }
}
