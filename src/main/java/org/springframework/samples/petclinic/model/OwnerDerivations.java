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
package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Map;
import java.util.Set;

/**
 * Pure derivations of an {@link Owner}'s read-only attributes.
 *
 * <p>Each method computes one derived value from an owner's own field values and
 * nothing else, so the computations stay stateless and side-effect free. {@link Owner}
 * exposes these through thin {@code @Transient} getters that pass in the relevant
 * fields; keeping the arithmetic here rather than on the entity leaves {@code Owner}
 * to describe its persistent state and relationships while its growing family of
 * derived attributes lives together in one place.
 */
final class OwnerDerivations {

    /** Fixed city-to-region table used to derive {@link #region}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high} used to derive
     *  {@link #region} in preference to the city. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Fixed region-to-timezone table (IANA names) used to derive {@link #timezone}. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /** Email domains that identify disposable, throw-away mailboxes, used to derive
     *  {@link #emailDomainIsDisposable(String)}. */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerDerivations() {
    }

    /**
     * The owner's locality, i.e. the canonical region it belongs to. This is read
     * from the unified {@code memberId} identity: the region is the leading
     * {@code <REGION>} component of the assigned member id, i.e. the run of letters
     * before its two-digit fiscal-year segment (e.g. {@code NSW} for
     * {@code NSW279F86D0817}). Until an owner has been assigned a member id (for example
     * while its create request is still being validated) this falls back to the region
     * derived directly from its own fields (see {@link #region(String, String)}).
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    static String locality(String memberId, String postcode, String city) {
        String assigned = memberIdRegion(memberId);
        if (assigned != null) {
            return assigned;
        }
        return region(postcode, city);
    }

    /**
     * The region component encoded in an assigned {@code memberId}: its leading
     * {@code <REGION>} run of letters, which precedes the two-digit fiscal-year segment
     * (e.g. {@code NSW} for {@code NSW279F86D0817}), or {@code null} when no member id has
     * been assigned yet or it carries no leading region component. Reading the region back
     * out of the identifier lives here so {@link #locality(String, String, String)} stays a
     * thin read-then-fall-back and the one place that knows the identifier's shape is
     * isolated.
     *
     * @return the encoded region, or {@code null}
     */
    private static String memberIdRegion(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i > 0 ? memberId.substring(0, i) : null;
    }

    /**
     * The canonical region derived for an owner from its own fields. The postcode is
     * consulted first: when it is present and falls within a known region's inclusive
     * 4-digit range ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD
     * 4000-4099}) that region is returned. Only when the postcode is absent or in no
     * known range does this fall back to the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}),
     * returning {@code "UNKNOWN"} when the city is not in the table either. This yields
     * the same region for the known cities while disambiguating cities that share a name
     * via their postcode.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    static String region(String postcode, String city) {
        String byPostcode = regionForPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The IANA timezone name derived from the given {@code locality} (a canonical
     * region) via the fixed region-to-timezone table ({@code NSW -> Australia/Sydney},
     * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}). Returns
     * {@code null} when the locality is not one of the known regions (for example
     * {@code "UNKNOWN"}).
     *
     * @return the IANA timezone name, or {@code null}
     */
    static String timezone(String locality) {
        return REGION_TIMEZONE.get(locality);
    }

    /**
     * The region whose inclusive postcode range contains the given postcode, or
     * {@code null} when the postcode is absent, not a four-digit number, or in no
     * known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether the given {@code postcode} is acceptable for the given {@code region}: an
     * absent postcode is always accepted, a present postcode must be exactly four
     * digits and, when the region has a known inclusive postcode range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), fall within
     * it. A region with no known range (for example {@code "UNKNOWN"}) accepts any
     * four-digit postcode.
     *
     * @return {@code true} if the postcode is absent or valid for the region
     */
    static boolean postcodeMatchesRegion(String postcode, String region) {
        if (postcode == null) {
            return true;
        }
        if (!postcode.matches("\\d{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(region);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * The owner's membership points, a non-negative score derived from its factors:
     * starting at 0, add 2 when an {@code email} is present, add 1 when
     * {@code namesakeCount} is 0, add 2 for a household of 3 or more (its
     * {@code householdSize}) and add 3 when the owner's tenure exceeds a year, measured as
     * the number of whole fiscal years elapsed from its {@code registrationDate} up to
     * {@code asOf} (the fiscal year starts on 1 July). Because a newly created owner has
     * zero elapsed fiscal years, it never earns the tenure points at creation.
     *
     * @return the membership points, 0 or more
     */
    static int membershipPoints(String email, Integer namesakeCount, Integer householdSize,
            LocalDate registrationDate, LocalDate asOf) {
        int points = 0;
        if (hasEmail(email)) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (tenureExceedsYear(registrationDate, asOf)) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4, mapped from its
     * {@link #membershipPoints(String, Integer, Integer, LocalDate, LocalDate) membership
     * points}: level 1 for 0-1 points, level 2 for 2-3, level 3 for 4-5 and level 4 for 6
     * or more.
     *
     * @return the membership level, from 1 to 4
     */
    static int membershipLevel(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The owner's membership level after applying its household level ceiling. The level
     * mapped from {@code points} (see {@link #membershipLevel(int)}) is returned unchanged
     * when {@code cap} is {@code null} (the owner joined no existing household), and
     * otherwise capped to at most {@code cap} — the ceiling assigned at creation, one above
     * the highest level then held by an existing household member — so a new owner can rise
     * to at most one level above its household.
     *
     * @return the capped membership level, from 1 to 4
     */
    static int membershipLevel(int points, Integer cap) {
        int level = membershipLevel(points);
        return cap == null ? level : Math.min(level, cap);
    }

    /**
     * The owner's marketing segment, formatted {@code <TIER>_<AREA>}, one of
     * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
     * {@code STANDARD_REGIONAL}. TIER is {@code PREMIUM} when {@code membershipLevel} is 3
     * or more, otherwise {@code STANDARD}. AREA is {@code METRO} when {@code locality} is a
     * known region ({@code NSW}, {@code VIC} or {@code QLD}), otherwise {@code REGIONAL}.
     *
     * @return the owner's segment as {@code <TIER>_<AREA>}
     */
    static String ownerSegment(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Whether the owner's tenure exceeds a year, i.e. at least one whole fiscal year has
     * elapsed from its {@code registrationDate} up to {@code asOf} (see
     * {@link #elapsedFiscalYears(LocalDate, LocalDate)}). This is the single tenure test
     * the tenure-driven membership derivations share, so they agree on exactly when an
     * owner has been a member for "over a year"; it is {@code false} whenever either date
     * is absent.
     */
    private static boolean tenureExceedsYear(LocalDate registrationDate, LocalDate asOf) {
        return registrationDate != null && asOf != null
            && elapsedFiscalYears(registrationDate, asOf) >= 1;
    }

    /**
     * The number of whole fiscal years elapsed from {@code registrationDate} up to
     * {@code asOf}, i.e. the difference between the fiscal year that contains {@code asOf}
     * and the one that contains {@code registrationDate} (see {@link #fiscalYearOf(LocalDate)}).
     * Two dates in the same fiscal year yield {@code 0}, so an owner registered in the
     * current fiscal year has zero tenure until the next 1 July rollover.
     */
    static int elapsedFiscalYears(LocalDate registrationDate, LocalDate asOf) {
        return fiscalYearOf(asOf) - fiscalYearOf(registrationDate);
    }

    /**
     * The fiscal year that contains the given date, as a four-digit calendar year. The
     * fiscal year runs from 1 July to 30 June and is labelled by the calendar year in
     * which it ends: a date from January to June falls in the fiscal year of its own
     * calendar year, while a date from July to December falls in the next calendar year's
     * fiscal year (e.g. {@code 2026-05-03 -> 2026} but {@code 2026-08-03 -> 2027}).
     */
    static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, formatted {@code FY<YY>} where YY is the last two digits of
     * the fiscal year (see {@link #fiscalYearOf(LocalDate)}) that contains its
     * business-day-adjusted {@code registrationDate}, e.g. {@code FY27} for a registration
     * date of {@code 2026-08-03}. Returns {@code null} when no registration date is present.
     *
     * @return the fiscal year as {@code FY<YY>}, or {@code null}
     */
    static String fiscalYear(LocalDate registrationDate) {
        if (registrationDate == null) {
            return null;
        }
        return "FY" + fiscalYearSegment(registrationDate);
    }

    /**
     * The two-digit fiscal-year segment for a registration date: the last two digits of the
     * fiscal year that contains it (see {@link #fiscalYearOf(LocalDate)}), zero-padded, e.g.
     * {@code "27"} for a registration date of {@code 2026-08-03}. This is the single
     * definition the fiscal-year-bearing identifiers share, so they agree on exactly which
     * two-digit year they carry.
     *
     * @return the two-digit fiscal-year segment
     */
    static String fiscalYearSegment(LocalDate registrationDate) {
        return String.format("%02d", fiscalYearOf(registrationDate) % 100);
    }

    /**
     * The owner's salutation, formed as {@code title + ' ' + lastName} when a
     * {@code title} is present, or just the {@code lastName} when no title is supplied.
     * A {@code null} or blank title contributes nothing, yielding the bare last name.
     *
     * @return the composed salutation
     */
    static String salutation(String title, String lastName) {
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an {@code email}
     * is present, otherwise {@code "PHONE"}.
     *
     * @return {@code "EMAIL"} or {@code "PHONE"}
     */
    static String contactPreference(String email) {
        if (hasEmail(email)) {
            return "EMAIL";
        }
        return "PHONE";
    }

    /**
     * Whether the owner has an email address on file, i.e. its {@code email} is present
     * and not blank. This is the single presence test the email-driven derivations share
     * (a membership-level factor and the {@link #contactPreference(String) contact
     * preference}), so they agree on exactly what counts as "has an email".
     */
    private static boolean hasEmail(String email) {
        return email != null && !email.isBlank();
    }

    /**
     * The domain component of an email address: the lower-cased run after its final
     * {@code '@'}, or the empty string when the value is absent or carries no {@code '@'}.
     * This is the single domain extraction the email-domain derivations share, so they
     * agree on exactly what an address's domain is; each then classifies that domain in
     * its own way.
     *
     * @return the lower-cased email domain, or the empty string when there is none
     */
    static String emailDomain(String email) {
        if (email == null) {
            return "";
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return "";
        }
        return email.substring(at + 1).toLowerCase();
    }

    /**
     * Whether the email's {@link #emailDomain(String) domain} is one of the known
     * disposable, throw-away mailbox domains ({@code mailinator.com}, {@code tempmail.com},
     * {@code guerrillamail.com}). An address without a domain (absent, or carrying no
     * {@code '@'}) is treated as having no disposable domain.
     *
     * @return {@code true} if the email's domain is on the disposable-domain blocklist
     */
    static boolean emailDomainIsDisposable(String email) {
        return DISPOSABLE_EMAIL_DOMAINS.contains(emailDomain(email));
    }

    /**
     * Whether the owner should be flagged as risky, i.e. any one of its risk signals
     * holds: it is a possible duplicate ({@code possibleDuplicate}), its email domain is
     * disposable-adjacent (on the disposable-domain blocklist, see
     * {@link #emailDomainIsDisposable(String)}), or its city was over its soft capacity at
     * creation ({@code capacityWarning}). The two persisted flags are treated as
     * {@code false} when absent, so the risk flag is {@code false} for an owner with no
     * signal set.
     *
     * @return {@code true} when any risk signal holds, otherwise {@code false}
     */
    static boolean riskFlag(Boolean possibleDuplicate, String email, Boolean capacityWarning) {
        return Boolean.TRUE.equals(possibleDuplicate)
            || emailDomainIsDisposable(email)
            || Boolean.TRUE.equals(capacityWarning);
    }

    /**
     * The Luhn check digit (0-9) computed over the decimal digits contained in the
     * given {@code value}, processed right-to-left with every second digit doubled (and
     * reduced by 9 when the double exceeds 9). Non-digit characters are ignored, and an
     * absent or digit-free value yields {@code 0}.
     *
     * @return the Luhn check digit, from 0 to 9
     */
    static int checkDigit(String value) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = (value == null ? 0 : value.length()) - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The owner's age band, derived from its {@code birthDate} measured against its
     * {@code registrationDate}: {@code "MINOR"} when the owner is under 18 on the
     * registration date, {@code "ADULT"} from 18 to 64, and {@code "SENIOR"} at 65 or
     * over. The age is the number of whole years between the two dates. Returns
     * {@code null} when either date is absent, so no band is reported for an owner
     * without a supplied birth date.
     *
     * @return {@code "MINOR"}, {@code "ADULT"}, {@code "SENIOR"}, or {@code null}
     */
    static String ageBand(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The single derived key used for duplicate detection: the lower-case hexadecimal
     * SHA-256 digest of {@code normalizedTelephone + '|' + lowerEmail + '|' +
     * soundex(lastName)}. The telephone is the owner's stored (E.164-normalised)
     * telephone (or the empty string when absent), the email is the stored (lower-cased)
     * email or the empty string when absent, and the last-name component is the
     * {@link #soundex(String) Soundex code} of the owner's last name. Because the
     * telephone is part of the key, two owners sharing a last name and postcode but with
     * different telephones have different keys.
     *
     * @return the owner's identity key, a 64-character lower-case hex string
     */
    static String identityKey(String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String mail = email == null ? "" : email.toLowerCase();
        return sha256Hex(tel + "|" + mail + "|" + soundex(lastName));
    }

    /**
     * The American Soundex code of the given name: its first letter followed by three
     * digits derived from the remaining consonants ({@code b,f,p,v -> 1};
     * {@code c,g,j,k,q,s,x,z -> 2}; {@code d,t -> 3}; {@code l -> 4}; {@code m,n -> 5};
     * {@code r -> 6}), where adjacent letters mapping to the same digit (including when
     * separated only by {@code h} or {@code w}) are coded once, vowels ({@code a,e,i,o,u},
     * and {@code y}) separate otherwise-identical digits, and the result is zero-padded or
     * truncated to exactly four characters. Non-letters are ignored; an absent or
     * letter-free value yields the empty string.
     *
     * @param value the name to encode (may be {@code null})
     * @return the four-character Soundex code, or the empty string
     */
    static String soundex(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (char c : value.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previousDigit = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = soundexDigit(letter);
            if (digit != '0' && digit != previousDigit) {
                code.append(digit);
            }
            if (letter != 'H' && letter != 'W') {
                previousDigit = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for a single upper-case letter, or {@code '0'} for a letter that
     * carries no Soundex digit (the vowels {@code a,e,i,o,u}, and {@code y,h,w}).
     */
    private static char soundexDigit(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    /**
     * The owner's unified member id, formatted {@code <REGION><FY><HASH8><CHK>}: REGION is the
     * {@link #identifierRegion(String, String) region embedded inside the owner's identifiers}
     * (derived from its postcode, falling back to its city), FY is the two-digit
     * {@link #fiscalYearSegment(LocalDate) fiscal-year segment} of its {@code registrationDate},
     * HASH8 is the {@link #hash8(String, String) HASH8} of its telephone and last name, and CHK
     * is a single {@link #checkDigit(String) Luhn check digit} computed over the decimal digits
     * of {@code <REGION><FY><HASH8>} (e.g. {@code NSW279F86D0817}). This unifies the former
     * customer code and membership number into one identifier.
     *
     * @return the member id
     */
    static String memberId(String postcode, String city, String telephone, String lastName,
            LocalDate registrationDate) {
        String base = identifierRegion(postcode, city) + fiscalYearSegment(registrationDate)
            + hash8(telephone, lastName);
        return base + checkDigit(base);
    }

    /**
     * The region component embedded inside the owner's identifiers — currently the leading
     * {@code <REGION>} of its {@link #memberId(String, String, String, String, LocalDate) member
     * id} — derived from the owner's own fields via {@link #region(String, String)}. Held as its
     * own derivation, kept deliberately distinct from the user-facing
     * {@link #locality(String, String, String) locality} region, so the region carried inside
     * identifiers has a single home and can evolve independently of the plain region reported to
     * callers.
     *
     * @return the region embedded inside the owner's identifiers
     */
    private static String identifierRegion(String postcode, String city) {
        return region(postcode, city);
    }

    /**
     * The HASH8 component of the region-and-hash identity: the first eight upper-case
     * hexadecimal characters of the SHA-256 digest of the owner's normalised
     * {@code telephone} concatenated with its {@code lastName} (e.g. {@code 9F86D081}).
     * Held as its own derivation so the identity's hash component has a single definition
     * rather than being spelled out at each identifier that embeds it.
     *
     * @return the eight upper-case hexadecimal HASH8 characters
     */
    static String hash8(String telephone, String lastName) {
        return sha256Hex(telephone + lastName).substring(0, 8).toUpperCase();
    }

    /**
     * The stable identifier for the household an owner belongs to, derived
     * deterministically from its {@code normalizedLastName} and {@code postcode} so that
     * every owner sharing a last name and postcode resolves to the same value. It is the
     * first twelve lower-case hexadecimal characters of the SHA-256 digest of
     * {@code normalizedLastName + '|' + postcode} (an absent postcode contributes the
     * empty string).
     *
     * @return the household identifier
     */
    static String householdId(String normalizedLastName, String postcode) {
        String pc = postcode == null ? "" : postcode;
        return sha256Hex(normalizedLastName + "|" + pc).substring(0, 12);
    }

    /**
     * Reduce an address to its canonical stored form: trim and collapse runs of
     * whitespace to a single space, upper-case, and expand common abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}) whenever
     * they appear as whole words. The result is what is stored, returned and used
     * for every household comparison; it is idempotent, so re-normalising an
     * already-normalised address leaves it unchanged.
     *
     * @param value the raw address (may be {@code null})
     * @return the normalised address, or the empty string if {@code value} is
     *         {@code null} or blank
     */
    static String normalizeAddress(String value) {
        String collapsed = collapseWhitespace(value).toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = switch (tokens[i]) {
                case "ST" -> "STREET";
                case "RD" -> "ROAD";
                case "AVE" -> "AVENUE";
                default -> tokens[i];
            };
        }
        return String.join(" ", tokens);
    }

    /**
     * Trim the given value and collapse every internal run of whitespace to a single
     * space, treating an absent value as empty. This is the single whitespace
     * normalisation the name and address normalisers share, so they agree on what
     * counts as insignificant whitespace; each then applies its own casing (and, for
     * addresses, whole-word abbreviation expansion).
     *
     * @param value the raw value (may be {@code null})
     * @return the trimmed, whitespace-collapsed value, or the empty string when
     *         {@code value} is {@code null}
     */
    static String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * The SHA-256 digest of the UTF-8 bytes of the given value, as a lower-case
     * hexadecimal string. Callers take the prefix and case they need.
     */
    static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
