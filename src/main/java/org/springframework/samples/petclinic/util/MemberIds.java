package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's unified {@code memberId} formatted {@code '<REGION><FY><HASH8><CHK>'}:
 * the region and 8-hex identity carried by the owner's (de-duplicated) customer code, the
 * two-digit fiscal year of the registration date, and a single trailing Luhn check digit
 * computed over the digits of {@code <REGION><FY><HASH8>}. Because it reads from the stored
 * customer code, collision handling on that code carries through to the member id.
 */
public final class MemberIds {

    private MemberIds() {
    }

    /** The {@code '<REGION><FY><HASH8><CHK>'} member id, or {@code null} when unavailable. */
    public static String of(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int dash = code.indexOf('-');
        String region = dash < 0 ? code : code.substring(0, dash);
        String hash = dash < 0 ? "" : code.substring(dash + 1);
        String stem = region + String.format("%02d", FiscalYears.yearOfCentury(owner.getRegistrationDate())) + hash;
        return stem + Luhn.checkDigit(stem);
    }
}
