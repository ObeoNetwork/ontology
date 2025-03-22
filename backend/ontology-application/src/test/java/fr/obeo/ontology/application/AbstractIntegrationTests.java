/***********************************************************************************************
 * Copyright (c) 2024, 2025 Obeo. All Rights Reserved.
 * This software and the attached documentation are the exclusive ownership
 * of its authors and was conceded to the profit of Obeo S.A.S.
 * This software and the attached documentation are protected under the rights
 * of intellectual ownership, including the section "Titre II  Droits des auteurs (Articles L121-1 L123-12)"
 * By installing this software, you acknowledge being aware of these rights and
 * accept them, and as a consequence you must:
 * - be in possession of a valid license of use conceded by Obeo only.
 * - agree that you have read, understood, and will comply with the license terms and conditions.
 * - agree not to do anything that could conflict with intellectual ownership owned by Obeo or its beneficiaries
 * or the authors of this software.
 *
 * Should you not agree with these terms, you must stop to use this software and give it back to its legitimate owner.
 ***********************************************************************************************/
package fr.obeo.ontology.application;

import org.eclipse.sirius.web.infrastructure.configuration.persistence.JDBCConfiguration;
import org.eclipse.sirius.web.starter.SiriusWebStarterConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Superclass of all the integration tests used to setup the PostgreSQL docker container.
 *
 * @author sbegaudeau
 */
@SpringJUnitConfig(classes = { IntegrationTestConfiguration.class, SiriusWebStarterConfiguration.class, JDBCConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTests {
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest").withReuse(true);
        POSTGRESQL_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }
}

//@SpringJUnitConfig(classes = { IntegrationTestConfiguration.class, SiriusWebStarterConfiguration.class, JDBCConfiguration.class, RestResponseSerializationCustomizer.class })
//@ContextConfiguration(initializers = PropertyOverrideContextInitializer.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//public abstract class AbstractIntegrationTests {
//    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;
//
//    public static final KeycloakContainer KEYCLOAK_CONTAINER;
//
//    static {
//        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest").withReuse(true);
//        POSTGRESQL_CONTAINER.start();
//        KEYCLOAK_CONTAINER = new KeycloakContainer().withVerboseOutput().withReuse(true).withRealmImportFile("realm-export.json");
//        KEYCLOAK_CONTAINER.start();
//    }
//
//    @DynamicPropertySource
//    public static void registerProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
//        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
//        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
//    }
//}
