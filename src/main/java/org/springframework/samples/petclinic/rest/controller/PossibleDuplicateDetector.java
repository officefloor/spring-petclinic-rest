/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Comparator;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Flags soft duplicates on owner creation. Unlike the hard {@link DuplicateIdentityValidator}
 * (which rejects an owner that re-registers an existing identity key), a soft match never blocks
 * creation: when an existing owner has a different identity key but shares the candidate's last-name
 * sound ({@link Soundex}) and postcode, the candidate is recorded as a possible duplicate of that
 * owner.
 */
@Component
public class PossibleDuplicateDetector {

    private final ClinicService clinicService;

    private final OwnerIdentity ownerIdentity;

    private final Soundex soundex;

    public PossibleDuplicateDetector(ClinicService clinicService, OwnerIdentity ownerIdentity,
            Soundex soundex) {
        this.clinicService = clinicService;
        this.ownerIdentity = ownerIdentity;
        this.soundex = soundex;
    }

    /**
     * Record whether {@code candidate} is a possible duplicate of an existing owner. Sets
     * {@code possibleDuplicate} true and {@code possibleDuplicateOf} to the earliest existing owner
     * whose identity key differs but whose last-name sound and postcode match the candidate;
     * otherwise sets {@code possibleDuplicate} false and leaves {@code possibleDuplicateOf} unset.
     *
     * @param candidate the owner about to be created, with its fields already normalized
     */
    public void detect(Owner candidate) {
        Owner match = findMatch(candidate);
        candidate.setPossibleDuplicate(match != null);
        if (match != null) {
            candidate.setPossibleDuplicateOf(match.getId());
        }
    }

    private Owner findMatch(Owner candidate) {
        if (candidate.getPostcode() == null) {
            return null;
        }
        String identityKey = ownerIdentity.key(candidate);
        String lastNameSound = soundex.of(candidate.getLastName());
        return clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> candidate.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> lastNameSound.equals(soundex.of(existing.getLastName())))
            .filter(existing -> !identityKey.equals(ownerIdentity.key(existing)))
            .min(Comparator.comparing(Owner::getId))
            .orElse(null);
    }
}
