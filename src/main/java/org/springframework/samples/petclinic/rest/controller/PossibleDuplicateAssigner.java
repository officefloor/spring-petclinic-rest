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

import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Records, on create, whether a new owner softly matches an already-stored one (see
 * {@link PossibleDuplicateMatcher}). Call this before the owner is saved so the match is
 * computed against the owners stored before this create and not the owner itself.
 */
@Component
public class PossibleDuplicateAssigner {

    private final PossibleDuplicateMatcher possibleDuplicateMatcher;

    public PossibleDuplicateAssigner(PossibleDuplicateMatcher possibleDuplicateMatcher) {
        this.possibleDuplicateMatcher = possibleDuplicateMatcher;
    }

    /**
     * Assigns {@code owner}'s possible-duplicate flag and, when it softly matches an
     * existing owner, the matched owner's id.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        Optional<Owner> match = possibleDuplicateMatcher.findMatch(owner);
        owner.setPossibleDuplicate(match.isPresent());
        owner.setPossibleDuplicateOf(match.map(Owner::getId).orElse(null));
    }
}
