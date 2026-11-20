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
package fr.obeo.ontology.services.representations.diagrams.relationsoverview.edges;

import fr.obeo.ontology.services.representations.diagrams.AbstractDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.relationsoverview.nodes.EntityUnsynchronizedNodeDescriptionProvider;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IEdgeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeType;

/**
 * Used to create an edge representing the super entity.
 *
 * @author lfasani
 */
public class SuperEntityEdgeRelationsOverviewDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {

    public static final String SUPER_ENTITY_EDGE = "Super Entity Edge Relations Overview";

    private final IColorProvider colorProvider;

    public SuperEntityEdgeRelationsOverviewDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                .color(this.colorProvider.getColor("black"))
                .edgeType(EdgeType.MANHATTAN)
                .targetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW)
                .borderSize(0)
                .build();

        return new DiagramBuilders().newEdgeDescription()
                .name(SUPER_ENTITY_EDGE)
                .isDomainBasedEdge(true)
                .domainType(ENTITY_ENTITY)
                // self is the semantic element associated with the diagram
                .semanticCandidatesExpression("aql:editingContext.getAllEntitiesInDiagram(diagramContext)")
                // self is the semantic element returned by semanticCandidatesExpression
                .sourceExpression("aql:self")
                .targetExpression("aql:self.supertype")
                .centerLabelExpression("")
                .style(edgeStyle)
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optionalReferenceEdgeDescription = cache.getEdgeDescription(SUPER_ENTITY_EDGE);
        var optionalSourceNodeDescription = cache.getNodeDescription(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME);
        var optionalTargetNodeDescription = cache.getNodeDescription(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME);

        if (optionalReferenceEdgeDescription.isPresent() && optionalSourceNodeDescription.isPresent() && optionalTargetNodeDescription.isPresent()) {
            diagramDescription.getEdgeDescriptions().add(optionalReferenceEdgeDescription.get());
            optionalReferenceEdgeDescription.get().getSourceDescriptions().add(optionalSourceNodeDescription.get());
            optionalReferenceEdgeDescription.get().getTargetDescriptions().add(optionalTargetNodeDescription.get());
        }
    }


}
