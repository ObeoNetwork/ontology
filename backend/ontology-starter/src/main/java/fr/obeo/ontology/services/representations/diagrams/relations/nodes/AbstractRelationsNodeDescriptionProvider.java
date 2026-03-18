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

import fr.obeo.ontology.services.representations.diagrams.AbstractDescriptionProvider;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.providers.INodeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.NodePalette;

/**
 * Abstract class for relations diagram node description providers.
 *
 * @author ntinsalhi
 */
public abstract class AbstractRelationsNodeDescriptionProvider extends AbstractDescriptionProvider implements INodeDescriptionProvider {
    public static final int DEFAULT_ENTITY_NODE_HEIGHT = 100;

    public static final int DEFAULT_ENTITY_NODE_WIDTH = 80;

    protected NodePalette createEntityNodePalette() {
        return new DiagramBuilders().newNodePalette()
                .labelEditTool(new DiagramBuilders().newLabelEditTool()
                        .name("Label Edit Tool")
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.defaultEditLabel(newLabel)")
                                .build())
                        .build())
                .deleteTool(new DiagramBuilders().newDeleteTool()
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.deleteEntity()")
                                .build())
                        .build())
                .build();
    }
}
