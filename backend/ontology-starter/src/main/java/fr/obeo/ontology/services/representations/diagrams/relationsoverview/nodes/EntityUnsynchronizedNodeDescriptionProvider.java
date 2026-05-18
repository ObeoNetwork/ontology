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
package fr.obeo.ontology.services.representations.diagrams.relationsoverview.nodes;

import fr.obeo.ontology.services.representations.diagrams.AbstractRelationsNodeDescriptionProvider;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;

/**
 * Used to create unsynchronized entity nodes.
 *
 * @author ntinsalhi
 */
public class EntityUnsynchronizedNodeDescriptionProvider extends AbstractRelationsNodeDescriptionProvider {

    public static final String ENTITY_UNSYNCHRONIZED_NODE_NAME = "Unsynchronized_EntityNode";

    private final IColorProvider colorProvider;

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    public EntityUnsynchronizedNodeDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public NodeDescription create() {
        var attributeContainerNodeDescription = this.attributesContainerNodeDescription(this.colorProvider);

        return this.diagramBuilderHelper.newNodeDescription()
                .name(ENTITY_UNSYNCHRONIZED_NODE_NAME)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression("aql:self.getEntities()")
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .style(this.entityRectangularNodeStyleDescription(this.colorProvider))
                .insideLabel(this.entityInsideLabelDescription())
                .defaultWidthExpression(String.valueOf(DEFAULT_ENTITY_NODE_HEIGHT))
                .defaultHeightExpression(String.valueOf(DEFAULT_ENTITY_NODE_WIDTH))
                .isCollapsedByDefaultExpression("aql:true")
                .collapsible(true)
                .childrenDescriptions(attributeContainerNodeDescription)
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(ENTITY_UNSYNCHRONIZED_NODE_NAME)
                .ifPresent(nodeDescription -> {
                    diagramDescription.getNodeDescriptions().add(nodeDescription);

                    nodeDescription.setPalette(this.createNodePalette(cache));
                });

    }

    private NodePalette createNodePalette(IViewDiagramElementFinder cache) {
        return this.createEntityNodePaletteBuilder()
                .quickAccessTools(this.getDeleteFromDiagramTool())
                .edgeTools(this.createEdgeToolRelation(cache))
                .build();
    }

    private NodeTool getDeleteFromDiagramTool() {
        var deleteView = this.diagramBuilderHelper.newDeleteView();

        return this.diagramBuilderHelper.newNodeTool()
                .name("Delete from Diagram")
                .iconURLsExpression("/diagram-images/graphicalDelete.svg")
                .body(deleteView.build())
                .build();
    }

    private EdgeTool createEdgeToolRelation(IViewDiagramElementFinder cache) {
        var entityNodeDescription = cache.getNodeDescription(ENTITY_UNSYNCHRONIZED_NODE_NAME).orElse(null);
        return this.diagramBuilderHelper.newEdgeTool()
                .name("Relation")
                .targetElementDescriptions(entityNodeDescription)
                .body(
                        new ViewBuilders().newChangeContext()
                                .expression("aql:semanticEdgeSource.createReferenceWithType('New Reference', semanticEdgeTarget)")
                                .build()
                )
                .build();
    }
}
