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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.stereotype.Component;

/**
 * Turns owner emails into the single canonical form that gets stored, returned and compared, so the REST controllers do
 * not have to carry the email rules themselves.
 *
 * <p>A syntactically valid address is already enforced by Bean Validation on the request payload, so normalization only
 * canonicalizes the value by lower-casing it. So {@code "Owner@Example.TEST"} becomes {@code "owner@example.test"}.
 *
 * <p>Unlike the address and telephone, an email is optional: a missing (blank or {@code null}) value is left untouched,
 * so callers can uniformly treat a blank result as a missing email. The same canonical form is used both for the value
 * that is stored and returned and for every email comparison (such as duplicate detection across existing owners), so
 * incidental case differences never affect the outcome.
 */
@Component
public class EmailNormalizer {

    /**
     * Normalizes an owner email into its canonical stored form by lower-casing it. A missing (blank or {@code null})
     * email is returned untouched, since the field is optional. The normalized value is what gets stored and returned as
     * {@code email}, and the same form is used when comparing an owner's email against those of existing owners.
     *
     * @param email the raw email value from the request, may be {@code null}
     * @return the lower-cased email, or the original value when none was supplied
     */
    public String normalize(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        return email.toLowerCase();
    }
}
