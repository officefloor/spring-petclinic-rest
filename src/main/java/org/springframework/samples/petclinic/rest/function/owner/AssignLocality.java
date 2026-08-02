package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the locality of a newly built owner: {@code "local"} when this owner's
 * city is the single most common city among the owners that already exist at the
 * moment this owner is created, {@code "remote"} otherwise. A city only counts as
 * most common when it has strictly more owners than every other city (a tie means
 * there is no single most common city, so the result is {@code "remote"}). The new
 * owner is not yet saved, so it is excluded from the tally. Runs after the city has
 * been normalised so it compares against each city's canonical spelling.
 */
public class AssignLocality {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        Map<String, Long> cityCounts = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        long max = cityCounts.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        long numberWithMax = cityCounts.values().stream().filter(count -> count == max).count();

        boolean local = max > 0 && numberWithMax == 1 && city != null
                && cityCounts.getOrDefault(city, 0L) == max;
        owner.setLocality(local ? "local" : "remote");
    }
}
