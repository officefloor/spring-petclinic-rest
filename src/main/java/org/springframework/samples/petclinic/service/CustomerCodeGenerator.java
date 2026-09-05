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
package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's customerCode as '<REGION>-<HASH8>': the region code derived from the
 * postcode, and the first eight upper-case hex characters of SHA-256 over the owner's
 * normalized telephone concatenated with the last name.
 */
public final class CustomerCodeGenerator {

    private CustomerCodeGenerator() {
    }

    public static String generate(Collection<Owner> existing, Owner owner) {
        String region = LocalityResolver.locality(owner.getCity(), owner.getPostcode());
        return region + "-" + hash8(owner.getTelephone() + owner.getLastName());
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
