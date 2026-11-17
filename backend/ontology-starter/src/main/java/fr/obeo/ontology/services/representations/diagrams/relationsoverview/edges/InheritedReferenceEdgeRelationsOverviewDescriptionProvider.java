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
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeType;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;

/**
 * Used to create inherited reference edge between unsynchronized entities.
 *
 * @author lfasani
 */
public class InheritedReferenceEdgeRelationsOverviewDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {

    public static final String INHERITED_REFERENCE_EDGE_RELATIONS_OVERVIEW = "Inherited Reference Edge Relations Overview";

    private final IColorProvider colorProvider;

    public InheritedReferenceEdgeRelationsOverviewDescriptionProvider(IColorProvider colorProvider) {
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
                .name(INHERITED_REFERENCE_EDGE_RELATIONS_OVERVIEW)
                .isDomainBasedEdge(true)
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .domainType(ENVIRONMENT_REFERENCE)
                // self is the semantic element associated with the diagram
                .semanticCandidatesExpression("aql:editingContext.getReferencesOfAllEntitiesInDiagram(diagramContext)")
                // self is the semantic element returned by semanticCandidatesExpression
                .sourceExpression("aql:self.getInheritedReferenceEdgeSourceInDiagram(diagramContext)")
                .targetExpression("aql:self.getReferenceReferencedType()")
                .style(edgeStyle)
                .centerLabelExpression("aql:self.name")
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optionalReferenceEdgeDescription = cache.getEdgeDescription(INHERITED_REFERENCE_EDGE_RELATIONS_OVERVIEW);
        var optionalSourceNodeDescription = cache.getNodeDescription(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME);
        var optionalTargetNodeDescription = cache.getNodeDescription(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME);

        if (optionalReferenceEdgeDescription.isPresent() && optionalSourceNodeDescription.isPresent() && optionalTargetNodeDescription.isPresent()) {
            diagramDescription.getEdgeDescriptions().add(optionalReferenceEdgeDescription.get());
            optionalReferenceEdgeDescription.get().getSourceDescriptions().add(optionalSourceNodeDescription.get());
            optionalReferenceEdgeDescription.get().getTargetDescriptions().add(optionalTargetNodeDescription.get());
        }
    }
}
