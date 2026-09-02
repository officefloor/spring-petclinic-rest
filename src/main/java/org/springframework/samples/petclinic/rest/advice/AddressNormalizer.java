/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.advice;

/**
 * Canonicalizes an owner's address: trims and collapses whitespace, upper-cases, and expands the
 * common abbreviations ST -> STREET, RD -> ROAD, AVE -> AVENUE (whole words only). Returns
 * {@code null} for a {@code null} input; a whitespace-only input normalizes to the empty string.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    static String normalize(String address) {
        if (address == null) {
            return null;
        }
        return address.strip().replaceAll("\\s+", " ").toUpperCase()
            .replaceAll("\\bST\\b", "STREET")
            .replaceAll("\\bRD\\b", "ROAD")
            .replaceAll("\\bAVE\\b", "AVENUE");
    }
}
