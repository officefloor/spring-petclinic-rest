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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's preferred contact channel: {@code "EMAIL"} when an email is
 * present, otherwise {@code "PHONE"}.
 */
public final class ContactPreference {

    /** Returned when the owner has an email address. */
    public static final String EMAIL = "EMAIL";

    /** Returned when the owner has no email address. */
    public static final String PHONE = "PHONE";

    private ContactPreference() {
    }

    /**
     * @param owner the owner whose contact preference to derive (its {@code email}
     *              decides the channel).
     * @return {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}.
     */
    public static String of(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isEmpty()) ? EMAIL : PHONE;
    }
}
