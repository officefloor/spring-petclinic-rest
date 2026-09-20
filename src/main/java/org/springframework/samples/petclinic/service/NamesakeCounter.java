/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Counts how many existing owners share a candidate owner's first and last name,
 * comparing both names case-insensitively.
 */
@Component
public class NamesakeCounter {

    /**
     * Count the owners in {@code existingOwners} whose first and last name match the
     * candidate's, ignoring case.
     *
     * @param candidate      the owner being created
     * @param existingOwners the owners to compare against (the candidate itself must not be included)
     * @return the number of matching namesakes
     */
    public int count(Owner candidate, Collection<Owner> existingOwners) {
        String firstName = candidate.getFirstName();
        String lastName = candidate.getLastName();
        return (int) existingOwners.stream()
            .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName()))
            .count();
    }
}
