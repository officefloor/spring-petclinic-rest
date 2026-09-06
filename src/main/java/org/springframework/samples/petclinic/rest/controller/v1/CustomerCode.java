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

package org.springframework.samples.petclinic.rest.controller.v1;

/**
 * Composes an owner's customer code. Kept apart from {@link OwnerRestControllerV1} so the request
 * handler stays focused on orchestration while the rule for how a customer code is composed lives in
 * one place, alongside the other owner-field derivations. The controller supplies the pieces that
 * depend on stored state (such as the per-city sequence); this helper only assembles the code.
 *
 * <p>The code is formatted {@code <CITY3>-<LAST3>-<NNNN>}: CITY3 is the upper-cased first three
 * letters of the city, LAST3 the upper-cased first three letters of the last name, and NNNN a
 * per-city 4-digit zero-padded sequence (for example {@code SYD-SMI-0007}).
 */
final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * Assembles the customer code for an owner from its city, last name and the per-city sequence
     * number the owner is being assigned.
     *
     * @param city     the owner's city, whose first three letters form the CITY3 segment
     * @param lastName the owner's last name, whose first three letters form the LAST3 segment
     * @param sequence the owner's per-city sequence number, rendered as the 4-digit NNNN segment
     * @return the assembled customer code
     */
    static String forSequence(String city, String lastName, int sequence) {
        return String.format("%s-%s-%04d", prefix3(city), prefix3(lastName), sequence);
    }

    /**
     * Returns the upper-cased first three letters of {@code value} (fewer when {@code value} is
     * shorter than three characters).
     */
    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

}
