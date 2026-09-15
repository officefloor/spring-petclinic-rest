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
package org.springframework.samples.petclinic.rest;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Counts an owner's namesakes, i.e. existing owners that share the same first and last
 * name compared case-insensitively.
 */
@Component
public class NamesakeCounter {

    /**
     * Count how many of the given existing owners share the candidate's first and last
     * name, comparing both case-insensitively.
     *
     * @param candidate      the owner about to be created
     * @param existingOwners owners already stored (typically pre-filtered by last name)
     * @return the number of existing owners sharing the candidate's first and last name
     */
    public int count(Owner candidate, Collection<Owner> existingOwners) {
        return (int) existingOwners.stream()
            .filter(existing -> existing.getFirstName().equalsIgnoreCase(candidate.getFirstName())
                && existing.getLastName().equalsIgnoreCase(candidate.getLastName()))
            .count();
    }
}
