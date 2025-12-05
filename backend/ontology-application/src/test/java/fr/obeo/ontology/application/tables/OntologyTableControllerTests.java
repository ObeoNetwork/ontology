/*******************************************************************************
 * Copyright (c) 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package fr.obeo.ontology.application.tables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import fr.obeo.ontology.application.AbstractIntegrationTests;
import fr.obeo.ontology.application.identifiers.OntologyProjectIdentifiers;
import fr.obeo.ontology.application.sql.GivenAnOntologyApplicationServer;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.collaborative.tables.TableRefreshedEventPayload;
import org.eclipse.sirius.web.tests.services.api.IGivenCreatedTableSubscription;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Integration tests of the Ontology diagram.
 *
 * @author lfasani
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OntologyTableControllerTests extends AbstractIntegrationTests {

    private static final String MISSING_TABLE = "Missing table";

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IGivenCreatedTableSubscription givenCreatedTableSubscription;

    @Autowired
    private OntologyTableDescriptionProvider ontologyTableDescriptionProvider;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    private Flux<Object> givenSubscriptionToTable() {
        var input = new CreateRepresentationInput(
                UUID.randomUUID(),
                OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID,
                this.ontologyTableDescriptionProvider.getRepresentationDescriptionId(),
                OntologyProjectIdentifiers.ONTOLOGY_NAMESPACE_ID,
                "Ontology Table"
        );
        return this.givenCreatedTableSubscription.createAndSubscribe(input);
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given a table representation, when we subscribe to its event, then the representation data are received")
    public void givenTableRepresentationWhenWeSubscribeToItsEventThenTheRepresentationDataAreReceived() {
        var flux = this.givenSubscriptionToTable();

        Consumer<Object> initialTableContentConsumer = payload -> Optional.of(payload)
                .filter(TableRefreshedEventPayload.class::isInstance)
                .map(TableRefreshedEventPayload.class::cast)
                .map(TableRefreshedEventPayload::table)
                .ifPresentOrElse(table -> {
                    assertThat(table).isNotNull();
                    assertThat(table).isNotNull();
                    assertThat(table.getColumns()).hasSize(2);
                }, () -> fail(MISSING_TABLE));

        StepVerifier.create(flux)
                .consumeNextWith(initialTableContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}
