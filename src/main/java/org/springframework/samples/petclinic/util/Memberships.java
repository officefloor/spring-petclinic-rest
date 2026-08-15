/*
 * Copyright 2016-2017 the original author or authors.
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

/**
 * Helpers for the numeric membership level a pet owner is assigned at creation. The level is
 * derived solely from the owner's own fields.
 */
public final class Memberships {

    /**
     * The lowest membership level every owner starts from.
     */
    public static final int BASE_LEVEL = 1;

    /**
     * The highest membership level derivable from an owner's own fields. Level 4 is reserved for
     * tenure and is never produced here.
     */
    public static final int MAX_LEVEL = 3;

    private Memberships() {
    }

    /**
     * Computes an owner's numeric membership level from their own fields. Starts at
     * {@link #BASE_LEVEL}; adds 1 when an email is present; adds 1 when {@code namesakeCount} is 0;
     * capped at {@link #MAX_LEVEL}.
     *
     * @param email         the owner's email, or {@code null} when absent
     * @param namesakeCount the owner's namesake count, or {@code null} when unknown
     * @return the membership level, from {@value #BASE_LEVEL} to {@value #MAX_LEVEL}
     */
    public static int membershipLevel(String email, Integer namesakeCount) {
        int level = BASE_LEVEL;
        if (email != null) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }
}
