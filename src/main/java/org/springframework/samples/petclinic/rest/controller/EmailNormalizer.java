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

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Normalizes an optional owner email into its canonical, storable form.
 * <p>
 * Email is optional, so a missing (null) or blank input yields {@code null}; any supplied
 * address is trimmed and lower-cased. Syntactic validity is enforced separately by the
 * {@code @Email} Bean Validation constraint during the {@code @Valid} pass, so this class is
 * concerned only with canonicalization.
 */
@Component
public class EmailNormalizer {

    /**
     * Canonicalize {@code rawEmail} for storage.
     *
     * @param rawEmail the email as supplied by the client (may be {@code null} or blank)
     * @return the trimmed, lower-cased email, or {@code null} when none was supplied
     */
    public String normalize(String rawEmail) {
        if (!StringUtils.hasText(rawEmail)) {
            return null;
        }
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }
}
