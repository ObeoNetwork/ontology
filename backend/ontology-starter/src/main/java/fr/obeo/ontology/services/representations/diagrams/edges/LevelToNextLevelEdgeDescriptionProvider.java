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
package fr.obeo.ontology.services.representations.diagrams.edges;

import static fr.obeo.ontology.services.representations.diagrams.nodes.LevelContainerEntityNodeDescriptionProvider.CONTAINER_NODE_LEVEL_NAME;

import fr.obeo.ontology.services.representations.diagrams.AbstractDescriptionProvider;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IEdgeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;

/**
 * Used to create the edge from nodes of level n to nodes of level n+1.
 *
 * @author lfasani
 */
public class LevelToNextLevelEdgeDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {
    private final int level;

    private final IColorProvider colorProvider;

    public LevelToNextLevelEdgeDescriptionProvider(int level, IColorProvider colorProvider) {
        this.level = level;
        this.colorProvider = colorProvider;
    }

    @Override
    public EdgeDescription create() {
        EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                .color(this.colorProvider.getColor(BORDER_COLOR))
                .build();
        return new DiagramBuilders().newEdgeDescription()
                .name(String.format("Level%sToLevel%sEdge", this.level, this.level + 1))
                .domainType("Entity::Entity")
                .semanticCandidatesExpression(AQL_SELF)
                .sourceExpression(AQL_SELF)
                .targetExpression("aql:self.getSubEntities()")
                .style(edgeStyle)
                .centerLabelExpression("")
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getEdgeDescription(String.format("Level%sToLevel%sEdge", this.level, this.level + 1)).ifPresent(edgeDescription -> {
            edgeDescription.getSourceDescriptions().add(cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + (this.level)).get().getChildrenDescriptions().get(0));
            edgeDescription.getTargetDescriptions()
                    .add(cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + (this.level + 1)).get().getChildrenDescriptions().get(0).getBorderNodesDescriptions().get(0));

            diagramDescription.getEdgeDescriptions().add(edgeDescription);
        });

    }
}
