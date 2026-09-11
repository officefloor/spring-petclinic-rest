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
 * Defines the layout of an owner's customer code, formatted {@code "<REGION>-<HASH8>"}: the
 * canonical region (see {@link LocalityResolver}) followed by an 8 upper-hex-character hash of the
 * owner's stable identity fields (see {@link Sha256Hex}). Keeping the code's format in one place
 * gathers its construction — done at creation time, before any uniqueness suffix is applied — and
 * the extraction of its segments — done on read — into a single home, rather than spreading the
 * separator and segment offsets across the controller that builds the code and the mapper that
 * reads it back.
 */
public final class CustomerCode {

    /** Separates the code's segments, and also the uniqueness suffix appended on a collision. */
    private static final String SEPARATOR = "-";

    private CustomerCode() {
    }

    /**
     * Formats the customer-code core {@code "<REGION>-<HASH8>"} from its segments. This is the code
     * before any uniqueness suffix is appended for a collision.
     *
     * @param region the canonical region segment (e.g. {@code "NSW"})
     * @param hash8  the 8 upper-hex-character identity hash segment
     * @return the formatted customer-code core
     */
    public static String format(String region, String hash8) {
        return region + SEPARATOR + hash8;
    }

    /**
     * Returns the REGION segment of a formatted customer code: the text before the first separator,
     * so a uniqueness suffix (e.g. {@code "NSW-A1B2C3D4-2"}) does not affect the result. Returns
     * {@code null} when {@code code} is {@code null}.
     *
     * @param code a formatted customer code, or {@code null}
     * @return the code's REGION segment, or {@code null} when {@code code} is {@code null}
     */
    public static String region(String code) {
        return code == null ? null : code.substring(0, code.indexOf(SEPARATOR));
    }
}
