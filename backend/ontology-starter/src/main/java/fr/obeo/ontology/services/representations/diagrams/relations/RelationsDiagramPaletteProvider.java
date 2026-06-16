/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package fr.obeo.ontology.services.representations.diagrams.relations;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DropTool;

/**
 * Provide Palette for the relation overview diagram.
 *
 * @author lfasani
 */
public class RelationsDiagramPaletteProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public RelationsDiagramPaletteProvider(DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = new ViewBuilders();
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropTool(this.createDropFromExplorerTool())
                .build();
    }

    public DropTool createDropFromExplorerTool() {
       var dropElementFromExplorer = this.viewBuilderHelper.newChangeContext()
                        .expression("aql:self.dropIntoRelationDiagramFromExplorer(editingContext, diagramContext)")
                        .build();

        return this.diagramBuilderHelper.newDropTool()
                .name("Drop from Explorer")
                .body(dropElementFromExplorer)
                .build();
    }
}
