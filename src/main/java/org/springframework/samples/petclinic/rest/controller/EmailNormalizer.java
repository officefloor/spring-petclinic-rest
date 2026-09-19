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
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Normalizes an owner's optional email address on create/update.
 *
 * <p>The email is optional: a {@code null} or blank value is treated as absent and left
 * unset. When a value is present it must be a syntactically valid address whose domain is
 * not on the disposable-domain blocklist; it is then stored and returned lower-cased.
 */
@Component
public class EmailNormalizer {

    /**
     * A pragmatic syntactic check: a non-empty local part, a single {@code @}, and a
     * domain containing at least one dot, none of the segments holding whitespace.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final DisposableEmailDomainValidator disposableEmailDomainValidator;

    public EmailNormalizer(DisposableEmailDomainValidator disposableEmailDomainValidator) {
        this.disposableEmailDomainValidator = disposableEmailDomainValidator;
    }

    /**
     * Normalizes an optional email address.
     *
     * @param raw the submitted email value, possibly {@code null} or blank
     * @return the lower-cased email, or {@code null} when no value was supplied
     * @throws InvalidEmailException if a value is present but not a valid address
     * @throws DisposableEmailDomainException if the domain is on the disposable-domain blocklist
     */
    public String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(raw);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        this.disposableEmailDomainValidator.validate(normalized);
        return normalized;
    }
}
