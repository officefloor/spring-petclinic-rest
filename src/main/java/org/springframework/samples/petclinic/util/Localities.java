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

import java.util.Map;

/**
 * Resolves an owner's locality (region) from their city using a fixed lookup table.
 *
 * <p>The mapping is pinned to Sydney-&gt;NSW, Melbourne-&gt;VIC and Brisbane-&gt;QLD. Any
 * city outside the table resolves to {@code UNKNOWN}.
 */
public final class Localities {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Localities() {
    }

    /** Return the canonical region for {@code city}, or {@code UNKNOWN} when it is not in the table. */
    public static String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
