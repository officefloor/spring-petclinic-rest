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
import java.util.Objects;
import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Flags a would-be owner as a <em>possible</em> (soft) duplicate of an existing one. Unlike the
 * hard {@link IdentityDuplicateValidator} rule, a soft match does not reject the create: an owner
 * that shares an existing owner's last name and postcode but carries a different telephone is a
 * distinct person who merely looks like a household namesake, so it is still created and only
 * annotated with the existing owner it resembles.
 *
 * <p>A hard duplicate (whole identity key equal) is expected to have been rejected before this
 * runs, so the candidates seen here are never exact duplicates.
 */
@Component
public class PossibleDuplicateDetector {

    /**
     * @param candidate the owner about to be created, with its telephone already in canonical
     *                  (stored) form
     * @param existingOwners owners already stored to compare against (typically pre-filtered to
     *                       the candidate's last name)
     * @return the earliest-created existing owner (lowest id) that shares the candidate's postcode
     *         but has a different telephone, or empty when none does
     */
    public Optional<Owner> findPossibleDuplicate(Owner candidate, Collection<Owner> existingOwners) {
        if (candidate.getPostcode() == null) {
            return Optional.empty();
        }
        return existingOwners.stream()
            .filter(existing -> candidate.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> !Objects.equals(candidate.getTelephone(), existing.getTelephone()))
            .min(Comparator.comparing(Owner::getId));
    }
}
