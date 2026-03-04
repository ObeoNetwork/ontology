/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package fr.obeo.ontology.application.diagrams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import fr.obeo.ontology.application.AbstractIntegrationTests;
import fr.obeo.ontology.application.identifiers.OntologyProjectIdentifiers;
import fr.obeo.ontology.application.sql.GivenAnOntologyApplicationServer;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramRefreshedEventPayload;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.diagrams.tests.navigation.DiagramNavigator;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLSubscriptionResult;
import org.eclipse.sirius.web.tests.services.api.IGivenCreatedDiagramSubscription;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.test.StepVerifier;

/**
 * Integration tests of the Ontology diagram.
 *
 * @author lfasani
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OntologyDiagramControllerTests extends AbstractIntegrationTests {

    private static final String MISSING_DIAGRAM = "Missing diagram";

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IGivenCreatedDiagramSubscription givenCreatedDiagramSubscription;

    @Autowired
    private OntologyDiagramDescriptionProvider ontologyDiagramDescriptionProvider;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given a ontology diagram on a core Entity, when it is opened, then entities are visible")
    public void givenDomainDiagramOnStudioWhenItIsOpenedThenEntitiesAreVisible() {
        var input = new CreateRepresentationInput(UUID.randomUUID(), OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID,
                this.ontologyDiagramDescriptionProvider.getRepresentationDescriptionId(),
                OntologyProjectIdentifiers.ONTOLOGY_CORE_ENTITY_ID, "Ontology Diagram");
        GraphQLSubscriptionResult graphQLSubscriptionResult = this.givenCreatedDiagramSubscription.createAndSubscribe(input);

        Consumer<Object> initialDiagramContentConsumer = payload -> Optional.of(payload)
                .filter(DiagramRefreshedEventPayload.class::isInstance)
                .map(DiagramRefreshedEventPayload.class::cast)
                .map(DiagramRefreshedEventPayload::diagram)
                .ifPresentOrElse(diagram -> {
                    Assertions.assertThat(diagram.getNodes().size()).isEqualTo(4);
                    Assertions.assertThat(diagram.getEdges().size()).isEqualTo(7);
                    var entityRootNode = new DiagramNavigator(diagram).nodeWithLabel("Entity Root").getNode();
                    assertThat(entityRootNode.getBorderNodes()).hasSize(1);

                    var entity1_1Node = new DiagramNavigator(diagram).nodeWithLabel("Entity level 1.1").getNode();
                    var container1 = new DiagramNavigator(diagram).nodeWithLabel("Level 1").getNode();
                    var entity1_1_1Node = new DiagramNavigator(diagram).nodeWithLabel("Entity level 1.1.1").getNode();
                    var entity1_1_2Node = new DiagramNavigator(diagram).nodeWithLabel("Entity level 1.1.2").getNode();
                    var entity1_2_1Node = new DiagramNavigator(diagram).nodeWithLabel("Entity level 1.2.1").getNode();

                    assertThat(diagram.getLayoutData().nodeLayoutData().get(entity1_1_1Node.getId()).position().x()).isEqualTo(20);
                    assertThat(diagram.getLayoutData().nodeLayoutData().get(entity1_1_1Node.getId()).position().y()).isEqualTo(50);
                    assertThat(diagram.getLayoutData().nodeLayoutData().get(entity1_1_2Node.getId()).position().y()).isEqualTo(120);
                    assertThat(diagram.getLayoutData().nodeLayoutData().get(entity1_2_1Node.getId()).position().y()).isEqualTo(190);

                }, () -> fail(MISSING_DIAGRAM));

        StepVerifier.create(graphQLSubscriptionResult.flux())
                .consumeNextWith(initialDiagramContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}
