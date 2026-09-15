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
 * Computes an owner's numeric membership level from its stored fields. The level starts at
 * {@link #BASE_LEVEL}, gains a point when the owner has an email and another when it has no
 * namesakes (a {@code namesakeCount} of 0), and is capped at {@link #MAX_LEVEL} ({@code 4}
 * is reserved for tenure).
 */
public abstract class MembershipLevelCalculator {

    /** Level every owner starts at on creation. */
    public static final int BASE_LEVEL = 1;

    /** Highest level this calculator awards; level 4 is reserved for tenure. */
    public static final int MAX_LEVEL = 3;

    /**
     * Return the membership level for the given owner: {@link #BASE_LEVEL}, plus one when an
     * email is present and one when {@code namesakeCount} is 0, capped at {@link #MAX_LEVEL}.
     */
    public static int levelOf(Owner owner) {
        int level = BASE_LEVEL;
        if (owner.hasEmail()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

}
