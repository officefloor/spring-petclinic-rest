package org.springframework.samples.petclinic.model;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * De-duplicates an owner's customerCode against the codes already in use: the base
 * {@code <REGION>-<HASH8>} (see {@link CustomerCode}), suffixed with '-<n>' using the
 * smallest {@code n} of 2 or more that makes it unique.
 */
public final class CustomerCodes {

    private CustomerCodes() {
    }

    /** The unique customerCode for {@code owner} given the {@code existing} owners. */
    public static String uniqueFor(Owner owner, Collection<Owner> existing) {
        String base = CustomerCode.of(owner);
        Set<String> taken = existing.stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        return code;
    }
}
