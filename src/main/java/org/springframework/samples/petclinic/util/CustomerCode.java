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

package org.springframework.samples.petclinic.util;

/**
 * Derivation of an owner's {@code customerCode}, the region-and-hash identity every other owner value
 * is now built from. Kept separate from the owner controller and the {@code Owner} model so the rule
 * lives in one place, as pure functions with no web or persistence dependencies. Mirrors
 * {@link LocalityResolver} (which supplies the region) and {@link LuhnCheckDigit}.
 *
 * <p>The code is {@code '<REGION>-<HASH8>'}: REGION is the region derived from the owner's postcode
 * (see {@link LocalityResolver#resolveFromPostcode}) and HASH8 is the first eight upper-case
 * hexadecimal characters of the SHA-256 digest of the owner's normalized telephone concatenated with
 * the last name. It carries no sequence number, so two distinct people always receive distinct codes
 * only insofar as their {@code (telephone, lastName)} pairs differ.
 */
public abstract class CustomerCode {

    /** Separates the region and hash components of the code. */
    private static final String SEPARATOR = "-";

    /** Number of leading hex characters of the SHA-256 digest kept as the HASH8 component. */
    private static final int HASH_LENGTH = 8;

    /**
     * Build a customer code {@code '<REGION>-<HASH8>'} from an owner's region, telephone and last name.
     *
     * @param region    the region code (e.g. {@code NSW}, or {@code UNKNOWN})
     * @param telephone the owner's normalized telephone, or null
     * @param lastName  the owner's last name, or null
     * @return the customer code
     */
    public static String of(String region, String telephone, String lastName) {
        return region + SEPARATOR + hash8(telephone, lastName);
    }

    /**
     * The region (the part before the {@code '-'}) of a customer code, or {@code null} when the code is
     * {@code null}. This is how the owner's locality is now derived from its identity.
     *
     * @param customerCode a customer code, or null
     * @return the region component, or null
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }

    /**
     * First eight upper-case hex characters of SHA-256 over {@code (telephone + lastName)}, each part
     * treated as the empty string when absent.
     */
    private static String hash8(String telephone, String lastName) {
        String telephonePart = telephone == null ? "" : telephone;
        String lastNamePart = lastName == null ? "" : lastName;
        return Sha256.hex(telephonePart + lastNamePart).substring(0, HASH_LENGTH).toUpperCase();
    }

}
