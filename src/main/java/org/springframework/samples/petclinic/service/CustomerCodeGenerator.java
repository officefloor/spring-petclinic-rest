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

import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where
 * {@code REGION} is the region code derived from the owner's postcode and {@code HASH8}
 * is the first eight upper-case hexadecimal characters of the SHA-256 digest over the
 * owner's normalized telephone followed by their last name (e.g. {@code NSW-1A2B3C4D}).
 */
@Component
public class CustomerCodeGenerator {

    private static final int HASH_LENGTH = 8;

    /**
     * Build the customer code for an owner.
     *
     * @param region    the owner's region code, forming the leading segment
     * @param telephone the owner's normalized telephone; hashed together with the last name
     * @param lastName  the owner's last name; hashed together with the telephone
     * @return the formatted customer code, e.g. {@code NSW-1A2B3C4D}
     */
    public String generate(String region, String telephone, String lastName) {
        return region + "-" + Sha256Hex.upperHexPrefix(telephone + lastName, HASH_LENGTH);
    }
}
