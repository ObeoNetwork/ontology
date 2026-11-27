/*******************************************************************************
 * Copyright (c) 2026 Obeo.
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
package fr.obeo.ontology.application.validation;

import static org.assertj.core.api.Assertions.assertThat;

import fr.obeo.ontology.application.AbstractIntegrationTests;
import fr.obeo.ontology.application.identifiers.OntologyProjectIdentifiers;
import fr.obeo.ontology.application.sql.GivenAnOntologyApplicationServer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sirius.components.collaborative.forms.dto.FormRefreshedEventPayload;
import org.eclipse.sirius.components.collaborative.validation.dto.ValidationEventInput;
import org.eclipse.sirius.components.collaborative.validation.dto.ValidationRefreshedEventPayload;
import org.eclipse.sirius.components.forms.Textfield;
import org.eclipse.sirius.components.forms.tests.navigation.FormNavigator;
import org.eclipse.sirius.web.application.views.details.dto.DetailsEventInput;
import org.eclipse.sirius.web.tests.graphql.DetailsEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.graphql.ValidationEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Tests the standard Validation subscription and its reaction to semantic changes.
 *
 * @author lfasani
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationViewControllerIntegrationTests extends AbstractIntegrationTests {

    private static final String ROOT_NAME_WARNING = "The ontology root has no name.";

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private ValidationEventSubscriptionRunner validationEventSubscriptionRunner;

    @Autowired
    private DetailsEventSubscriptionRunner detailsEventSubscriptionRunner;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @Autowired
    private EditValidationRootNameExecutor editValidationRootNameExecutor;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given an ontology, when its name is removed and restored, then Validation publishes and removes the warning")
    public void givenOntologyWhenNameRemovedAndRestoredThenValidationPublishesAndRemovesWarning() {
        var editingContextId = OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID;
        var validationInput = new ValidationEventInput(UUID.randomUUID(), editingContextId, this.representationIdBuilder.buildValidationRepresentationId());
        var validationFlux = this.validationEventSubscriptionRunner.run(validationInput).flux();
        var detailsId = this.representationIdBuilder.buildDetailsRepresentationId(List.of(OntologyProjectIdentifiers.ONTOLOGY_ROOT_ID));
        var detailsInput = new DetailsEventInput(UUID.randomUUID(), editingContextId, detailsId);
        var detailsFlux = this.detailsEventSubscriptionRunner.run(detailsInput).flux().filter(FormRefreshedEventPayload.class::isInstance);
        var formId = new AtomicReference<String>();
        var textfieldId = new AtomicReference<String>();
        var originalName = new AtomicReference<String>();

        var flux = Flux.merge(validationFlux, detailsFlux)
                .filter(payload -> payload instanceof ValidationRefreshedEventPayload || formId.get() == null);

        StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .expectNextCount(2)
                .consumeRecordedWith(payloads -> {
                    assertThat(payloads).filteredOn(FormRefreshedEventPayload.class::isInstance).singleElement().satisfies(payload -> {
                        var form = ((FormRefreshedEventPayload) payload).form();
                        var textfield = new FormNavigator(form).page("Properties").group("General").findWidget("Name", Textfield.class);
                        formId.set(form.getId());
                        textfieldId.set(textfield.getId());
                        originalName.set(textfield.getValue());
                        assertThat(originalName.get()).isNotBlank();
                    });
                    assertThat(payloads).filteredOn(ValidationRefreshedEventPayload.class::isInstance).singleElement()
                            .satisfies(payload -> this.assertRootNameWarning(payload, false));
                })
                .then(() -> this.editValidationRootNameExecutor.execute(editingContextId, formId.get(), textfieldId.get(), "").isSuccess())
                .consumeNextWith(payload -> this.assertRootNameWarning(payload, true))
                .then(() -> this.editValidationRootNameExecutor.execute(editingContextId, formId.get(), textfieldId.get(), originalName.get()).isSuccess())
                .consumeNextWith(payload -> this.assertRootNameWarning(payload, false))
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    private void assertRootNameWarning(Object payload, boolean expected) {
        assertThat(payload).isInstanceOf(ValidationRefreshedEventPayload.class);
        var diagnostics = ((ValidationRefreshedEventPayload) payload).validation().getDiagnostics();
        var warnings = diagnostics.stream().filter(diagnostic -> ROOT_NAME_WARNING.equals(diagnostic.getMessage())).toList();
        if (expected) {
            assertThat(warnings).singleElement().satisfies(diagnostic -> assertThat(diagnostic.getKind()).isEqualTo("Warning"));
        } else {
            assertThat(warnings).isEmpty();
        }
    }
}
