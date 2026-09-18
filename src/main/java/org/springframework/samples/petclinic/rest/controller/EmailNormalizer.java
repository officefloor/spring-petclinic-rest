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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Validates and canonicalises an owner email address.
 * <p>
 * A canonical email is lower-cased so addresses that differ only in letter case are stored
 * and compared identically. An address is considered syntactically valid when it is a single
 * {@code local-part@domain} pair whose domain carries a dotted label.
 */
@Component
public class EmailNormalizer {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * @param email an email address as supplied by the client
     * @return {@code true} when {@code email} is a syntactically valid address
     */
    public boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * @param email an already-validated email address
     * @return the address in canonical lower-cased form
     */
    public String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
