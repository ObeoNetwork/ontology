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
package fr.obeo.ontology.services.representations.providers;

import fr.obeo.ontology.services.representations.diagrams.relations.edges.ReferenceEdgeDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.relations.nodes.EntitySynchronizedNodeDescriptionProvider;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.DefaultViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramToolbarBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * "Relations" diagram view description builder.
 *
 * @author ntinsalhi
 */
@Service
public class ViewRelationsDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String RELATIONS_DIAGRAM_NAME = "Relations Diagram";

    private IColorProvider colorProvider;

    private final DiagramToolbarBuilder diagramToolbarBuilder = new DiagramToolbarBuilder();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        this.colorProvider = colorProvider;
        return this.createRelationsDiagramDescription();
    }

    private DiagramDescription createRelationsDiagramDescription() {
        var diagramDescription = new DiagramBuilders().newDiagramDescription()
                .name(RELATIONS_DIAGRAM_NAME)
                .domainType("entity::Entity")
                .titleExpression("aql:self.name + ' Relations'")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .autoLayout(false)
                .toolbar(this.diagramToolbarBuilder.build())
                .build();

        var cache = new DefaultViewDiagramElementFinder();
        var diagramElementDescriptionProviders = this.createDiagramElementDescriptionProviders(this.colorProvider);

        diagramElementDescriptionProviders.forEach(provider -> {
            var diagramElementDescription = provider.create();
            cache.put(diagramElementDescription);
        });

        diagramElementDescriptionProviders.forEach(diagramElementDescriptionProvider -> diagramElementDescriptionProvider.link(diagramDescription, cache));

        return diagramDescription;
    }

    private List<IDiagramElementDescriptionProvider<?>> createDiagramElementDescriptionProviders(IColorProvider colorProvider) {
        var diagramElementDescriptionProviders = new ArrayList<IDiagramElementDescriptionProvider<?>>();

        diagramElementDescriptionProviders.add(new EntitySynchronizedNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new ReferenceEdgeDescriptionProvider(colorProvider));

        return diagramElementDescriptionProviders;
    }
}
