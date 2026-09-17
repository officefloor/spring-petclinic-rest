package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Business-day calendar rules, including the fixed public-holiday roll-forward. */
class BusinessDayTest {

	@Test
	void weekdayIsBusinessDay() {
		// Friday, 2026-09-18
		assertTrue(BusinessDay.isBusinessDay(LocalDate.of(2026, 9, 18)));
	}

	@Test
	void weekendIsNotBusinessDay() {
		assertFalse(BusinessDay.isBusinessDay(LocalDate.of(2026, 9, 19))); // Saturday
		assertFalse(BusinessDay.isBusinessDay(LocalDate.of(2026, 9, 20))); // Sunday
	}

	@Test
	void listedHolidayIsNotBusinessDay() {
		assertFalse(BusinessDay.isBusinessDay(LocalDate.of(2026, 12, 25))); // Fri, Christmas
	}

	@Test
	void businessDayIsUnchanged() {
		LocalDate friday = LocalDate.of(2026, 9, 18);
		assertEquals(friday, BusinessDay.onOrAfter(friday));
	}

	@Test
	void weekendRollsToMonday() {
		// Sat 2026-09-19 -> Mon 2026-09-21
		assertEquals(LocalDate.of(2026, 9, 21), BusinessDay.onOrAfter(LocalDate.of(2026, 9, 19)));
	}

	@Test
	void holidayOnWeekdayRollsToNextBusinessDay() {
		// Christmas is Fri 2026-12-25; Sat/Sun follow; Mon 2026-12-28 is also a listed holiday;
		// so it rolls to Tue 2026-12-29.
		assertEquals(LocalDate.of(2026, 12, 29), BusinessDay.onOrAfter(LocalDate.of(2026, 12, 25)));
	}

	@Test
	void newYearsDayRollsToFriday() {
		// Thu 2026-01-01 is a holiday -> Fri 2026-01-02.
		assertEquals(LocalDate.of(2026, 1, 2), BusinessDay.onOrAfter(LocalDate.of(2026, 1, 1)));
	}

	@Test
	void anzacDayRollsPastWeekend() {
		// Sat 2026-04-25 (holiday) -> Sun 26th -> Mon 2026-04-27.
		assertEquals(LocalDate.of(2026, 4, 27), BusinessDay.onOrAfter(LocalDate.of(2026, 4, 25)));
	}

	@Test
	void australiaDayRollsToTuesday() {
		// Mon 2026-01-26 is a holiday -> Tue 2026-01-27.
		assertEquals(LocalDate.of(2026, 1, 27), BusinessDay.onOrAfter(LocalDate.of(2026, 1, 26)));
	}
}
