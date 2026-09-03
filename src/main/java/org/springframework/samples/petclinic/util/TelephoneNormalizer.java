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

/**
 * Normalizes telephone numbers to E.164 form.
 *
 * <p>Spaces, dashes and brackets are stripped. A leading '+' with its country code is
 * kept as given; otherwise country code '+61' is assumed and a single leading '0' is
 * dropped from the national digits. The result is expected to carry 8 to 15 digits after
 * the '+', which callers enforce (Bean Validation / DTO pattern) so an unformattable
 * input is rejected with 400. Normalization is idempotent, so a value already in E.164
 * form is returned unchanged.
 */
public final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /** Return {@code telephone} in E.164 form, or {@code null} when it is {@code null}. */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String digits = telephone.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+")) {
            return "+" + digits.substring(1).replace("+", "");
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return "+61" + digits;
    }
}
