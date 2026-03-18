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
import fr.obeo.ontology.services.representations.diagrams.relations.nodes.EntityNodeDescriptionProvider;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IEdgeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeType;

import java.util.Objects;

/**
 * Used to create reference edge between entities.
 *
 * @author ntinsalhi
 */
public class ReferenceEdgeDescriptionProvider extends AbstractDescriptionProvider implements IEdgeDescriptionProvider {

    public static final String REFERENCE_EDGE_NAME = "Reference Edge";

    private final IColorProvider colorProvider;

    public ReferenceEdgeDescriptionProvider(IColorProvider colorProvider) {
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
                .name(REFERENCE_EDGE_NAME)
                .isDomainBasedEdge(true)
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
        var optionalReferenceEdgeDescription = cache.getEdgeDescription(REFERENCE_EDGE_NAME);
        var optionalSourceNodeDescription = cache.getNodeDescription(EntityNodeDescriptionProvider.ENTITY_NODE_NAME);
        var optionalTargetNodeDescription = cache.getNodeDescription(EntityNodeDescriptionProvider.ENTITY_NODE_NAME);

        if (optionalReferenceEdgeDescription.isPresent() && optionalSourceNodeDescription.isPresent() && optionalTargetNodeDescription.isPresent()) {
            diagramDescription.getEdgeDescriptions().add(optionalReferenceEdgeDescription.get());
            optionalReferenceEdgeDescription.get().getSourceDescriptions().add(optionalSourceNodeDescription.get());
            optionalReferenceEdgeDescription.get().getTargetDescriptions().add(optionalTargetNodeDescription.get());
        }
    }


}
