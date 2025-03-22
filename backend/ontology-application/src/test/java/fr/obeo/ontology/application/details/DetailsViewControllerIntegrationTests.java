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
package fr.obeo.ontology.application.details;

import fr.obeo.ontology.application.AbstractIntegrationTests;
import fr.obeo.ontology.application.identifiers.OntologyProjectIdentifiers;
import fr.obeo.ontology.application.sql.GivenAnOntologyApplicationServer;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.sirius.components.collaborative.forms.dto.FormRefreshedEventPayload;
import org.eclipse.sirius.web.application.views.details.dto.DetailsEventInput;
import org.eclipse.sirius.web.tests.graphql.DetailsEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import graphql.execution.DataFetcherResult;
import reactor.test.StepVerifier;

/**
 * Integration tests of the details view.
 *
 * @author sbegaudeau
 * @author fbarbin
 */
@Transactional
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DetailsViewControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private DetailsEventSubscriptionRunner detailsEventSubscriptionRunner;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given a semantic object, when we subscribe to its properties events, then the form is sent")
    public void givenSemanticObjectWhenWeSubscribeToItsPropertiesEventsThenTheFormIsSent() {
        var detailsRepresentationId = this.representationIdBuilder.buildDetailsRepresentationId(List.of(OntologyProjectIdentifiers.ONTOLOGY_ROOT_ID));
        var input = new DetailsEventInput(UUID.randomUUID(), OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID, detailsRepresentationId);
        var flux = this.detailsEventSubscriptionRunner.run(input);

        Predicate<Object> formContentMatcher = object -> {
            return Optional.of(object)
                    .filter(DataFetcherResult.class::isInstance)
                    .map(DataFetcherResult.class::cast)
                    .map(DataFetcherResult::getData)
                    .filter(FormRefreshedEventPayload.class::isInstance)
                    .isPresent();
        };

        StepVerifier.create(flux)
                .expectNextMatches(formContentMatcher)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

}
