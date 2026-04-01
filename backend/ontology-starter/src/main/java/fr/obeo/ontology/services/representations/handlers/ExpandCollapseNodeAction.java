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
package fr.obeo.ontology.services.representations.handlers;

import java.util.List;

import org.eclipse.sirius.components.collaborative.diagrams.api.IActionsProvider;
import org.eclipse.sirius.components.collaborative.diagrams.dto.Action;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.CollapsingState;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.springframework.stereotype.Service;

import static fr.obeo.ontology.services.representations.providers.ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME;
import static fr.obeo.ontology.services.representations.providers.ViewRelationsOverviewDiagramDescriptionProvider.RELATIONS_OVERVIEW_DIAGRAM_NAME;

/**
 * Used to provide the expand/collapse node action.
 *
 * @author ntinsalhi
 */
@Service
public class ExpandCollapseNodeAction implements IActionsProvider {

    protected static final String EXPAND_COLLAPSE_ACTION_ID = "expand-collapse-action-id";

    @Override
    public boolean canHandle(IEditingContext editingContext, DiagramDescription diagramDescription, IDiagramElement diagramElement) {
        var diagramLabel = diagramDescription.getLabel();
        var isRelationsDiagram = diagramLabel.equals(RELATIONS_DIAGRAM_NAME) || diagramLabel.equals(RELATIONS_OVERVIEW_DIAGRAM_NAME);
        return isRelationsDiagram && diagramElement instanceof Node node && this.isRelationsDiagramEntityNode(node);
    }

    @Override
    public List<Action> handle(IEditingContext editingContext, DiagramDescription diagramDescription, IDiagramElement diagramElement) {
        Node diagramNode = (Node) diagramElement;
        List<String> iconPath = diagramNode.getCollapsingState().equals(CollapsingState.COLLAPSED) ? List.of("/diagram-images/expand.svg") : List.of("/diagram-images/collapse.svg");
        String tooltip = diagramNode.getCollapsingState().equals(CollapsingState.COLLAPSED) ? "Expand" : "Collapse";


        return List.of(new Action(EXPAND_COLLAPSE_ACTION_ID, iconPath, tooltip));
    }

    private boolean isRelationsDiagramEntityNode(Node node) {
        return node.getChildNodes()
                .stream()
                .anyMatch(currentNode -> currentNode.getType().equals("node:rectangle"));
    }
}
