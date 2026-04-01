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

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IActionHandler;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.CollapsingState;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.events.UpdateCollapsingStateEvent;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.springframework.stereotype.Service;

/**
 * Expand/Collapse node action handler.
 *
 * @author ntinsalhi
 */
@Service
public class ExpandCollapseActionHandler implements IActionHandler {

    @Override
    public boolean canHandle(IEditingContext editingContext, DiagramContext diagramContext, IDiagramElement diagramElement, String actionId) {
        return actionId.equals(ExpandCollapseNodeAction.EXPAND_COLLAPSE_ACTION_ID);
    }

    @Override
    public IStatus handle(IEditingContext editingContext, DiagramContext diagramContext, IDiagramElement diagramElement, String actionId) {
        if (diagramContext.diagram().getNodes().stream().anyMatch(node -> node.getId().equals(diagramElement.getId()) && node.getCollapsingState() == CollapsingState.COLLAPSED)) {
            diagramContext.diagramEvents().add(new UpdateCollapsingStateEvent(diagramElement.getId(), CollapsingState.EXPANDED));
        } else {
            diagramContext.diagramEvents().add(new UpdateCollapsingStateEvent(diagramElement.getId(), CollapsingState.COLLAPSED));
        }

        return new Success();
    }
}
