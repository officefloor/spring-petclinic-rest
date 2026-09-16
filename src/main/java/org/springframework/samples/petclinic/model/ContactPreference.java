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
package org.springframework.samples.petclinic.model;

/**
 * The preferred channel for contacting an owner, derived from the owner's own fields:
 * {@link #EMAIL} when an email address is present, otherwise {@link #PHONE}.
 */
public enum ContactPreference {

    /** Preferred when the owner has an email address. */
    EMAIL,

    /** Preferred when the owner has no email address. */
    PHONE;

    /**
     * Derive the contact preference for the given owner.
     *
     * @param owner the owner (must not be {@code null}).
     * @return {@link #EMAIL} when the owner has a non-blank email, otherwise {@link #PHONE}.
     */
    public static ContactPreference forOwner(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? EMAIL : PHONE;
    }
}
