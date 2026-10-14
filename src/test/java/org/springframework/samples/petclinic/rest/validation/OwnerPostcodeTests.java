package org.springframework.samples.petclinic.rest.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OwnerPostcodeTests {

    @Test
    void acceptsAbsentPostcode() {
        assertThatCode(() -> OwnerPostcode.validate(null, "Sydney")).doesNotThrowAnyException();
    }

    @Test
    void acceptsPostcodesWithinTheCityRegionRange() {
        assertThatCode(() -> {
            OwnerPostcode.validate("2000", "Sydney");
            OwnerPostcode.validate("2099", "Sydney");
            OwnerPostcode.validate("3000", "Melbourne");
            OwnerPostcode.validate("3099", "Melbourne");
            OwnerPostcode.validate("4000", "Brisbane");
            OwnerPostcode.validate("4099", "Brisbane");
        }).doesNotThrowAnyException();
    }

    @Test
    void rejectsPostcodesOutsideTheCityRegionRange() {
        assertThatThrownBy(() -> OwnerPostcode.validate("1999", "Sydney")).isInstanceOf(InvalidPostcodeException.class);
        assertThatThrownBy(() -> OwnerPostcode.validate("2100", "Sydney")).isInstanceOf(InvalidPostcodeException.class);
        assertThatThrownBy(() -> OwnerPostcode.validate("2000", "Melbourne")).isInstanceOf(InvalidPostcodeException.class);
        assertThatThrownBy(() -> OwnerPostcode.validate("3000", "Brisbane")).isInstanceOf(InvalidPostcodeException.class);
    }

    @Test
    void acceptsAnyFourDigitPostcodeForCityWithoutKnownRegion() {
        assertThatCode(() -> OwnerPostcode.validate("9999", "Madison")).doesNotThrowAnyException();
    }

    @Test
    void rejectsMalformedPostcode() {
        assertThatThrownBy(() -> OwnerPostcode.validate("12", "Madison")).isInstanceOf(InvalidPostcodeException.class);
        assertThatThrownBy(() -> OwnerPostcode.validate("20a0", "Sydney")).isInstanceOf(InvalidPostcodeException.class);
    }
}
