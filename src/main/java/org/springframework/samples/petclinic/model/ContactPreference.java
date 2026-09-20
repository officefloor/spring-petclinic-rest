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
 * Derives an owner's preferred contact channel: {@link #EMAIL} when the owner has an
 * email address on file, otherwise {@link #PHONE}.
 */
public final class ContactPreference {

    /** Preferred channel for owners that have an email address on file. */
    public static final String EMAIL = "EMAIL";

    /** Preferred channel for owners without an email address. */
    public static final String PHONE = "PHONE";

    private ContactPreference() {
    }

    /**
     * Returns {@link #EMAIL} when the owner has a non-blank email address on file,
     * otherwise {@link #PHONE} (including when the owner is {@code null}).
     */
    public static String forOwner(Owner owner) {
        return owner != null && owner.hasEmail() ? EMAIL : PHONE;
    }
}
