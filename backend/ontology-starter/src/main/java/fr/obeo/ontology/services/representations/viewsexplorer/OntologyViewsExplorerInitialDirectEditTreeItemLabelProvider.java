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
package fr.obeo.ontology.services.representations.viewsexplorer;

import fr.obeo.ontology.services.project.OntologyEditingContextPredicate;
import fr.obeo.ontology.services.representations.providers.ViewRelationsDiagramDescriptionProvider;

import java.util.Objects;

import org.eclipse.sirius.components.collaborative.trees.api.IInitialDirectEditTreeItemLabelProvider;
import org.eclipse.sirius.components.collaborative.trees.dto.InitialDirectEditElementLabelInput;
import org.eclipse.sirius.components.collaborative.trees.dto.InitialDirectEditElementLabelSuccessPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.ViewsExplorerInitialDirectEditTreeItemLabelProvider;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Starts direct editing with the stored diagram name instead of the entity path displayed in Views.
 *
 * @author lfasani
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OntologyViewsExplorerInitialDirectEditTreeItemLabelProvider implements IInitialDirectEditTreeItemLabelProvider {

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    private final IObjectSearchService objectSearchService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final ViewsExplorerInitialDirectEditTreeItemLabelProvider defaultProvider;

    public OntologyViewsExplorerInitialDirectEditTreeItemLabelProvider(OntologyEditingContextPredicate ontologyEditingContextPredicate,
            IObjectSearchService objectSearchService, IRepresentationDescriptionSearchService representationDescriptionSearchService,
            ViewsExplorerInitialDirectEditTreeItemLabelProvider defaultProvider) {
        this.ontologyEditingContextPredicate = Objects.requireNonNull(ontologyEditingContextPredicate);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.defaultProvider = Objects.requireNonNull(defaultProvider);
    }

    @Override
    public boolean canHandle(Tree tree) {
        return tree != null && this.defaultProvider.canHandle(tree);
    }

    @Override
    public IPayload handle(IEditingContext editingContext, Tree tree, InitialDirectEditElementLabelInput input) {
        if (this.ontologyEditingContextPredicate.test(editingContext)) {
            var optionalMetadata = this.objectSearchService.getObject(editingContext, input.treeItemId())
                    .filter(RepresentationMetadata.class::isInstance)
                    .map(RepresentationMetadata.class::cast)
                    .filter(metadata -> this.isRelationsDiagram(editingContext, metadata));
            if (optionalMetadata.isPresent()) {
                return new InitialDirectEditElementLabelSuccessPayload(input.id(), optionalMetadata.get().getLabel());
            }
        }
        return this.defaultProvider.handle(editingContext, tree, input);
    }

    private boolean isRelationsDiagram(IEditingContext editingContext, RepresentationMetadata metadata) {
        return this.representationDescriptionSearchService.findById(editingContext, metadata.getDescriptionId())
                .map(description -> ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME.equals(description.getLabel()))
                .orElse(false);
    }
}
