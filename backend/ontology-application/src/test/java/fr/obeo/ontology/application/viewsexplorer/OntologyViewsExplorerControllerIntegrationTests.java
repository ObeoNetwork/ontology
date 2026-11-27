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
package fr.obeo.ontology.application.viewsexplorer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sirius.components.trees.tests.TreeEventPayloadConsumer.assertRefreshedTreeThat;

import com.jayway.jsonpath.JsonPath;

import fr.obeo.ontology.application.AbstractIntegrationTests;
import fr.obeo.ontology.application.identifiers.OntologyProjectIdentifiers;
import fr.obeo.ontology.application.sql.GivenAnOntologyApplicationServer;
import fr.obeo.ontology.services.representations.providers.ViewDiagramDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewEntityTableDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewRelationsDiagramDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewRelationsOverviewDiagramDescriptionProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.collaborative.trees.dto.DeleteTreeItemInput;
import org.eclipse.sirius.components.collaborative.trees.dto.RenameTreeItemInput;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLSubscriptionResult;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.tables.Table;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.trees.tests.graphql.DeleteTreeItemMutationRunner;
import org.eclipse.sirius.components.trees.tests.graphql.InitialDirectEditTreeItemLabelQueryRunner;
import org.eclipse.sirius.components.trees.tests.graphql.RenameTreeItemMutationRunner;
import org.eclipse.sirius.components.trees.tests.graphql.TreePathQueryRunner;
import org.eclipse.sirius.web.application.views.viewsexplorer.ViewsExplorerEventInput;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.ViewsExplorerTreeDescriptionProvider;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.eclipse.sirius.web.tests.graphql.ViewsExplorerEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenCreatedRepresentation;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import reactor.test.StepVerifier;

/**
 * Exercises the standard Views tree with the ontology content delegate.
 *
 * @author lfasani
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class OntologyViewsExplorerControllerIntegrationTests extends AbstractIntegrationTests {

    private static final String EDITING_CONTEXT_ID = OntologyProjectIdentifiers.ONTOLOGY_PROJECT_EDITING_CONTEXT_ID;

    private static final String CHILD_ID = "58979966-b232-496f-9aa9-53a3bcdb6a17";

    private static final String CHILD_PATH = "Entity Root > Entity level 1.1 > Entity level 1.1.1";

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IGivenCreatedRepresentation givenCreatedRepresentation;

    @Autowired
    private ViewsExplorerEventSubscriptionRunner viewsExplorerEventSubscriptionRunner;

    @Autowired
    private IEditingContextSearchService editingContextSearchService;

    @Autowired
    private IRepresentationDescriptionSearchService representationDescriptionSearchService;

    @Autowired
    private IRepresentationMetadataSearchService representationMetadataSearchService;

    @Autowired
    private InitialDirectEditTreeItemLabelQueryRunner initialDirectEditTreeItemLabelQueryRunner;

    @Autowired
    private RenameTreeItemMutationRunner renameTreeItemMutationRunner;

    @Autowired
    private DeleteTreeItemMutationRunner deleteTreeItemMutationRunner;

    @Autowired
    private TreePathQueryRunner treePathQueryRunner;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
        UUID semanticDataId = UUID.fromString(EDITING_CONTEXT_ID);
        this.jdbcTemplate.update("DELETE FROM representation_content WHERE semantic_data_id = ?", semanticDataId);
        this.jdbcTemplate.update("DELETE FROM representation_metadata WHERE semantic_data_id = ?", semanticDataId);
        this.commit();
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given all ontology representation types, when Views is opened, then categories and displayed labels are ordered")
    public void givenAllOntologyRepresentationTypesWhenViewsIsOpenedThenCategoriesAndDisplayedLabelsAreOrdered() {
        var descriptions = this.getDescriptions();
        this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME, CHILD_ID, "A saved relations name");
        this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME, CHILD_ID, "Another saved relations name");
        this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME,
                OntologyProjectIdentifiers.ONTOLOGY_CORE_ENTITY_ID, "Z saved relations name");
        this.createRepresentation(descriptions, ViewRelationsOverviewDiagramDescriptionProvider.RELATIONS_OVERVIEW_DIAGRAM_NAME, CHILD_ID, "Overview saved name");
        this.createRepresentation(descriptions, ViewDiagramDescriptionProvider.ONTOLOGY_DIAGRAM_NAME,
                OntologyProjectIdentifiers.ONTOLOGY_CORE_ENTITY_ID, "Ontology saved name");
        this.createRepresentation(descriptions, ViewEntityTableDescriptionProvider.ENTITIES_TABLE_NAME,
                OntologyProjectIdentifiers.ONTOLOGY_NAMESPACE_ID, "Table saved name");

        StepVerifier.create(this.subscribe(descriptions).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(tree.getDescriptionId()).isEqualTo(ViewsExplorerTreeDescriptionProvider.DESCRIPTION_ID);
                    assertThat(tree.getChildren()).extracting(TreeItem::getId).containsExactly(Table.KIND, Diagram.KIND);
                    var tableCategories = tree.getChildren().get(0).getChildren();
                    assertThat(tableCategories).extracting(TreeItem::getId)
                            .containsExactly(descriptions.get(ViewEntityTableDescriptionProvider.ENTITIES_TABLE_NAME));
                    assertThat(this.labels(tableCategories.get(0).getChildren())).containsExactly("Table saved name");
                    var diagramCategories = tree.getChildren().get(1).getChildren();
                    assertThat(diagramCategories).extracting(TreeItem::getId).containsExactly(
                            descriptions.get(ViewDiagramDescriptionProvider.ONTOLOGY_DIAGRAM_NAME),
                            descriptions.get(ViewRelationsOverviewDiagramDescriptionProvider.RELATIONS_OVERVIEW_DIAGRAM_NAME),
                            descriptions.get(ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME));
                    assertThat(this.labels(diagramCategories.get(0).getChildren())).containsExactly("Ontology saved name");
                    assertThat(this.labels(diagramCategories.get(1).getChildren())).containsExactly("Overview saved name");
                    assertThat(this.labels(diagramCategories.get(2).getChildren())).containsExactly("Entity Root", CHILD_PATH, CHILD_PATH);
                    assertThat(diagramCategories.get(2).getChildren().subList(1, 3))
                            .extracting(item -> UUID.fromString(item.getId())).isSorted();
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given a relations diagram, when its name is edited, then the saved name changes and Views keeps the entity path")
    public void givenRelationsDiagramWhenItsNameIsEditedThenSavedNameChangesAndViewsKeepsEntityPath() {
        var descriptions = this.getDescriptions();
        String representationId = this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME, CHILD_ID, "Saved name");
        var treeId = new AtomicReference<String>();

        StepVerifier.create(this.subscribe(descriptions).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    treeId.set(tree.getId());
                    assertThat(this.labels(this.relationsItems(tree))).containsExactly(CHILD_PATH);
                }))
                .then(() -> this.assertSavedNameAndNavigation(treeId.get(), representationId, descriptions, "Saved name"))
                .then(() -> {
                    var input = new RenameTreeItemInput(UUID.randomUUID(), EDITING_CONTEXT_ID, treeId.get(), UUID.fromString(representationId), "Renamed diagram");
                    var result = this.renameTreeItemMutationRunner.run(input);
                    assertThat((String) JsonPath.read(result.data(), "$.data.renameTreeItem.__typename")).isEqualTo("SuccessPayload");
                    this.commit();
                })
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(this.labels(this.relationsItems(tree))).containsExactly(CHILD_PATH);
                }))
                .then(() -> this.assertSavedNameAndNavigation(treeId.get(), representationId, descriptions, "Renamed diagram"))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given only a relations diagram, when it is deleted, then empty categories disappear")
    public void givenOnlyRelationsDiagramWhenItIsDeletedThenEmptyCategoriesDisappear() {
        var descriptions = this.getDescriptions();
        String representationId = this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME, CHILD_ID, "Saved name");
        var treeId = new AtomicReference<String>();

        StepVerifier.create(this.subscribe(descriptions).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    treeId.set(tree.getId());
                    assertThat(tree.getChildren()).extracting(TreeItem::getId).containsExactly(Diagram.KIND);
                    assertThat(this.relationsItems(tree)).hasSize(1);
                }))
                .then(() -> {
                    var input = new DeleteTreeItemInput(UUID.randomUUID(), EDITING_CONTEXT_ID, treeId.get(), UUID.fromString(representationId));
                    var result = this.deleteTreeItemMutationRunner.run(input);
                    assertThat((String) JsonPath.read(result.data(), "$.data.deleteTreeItem.__typename")).isEqualTo("SuccessPayload");
                    this.commit();
                })
                .consumeNextWith(assertRefreshedTreeThat(tree -> assertThat(tree.getChildren()).isEmpty()))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }


    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given a missing diagram target, when Views is opened, then the saved name is displayed")
    public void givenMissingDiagramTargetWhenViewsIsOpenedThenSavedNameIsDisplayed() {
        var descriptions = this.getDescriptions();
        String representationId = this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME, CHILD_ID, "Missing target saved name");
        this.jdbcTemplate.update("UPDATE representation_metadata SET target_object_id = ? WHERE representation_metadata_id = ? AND semantic_data_id = ?",
                UUID.randomUUID().toString(), UUID.fromString(representationId), UUID.fromString(EDITING_CONTEXT_ID));
        this.commit();

        StepVerifier.create(this.subscribe(descriptions).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(this.labels(this.relationsItems(tree))).containsExactly("Missing target saved name");
                    assertThat(this.relationsItems(tree)).extracting(TreeItem::getId).containsExactly(representationId);
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @GivenAnOntologyApplicationServer
    @DisplayName("Given a project without ontology nature, when Views is opened, then standard ordering and labels are preserved")
    public void givenProjectWithoutOntologyNatureWhenViewsIsOpenedThenStandardOrderingAndLabelsArePreserved() {
        var descriptions = this.getDescriptions();
        this.createRepresentation(descriptions, ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME, CHILD_ID, "Stored relations name");
        this.createRepresentation(descriptions, ViewEntityTableDescriptionProvider.ENTITIES_TABLE_NAME,
                OntologyProjectIdentifiers.ONTOLOGY_NAMESPACE_ID, "Table saved name");
        this.jdbcTemplate.update("DELETE FROM nature WHERE name = ? AND project_id IN (SELECT project_id FROM project_semantic_data WHERE semantic_data_id = ?)",
                "siriusWeb://nature?kind=ontology", UUID.fromString(EDITING_CONTEXT_ID));
        this.commit();

        StepVerifier.create(this.subscribe(descriptions).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(tree.getChildren()).extracting(TreeItem::getId).containsExactly(Diagram.KIND, Table.KIND);
                    assertThat(this.labels(this.relationsItems(tree))).containsExactly("Stored relations name");
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }


    private Map<String, String> getDescriptions() {
        var editingContext = this.editingContextSearchService.findById(EDITING_CONTEXT_ID).orElseThrow();
        return this.representationDescriptionSearchService.findAll(editingContext).values().stream()
                .collect(Collectors.toMap(IRepresentationDescription::getLabel, IRepresentationDescription::getId,
                        (first, second) -> first.compareTo(second) < 0 ? first : second));
    }

    private String createRepresentation(Map<String, String> descriptions, String type, String targetId, String name) {
        return this.givenCreatedRepresentation.createRepresentation(new CreateRepresentationInput(UUID.randomUUID(), EDITING_CONTEXT_ID, descriptions.get(type), targetId, name));
    }

    private GraphQLSubscriptionResult subscribe(Map<String, String> descriptions) {
        List<String> expandedIds = new ArrayList<>(descriptions.values());
        expandedIds.add(Table.KIND);
        expandedIds.add(Diagram.KIND);
        String treeId = new RepresentationIdBuilder().buildViewsExplorerViewRepresentationId(expandedIds);
        return this.viewsExplorerEventSubscriptionRunner.run(new ViewsExplorerEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, treeId));
    }

    private List<TreeItem> relationsItems(Tree tree) {
        return tree.getChildren().get(0).getChildren().get(0).getChildren();
    }

    private List<String> labels(List<TreeItem> items) {
        return items.stream().map(item -> item.getLabel().toString()).toList();
    }

    private void assertSavedNameAndNavigation(String treeId, String representationId, Map<String, String> descriptions, String expectedName) {
        var initialLabel = this.initialDirectEditTreeItemLabelQueryRunner.run(Map.of(
                "editingContextId", EDITING_CONTEXT_ID, "representationId", treeId, "treeItemId", representationId));
        assertThat((String) JsonPath.read(initialLabel.data(),
                "$.data.viewer.editingContext.representation.description.initialDirectEditTreeItemLabel")).isEqualTo(expectedName);
        var metadata = this.representationMetadataSearchService.findMetadataById(
                AggregateReference.to(UUID.fromString(EDITING_CONTEXT_ID)), UUID.fromString(representationId)).orElseThrow();
        assertThat(metadata.getLabel()).isEqualTo(expectedName);
        var path = this.treePathQueryRunner.run(Map.of("editingContextId", EDITING_CONTEXT_ID,
                "treeId", treeId, "selectionEntryIds", List.of(representationId)));
        List<String> ancestorIds = JsonPath.read(path.data(), "$.data.viewer.editingContext.treePath.treeItemIdsToExpand");
        assertThat(ancestorIds).containsExactly(Diagram.KIND, descriptions.get(ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME));
    }

    private void commit() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }
}
