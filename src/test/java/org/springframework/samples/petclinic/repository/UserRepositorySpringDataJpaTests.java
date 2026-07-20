package org.springframework.samples.petclinic.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class UserRepositorySpringDataJpaTests extends AbstractUserRepositoryTests {

}
