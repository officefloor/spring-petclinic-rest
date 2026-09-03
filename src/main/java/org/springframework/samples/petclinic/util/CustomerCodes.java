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
package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Builds and reads the {@code <REGION>-<HASH8>} owner customer code, where REGION is the
 * region resolved from the postcode and HASH8 is the first 8 upper-case hex characters of
 * SHA-256 over (normalizedTelephone + lastName).
 */
public final class CustomerCodes {

    private CustomerCodes() {
    }

    /** Build the {@code <REGION>-<HASH8>} code from the postcode region and the telephone+lastName hash. */
    public static String build(String city, String postcode, String telephone, String lastName) {
        return Localities.regionFor(city, postcode) + "-" + hash8(telephone + lastName);
    }

    /** The region portion of a customer code (the text before the first {@code '-'}), or null when absent. */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return String.format("%02X%02X%02X%02X", digest[0], digest[1], digest[2], digest[3]);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
