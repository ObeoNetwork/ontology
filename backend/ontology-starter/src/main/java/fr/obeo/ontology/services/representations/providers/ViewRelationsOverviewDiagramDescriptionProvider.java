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

import fr.obeo.ontology.services.representations.diagrams.relationsoverview.RelationsOverviewDiagramPaletteProvider;
import fr.obeo.ontology.services.representations.diagrams.relationsoverview.edges.ReferenceEdgeRelationsOverviewDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.relationsoverview.nodes.EntityUnsynchronizedNodeDescriptionProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.DefaultViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramToolbarBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramLayoutOption;
import org.springframework.stereotype.Service;

/**
 * "Relations Overview" diagram view description builder.
 *
 * @author ntinsalhi
 */
@Service
public class ViewRelationsOverviewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String RELATIONS_OVERVIEW_DIAGRAM_NAME = "Relations Overview Diagram";

    private IColorProvider colorProvider;

    private final DiagramToolbarBuilder diagramToolbarBuilder = new DiagramToolbarBuilder();

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        this.colorProvider = colorProvider;
        return this.createRelationsOverviewDiagramDescription();
    }

    private DiagramDescription createRelationsOverviewDiagramDescription() {
        var diagramDescription = diagramBuilderHelper.newDiagramDescription()
                .name(RELATIONS_OVERVIEW_DIAGRAM_NAME)
                .domainType("entity::Entity")
                .titleExpression("aql:self.name + ' Relations Overview'")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.NONE)
                .style(diagramBuilderHelper.newDiagramStyleDescription().build())
                .toolbar(this.diagramToolbarBuilder.build())
                .build();

        var cache = new DefaultViewDiagramElementFinder();
        var diagramElementDescriptionProviders = this.createDiagramElementDescriptionProviders(this.colorProvider);

        diagramElementDescriptionProviders.forEach(provider -> {
            var diagramElementDescription = provider.create();
            cache.put(diagramElementDescription);
        });

        diagramElementDescriptionProviders.forEach(diagramElementDescriptionProvider -> diagramElementDescriptionProvider.link(diagramDescription, cache));

        var palette = new RelationsOverviewDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }

    private List<IDiagramElementDescriptionProvider<?>> createDiagramElementDescriptionProviders(IColorProvider colorProvider) {
        var diagramElementDescriptionProviders = new ArrayList<IDiagramElementDescriptionProvider<?>>();

        diagramElementDescriptionProviders.add(new EntityUnsynchronizedNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new ReferenceEdgeRelationsOverviewDescriptionProvider(colorProvider));

        return diagramElementDescriptionProviders;
    }
}
