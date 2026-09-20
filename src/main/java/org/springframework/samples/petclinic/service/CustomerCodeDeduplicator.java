/*
 * Copyright 2002-2017 the original author or authors.
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Ensures a freshly generated {@code customerCode} does not collide with one an existing owner
 * already holds. When the base code is free it is returned unchanged; otherwise {@code '-<n>'} is
 * appended, using the smallest {@code n} of 2 or more that yields a code no existing owner holds.
 */
@Component
public class CustomerCodeDeduplicator {

    /**
     * De-duplicate {@code baseCode} against the codes already in use.
     *
     * @param baseCode      the freshly generated customer code
     * @param existingCodes the customer codes already held by existing owners
     * @return {@code baseCode} when it is free, otherwise {@code '<baseCode>-<n>'} with the smallest
     * {@code n} of 2 or more that is not already taken
     */
    public String deDuplicate(String baseCode, Collection<String> existingCodes) {
        Set<String> taken = new HashSet<>(existingCodes);
        if (!taken.contains(baseCode)) {
            return baseCode;
        }
        for (int n = 2; ; n++) {
            String candidate = baseCode + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }
}
