/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level, fixed at creation. Starts at 1,
 * adds 1 when an email is present, adds 1 when {@code namesakeCount} is 0, and is
 * capped at 3. Level 4 is reserved for tenure and is never produced here.
 */
public final class MembershipLevel {

    /** Highest level this derivation produces; level 4 is reserved for tenure. */
    private static final int CAP = 3;

    private MembershipLevel() {
    }

    /**
     * @param owner the owner whose level to derive (its {@code email} and
     *              {@code namesakeCount} decide the level).
     * @return the membership level, from 1 to {@value #CAP}.
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, CAP);
    }
}
