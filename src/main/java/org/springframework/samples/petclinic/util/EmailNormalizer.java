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

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Produces the canonical stored form of an owner's email address.
 * <p>
 * Centralising this here keeps the REST controllers thin: they delegate to
 * {@link #normalize(String)} when canonicalising an incoming value before it is persisted, on
 * both the create and update paths, so an email is stored and returned in one consistent form.
 * <p>
 * The canonical form lower-cases the value. Request-level bean validation on the owner DTO has
 * already guaranteed that any non-null value is a syntactically valid address, so this only
 * canonicalises; a {@code null} email (email is optional) is left untouched. Because values that
 * differ only in letter case normalize to the same result, comparing normalized emails reliably
 * treats them as equal.
 */
@Component
public class EmailNormalizer {

    /**
     * Normalizes the supplied email to its canonical stored form by lower-casing it. Bean
     * validation on the request DTO has already guaranteed that any non-null value is a
     * syntactically valid address (rejecting anything else with 400).
     *
     * @param email the raw email value (may be {@code null})
     * @return the lower-cased email, or {@code null} if the input was {@code null}
     */
    public String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
