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
package fr.obeo.ontology.services.representations.modelexplorer;

import fr.obeo.ontology.services.representations.providers.ViewExplorerTreeDescriptionProvider;
import org.eclipse.sirius.components.collaborative.trees.api.IDropTreeItemHandler;
import org.eclipse.sirius.components.collaborative.trees.dto.DropTreeItemInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.trees.Tree;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Handles drop operations in the Ontology explorer tree
 *
 * @author ntinsalhi
 */
@Service
public class OntologyExplorerDropTreeItemHandler implements IDropTreeItemHandler {

    private final ViewExplorerTreeDescriptionProvider viewOntologyExplorerTreeDescriptionProvider;

    private final OntologyExplorerDropTreeItemExecutor ontologyExplorerDropTreeItemExecutor;

    public OntologyExplorerDropTreeItemHandler(ViewExplorerTreeDescriptionProvider viewOntologyExplorerTreeDescriptionProvider, OntologyExplorerDropTreeItemExecutor ontologyExplorerDropTreeItemExecutor) {
        this.viewOntologyExplorerTreeDescriptionProvider = Objects.requireNonNull(viewOntologyExplorerTreeDescriptionProvider);
        this.ontologyExplorerDropTreeItemExecutor = Objects.requireNonNull(ontologyExplorerDropTreeItemExecutor);
    }


    @Override
    public boolean canHandle(IEditingContext editingContext, Tree tree) {
        return this.viewOntologyExplorerTreeDescriptionProvider.getRepresentationDescriptionId().equals(tree.getDescriptionId());
    }

    @Override
    public IStatus handle(IEditingContext editingContext, Tree tree, DropTreeItemInput input) {
        return this.ontologyExplorerDropTreeItemExecutor.drop(editingContext, tree, input.droppedElementIds(), input.targetElementId(), input.index());
    }
}
