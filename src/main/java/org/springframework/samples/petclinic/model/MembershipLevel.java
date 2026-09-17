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
 * Derives an owner's numeric membership level from their {@link MembershipPoints}.
 *
 * <p>Points map to a level as follows: {@code 0-1} points is level {@value #MIN},
 * {@code 2-3} is level 2, {@code 4-5} is level 3, and {@code 6} or more is level
 * {@value #MAX}.
 */
public final class MembershipLevel {

    /** The lowest level attainable. */
    public static final int MIN = 1;

    /** The highest level attainable. */
    public static final int MAX = 4;

    private MembershipLevel() {
    }

    /**
     * Derive the membership level for the given owner.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the level, from {@value #MIN} to {@value #MAX}.
     */
    public static int of(Owner owner) {
        return forPoints(MembershipPoints.of(owner));
    }

    /**
     * The owner's effective membership level: the value fixed at creation time (capped against
     * the household) when one was recorded, otherwise the level derived from the owner's points.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the effective level, from {@value #MIN} to {@value #MAX}.
     */
    public static int effective(Owner owner) {
        Integer recorded = owner.getMembershipLevel();
        return recorded != null ? recorded : of(owner);
    }

    /**
     * Cap a level so it exceeds the highest level already held in the household by at most one.
     *
     * @param level             the owner's own derived level.
     * @param householdMaxLevel the maximum effective level among the household's existing members.
     * @return {@code level}, reduced to {@code householdMaxLevel + 1} when it would exceed it.
     */
    public static int cappedToHousehold(int level, int householdMaxLevel) {
        return Math.min(level, householdMaxLevel + 1);
    }

    /**
     * Map a membership points total to its level.
     *
     * @param points the membership points (see {@link MembershipPoints}).
     * @return the level, from {@value #MIN} to {@value #MAX}.
     */
    public static int forPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return MAX;
    }
}
