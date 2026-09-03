package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats an owner's stored E.164 telephone for humans: keep the leading '+' and country
 * code, add a space, then the national digits grouped in threes (e.g. '+61412345678' ->
 * '+61 412 345 678'). '+61' and '+1' country codes are recognised; anything else keeps its
 * digits ungrouped after the '+'. The raw stored 'telephone' is left untouched.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int codeLength = digits.startsWith("61") ? 2 : digits.startsWith("1") ? 1 : 0;
        String national = groupInThrees(digits.substring(codeLength));
        return "+" + digits.substring(0, codeLength) + " " + national;
    }

    private static String groupInThrees(String national) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }
}
