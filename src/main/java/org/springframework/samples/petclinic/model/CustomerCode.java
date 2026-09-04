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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}: the
 * region derived from the owner's postcode, and the first 8 upper-case hex
 * characters of {@code SHA-256(normalizedTelephone + lastName)}, e.g.
 * {@code NSW-1A2B3C4D}.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * @param owner          the owner being coded (its postcode, telephone and last name are used)
     * @param existingOwners the current owners (unused; identity no longer depends on other owners)
     * @return the formatted customer code, e.g. {@code NSW-1A2B3C4D}
     */
    public static String of(Owner owner, Collection<Owner> existingOwners) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        return region + "-" + hash8(owner.getTelephone() + owner.getLastName());
    }

    /** The {@code <REGION>} component of a customer code, or {@code "UNKNOWN"} when absent. */
    public static String regionOf(String customerCode) {
        int dash = customerCode == null ? -1 : customerCode.indexOf('-');
        return dash < 0 ? "UNKNOWN" : customerCode.substring(0, dash);
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
