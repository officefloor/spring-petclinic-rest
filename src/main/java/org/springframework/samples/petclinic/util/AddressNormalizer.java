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

import java.util.Locale;

/**
 * Normalizes postal addresses to a canonical form.
 *
 * <p>Surrounding whitespace is trimmed and internal runs collapsed to a single space, the
 * text is upper-cased, and common street-type abbreviations are expanded (ST-&gt;STREET,
 * RD-&gt;ROAD, AVE-&gt;AVENUE). A blank input normalizes to the empty string, which callers
 * treat as a missing required field. Normalization is idempotent, so an already-normalized
 * value is returned unchanged, letting create and duplicate detection share one form.
 */
public final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /** Return {@code address} in canonical form, or {@code null} when it is {@code null}. */
    public static String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(expand(token));
        }
        return result.toString();
    }

    /**
     * Compose the canonical address from the structured lines, falling back to the flat address.
     * Returns the normalized {@code addressLine1}, with a single space and the normalized
     * {@code addressLine2} appended when {@code addressLine2} is present; when {@code addressLine1}
     * is blank the flat {@code address} is normalized and returned instead.
     */
    public static String compose(String addressLine1, String addressLine2, String address) {
        String line1 = normalize(addressLine1);
        if (line1 == null || line1.isEmpty()) {
            return normalize(address);
        }
        String line2 = normalize(addressLine2);
        return (line2 == null || line2.isEmpty()) ? line1 : line1 + " " + line2;
    }

    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
