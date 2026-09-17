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
 * Derives an owner's marketing segment, formatted {@code <TIER>_<AREA>}, from the owner's own
 * fields.
 *
 * <p>The {@code TIER} is {@link #PREMIUM} once the owner's effective membership level (see
 * {@link MembershipLevel#effective(Owner)}) reaches {@value #PREMIUM_MIN_LEVEL}, otherwise
 * {@link #STANDARD}. The {@code AREA} is {@link #METRO} when the owner's locality (see
 * {@link Locality}) is a known region, otherwise {@link #REGIONAL}.
 */
public final class OwnerSegment {

    /** Tier for an owner whose effective membership level is at least {@link #PREMIUM_MIN_LEVEL}. */
    public static final String PREMIUM = "PREMIUM";

    /** Tier for an owner below {@link #PREMIUM_MIN_LEVEL}. */
    public static final String STANDARD = "STANDARD";

    /** Area for an owner whose locality resolves to a known region. */
    public static final String METRO = "METRO";

    /** Area for an owner whose locality is {@link Locality#UNKNOWN}. */
    public static final String REGIONAL = "REGIONAL";

    /** The lowest effective membership level that earns the {@link #PREMIUM} tier. */
    public static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegment() {
    }

    /**
     * Derive the segment for the given owner.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the segment, one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
     *         {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
     */
    public static String forOwner(Owner owner) {
        return tier(owner) + "_" + area(owner);
    }

    private static String tier(Owner owner) {
        return MembershipLevel.effective(owner) >= PREMIUM_MIN_LEVEL ? PREMIUM : STANDARD;
    }

    private static String area(Owner owner) {
        return Locality.UNKNOWN.equals(Locality.forPostcode(owner.getPostcode())) ? REGIONAL : METRO;
    }
}
