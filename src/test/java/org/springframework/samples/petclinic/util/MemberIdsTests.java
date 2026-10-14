package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MemberIdsTests {

    @Test
    void buildsRegionFiscalYearHashAndCheckDigit() {
        // Luhn over the digits 2,7,3,9,2,7 of "NSW273FA9C27B" is 4.
        assertEquals("NSW273FA9C27B4", MemberIds.of("NSW", LocalDate.parse("2026-10-14"), "3FA9C27B"));
        assertEquals("VIC263FA9C27B6", MemberIds.of("VIC", LocalDate.parse("2026-06-30"), "3FA9C27B"));
    }

    @Test
    void parsesRegionAndFiscalYear() {
        assertEquals("NSW", MemberIds.regionOf("NSW273FA9C27B4"));
        assertEquals("UNKNOWN", MemberIds.regionOf("UNKNOWN27ABCDEF012"));
        assertEquals(27, MemberIds.fiscalYearOf("NSW273FA9C27B4-2"));
        assertEquals("QLD", MemberIds.regionOf("QLD0700000000" + MemberIds.luhn("QLD0700000000") + "-12"));
    }

    @Test
    void rejectsOtherValues() {
        assertNull(MemberIds.regionOf(null));
        assertNull(MemberIds.regionOf("NSW-3FA9C27B"));
        assertNull(MemberIds.fiscalYearOf("NSW273FA9C27B"));
    }
}
