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

import fr.obeo.ontology.services.representations.EntityJavaService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerLabelService;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerServices;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.Reference;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * Java service for Explorer tree.
 *
 * @author lfasani
 */
public class OntologyExplorerServices {

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IExplorerServices explorerServices;

    private final IExplorerLabelService explorerLabelService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    private final EntityJavaService entityJavaService;

    public OntologyExplorerServices(IIdentityService identityService, ILabelService labelService, IRepresentationMetadataSearchService representationMetadataSearchService,
            IExplorerServices explorerServices, IExplorerLabelService explorerLabelService,
            IProjectSemanticDataSearchService projectSemanticDataSearchService, EntityJavaService entityJavaService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = labelService;
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.explorerServices = Objects.requireNonNull(explorerServices);
        this.explorerLabelService = explorerLabelService;
        this.projectSemanticDataSearchService = Objects.requireNonNull(projectSemanticDataSearchService);
        this.entityJavaService = entityJavaService;
    }

    public List<Object> getElements(IEditingContext editingContext) {
        List<Object> results = new ArrayList<>();
        if (editingContext instanceof EditingContext siriusWebContext) {
            siriusWebContext.getDomain().getResourceSet().getResources().stream()
                    .forEach(results::add);
        }
        return results;
    }

    public List<Object> getElements(Object self, List<String> activeFilterIds) {
        List<Object> results = new ArrayList<>();
        if (self instanceof EntityTreeItemElement entityTreeItemElement) {
            results.add(entityTreeItemElement.getEntity());
        }
        return results;
    }

    public String getTreeItemId(Object self) {
        String id = null;
        if (self instanceof TreeItemFragment treeItemFragment) {
            id = treeItemFragment.getTreeItemId();
        } else {
            id = this.explorerServices.getTreeItemId(self);
        }
        return id;
    }

    public String getKind(Object self) {
        String kind = "";
        if (self instanceof TreeItemFragment treeItemFragment) {
            kind = treeItemFragment.getKind();
        } else {
            kind = this.explorerServices.getKind(self);
        }
        return kind;
    }

    public String getLabel(Object self) {
        String label = "";
        if (self instanceof TreeItemFragment treeItemFragment) {
            label = treeItemFragment.getLabel();
        } else {
            label = String.valueOf(this.labelService.getStyledLabel(self));
        }
        return label;
    }

    public List<String> getImageURL(Object self) {
        List<String> result = List.of();
        if (self instanceof TreeItemFragment treeItemFragment) {
            result = treeItemFragment.getIconURL();
        } else {
            result = this.labelService.getImagePaths(self);
        }
        return result;
    }

    public boolean hasChildren(Object self, IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        boolean hasChildren = false;
        if (self instanceof TreeItemFragment treeItemFragment) {
            hasChildren = treeItemFragment.hasChildren(editingContext, expandedIds, activeFilterIds);
        } else if (self instanceof Resource resource) {
            hasChildren = resource.getContents().stream()
                    .filter(Root.class::isInstance)
                    .map(Root.class::cast)
                    .flatMap(root -> root.getOwnedNamespaces().stream())
                    .findFirst()
                    .isPresent();
        } else if (self instanceof Namespace namespace) {
            hasChildren = namespace.getTypes().stream()
                    .filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .anyMatch(entity -> entity.getSupertype() == null);
        } else if (self instanceof EObject eObject) {
            hasChildren = !eObject.eContents().isEmpty();
        }
        return hasChildren;
    }

    public List<Object> getChildren(Object self, IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds, List<RepresentationMetadata> existingRepresentations) {
        List<Object> result = new ArrayList<>();
        String id = this.getTreeItemId(self);
        if (expandedIds.contains(id)) {
            if (self instanceof TreeItemFragment treeItemFragment) {
                result.addAll(treeItemFragment.getChildren(editingContext, expandedIds, activeFilterIds));
            } else if (self instanceof Resource resource) {
                result.addAll(resource.getContents().stream()
                        .filter(Root.class::isInstance)
                        .map(Root.class::cast)
                        .flatMap(root -> root.getOwnedNamespaces().stream())
                        .toList());
            } else if (self instanceof Namespace namespace) {
                var semanticDataId = new UUIDParser().parse(editingContext.getId());

                if (semanticDataId.isPresent()) {
                    var representationMetadata = new ArrayList<>(
                            this.representationMetadataSearchService.findAllRepresentationMetadataBySemanticDataAndTargetObjectId(AggregateReference.to(semanticDataId.get()),
                                    this.identityService.getId(namespace)));
                    representationMetadata.sort(Comparator.comparing(RepresentationMetadata::getLabel));
                    result.addAll(representationMetadata);
                }

                result.addAll(namespace.getTypes().stream()
                        .filter(Entity.class::isInstance)
                        .map(Entity.class::cast)
                        .filter(entity -> entity.getSupertype() == null)
                        .map(e -> new EntityTreeItemElement(e, this.projectSemanticDataSearchService, this.representationMetadataSearchService, this.identityService, this.labelService,
                                this.explorerServices))
                        .toList());
            } else {
                result.addAll(this.explorerServices.getDefaultChildren(self, editingContext, expandedIds, existingRepresentations));
            }
        }
        return result;
    }

    public Object getParent(Object self, String treeItemId, IEditingContext editingContext) {
        return Optional.of(self)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .map(Entity::getSupertype)
                .map(Object.class::cast)
                .orElseGet(() -> this.explorerServices.getParent(self, treeItemId, editingContext));
    }

    public Object getTreeItemObject(String treeItemId, IEditingContext editingContext) {
        return this.explorerServices.getTreeItemObject(treeItemId, editingContext);
    }

    public boolean isEditable(Object self) {
        boolean result = true;
        if (self instanceof TreeItemFragment treeItemFragment) {
            result = treeItemFragment.isEditable();
        } else {
            result = this.explorerLabelService.isEditable(self);
        }
        return result;
    }

    public boolean isDeletable(Object self) {
        boolean result = true;
        if (self instanceof TreeItemFragment treeItemFragment) {
            result = treeItemFragment.isDeletable();
        } else {
            result = this.explorerServices.isDeletable(self);
        }
        return result;
    }

    public boolean isSelectable(Object self) {
        return true;
    }

    public boolean isEntityTreeItemElement(Object object) {
        return object instanceof EntityTreeItemElement;
    }

    public int getEntityLevel(EntityTreeItemElement entityTreeItemElement) {
        Entity entity = entityTreeItemElement.getEntity();
        return this.entityJavaService.getEntityLevel(entity);
    }

    public String getEntityTreeItemLabelPrefix(EntityTreeItemElement entityTreeItemElement) {
        int entityLevel = this.getEntityLevel(entityTreeItemElement);
        return entityLevel > 0 ? "[" + entityLevel + "] " : "";
    }

    public String getEntityTreeItemLabelValue(EntityTreeItemElement entityTreeItemElement) {
        return entityTreeItemElement.getLabel();
    }

    public Entity createSubEntity(EntityTreeItemElement entityTreeItemElement, String name) {
        return this.entityJavaService.createSubEntity(entityTreeItemElement.getEntity(), name);
    }

    public boolean isDeleteAuthorized(EObject eObject) {
        return List.of(Attribute.class, Annotation.class, Reference.class).stream()
                .anyMatch(clazz -> clazz.isInstance(eObject));
    }

    public Entity deleteEntity(EntityTreeItemElement entityTreeItemElement) {
        return this.entityJavaService.deleteEntity(entityTreeItemElement.getEntity());
    }
}
