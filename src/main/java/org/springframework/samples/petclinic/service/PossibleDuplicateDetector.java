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

import java.util.Comparator;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.TextNormalizer;
import org.springframework.stereotype.Component;

/**
 * Detects a soft (non-hard) duplicate for an owner being registered.
 *
 * <p>An owner is a possible duplicate of an existing owner when they share a last
 * name (compared case-insensitively, consistent with the rest of the application's
 * name handling) and a postcode but carry a different telephone. A matching
 * telephone is excluded because that is the territory of hard-duplicate detection
 * (see {@link Owner#getIdentityKey()}), not this softer signal.
 */
@Component
public class PossibleDuplicateDetector {

    private final OwnerRepository ownerRepository;

    public PossibleDuplicateDetector(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Find the existing owner the given owner possibly duplicates, or {@code null}
     * when there is none. When several existing owners match, the one with the
     * lowest id (the earliest registered) is returned.
     *
     * @param owner the owner being registered
     * @return the matching existing owner, or {@code null} if none
     */
    public Owner findPossibleDuplicate(Owner owner) {
        if (owner.getPostcode() == null) {
            return null;
        }
        String lastName = TextNormalizer.normalizeForComparison(owner.getLastName());
        return ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(other -> !other.isDeleted())
            .filter(other -> TextNormalizer.normalizeForComparison(other.getLastName()).equals(lastName))
            .filter(other -> owner.getPostcode().equals(other.getPostcode()))
            .filter(other -> !Objects.equals(owner.getTelephone(), other.getTelephone()))
            .min(Comparator.comparing(Owner::getId))
            .orElse(null);
    }
}
