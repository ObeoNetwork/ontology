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
package fr.obeo.ontology.services.representations.diagrams.entity.nodes;

import static fr.obeo.ontology.services.representations.diagrams.entity.nodes.LevelContainerEntityNodeDescriptionProvider.CONTAINER_NODE_LEVEL_NAME;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;

/**
 * Used to create the core entity node.
 *
 * @author lfasani
 */
public class CoreEntityNodeDescriptionProvider extends AbstractNodeDescriptionProvider {
    public static final String CORE_ENTITY_NODE_NAME = "CoreEntityNode";

    protected final IColorProvider colorProvider;

    public CoreEntityNodeDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public NodeDescription create() {
        NodeDescription borderNodeDescription = this.createBorderNodeDescription(0);
        RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(BORDER_COLOR))
                .borderSize(3)
                .childrenLayoutStrategy(new DiagramBuilders().newFreeFormLayoutStrategyDescription()
                        .onEastAtCreationBorderNodes(borderNodeDescription)
                        .build())
                .build();

        InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                .labelExpression("aql:self.name")
                .style(new DiagramBuilders().newInsideLabelStyle().borderSize(0).build())
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name(CORE_ENTITY_NODE_NAME)
                .domainType("entity::Entity")
                .semanticCandidatesExpression("aql:self")
                .style(rectangularNodeStyleDescription)
                .borderNodesDescriptions(borderNodeDescription)
                .insideLabel(insideLabelDescription)
                .defaultWidthExpression(String.valueOf(DEFAULT_NODE_HEIGHT))
                .defaultHeightExpression(String.valueOf(DEFAULT_NODE_HEIGHT))
                .palette(this.createEntityNodePalette(0))
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(CORE_ENTITY_NODE_NAME).ifPresent(nodeDescription -> {
            nodeDescription.getPalette().getEdgeTools().add(this.createEdgeTool(cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + 1).get()));
            diagramDescription.getNodeDescriptions().add(nodeDescription);
        });
    }
}
