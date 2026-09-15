package org.springframework.samples.petclinic.rest.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Unit tests for {@link AddressResolver}: structured fields are normalized and composed into the
 * flat {@code address}, with the flat input used as a backward-compatible fallback.
 */
class AddressResolverTest {

    private final AddressResolver resolver = new AddressResolver(new AddressNormalizer());

    @Test
    void composesAddressLine1WithoutLine2() {
        OwnerFieldsDto owner = new OwnerFieldsDto();
        owner.setAddressLine1("10 king st");

        resolver.resolve(owner);

        assertThat(owner.getAddressLine1()).isEqualTo("10 KING STREET");
        assertThat(owner.getAddress()).isEqualTo("10 KING STREET");
    }

    @Test
    void appendsNormalizedAddressLine2WhenPresent() {
        OwnerFieldsDto owner = new OwnerFieldsDto();
        owner.setAddressLine1("10 king st");
        owner.setAddressLine2("apt  4");

        resolver.resolve(owner);

        assertThat(owner.getAddressLine2()).isEqualTo("APT 4");
        assertThat(owner.getAddress()).isEqualTo("10 KING STREET APT 4");
    }

    @Test
    void fallsBackToFlatAddressWhenNoStructuredLine1() {
        OwnerFieldsDto owner = new OwnerFieldsDto();
        owner.setAddress("110 w. liberty ave");

        resolver.resolve(owner);

        assertThat(owner.getAddress()).isEqualTo("110 W. LIBERTY AVENUE");
    }
}
