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
 * Builds an owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>}: the
 * upper-cased first three letters of the last name, and a global 4-digit
 * zero-padded sequence one greater than the current number of owners.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * @param lastName      the owner's last name
     * @param existingOwners the current number of owners (this one excluded)
     * @return the formatted customer code, e.g. {@code SMI-0007}
     */
    public static String of(String lastName, int existingOwners) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        return String.format("%s-%04d", last3, existingOwners + 1);
    }
}
