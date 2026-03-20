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
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IEdgeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeType;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;

import java.util.Objects;

/**
 * Used to create reference edge between unsynchronized entities.
 *
 * @author ntinsalhi
 */
public class ReferenceEdgeRelationsOverviewDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {

    public static final String REFERENCE_EDGE_RELATIONS_OVERVIEW_NAME = "Reference Edge Relations Overview";

    private final IColorProvider colorProvider;

    public ReferenceEdgeRelationsOverviewDescriptionProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                .color(this.colorProvider.getColor(BORDER_COLOR))
                .edgeType(EdgeType.MANHATTAN)
                .borderSize(0)
                .build();

        return new DiagramBuilders().newEdgeDescription()
                .name(REFERENCE_EDGE_RELATIONS_OVERVIEW_NAME)
                .isDomainBasedEdge(true)
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .domainType(ENVIRONMENT_REFERENCE)
                .semanticCandidatesExpression("aql:self.getReferences()")
                .sourceExpression("aql:self.getReferenceContainingType()")
                .targetExpression("aql:self.getReferenceReferencedType()")
                .style(edgeStyle)
                .endLabelExpression("aql:self.name")
                .centerLabelExpression("")
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optionalReferenceEdgeDescription = cache.getEdgeDescription(REFERENCE_EDGE_RELATIONS_OVERVIEW_NAME);
        var optionalSourceNodeDescription = cache.getNodeDescription(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME);
        var optionalTargetNodeDescription = cache.getNodeDescription(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME);

        if (optionalReferenceEdgeDescription.isPresent() && optionalSourceNodeDescription.isPresent() && optionalTargetNodeDescription.isPresent()) {
            diagramDescription.getEdgeDescriptions().add(optionalReferenceEdgeDescription.get());
            optionalReferenceEdgeDescription.get().getSourceDescriptions().add(optionalSourceNodeDescription.get());
            optionalReferenceEdgeDescription.get().getTargetDescriptions().add(optionalTargetNodeDescription.get());
        }
    }
}
