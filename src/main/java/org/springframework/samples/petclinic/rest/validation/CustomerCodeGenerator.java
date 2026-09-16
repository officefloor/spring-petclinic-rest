/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Derives an owner's customer code, formatted {@code <LAST3>-<NNNN>} where LAST3 is the
 * upper-cased first three letters of the last name and NNNN is a global 4-digit,
 * zero-padded sequence equal to one more than the current number of owners
 * (e.g. {@code SMI-0007}).
 */
public final class CustomerCodeGenerator {

    private CustomerCodeGenerator() {
    }

    /**
     * Builds the customer code for a new owner.
     *
     * @param lastName          the owner's last name
     * @param currentOwnerCount the number of owners currently stored
     * @return the customer code, e.g. {@code "SMI-0007"}
     */
    public static String generate(String lastName, long currentOwnerCount) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        return String.format("%s-%04d", last3, currentOwnerCount + 1);
    }
}
