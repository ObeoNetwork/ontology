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
package fr.obeo.ontology.application.details;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sirius.components.forms.tests.FormEventPayloadConsumer.assertRefreshedFormThat;

import fr.obeo.ontology.application.AbstractIntegrationTests;
import fr.obeo.ontology.application.identifiers.OntologyProjectIdentifiers;
import fr.obeo.ontology.application.sql.GivenAnOntologyApplicationServer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.function.Predicate;

import org.eclipse.sirius.components.collaborative.forms.dto.FormCapabilitiesRefreshedEventPayload;
import org.eclipse.sirius.components.collaborative.forms.dto.FormRefreshedEventPayload;
import org.eclipse.sirius.components.forms.Group;
import org.eclipse.sirius.components.forms.tests.navigation.FormNavigator;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLSubscriptionResult;
import org.eclipse.sirius.components.tables.Column;
import org.eclipse.sirius.components.tables.IconLabelCell;
import org.eclipse.sirius.components.tables.Line;
import org.eclipse.sirius.components.widget.table.TableWidget;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlMergeMode;

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
@GivenAnOntologyApplicationServer
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
    @DisplayName("Given a semantic object, when we subscribe to its properties events, then the form is sent")
    public void givenSemanticObjectWhenWeSubscribeToItsPropertiesEventsThenTheFormIsSent() {
        var detailsRepresentationId = this.representationIdBuilder.buildDetailsRepresentationId(List.of(OntologyProjectIdentifiers.ONTOLOGY_ROOT_ID));
        var input = new DetailsEventInput(UUID.randomUUID(), OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID, detailsRepresentationId);
        GraphQLSubscriptionResult graphQLSubscriptionResult = this.detailsEventSubscriptionRunner.run(input);

        Predicate<Object> formCapabilitiesMatcher = object -> Optional.of(object)
                .filter(FormCapabilitiesRefreshedEventPayload.class::isInstance)
                .isPresent();

        Predicate<Object> formContentMatcher = object -> Optional.of(object)
                .filter(FormRefreshedEventPayload.class::isInstance)
                .isPresent();

        StepVerifier.create(graphQLSubscriptionResult.flux())
                .expectNextMatches(formCapabilitiesMatcher)
                .expectNextMatches(formContentMatcher)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql(scripts = "/scripts/details-references-test-data.sql", config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @DisplayName("Given an entity with owned and inherited references, when details are displayed, then only owned references and navigation targets are shown")
    public void givenEntityWithOwnedAndInheritedReferencesWhenDetailsAreDisplayedThenOnlyOwnedReferencesAndNavigationTargetsAreShown() {
        var input = new DetailsEventInput(UUID.randomUUID(), OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID,
                this.representationIdBuilder.buildDetailsRepresentationId(List.of("10000000-0000-0000-0000-000000000003")));
        var flux = this.detailsEventSubscriptionRunner.run(input).flux().filter(FormRefreshedEventPayload.class::isInstance);
        StepVerifier.create(flux)
                .consumeNextWith(assertRefreshedFormThat(form -> {
                    assertThat(form.getPages().get(0).getGroups()).extracting(Group::getLabel).containsExactly("General", "Organization", "References");
                    var widget = new FormNavigator(form).page("Properties").group("References").findWidget("", TableWidget.class);
                    assertThat(widget.getId()).isEqualTo("entity.ownedReferences.10000000-0000-0000-0000-000000000003");
                    assertThat(widget.isReadOnly()).isTrue();
                    var table = widget.getTable();
                    assertThat(table.getColumns()).extracting(Column::getHeaderLabel).containsExactly("Referenced Type");
                    var expectedNames = new ArrayList<>(List.of("relates", "relates", "Missing target", ""));
                    IntStream.range(4, 13).mapToObj(index -> "Reference " + index).forEach(expectedNames::add);
                    assertThat(table.getLines()).extracting(Line::getHeaderLabel).containsExactlyElementsOf(expectedNames);
                    assertThat(table.getLines()).extracting(Line::getId).doesNotHaveDuplicates();
                    assertThat(table.getLines()).extracting(Line::getTargetObjectId)
                            .containsExactlyElementsOf(IntStream.range(10, 23).mapToObj(index -> "10000000-0000-0000-0000-%012d".formatted(index)).toList());
                    var cell = (IconLabelCell) table.getLines().get(0).getCells().get(0);
                    assertThat(cell.getValue()).isEqualTo("Target");
                    assertThat(cell.getTargetObjectId()).isEqualTo("10000000-0000-0000-0000-000000000002");
                    assertThat(cell.getIconURLs()).isEmpty();
                    assertThat(table.getLines().get(2).getCells()).isEmpty();
                    assertThat(((IconLabelCell) table.getLines().get(3).getCells().get(0)).getValue()).isEmpty();
                    assertThat(table.getPaginationData().totalRowCount()).isEqualTo(13);
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql(scripts = "/scripts/details-references-test-data.sql", config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @DisplayName("Given an entity with only inherited references, when details are displayed, then the references table is empty")
    public void givenEntityWithOnlyInheritedReferencesWhenDetailsAreDisplayedThenTheReferencesTableIsEmpty() {
        var input = new DetailsEventInput(UUID.randomUUID(), OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID,
                this.representationIdBuilder.buildDetailsRepresentationId(List.of("10000000-0000-0000-0000-000000000004")));
        var flux = this.detailsEventSubscriptionRunner.run(input).flux().filter(FormRefreshedEventPayload.class::isInstance);
        StepVerifier.create(flux)
                .consumeNextWith(assertRefreshedFormThat(form -> {
                    var table = new FormNavigator(form).page("Properties").group("References").findWidget("", TableWidget.class).getTable();
                    assertThat(table.getColumns()).extracting(Column::getHeaderLabel).containsExactly("Referenced Type");
                    assertThat(table.getLines()).isEmpty();
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

}
