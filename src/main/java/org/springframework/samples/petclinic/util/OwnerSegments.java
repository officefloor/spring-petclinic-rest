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
 * Resolves an owner's '&lt;TIER&gt;_&lt;AREA&gt;' segment from their membership level and region.
 *
 * <p>TIER is {@code PREMIUM} at membership level 3 or above, otherwise {@code STANDARD}. AREA is
 * {@code METRO} for a known region (NSW/VIC/QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegments {

    private OwnerSegments() {
    }

    /** Compose the segment from the resolved {@code membershipLevel} and {@code region}. */
    public static String of(int membershipLevel, String region) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = Localities.timezoneFor(region) != null ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
