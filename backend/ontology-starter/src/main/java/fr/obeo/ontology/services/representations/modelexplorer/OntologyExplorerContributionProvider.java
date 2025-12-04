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
package fr.obeo.ontology.services.representations.modelexplorer;

import fr.obeo.ontology.services.project.OntologyEditingContextPredicate;
import fr.obeo.ontology.services.representations.providers.ViewExplorerTreeDescriptionProvider;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.domain.Domain;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.view.emf.tree.ITreeIdProvider;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerTreeDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * This class is used to provide tree descriptions that can be used as explorer.
 *
 * @author lfasani
 */
@Service
public class OntologyExplorerContributionProvider implements IExplorerTreeDescriptionProvider {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService;

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    public OntologyExplorerContributionProvider(IRepresentationDescriptionSearchService representationDescriptionSearchService,
            IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService,
            OntologyEditingContextPredicate ontologyEditingContextPredicate) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.viewRepresentationDescriptionSearchService = Objects.requireNonNull(viewRepresentationDescriptionSearchService);
        this.ontologyEditingContextPredicate = Objects.requireNonNull(ontologyEditingContextPredicate);
    }

    @Override
    public List<TreeDescription> getDescriptions(IEditingContext editingContext) {
        var optionalDomainExplorerDescription = this.getOntologyExplorerTreeDescription(editingContext);

        return optionalDomainExplorerDescription.map(List::of).orElseGet(List::of);
    }

    private Optional<TreeDescription> getOntologyExplorerTreeDescription(IEditingContext editingContext) {
        if (ontologyEditingContextPredicate.test(editingContext)) {
            return this.representationDescriptionSearchService
                    .findAll(editingContext).values().stream()
                    .filter(TreeDescription.class::isInstance)
                    .map(TreeDescription.class::cast)
                    .filter(td -> this.isOntologyExplorerViewTreeDescription(td, editingContext))
                    .findFirst();
        }
        return Optional.empty();
    }

    private boolean isContainingDomainElement(IEditingContext editingContext) {
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            return emfEditingContext.getDomain().getResourceSet().getResources().stream()
                    .flatMap(res -> res.getContents().stream())
                    .anyMatch(Domain.class::isInstance);
        }
        return false;
    }

    private boolean isOntologyExplorerViewTreeDescription(TreeDescription treeDescription, IEditingContext editingContext) {
        if (treeDescription.getId().startsWith(ITreeIdProvider.TREE_DESCRIPTION_KIND)) {
            // this tree description comes from a tree DSL
            var optionalViewTreeDescription = this.viewRepresentationDescriptionSearchService.findById(editingContext, treeDescription.getId());
            if (optionalViewTreeDescription.isPresent()) {
                return optionalViewTreeDescription.get().getName().equals(ViewExplorerTreeDescriptionProvider.ONTOLOGY_EXPLORER_DESCRIPTION_NAME);
            }
        }
        return false;
//        return true;
    }
}
