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
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.stereotype.Component;

/**
 * Detects a soft (non-hard) duplicate for an owner being registered.
 *
 * <p>An owner is a possible duplicate of an existing owner when they share a postcode and a
 * like-sounding last name (compared by {@linkplain Soundex Soundex} code) yet carry a different
 * {@linkplain Owner#getIdentityKey() identity key}. A matching identity key is excluded because that
 * is the territory of hard-duplicate detection (a 409), not this softer signal; the identity key
 * already folds in the telephone, so two owners with the same postcode and last name but different
 * telephones surface here rather than being rejected.
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
        String lastNameCode = Soundex.encode(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
            .filter(other -> !other.isDeleted())
            .filter(other -> owner.getPostcode().equals(other.getPostcode()))
            .filter(other -> Soundex.encode(other.getLastName()).equals(lastNameCode))
            .filter(other -> !Objects.equals(identityKey, other.getIdentityKey()))
            .min(Comparator.comparing(Owner::getId))
            .orElse(null);
    }
}
