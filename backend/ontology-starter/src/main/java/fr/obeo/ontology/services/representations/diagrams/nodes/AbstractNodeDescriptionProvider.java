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
package fr.obeo.ontology.services.representations.diagrams.nodes;

import fr.obeo.ontology.services.representations.diagrams.AbstractDescriptionProvider;

import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.providers.INodeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;

/**
 * Abstract class for Node Description Providers.
 *
 * @author lfasani
 */
public abstract class AbstractNodeDescriptionProvider extends AbstractDescriptionProvider implements INodeDescriptionProvider {

    public static final String INITIAL_NODE_SIZE = "0";

    protected NodePalette createEntityNodePalette(int level) {
        return new DiagramBuilders().newNodePalette()
                .labelEditTool(new DiagramBuilders().newLabelEditTool()
                        .name("Label Edit Tool " + level)
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.defaultEditLabel(newLabel)")
                                .build())
                        .build())
                .deleteTool(new DiagramBuilders().newDeleteTool()
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.deleteEntity()")
                                .build())
                        .build())
                .nodeTools(new DiagramBuilders().newNodeTool()
                        .name("New Sub Entity")
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.createSubEntity('New Sub Entity')")
                                .build())
                        .build())
                .build();
    }

    protected NodeDescription createBorderNodeDescription(int level) {
        ImageNodeStyleDescription imageNodeStyleDescription = new DiagramBuilders().newImageNodeStyleDescription()
                .shape("/customImages/blueRing.svg")
                .borderSize(0)
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name("BorderNodeLevel" + level)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(AQL_SELF)
                .style(imageNodeStyleDescription)
                .defaultHeightExpression("20")
                .defaultWidthExpression("20")
                .keepAspectRatio(true)
                .userResizable(UserResizableDirection.NONE)
                .build();
    }

    protected EdgeTool createEdgeTool(NodeDescription targetNodeDescription) {
        return new DiagramBuilders().newEdgeTool()
                .targetElementDescriptions(targetNodeDescription)
                .body(new ChangeContextBuilder()
                        .expression("aql:semanticEdgeSource.createSubEntity('New Entity')")
                        .build())
                .build();
    }

}
