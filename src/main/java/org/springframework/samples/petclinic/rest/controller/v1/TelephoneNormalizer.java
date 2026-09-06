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
import java.util.Map;

import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;

/**
 * Normalizes a submitted telephone number into the canonical form that is stored for an owner and
 * compared when detecting duplicate telephones. Kept apart from {@link OwnerRestControllerV1} so the
 * request handler stays focused on orchestration while the rules for what makes a telephone
 * acceptable, and how it is canonicalized, live in one place.
 *
 * <p>The rule canonicalizes to E.164: spaces, dashes and brackets are stripped; a leading '+' with a
 * country code is kept as-is, otherwise country code '+61' is assumed and a single leading '0' is
 * dropped from the national digits. The result must be a '+' followed by 8 to 15 digits. That '+' and
 * digit string is what gets stored and returned.
 *
 * <p>For the country codes with a known fixed-length numbering plan the national number (the digits
 * that follow the country code) must additionally be exactly the expected length: '+61' (Australia)
 * requires 9 national digits and '+1' (NANP) requires 10. A value whose national-number length is
 * wrong for its country is rejected as an invalid telephone.
 */
final class TelephoneNormalizer {

    /**
     * Country codes (including the leading '+') whose numbering plan fixes the national-number length,
     * mapped to that required number of national digits. A normalized number beginning with one of
     * these country codes must carry exactly this many digits after the country code. Country codes
     * absent from this map are not length-checked beyond the general 8-to-15 digit E.164 bound.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY_CODE = Map.of(
        "+61", 9,
        "+1", 10);

    private TelephoneNormalizer() {
    }

    /**
     * Normalizes a raw telephone value for owner creation into E.164 form. The normalized value is both
     * what gets stored and what duplicate detection compares against, so two inputs that canonicalize to
     * the same value are treated as the same telephone.
     *
     * <p>Spaces, dashes and brackets are removed. A value beginning with '+' keeps its leading '+' and
     * country code; any other value assumes country code '+61' and drops a single leading '0' from the
     * national digits. The result must be a '+' followed by 8 to 15 digits.
     *
     * @param telephone the raw telephone value submitted by the client
     * @return the normalized E.164 telephone number (a '+' followed by 8 to 15 digits)
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    static String normalize(String telephone) {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s\\-()\\[\\]{}]", "");

        String e164;
        if (cleaned.startsWith("+")) {
            e164 = "+" + cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }

        if (!e164.matches("^\\+[0-9]{8,15}$")) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        for (Map.Entry<String, Integer> plan : NATIONAL_LENGTH_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = plan.getKey();
            if (e164.startsWith(countryCode)) {
                int nationalLength = e164.length() - countryCode.length();
                if (nationalLength != plan.getValue()) {
                    throw new InvalidOwnerFieldsException(List.of("telephone"));
                }
                break;
            }
        }
        return e164;
    }

}
