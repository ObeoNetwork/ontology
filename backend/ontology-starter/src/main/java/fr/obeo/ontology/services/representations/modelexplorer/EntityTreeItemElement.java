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
package fr.obeo.ontology.services.representations.modelexplorer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerServices;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Namespace;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This virtual tree item corresponds to an Entity.
 *
 * @author lfasani
 */
public class EntityTreeItemElement implements TreeItemFragment {

    private final Entity entity;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IExplorerServices explorerServices;

    static final String TYPE = "EntityTreeItemFragment";

    public EntityTreeItemElement(Entity entity, IProjectSemanticDataSearchService projectSemanticDataSearchService, IRepresentationMetadataSearchService representationMetadataSearchService,
            IIdentityService identityService, ILabelService labelService, IExplorerServices explorerServices) {
        this.entity = Objects.requireNonNull(entity);
        this.projectSemanticDataSearchService = projectSemanticDataSearchService;
        this.representationMetadataSearchService = representationMetadataSearchService;
        this.identityService = identityService;
        this.labelService = labelService;
        this.explorerServices = explorerServices;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public String getId() {
        return this.identityService.getId(this.entity);
    }

    public String getLabel() {
        return this.entity.getName();
    }

    public List<String> getIconURL() {
        return this.labelService.getImagePaths(this.entity);
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        boolean hasComments = !activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_COMMENTS_TREE_ITEM_FILTER_ID) && this.hasComments();
        boolean hasAttributes = !activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_ATTRIBUTES_TREE_FILTER_ID) && this.hasOwnedAttributes();
        boolean hasReferences = !activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_REFERENCES_TREE_FILTER_ID) && this.hasOwnedReferences();

        boolean result = hasComments || hasAttributes || hasReferences;

        result = result || Optional.ofNullable(this.entity.eContainer())
                .filter(Namespace.class::isInstance)
                .stream()
                .flatMap(namespace -> ((Namespace) namespace).getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .anyMatch(entity -> this.entity.equals(entity.getSupertype()));

        result = result || this.hasRepresentation(this.entity, editingContext);
        return result;
    }

    private boolean hasRepresentation(EObject self, IEditingContext editingContext) {
        String id = this.identityService.getId(self);
        return new UUIDParser().parse(editingContext.getId())
                .map(uuid -> this.representationMetadataSearchService.existAnyRepresentationMetadataForSemanticDataAndTargetObjectId(AggregateReference.to(uuid), id))
                .orElse(false);
    }

    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        List<Object> result = new ArrayList<>();
        var semanticDataId = new UUIDParser().parse(editingContext.getId());

        if (semanticDataId.isPresent()) {
            var representationMetadata = new ArrayList<>(
                    this.representationMetadataSearchService.findAllRepresentationMetadataBySemanticDataAndTargetObjectId(AggregateReference.to(semanticDataId.get()), this.getId()));
            representationMetadata.sort(Comparator.comparing(RepresentationMetadata::getLabel));
            result.addAll(representationMetadata);
        }

        if (!activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_COMMENTS_TREE_ITEM_FILTER_ID) && this.hasComments()) {
            result.add(new CommentsTreeItemFragment(this.entity, this.identityService, this.labelService));
        }

        if (!activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_ATTRIBUTES_TREE_FILTER_ID)
                && this.hasOwnedAttributes()) {
            result.add(new AttributesTreeItemFragment(this.entity, this.identityService, this.labelService));
        }

        if (!activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_REFERENCES_TREE_FILTER_ID)
                && this.hasOwnedReferences()) {
            result.add(new ReferencesTreeItemFragment(this.entity, this.identityService, this.labelService));
        }

        result.addAll(Optional.ofNullable(this.entity.eContainer())
                .filter(Namespace.class::isInstance)
                .stream()
                .flatMap(namespace -> ((Namespace) namespace).getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(e -> this.entity.equals(e.getSupertype()))
                .map(e -> new EntityTreeItemElement(e, this.projectSemanticDataSearchService, this.representationMetadataSearchService, this.identityService, this.labelService, this.explorerServices))
                .map(Object.class::cast)
                .toList());

        return result;
    }

    private boolean hasComments() {
        var metadatas = this.entity.getMetadatas();
        return metadatas != null
                && metadatas.getMetadatas() != null
                && !metadatas.getMetadatas().isEmpty();
    }

    private boolean hasOwnedAttributes() {
        return !this.entity.getOwnedAttributes().isEmpty();
    }

    private boolean hasOwnedReferences() {
        return !this.entity.getOwnedReferences().isEmpty();
    }

    public String getTreeItemId() {
        return UriComponentsBuilder.fromUriString(OntologyExplorerServices.FRAGMENT_URI_PREFIX)
                .queryParam(OntologyExplorerServices.FRAGMENT_TYPE_PARAM, TYPE)
                .queryParam(OntologyExplorerServices.SEMANTIC_OBJECT_ID_PARAM, this.identityService.getId(this.entity))
                .encode()
                .build().toUri().toString();
    }

    public boolean isEditable() {
        return false;
    }

    public boolean isDeletable() {
        return false;
    }

    public boolean isSelectable() {
        return true;
    }

    @Override
    public String getKind() {
        return this.explorerServices.getKind(this.entity);
    }
}
