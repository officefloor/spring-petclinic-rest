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
 * The marketing segment an owner falls into, formatted {@code <TIER>_<AREA>}: a membership
 * {@code TIER} of {@code PREMIUM} (membership level 3 or more) or {@code STANDARD}, and a
 * geographic {@code AREA} of {@code METRO} (a known region) or {@code REGIONAL}.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /**
     * The segment for an owner with the given membership level and locality: {@code PREMIUM}
     * when {@code membershipLevel} is 3 or more (else {@code STANDARD}) joined by {@code '_'}
     * to {@code METRO} when {@code locality} is a known region (else {@code REGIONAL}).
     */
    public static String of(Integer membershipLevel, String locality) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = locality != null && !CityRegionTable.UNKNOWN.equals(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
