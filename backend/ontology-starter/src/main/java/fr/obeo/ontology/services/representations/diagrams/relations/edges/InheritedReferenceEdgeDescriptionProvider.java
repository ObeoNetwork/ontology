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
package fr.obeo.ontology.services.representations.diagrams.relations.edges;

import fr.obeo.ontology.services.representations.diagrams.AbstractDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.relations.nodes.EntitySynchronizedNodeDescriptionProvider;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IEdgeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeType;
import org.eclipse.sirius.components.view.diagram.LineStyle;

/**
 * Used to create inherited reference edge between entities.
 *
 * @author lfasani
 */
public class InheritedReferenceEdgeDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {

    public static final String INHERITED_REFERENCE_EDGE = "Inherited Reference Edge";

    private final IColorProvider colorProvider;

    public InheritedReferenceEdgeDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                .color(this.colorProvider.getColor("red 500"))
                .edgeType(EdgeType.MANHATTAN)
                .lineStyle(LineStyle.DASH)
                .borderSize(0)
                .build();

        return new DiagramBuilders().newEdgeDescription()
                .name(INHERITED_REFERENCE_EDGE)
                .isDomainBasedEdge(true)
                .domainType(ENVIRONMENT_REFERENCE)
                // self is the semantic element associated with the diagram
                .semanticCandidatesExpression("aql:self.getInheritedReferences()")
                // self is the semantic element returned by semanticCandidatesExpression
                .sourceExpression("aql:self.getInheritedReferenceEdgeSourceInDiagram(diagramContext)")
                .targetExpression("aql:self.getReferenceReferencedType()")
                .style(edgeStyle)
                .centerLabelExpression("aql:self.name")
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optionalReferenceEdgeDescription = cache.getEdgeDescription(INHERITED_REFERENCE_EDGE);
        var optionalSourceNodeDescription = cache.getNodeDescription(EntitySynchronizedNodeDescriptionProvider.ENTITY_SYNCHRONIZED_NODE_NAME);
        var optionalTargetNodeDescription = cache.getNodeDescription(EntitySynchronizedNodeDescriptionProvider.ENTITY_SYNCHRONIZED_NODE_NAME);

        if (optionalReferenceEdgeDescription.isPresent() && optionalSourceNodeDescription.isPresent() && optionalTargetNodeDescription.isPresent()) {
            diagramDescription.getEdgeDescriptions().add(optionalReferenceEdgeDescription.get());
            optionalReferenceEdgeDescription.get().getSourceDescriptions().add(optionalSourceNodeDescription.get());
            optionalReferenceEdgeDescription.get().getTargetDescriptions().add(optionalTargetNodeDescription.get());
        }
    }


}
