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

package org.springframework.samples.petclinic.rest.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

/**
 * Computes lower-case hex SHA-256 digests. Shared by the derived identifiers that key off a
 * hash (owner identity, household id) so the hashing lives in one place.
 */
@Component
public class Sha256 {

    /** The lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}. */
    public String hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
