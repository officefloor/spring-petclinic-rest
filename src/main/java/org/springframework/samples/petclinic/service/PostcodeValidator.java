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
 * Validates an owner's optional 4-digit postcode against the postcode range of its
 * city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region ("UNKNOWN") accepts any 4-digit postcode.
 */
public final class PostcodeValidator {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private PostcodeValidator() {
    }

    /**
     * Return the postcode unchanged when absent or valid for the city's region; throw
     * {@link IllegalArgumentException} when a supplied postcode is out of range.
     */
    public static String validate(String city, String postcode) {
        if (postcode == null) {
            return null;
        }
        int[] range = REGION_RANGE.get(LocalityResolver.locality(city));
        int value = Integer.parseInt(postcode);
        if (range != null && (value < range[0] || value > range[1])) {
            throw new IllegalArgumentException("Postcode " + postcode + " is not valid for city " + city);
        }
        return postcode;
    }
}
