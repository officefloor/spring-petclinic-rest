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

import java.util.Collection;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;

/** Soft-match detection for a new owner against the existing owners. */
public final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * Id of an existing owner that shares {@code owner}'s lastName and postcode but has a different
     * telephone (a possible, non-hard duplicate), or {@code null} when there is no such owner.
     */
    public static Integer matchIn(Owner owner, Collection<Owner> existing) {
        if (owner.getPostcode() == null) {
            return null;
        }
        for (Owner other : existing) {
            if (owner.getLastName().equalsIgnoreCase(other.getLastName())
                && owner.getPostcode().equals(other.getPostcode())
                && !Objects.equals(owner.getTelephone(), other.getTelephone())) {
                return other.getId();
            }
        }
        return null;
    }
}
