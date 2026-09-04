package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns an owner's {@code customerCode}, de-duplicated against existing owners. When the
 * code computed by {@link CustomerCode} already belongs to another owner, appends
 * {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique.
 */
public final class UniqueCustomerCode {

    private UniqueCustomerCode() {
    }

    public static String assign(Owner owner, Collection<Owner> existing) {
        String base = CustomerCode.assign(owner);
        String code = base;
        int n = 1;
        while (isTaken(code, existing)) {
            code = base + "-" + (++n);
        }
        return code;
    }

    private static boolean isTaken(String code, Collection<Owner> existing) {
        for (Owner other : existing) {
            if (code.equals(other.getCustomerCode())) {
                return true;
            }
        }
        return false;
    }
}
