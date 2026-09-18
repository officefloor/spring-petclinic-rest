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

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Canonicalises an owner postal address.
 * <p>
 * Surrounding whitespace is trimmed, internal runs of whitespace are collapsed to a single
 * space and letters are upper-cased, so addresses that differ only in spacing or
 * capitalisation share a single canonical form. Common street-type abbreviations are then
 * expanded token by token ({@code ST}->{@code STREET}, {@code RD}->{@code ROAD},
 * {@code AVE}->{@code AVENUE}) so those variants canonicalise identically too. A blank input
 * canonicalises to the empty string.
 */
@Component
public class AddressNormalizer {

    /** Runs of whitespace collapsed to a single space. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Street-type abbreviations expanded to their full (upper-cased) form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Converts a raw address into its canonical form.
     *
     * @param raw the address as supplied by the client, possibly {@code null}
     * @return the canonical address, or the empty string when {@code raw} is {@code null} or
     *         blank
     */
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = WHITESPACE.matcher(raw.trim()).replaceAll(" ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        return Arrays.stream(collapsed.split(" "))
            .map(token -> ABBREVIATIONS.getOrDefault(token, token))
            .collect(Collectors.joining(" "));
    }
}
