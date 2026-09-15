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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.Soundex;
import org.springframework.stereotype.Component;

/**
 * Flags a would-be owner as a <em>possible</em> (soft) duplicate of an existing one. Unlike the
 * hard {@link IdentityDuplicateValidator} rule, a soft match does not reject the create: an owner
 * whose {@link IdentityKey} differs from every existing owner's yet whose last name (compared by
 * {@link Soundex}) and postcode match an existing owner is a distinct person who merely looks like a
 * household namesake, so it is still created and only annotated with the existing owner it resembles.
 *
 * <p>A hard duplicate (whole identity key equal) is expected to have been rejected before this
 * runs, so the candidates seen here are never exact duplicates.
 */
@Component
public class PossibleDuplicateDetector {

    /**
     * @param candidate the owner about to be created, with its telephone and email already in
     *                  canonical (stored) form
     * @param existingOwners owners already stored to compare against (typically pre-filtered to
     *                       the candidate's last name)
     * @return the earliest-created existing owner (lowest id) whose Soundex last name and postcode
     *         match the candidate's while its identity key differs, or empty when none does
     */
    public Optional<Owner> findPossibleDuplicate(Owner candidate, Collection<Owner> existingOwners) {
        if (candidate.getPostcode() == null) {
            return Optional.empty();
        }
        String candidateKey = IdentityKey.of(candidate);
        String candidateSoundex = Soundex.encode(candidate.getLastName());
        return existingOwners.stream()
            .filter(existing -> candidate.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> candidateSoundex.equals(Soundex.encode(existing.getLastName())))
            .filter(existing -> !candidateKey.equals(IdentityKey.of(existing)))
            .min(Comparator.comparing(Owner::getId));
    }
}
