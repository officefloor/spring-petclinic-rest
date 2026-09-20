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
 * Composes an owner's salutation from its optional honorific title and last name:
 * {@code "<title> <lastName>"} when a title is on file, or just the last name otherwise.
 */
public final class Salutation {

    private Salutation() {
    }

    /**
     * Returns the owner's salutation: the title followed by a single space and the last
     * name when a non-blank title is present, otherwise the last name alone. Returns
     * {@code null} for a {@code null} owner.
     */
    public static String forOwner(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }
}
