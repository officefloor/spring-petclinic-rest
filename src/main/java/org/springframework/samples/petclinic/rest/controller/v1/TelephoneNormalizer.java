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

import java.util.List;

import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;

/**
 * Normalizes a submitted telephone number into the canonical form that is stored for an owner and
 * compared when detecting duplicate telephones. Kept apart from {@link OwnerRestControllerV1} so the
 * request handler stays focused on orchestration while the rules for what makes a telephone
 * acceptable, and how it is canonicalized, live in one place.
 *
 * <p>The current rule strips every non-digit character and requires exactly ten digits to remain;
 * that stripped, ten-digit value is what gets stored and returned.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Normalizes a raw telephone value for owner creation. The normalized value is both what gets
     * stored and what duplicate detection compares against, so two inputs that canonicalize to the
     * same value are treated as the same telephone.
     *
     * @param telephone the raw telephone value submitted by the client
     * @return the normalized ten-digit telephone number
     * @throws InvalidOwnerFieldsException if the value does not contain exactly ten digits after stripping
     */
    static String normalize(String telephone) {
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return digits;
    }

}
