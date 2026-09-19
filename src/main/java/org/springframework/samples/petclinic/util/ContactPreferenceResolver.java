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

/**
 * Derives an owner's preferred contact channel from the details on file: {@code EMAIL}
 * when an email address is present, otherwise {@code PHONE}.
 */
public final class ContactPreferenceResolver {

    /** The preference used when an email address is on file. */
    private static final String EMAIL = "EMAIL";

    /** The preference used when no email address is on file. */
    private static final String PHONE = "PHONE";

    private ContactPreferenceResolver() {
    }

    /**
     * Resolve the owner's contact preference.
     *
     * @param email the owner's email address, may be {@code null}
     * @return {@code "EMAIL"} when an email address is present, otherwise {@code "PHONE"}
     */
    public static String resolve(String email) {
        return (email != null && !email.isBlank()) ? EMAIL : PHONE;
    }
}
