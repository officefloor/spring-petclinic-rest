package org.springframework.samples.petclinic.rest.function.owner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class BusinessDaysTest {

	@Test
	void weekdayUnchanged() {
		LocalDate tuesday = LocalDate.of(2026, 3, 10);
		assertEquals(tuesday, BusinessDays.rollForward(tuesday));
	}

	@Test
	void weekendRollsToMonday() {
		assertEquals(LocalDate.of(2026, 3, 16), BusinessDays.rollForward(LocalDate.of(2026, 3, 14)));
		assertEquals(LocalDate.of(2026, 3, 16), BusinessDays.rollForward(LocalDate.of(2026, 3, 15)));
	}

	@Test
	void weekdayHolidayRollsToNextBusinessDay() {
		// 2026-01-01 is a Thursday holiday; the next business day is Friday the 2nd.
		assertEquals(LocalDate.of(2026, 1, 2), BusinessDays.rollForward(LocalDate.of(2026, 1, 1)));
		// 2026-01-26 is a Monday holiday; the next business day is Tuesday the 27th.
		assertEquals(LocalDate.of(2026, 1, 27), BusinessDays.rollForward(LocalDate.of(2026, 1, 26)));
	}

	@Test
	void holidayFallingOnWeekendRollsPastBoth() {
		// 2026-04-25 is a Saturday holiday; roll past the weekend to Monday the 27th.
		assertEquals(LocalDate.of(2026, 4, 27), BusinessDays.rollForward(LocalDate.of(2026, 4, 25)));
	}

	@Test
	void chainsPastConsecutiveWeekendAndHolidays() {
		// 2026-12-25 Fri holiday -> weekend -> 2026-12-28 Mon holiday -> Tue the 29th.
		assertEquals(LocalDate.of(2026, 12, 29), BusinessDays.rollForward(LocalDate.of(2026, 12, 25)));
	}
}
