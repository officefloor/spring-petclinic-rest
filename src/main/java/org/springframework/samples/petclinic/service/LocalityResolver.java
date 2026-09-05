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
package org.springframework.samples.petclinic.service;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region) from its city using a fixed
 * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), returning
 * "UNKNOWN" for any city not in the table.
 */
public final class LocalityResolver {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private LocalityResolver() {
    }

    public static String locality(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
