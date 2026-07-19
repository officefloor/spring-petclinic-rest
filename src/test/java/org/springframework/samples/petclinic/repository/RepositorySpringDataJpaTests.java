package org.springframework.samples.petclinic.repository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * <p> Integration test using the 'Spring Data' profile.
 *
 * @author Michael Isvy
 * @see AbstractRepositoryTests AbstractRepositoryTests for more details. </p>
 */

@SpringBootTest
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class RepositorySpringDataJpaTests extends AbstractRepositoryTests {

    @Autowired
    EntityManager entityManager;

    @Override
    void clearCache() {
        entityManager.clear();
    }
}
