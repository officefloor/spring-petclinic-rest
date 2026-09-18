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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Assigns, on create, the owner's numeric membership level.
 * <p>
 * The level starts at {@link #BASE_LEVEL}, gains a point when the owner has an email
 * address and another when the owner has no namesakes ({@code namesakeCount} is 0), and
 * is capped at {@link #MAX_CREATION_LEVEL}. Level {@code 4} is reserved for tenure and is
 * never assigned here. As it reads the owner's namesake count, this must be assigned after
 * {@link NamesakeCounter} and before the owner is saved.
 */
@Component
public class MembershipLevelAssigner {

    /** The level every owner starts at. */
    static final int BASE_LEVEL = 1;

    /** The highest level earnable from the owner's own fields on create; level 4 is reserved for tenure. */
    static final int MAX_CREATION_LEVEL = 3;

    /**
     * Assigns {@code owner}'s membership level from its own fields. Call this after the
     * namesake count has been assigned and before the owner is saved.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        int level = BASE_LEVEL;
        if (hasEmail(owner)) {
            level++;
        }
        if (hasNoNamesakes(owner)) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_CREATION_LEVEL));
    }

    private boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    private boolean hasNoNamesakes(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }
}
