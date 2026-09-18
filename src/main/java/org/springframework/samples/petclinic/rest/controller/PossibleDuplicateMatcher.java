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
import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Finds, on create, an already-stored owner that a new owner softly matches: one that
 * shares the new owner's last name (compared ignoring case) and postcode while carrying a
 * different (already normalized) telephone. This is a weaker signal than the outright
 * {@link IdentityDuplicateChecker identity duplicate} that rejects a create; a soft match
 * merely flags the created owner as a possible duplicate. An owner with no postcode can
 * never softly match, since a shared postcode is part of the rule.
 */
@Component
public class PossibleDuplicateMatcher {

    private final ClinicService clinicService;

    public PossibleDuplicateMatcher(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Returns the earliest-created already-stored owner that softly matches
     * {@code candidate}, or empty when there is none.
     *
     * @param candidate the owner being created, not yet stored
     * @return the matched owner, or empty
     */
    public Optional<Owner> findMatch(Owner candidate) {
        if (candidate.getPostcode() == null || candidate.getPostcode().isBlank()) {
            return Optional.empty();
        }
        return clinicService.findAllOwners().stream()
            .filter(existing -> softlyMatches(existing, candidate))
            .min(Comparator.comparing(Owner::getId));
    }

    private boolean softlyMatches(Owner existing, Owner candidate) {
        return candidate.getPostcode().equals(existing.getPostcode())
            && equalsIgnoreCase(existing.getLastName(), candidate.getLastName())
            && !equals(existing.getTelephone(), candidate.getTelephone());
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
