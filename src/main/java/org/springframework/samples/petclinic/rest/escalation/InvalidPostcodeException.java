package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a supplied owner postcode is not acceptable: either it is not exactly four digits, or it
 * falls outside the inclusive range of the city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
 * A postcode is validated only when present; an absent postcode never raises this. Carries the value as
 * submitted so {@link InvalidPostcodeExceptionHandler} can report what was actually seen. Checked so it
 * appears in the function's {@code throws} clause and is routed as an escalation.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode, String reason) {
        super("Postcode '" + postcode + "' is invalid: " + reason);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
