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
package fr.obeo.ontology.services.representations.diagrams.nodes;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.FreeFormLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;

/**
 * Used to create the container node for level>0.
 *
 * @author lfasani
 */
public class LevelContainerEntityNodeDescriptionProvider extends AbstractNodeDescriptionProvider {
    public static final String CONTAINER_NODE_LEVEL_NAME = "ContainerNodeLevel";

    private final IColorProvider colorProvider;

    private final int level;

    public LevelContainerEntityNodeDescriptionProvider(int level, IColorProvider colorProvider) {
        this.level = level;
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public NodeDescription create() {
        FreeFormLayoutStrategyDescription freeFormLayoutStrategyDescription = new DiagramBuilders().newFreeFormLayoutStrategyDescription().build();

        RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(BACKGROUND_COLOR))
                .borderSize(3)
                .childrenLayoutStrategy(freeFormLayoutStrategyDescription)
                .build();

        InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                .labelExpression("Level " + this.level)
                .style(new DiagramBuilders().newInsideLabelStyle()
                        .withHeader(true)
                        .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.ALWAYS)
                        .borderSize(0)
                        .build())
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name(CONTAINER_NODE_LEVEL_NAME + this.level)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression("aql:self")
                .style(rectangularNodeStyleDescription)
                .insideLabel(insideLabelDescription)
                .childrenDescriptions(this.createEntityNodeDescription(this.level))
                .build();
    }

    private NodeDescription createEntityNodeDescription(int level) {
        RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(BORDER_COLOR))
                .build();

        InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                .labelExpression("aql:self.name")
                .style(new DiagramBuilders().newInsideLabelStyle().borderSize(0).build())
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name("EntityNodeLevel" + level)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(String.format("aql:self.getEntitiesOfLevel(%s)", level))
                .style(rectangularNodeStyleDescription)
                .borderNodesDescriptions(this.createBorderNodeDescription(level))
                .insideLabel(insideLabelDescription)
                .defaultWidthExpression(INITIAL_NODE_SIZE)
                .defaultHeightExpression(INITIAL_NODE_SIZE)
                .palette(this.createEntityNodePalette(level))
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + this.level).ifPresent(nodeDescription -> {
            if (this.level < NB_LEVEL) {
                nodeDescription.getChildrenDescriptions().get(0).getPalette().getEdgeTools().add(this.createEdgeTool(cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + (this.level + 1)).get()));
            }

            diagramDescription.getNodeDescriptions().add(nodeDescription);
        });

        diagramDescription.getNodeDescriptions().add(cache.getNodeDescription(CONTAINER_NODE_LEVEL_NAME + this.level).get());
    }
}
