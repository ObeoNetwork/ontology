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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerServices;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Namespace;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * This virtual tree item corresponds to an Entity.
 *
 * @author lfasani
 */
public class EntityTreeItemElement {

    private final Entity entity;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    private final IObjectService objectService;

    private final IExplorerServices explorerServices;

    public Entity getEntity() {
        return entity;
    }

    public EntityTreeItemElement(Entity entity, IProjectSemanticDataSearchService projectSemanticDataSearchService, IRepresentationMetadataSearchService representationMetadataSearchService,
            IObjectService objectService, IExplorerServices explorerServices) {
        this.entity = Objects.requireNonNull(entity);
        this.projectSemanticDataSearchService = projectSemanticDataSearchService;
        this.representationMetadataSearchService = representationMetadataSearchService;
        this.objectService = objectService;
        this.explorerServices = explorerServices;
    }

    public String getId() {
        return objectService.getId(entity);
    }

    public String getLabel() {
        return this.entity.getName();
    }

    public List<String> getIconURL() {
        return this.explorerServices.getImageURL(this.entity);
    }

    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        boolean result = Optional.ofNullable(entity.eContainer())
                .filter(Namespace.class::isInstance)
                .stream()
                .flatMap(namespace -> ((Namespace) namespace).getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> this.entity.equals(entity.getSupertype()))
                .findFirst()
                .isPresent();
        return result;
    }

    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        List<Object> result = new ArrayList<>();

        if (this.entity.getSupertype() == null) {
            var semanticDataId = new UUIDParser().parse(editingContext.getId());

            if (semanticDataId.isPresent()) {
                var representationMetadata = new ArrayList<>(
                        this.representationMetadataSearchService.findAllRepresentationMetadataBySemanticDataAndTargetObjectId(AggregateReference.to(semanticDataId.get()), getId()));
                representationMetadata.sort(Comparator.comparing(RepresentationMetadata::getLabel));
                result.addAll(representationMetadata);
            }
        }

        result.addAll(Optional.ofNullable(entity.eContainer())
                .filter(Namespace.class::isInstance)
                .stream()
                .flatMap(namespace -> ((Namespace) namespace).getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(e -> this.entity.equals(e.getSupertype()))
                .map(e -> new EntityTreeItemElement(e, projectSemanticDataSearchService, representationMetadataSearchService, objectService, explorerServices))
                .map(Object.class::cast)
                .toList());

        return result;
    }

    public String getTreeItemId() {
        return this.explorerServices.getTreeItemId(this.getEntity());
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
}
