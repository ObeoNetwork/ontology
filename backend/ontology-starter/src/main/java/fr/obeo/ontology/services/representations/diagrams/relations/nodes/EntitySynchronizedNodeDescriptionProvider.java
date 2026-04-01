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
package fr.obeo.ontology.services.representations.diagrams.relations.nodes;

import fr.obeo.ontology.services.representations.diagrams.AbstractRelationsNodeDescriptionProvider;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;

/**
 * Used to create synchronized entity nodes.
 *
 * @author ntinsalhi
 */
public class EntitySynchronizedNodeDescriptionProvider extends AbstractRelationsNodeDescriptionProvider {

    public static final String ENTITY_SYNCHRONIZED_NODE_NAME = "SynchronizedEntityNode";

    private final IColorProvider colorProvider;

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();


    public EntitySynchronizedNodeDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public NodeDescription create() {
        var attributeItemNodeDescription = this.attributeItemNodeDescription();
        var entityNodePalette = this.createEntityNodePaletteBuilder().build();

        return this.diagramBuilderHelper.newNodeDescription()
                .name(ENTITY_SYNCHRONIZED_NODE_NAME)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression("aql:self.getRelationsSemanticCandidates()")
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .style(this.entityRectangularNodeStyleDescription(this.colorProvider))
                .insideLabel(this.entityInsideLabelDescription())
                .conditionalStyles(this.mainEntityConditionalNodeStyle(this.colorProvider))
                .defaultWidthExpression(String.valueOf(DEFAULT_ENTITY_NODE_HEIGHT))
                .defaultHeightExpression(String.valueOf(DEFAULT_ENTITY_NODE_WIDTH))
                .palette(entityNodePalette)
                .isCollapsedByDefaultExpression("aql:true")
                .collapsible(true)
                .childrenDescriptions(attributeItemNodeDescription)
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(ENTITY_SYNCHRONIZED_NODE_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
        });
    }
}
