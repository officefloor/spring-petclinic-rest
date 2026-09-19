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

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}, by combining
 * their membership tier with the area implied by their locality.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /**
     * Compose the owner's segment from their membership level and locality.
     *
     * @param membershipLevel the owner's membership level
     * @param locality the owner's canonical region (locality), may be {@code null}
     * @return the segment, one of {@code 'PREMIUM_METRO'}, {@code 'PREMIUM_REGIONAL'},
     * {@code 'STANDARD_METRO'} or {@code 'STANDARD_REGIONAL'}
     */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = LocalityResolver.isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
