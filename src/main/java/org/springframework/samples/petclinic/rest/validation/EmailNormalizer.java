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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.error.InvalidEmailException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Normalizes an optionally-submitted email address: a present value must be a syntactically valid
 * address and is returned lower-cased, while an absent (null or blank) value is left unset.
 */
@Component
public class EmailNormalizer {

    /**
     * A single {@code @} separating a non-empty local part from a domain that carries at least one
     * dot-separated label; no whitespace is allowed anywhere.
     */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /**
     * @param email the raw, optionally-submitted email address
     * @return the trimmed, lower-cased address, or {@code null} when no address was supplied
     * @throws InvalidEmailException if a value is present but not a syntactically valid address
     */
    public String normalize(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
