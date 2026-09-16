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
package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level from the owner's own fields.
 *
 * <p>The level starts at {@value #MIN}; a present email adds one; having no
 * namesakes (namesakeCount is 0) adds one; the total is capped at {@value #MAX}.
 * Level 4 is reserved for tenure and is not awarded here.
 */
public final class MembershipLevel {

    /** The level every owner starts at. */
    public static final int MIN = 1;

    /** The highest level derivable from an owner's own fields; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    private MembershipLevel() {
    }

    /**
     * Derive the membership level for the given owner.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the level, from {@value #MIN} to {@value #MAX}.
     */
    public static int of(Owner owner) {
        int level = MIN;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX);
    }
}
