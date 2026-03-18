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

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;

import java.util.Objects;

import static fr.obeo.ontology.services.representations.providers.ViewOntologyPaletteFactory.BLUE_GREY;

/**
 * Used to create entity nodes.
 *
 * @author ntinsalhi
 */
public class EntityNodeDescriptionProvider extends AbstractRelationsNodeDescriptionProvider {

    public static final String ENTITY_NODE_NAME = "EntityNode";

    private final IColorProvider colorProvider;

    public EntityNodeDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public NodeDescription create() {
        RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(BORDER_COLOR))
                .borderSize(3)
                .build();

        RectangularNodeStyleDescription mainEntityRectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(BLUE_GREY))
                .borderColor(this.colorProvider.getColor(BORDER_COLOR))
                .borderSize(5)
                .build();

        ConditionalNodeStyle mainEntityConditionalNodeStyle = new DiagramBuilders()
                .newConditionalNodeStyle()
                .condition("aql:self.isMainEntity(diagramContext)")
                .style(mainEntityRectangularNodeStyleDescription)
                .build();

        InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                .labelExpression("aql:self.name")
                .style(new DiagramBuilders().newInsideLabelStyle().borderSize(0).build())
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name(ENTITY_NODE_NAME)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression("aql:self.getRelationsSemanticCandidates()")
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .style(rectangularNodeStyleDescription)
                .insideLabel(insideLabelDescription)
                .conditionalStyles(mainEntityConditionalNodeStyle)
                .defaultWidthExpression(String.valueOf(DEFAULT_ENTITY_NODE_HEIGHT))
                .defaultHeightExpression(String.valueOf(DEFAULT_ENTITY_NODE_WIDTH))
                .palette(this.createEntityNodePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(ENTITY_NODE_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
        });
    }
}
