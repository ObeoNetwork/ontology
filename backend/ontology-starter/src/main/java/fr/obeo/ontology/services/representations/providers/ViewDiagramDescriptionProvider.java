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
package fr.obeo.ontology.services.representations.providers;

import fr.obeo.ontology.services.representations.diagrams.AbstractDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.edges.CoreToLevelEdgeDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.edges.LevelToNextLevelEdgeDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.nodes.CoreEntityNodeDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.nodes.LevelContainerEntityNodeDescriptionProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.DefaultViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.springframework.stereotype.Service;

/**
 * Builder of the "Diagram" view description.
 *
 * @author lfasani
 */
@Service
public class ViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String ONTOLOGY_DIAGRAM_NAME = "Ontology Diagram";

    private IColorProvider colorProvider;

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        this.colorProvider = colorProvider;
        return this.createOntologyDiagramDescription();
    }

    private DiagramDescription createOntologyDiagramDescription() {
        var diagramDescription = new DiagramBuilders().newDiagramDescription()
                .name(ONTOLOGY_DIAGRAM_NAME)
                .domainType("entity::Entity")
                .titleExpression("aql:self.name")
                .preconditionExpression("aql:self.supertype==null")
                .arrangeLayoutDirection(ArrangeLayoutDirection.DOWN)
                .autoLayout(true)
                .build();

        var cache = new DefaultViewDiagramElementFinder();
        var diagramElementDescriptionProviders = this.createDiagramElementDescriptionProviders(this.colorProvider);

        diagramElementDescriptionProviders.forEach(provider -> {
            var diagramElementDescription = provider.create();
            cache.put(diagramElementDescription);
        });

        // link elements each other
        diagramElementDescriptionProviders.forEach(diagramElementDescriptionProvider -> diagramElementDescriptionProvider.link(diagramDescription, cache));

        return diagramDescription;
    }

    private List<IDiagramElementDescriptionProvider<?>> createDiagramElementDescriptionProviders(IColorProvider colorProvider) {

        var diagramElementDescriptionProviders = new ArrayList<IDiagramElementDescriptionProvider<?>>();

        diagramElementDescriptionProviders.add(new CoreEntityNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new CoreToLevelEdgeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.addAll(this.createAllLevelContainerDescriptionProviders(colorProvider));

        return diagramElementDescriptionProviders;
    }

    private List<IDiagramElementDescriptionProvider<?>> createAllLevelContainerDescriptionProviders(IColorProvider colorProvider) {
        final var diagramElementDescriptionProviders = new ArrayList<IDiagramElementDescriptionProvider<?>>();

        List<NodeDescription> containerDescriptions = new ArrayList<>();
        for (int level = 1; level <= AbstractDescriptionProvider.NB_LEVEL; level++) {
            diagramElementDescriptionProviders.add(new LevelContainerEntityNodeDescriptionProvider(level, colorProvider));
        }

        for (int level = 1; level < AbstractDescriptionProvider.NB_LEVEL; level++) {
            diagramElementDescriptionProviders.add(new LevelToNextLevelEdgeDescriptionProvider(level, colorProvider));
        }

        return diagramElementDescriptionProviders;
    }
}
