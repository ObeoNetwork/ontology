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
package fr.obeo.ontology.services.representations.diagrams.handlers;

import fr.obeo.ontology.services.representations.diagrams.relations.nodes.EntitySynchronizedNodeDescriptionProvider;
import fr.obeo.ontology.services.representations.diagrams.relationsoverview.nodes.EntityUnsynchronizedNodeDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewRelationsDiagramDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewRelationsOverviewDiagramDescriptionProvider;

import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.collaborative.diagrams.api.IActionsProvider;
import org.eclipse.sirius.components.collaborative.diagrams.dto.Action;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.CollapsingState;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.springframework.stereotype.Service;

/**
 * Used to provide the expand/collapse node action.
 *
 * @author ntinsalhi
 */
@Service
public class ExpandCollapseNodeAction implements IActionsProvider {

    protected static final String EXPAND_COLLAPSE_ACTION_ID = "expand-collapse-action-id";

    private final IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService;

    public ExpandCollapseNodeAction(IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService) {
        this.viewDiagramDescriptionSearchService = Objects.requireNonNull(viewDiagramDescriptionSearchService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, DiagramDescription diagramDescription, IDiagramElement diagramElement) {
        var diagramLabel = diagramDescription.getLabel();
        var isRelationsDiagram = diagramLabel.equals(ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME)
                || diagramLabel.equals(ViewRelationsOverviewDiagramDescriptionProvider.RELATIONS_OVERVIEW_DIAGRAM_NAME);
        return isRelationsDiagram && diagramElement instanceof Node node && this.isRelationsDiagramEntityNode(node, editingContext);
    }

    @Override
    public List<Action> handle(IEditingContext editingContext, DiagramDescription diagramDescription, IDiagramElement diagramElement) {
        Node diagramNode = (Node) diagramElement;
        List<String> iconPath = diagramNode.getCollapsingState().equals(CollapsingState.COLLAPSED) ?
                List.of("/diagram-images/expand.svg")
                : List.of("/diagram-images/collapse.svg");

        String tooltip = diagramNode.getCollapsingState().equals(CollapsingState.COLLAPSED) ? "Expand" : "Collapse";

        return List.of(new Action(EXPAND_COLLAPSE_ACTION_ID, iconPath, tooltip));
    }

    private boolean isRelationsDiagramEntityNode(Node node, IEditingContext editingContext) {
        return this.viewDiagramDescriptionSearchService.findViewNodeDescriptionById(editingContext, node.getDescriptionId())
                .filter(nodeDescription ->
                        EntitySynchronizedNodeDescriptionProvider.ENTITY_SYNCHRONIZED_NODE_NAME.equals(nodeDescription.getName())
                                || EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME.equals(nodeDescription.getName()))
                .isPresent();
    }
}
