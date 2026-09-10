package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;

/**
 * Shared knowledge of the owner {@code customerCode} string format, kept in one place so every
 * value derived from it stays consistent. The customerCode is {@code <REGION>-<HASH8>}, where
 * {@code REGION} is the region code derived from the owner's postcode (see
 * {@link Localities#locality(String, String)}) and {@code HASH8} is the first 8 upper-case hex
 * characters of the SHA-256 digest over the normalized (E.164) telephone concatenated with the
 * last name (see {@link Sha256#hexPrefix(String, int)}).
 *
 * <p>It is a stable identity carrying no sequence number; when the {@link #base base} value
 * collides with an existing owner's customerCode the producer appends {@code -<n>} (see
 * {@code org.springframework.samples.petclinic.rest.function.owner.AssignOwnerCustomerCode}).
 * Every value that follows from the customerCode — the {@link #membershipNumber membership
 * number}, the create audit line and the {@link Localities#localityOf locality/timezone} read
 * back from its {@link #regionOf region} prefix — is built through this class so they all agree
 * on the one format.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * The base {@code <REGION>-<HASH8>} customerCode for a region and the owner's identity
     * inputs, before any collision suffix. {@code lastName} is treated as empty when null.
     */
    public static String base(String region, String normalizedTelephone, String lastName) {
        String name = lastName == null ? "" : lastName;
        return region + "-" + Sha256.hexPrefix(normalizedTelephone + name, 8);
    }

    /**
     * The region encoded in a stored customerCode — the {@code REGION} prefix before the first
     * {@code '-'} — or {@code null} when the code is absent or carries no region prefix (e.g.
     * legacy records).
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash > 0 ? customerCode.substring(0, dash) : null;
    }

    /**
     * The membership number {@code <customerCode>-M<YY>}, where {@code YY} is the two-digit
     * fiscal year of the registration date (see {@link FiscalYear#twoDigit(LocalDate)}), or
     * {@code null} when the customerCode or registration date is absent.
     */
    public static String membershipNumber(String customerCode, LocalDate registrationDate) {
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return customerCode + "-M" + FiscalYear.twoDigit(registrationDate);
    }
}
