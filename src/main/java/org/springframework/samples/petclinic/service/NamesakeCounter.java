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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.TextNormalizer;
import org.springframework.stereotype.Component;

/**
 * Counts the owners already registered that share a given owner's name.
 *
 * <p>Two owners are namesakes when both their first and last names match,
 * compared case-insensitively (surrounding and repeated whitespace is also
 * ignored, consistent with the rest of the application's name handling).
 */
@Component
public class NamesakeCounter {

    private final OwnerRepository ownerRepository;

    public NamesakeCounter(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Count the currently persisted owners that share the given owner's first
     * and last name.
     *
     * @param owner the owner being registered
     * @return the number of existing namesakes
     */
    public int count(Owner owner) {
        String firstName = TextNormalizer.normalizeForComparison(owner.getFirstName());
        String lastName = TextNormalizer.normalizeForComparison(owner.getLastName());
        return (int) ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(other -> TextNormalizer.normalizeForComparison(other.getLastName()).equals(lastName))
            .filter(other -> TextNormalizer.normalizeForComparison(other.getFirstName()).equals(firstName))
            .count();
    }
}
