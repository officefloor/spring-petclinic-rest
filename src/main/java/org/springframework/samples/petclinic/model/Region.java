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
package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The Australian region an owner's city belongs to. Each constant's name is the canonical region
 * code as stored and returned (e.g. {@code "NSW"}). Regions are resolved from a single fixed
 * city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD});
 * a city that is not in the table has no known region.
 *
 * <p>This is the one place the city-to-region mapping lives, so every rule that keys off an owner's
 * region - such as the derived {@code locality} - resolves it identically.
 */
public enum Region {

    NSW(2000, 2099, "Australia/Sydney"),
    VIC(3000, 3099, "Australia/Melbourne"),
    QLD(4000, 4099, "Australia/Brisbane");

    private final int minPostcode;

    private final int maxPostcode;

    private final String timezone;

    Region(int minPostcode, int maxPostcode, String timezone) {
        this.minPostcode = minPostcode;
        this.maxPostcode = maxPostcode;
        this.timezone = timezone;
    }

    /**
     * Returns this region's IANA timezone name from the fixed region-to-timezone table
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}).
     *
     * @return the region's IANA timezone name
     */
    public String timezone() {
        return this.timezone;
    }

    /**
     * Resolves the IANA timezone name for the given canonical region code from the fixed
     * region-to-timezone table ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}). This is the one place the region-to-timezone mapping lives,
     * so every rule that labels an owner by timezone resolves it identically.
     *
     * @param code the canonical region code (e.g. {@code "NSW"}), or {@code null}
     * @return the matching IANA timezone name, or {@code null} when the code is {@code null} or has no
     *         known timezone (such as {@link #UNKNOWN_CODE})
     */
    public static String timezoneForCode(String code) {
        return forCode(code).map(Region::timezone).orElse(null);
    }

    /** Fixed city -> region table; a city absent from this map has no known region. */
    private static final Map<String, Region> BY_CITY = Map.of(
        "Sydney", NSW,
        "Melbourne", VIC,
        "Brisbane", QLD);

    /** Canonical region code returned when neither the postcode nor the city resolves to a region. */
    public static final String UNKNOWN_CODE = "UNKNOWN";

    /**
     * Reports whether the given 4-digit postcode falls within this region's inclusive postcode range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). This is the one place the region-to-postcode
     * range lives, so every rule that validates a postcode against an owner's region resolves it
     * identically.
     *
     * @param postcode the numeric value of the owner's 4-digit postcode
     * @return {@code true} when the postcode is within this region's range
     */
    public boolean acceptsPostcode(int postcode) {
        return postcode >= this.minPostcode && postcode <= this.maxPostcode;
    }

    /**
     * Resolves the region named by the given canonical region code (e.g. {@code "NSW"}) by matching it
     * against the region constants' {@linkplain #name() names}. This is the by-code counterpart of
     * {@link #forCity(String)} and {@link #forPostcode(String)}, for a value that is already a region
     * code rather than a city or postcode, and is the one place a code is turned back into its
     * {@link Region}, so every rule that keys off a region code resolves it identically.
     *
     * @param code the canonical region code (e.g. {@code "NSW"}), or {@code null}
     * @return the matching region, or empty when the code is {@code null} or names no known region
     *         (such as {@link #UNKNOWN_CODE})
     */
    public static Optional<Region> forCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return firstMatching(region -> region.name().equals(code));
    }

    /**
     * Resolves the region for the given city from the fixed city-to-region table.
     *
     * @param city the owner's city, or {@code null}
     * @return the matching region, or empty when the city is {@code null} or has no known region
     */
    public static Optional<Region> forCity(String city) {
        return city == null ? Optional.empty() : Optional.ofNullable(BY_CITY.get(city));
    }

    /**
     * Resolves the region whose postcode range contains the given 4-digit postcode
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     *
     * @param postcode the owner's 4-digit postcode, or {@code null}
     * @return the matching region, or empty when the postcode is {@code null}, not numeric, or in no
     *         known range
     */
    public static Optional<Region> forPostcode(String postcode) {
        if (postcode == null) {
            return Optional.empty();
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        return firstMatching(region -> region.acceptsPostcode(value));
    }

    /**
     * Returns the first region constant matching the given predicate, or empty when none does. This is
     * the one place the "scan {@link #values()} for the first matching region" loop lives, so
     * {@link #forCode(String)} and {@link #forPostcode(String)} resolve a region identically, each
     * supplying only its own match condition.
     *
     * @param predicate the condition a region must satisfy to be selected
     * @return the first matching region, or empty when none matches
     */
    private static Optional<Region> firstMatching(Predicate<Region> predicate) {
        for (Region region : values()) {
            if (predicate.test(region)) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves the canonical region code for an owner located at the given {@code postcode} and
     * {@code city}, preferring the postcode: it first looks up the region by
     * {@link #forPostcode(String) postcode range} (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and
     * only falls back to the {@link #forCity(String) city-to-region} table ({@code Sydney -> NSW},
     * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}) when the postcode is absent or in no known
     * range. Returns the region's code, or {@link #UNKNOWN_CODE} when neither resolves.
     *
     * <p>This is the one place an owner's postcode and city are turned into a region code, so every
     * rule that labels an owner by region resolves it identically.
     *
     * @param postcode the owner's 4-digit postcode, or {@code null}
     * @param city the owner's city, or {@code null}
     * @return the canonical region code, or {@link #UNKNOWN_CODE} when neither postcode nor city resolves
     */
    public static String code(String postcode, String city) {
        return forPostcode(postcode)
            .or(() -> forCity(city))
            .map(Region::name)
            .orElse(UNKNOWN_CODE);
    }
}
