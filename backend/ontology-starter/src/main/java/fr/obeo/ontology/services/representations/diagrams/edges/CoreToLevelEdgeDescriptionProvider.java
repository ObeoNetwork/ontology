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

import static fr.obeo.ontology.services.representations.diagrams.nodes.CoreEntityNodeDescriptionProvider.CORE_ENTITY_NODE_NAME;
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
 * Used to create the edge from core node to nodes of level 1.
 *
 * @author lfasani
 */
public class CoreToLevelEdgeDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {
    private final IColorProvider colorProvider;

    public CoreToLevelEdgeDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = colorProvider;
    }

    @Override
    public EdgeDescription create() {
        EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                .color(this.colorProvider.getColor(BORDER_COLOR))
                .build();
        return new DiagramBuilders().newEdgeDescription()
                .name("CoreToLevel1Edge")
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(AQL_SELF)
                .sourceExpression(AQL_SELF)
                .targetExpression("aql:self.getSubEntities()")
                .style(edgeStyle)
                .centerLabelExpression("")
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getEdgeDescription("CoreToLevel1Edge").ifPresent(edgeDescription -> {
            edgeDescription.getSourceDescriptions().add(cache.getNodeDescription(CORE_ENTITY_NODE_NAME).get().getBorderNodesDescriptions().get(0));
            edgeDescription.getTargetDescriptions().add(cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + 1).get().getChildrenDescriptions().get(0).getBorderNodesDescriptions().get(0));

            diagramDescription.getEdgeDescriptions().add(edgeDescription);
        });
    }
}
