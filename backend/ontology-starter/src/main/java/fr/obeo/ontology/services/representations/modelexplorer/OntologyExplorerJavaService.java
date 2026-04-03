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

import fr.obeo.ontology.ontologymm.BusinessDomain;
import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.DataSource;
import fr.obeo.ontology.ontologymm.OntologyFactory;
import fr.obeo.ontology.ontologymm.OrganizationInformation;
import fr.obeo.ontology.services.representations.EntityJavaService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerLabelService;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerServices;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * Java service for Explorer tree.
 *
 * @author lfasani
 */
public class OntologyExplorerJavaService {
    public static final String FRAGMENT_URI_PREFIX = "o://fragment";

    public static final String SEMANTIC_OBJECT_ID_PARAM = "objectId";

    public static final String FRAGMENT_TYPE_PARAM = "fragmentType";

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyExplorerJavaService.class);

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IObjectSearchService objectSearchService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IExplorerServices explorerServices;

    private final IExplorerLabelService explorerLabelService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    private final EntityJavaService entityJavaService;

    private final IURLParser urlParser;

    public OntologyExplorerJavaService(IIdentityService identityService, ILabelService labelService, IObjectSearchService objectSearchService,
                                       IRepresentationMetadataSearchService representationMetadataSearchService,
                                       IExplorerServices explorerServices, IExplorerLabelService explorerLabelService,
                                       IProjectSemanticDataSearchService projectSemanticDataSearchService, EntityJavaService entityJavaService, IURLParser urlParser) {
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = labelService;
        this.objectSearchService = objectSearchService;
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.explorerServices = Objects.requireNonNull(explorerServices);
        this.explorerLabelService = explorerLabelService;
        this.projectSemanticDataSearchService = Objects.requireNonNull(projectSemanticDataSearchService);
        this.entityJavaService = entityJavaService;
        this.urlParser = urlParser;
    }

    public List<Object> getElements(IEditingContext editingContext) {
        List<Object> results = new ArrayList<>();
        if (editingContext instanceof EditingContext siriusWebContext) {
            siriusWebContext.getDomain().getResourceSet().getResources().stream()
                    .forEach(results::add);
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
        } else if (self instanceof Entity entity) {
            boolean hasComments = !activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_COMMENTS_TREE_ITEM_FILTER_ID) && hasComments(entity);
            boolean hasAttributes = !activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_ATTRIBUTES_TREE_FILTER_ID) && hasOwnedAttributes(entity);
            boolean hasReferences = !activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_REFERENCES_TREE_FILTER_ID) && hasOwnedReferences(entity);

            hasChildren = hasComments || hasAttributes || hasReferences;

            hasChildren = hasChildren || !entityJavaService.getSubEntities(entity).isEmpty();

            hasChildren = hasChildren || this.hasRepresentation(entity, editingContext);
        } else if (self instanceof EObject eObject) {
            hasChildren = !eObject.eContents().isEmpty();
        }
        return hasChildren;
    }

    private boolean hasRepresentation(EObject self, IEditingContext editingContext) {
        String id = this.identityService.getId(self);
        return new UUIDParser().parse(editingContext.getId())
                .map(uuid -> this.representationMetadataSearchService.existAnyRepresentationMetadataForSemanticDataAndTargetObjectId(AggregateReference.to(uuid), id))
                .orElse(false);
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

                resource.getContents().stream()
                        .filter(OrganizationInformation.class::isInstance)
                        .map(OrganizationInformation.class::cast)
                        .findFirst()
                        .ifPresent(organizationInformation -> {
                            result.add(new BusinessDomainsTreeItemFragment(organizationInformation, this.identityService));
                            result.add(new DataOwnersTreeItemFragment(organizationInformation, this.identityService));
                            result.add(new DataSourcesTreeItemFragment(organizationInformation, this.identityService));
                        });
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
                        .toList());
            } else if (self instanceof Entity entity) {
                var semanticDataId = new UUIDParser().parse(editingContext.getId());

                if (semanticDataId.isPresent()) {
                    var representationMetadata = new ArrayList<>(
                            this.representationMetadataSearchService.findAllRepresentationMetadataBySemanticDataAndTargetObjectId(AggregateReference.to(semanticDataId.get()),
                                    this.identityService.getId(entity)));
                    representationMetadata.sort(Comparator.comparing(RepresentationMetadata::getLabel));
                    result.addAll(representationMetadata);
                }

                if (!activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_COMMENTS_TREE_ITEM_FILTER_ID) && this.hasComments(entity)) {
                    result.add(new CommentsTreeItemFragment(entity, this.identityService, this.labelService));
                }

                if (!activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_ATTRIBUTES_TREE_FILTER_ID)
                        && this.hasOwnedAttributes(entity)) {
                    result.add(new AttributesTreeItemFragment(entity, this.identityService, this.labelService));
                }

                if (!activeFilterIds.contains(OntologyTreeFilterProvider.HIDE_REFERENCES_TREE_FILTER_ID)
                        && this.hasOwnedReferences(entity)) {
                    result.add(new ReferencesTreeItemFragment(entity, this.identityService, this.labelService));
                }

                result.addAll(entityJavaService.getSubEntities(entity));
            } else {
                result.addAll(this.explorerServices.getDefaultChildren(self, editingContext, expandedIds, existingRepresentations));
            }
        }
        return result;
    }

    private boolean hasComments(Entity entity) {
        var metadatas = entity.getMetadatas();
        return metadatas != null
                && metadatas.getMetadatas() != null
                && !metadatas.getMetadatas().isEmpty();
    }

    private boolean hasOwnedAttributes(Entity entity) {
        return !entity.getOwnedAttributes().isEmpty();
    }

    private boolean hasOwnedReferences(Entity entity) {
        return !entity.getOwnedReferences().isEmpty();
    }

    public Object getParent(Object self, String treeItemId, IEditingContext editingContext) {
        Object result = null;
        if (self instanceof Attribute attribute) {
            result = createFragment(attribute.eContainer(), AttributesTreeItemFragment.TYPE);
        } else if (self instanceof Reference reference) {
            result = createFragment(reference.eContainer(), ReferencesTreeItemFragment.TYPE);
        } else if (self instanceof Entity entity) {
            result = entity.getSupertype() != null ? entity.getSupertype() : entity.eContainer();
        } else if (self instanceof TreeItemFragment treeItemElement) {
            result = this.getSemanticObjectFromFragmentId(editingContext, treeItemId)
                    .map(semanticObject -> {
                        if (semanticObject instanceof Entity entity) {
                            String fragmentType = getFragmentType(editingContext, treeItemId);
                            if (ReferencesTreeItemFragment.TYPE.equals(fragmentType) || AttributesTreeItemFragment.TYPE.equals(fragmentType)) {
                                return entity;
                            }
                        }
                        return null;
                    })
                    .orElse(null);
        } else {
            result = this.explorerServices.getParent(self, treeItemId, editingContext);
        }

        return result;
    }

    private Optional<EObject> getSemanticObjectFromFragmentId(IEditingContext editingContext, String itemId) {
        try {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(itemId);
            if (parameters != null) {
                String semanticObjectId = parameters.get(SEMANTIC_OBJECT_ID_PARAM).get(0);
                return this.objectSearchService.getObject(editingContext, semanticObjectId).map(EObject.class::cast);
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Unparsable id {} : {}", itemId, e.getCause());
        }

        return Optional.empty();
    }

    private String getFragmentType(IEditingContext editingContext, String itemId) {
        try {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(itemId);
            if (parameters != null) {
                return parameters.get(FRAGMENT_TYPE_PARAM).get(0);
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Unparsable id {} : {}", itemId, e.getCause());
        }

        return "";
    }

    public Object getTreeItemObject(String treeItemId, IEditingContext editingContext) {
        Object result = null;
        if (treeItemId != null && treeItemId.startsWith(FRAGMENT_URI_PREFIX)) {
            result = this.fromFragmentId(editingContext, treeItemId);
        } else {
            result = this.explorerServices.getTreeItemObject(treeItemId, editingContext);
        }
        return result;
    }

    /**
     * Creates a new {@link TreeItemFragment} from a given id
     *
     * @param editingContext
     *         the current {@link IEditingContext}
     * @param itemId
     *         the id of the tree item
     * @return the right tree item fragment corresponding to given tree item
     */
    private TreeItemFragment fromFragmentId(IEditingContext editingContext, String itemId) {
        TreeItemFragment result = null;
        try {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(itemId);
            if (parameters != null) {
                String semanticObjectId = parameters.get(SEMANTIC_OBJECT_ID_PARAM).get(0);
                result = this.objectSearchService.getObject(editingContext, semanticObjectId)
                        .filter(EObject.class::isInstance)
                        .map(EObject.class::cast)
                        .map(eObject -> {
                            String fragmentType = parameters.get(FRAGMENT_TYPE_PARAM).get(0);
                            return createFragment(eObject, fragmentType);
                        })
                        .orElse(null);
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Unparsable id {} : {}", itemId, e.getCause());
        }

        return result;
    }

    private TreeItemFragment createFragment(EObject semanticObject, String fragmentType) {

        return switch (fragmentType) {
            case AttributesTreeItemFragment.TYPE -> Optional.of(semanticObject).filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .map(e -> new AttributesTreeItemFragment(e, this.identityService, this.labelService))
                    .orElse(null);
            case ReferencesTreeItemFragment.TYPE -> Optional.of(semanticObject).filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .map(e -> new ReferencesTreeItemFragment(e, this.identityService, this.labelService))
                    .orElse(null);
            case CommentsTreeItemFragment.TYPE -> Optional.of(semanticObject).filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .map(e -> new CommentsTreeItemFragment(e, this.identityService, this.labelService))
                    .orElse(null);
            case BusinessDomainsTreeItemFragment.TYPE -> Optional.of(semanticObject).filter(OrganizationInformation.class::isInstance)
                    .map(OrganizationInformation.class::cast)
                    .map(organizationInformation -> new BusinessDomainsTreeItemFragment(organizationInformation, this.identityService))
                    .orElse(null);
            case DataOwnersTreeItemFragment.TYPE -> Optional.of(semanticObject).filter(OrganizationInformation.class::isInstance)
                    .map(OrganizationInformation.class::cast)
                    .map(organizationInformation -> new DataOwnersTreeItemFragment(organizationInformation, this.identityService))
                    .orElse(null);
            case DataSourcesTreeItemFragment.TYPE -> Optional.of(semanticObject).filter(OrganizationInformation.class::isInstance)
                    .map(OrganizationInformation.class::cast)
                    .map(organizationInformation -> new DataSourcesTreeItemFragment(organizationInformation, this.identityService))
                    .orElse(null);
            default -> null;
        };
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

//    public boolean isEntityTreeItemElement(Object object) {
//        return object instanceof EntityTreeItemElement;
//    }

//    public int getEntityLevel(EntityTreeItemElement entityTreeItemElement) {
//        Entity entity = entityTreeItemElement.getEntity();
//        return this.entityJavaService.getEntityLevel(entity);
//    }

    public String getEntityLabelPrefix(Entity entity) {
        int entityLevel = entityJavaService.getEntityLevel(entity);
        return entityLevel > 0 ? "[" + entityLevel + "] " : "";
    }

    public boolean isDeleteAuthorized(EObject eObject) {
        return List.of(Attribute.class, Annotation.class, Reference.class).stream()
                .anyMatch(clazz -> clazz.isInstance(eObject));
    }

    public boolean isCreateObjectAllowed(TreeItem treeItem) {
        return Optional.ofNullable(this.urlParser.getParameterValues(treeItem.getId()))
                .map(stringListMap -> stringListMap.get(FRAGMENT_TYPE_PARAM))
                .filter(Objects::nonNull)
                .map(strings -> strings.get(0))
                .map(type -> {
                    List<String> idTypes = List.of(BusinessDomainsTreeItemFragment.TYPE, DataSourcesTreeItemFragment.TYPE, DataOwnersTreeItemFragment.TYPE);
                    return idTypes.contains(type);
                })
                .orElse(false);
    }

    public Object createObject(IEditingContext editingContext, TreeItem treeItem) {
        AtomicReference<Object> createObject = new AtomicReference<>();
        try {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(treeItem.getId());
            if (parameters != null) {
                String semanticObjectId = parameters.get(SEMANTIC_OBJECT_ID_PARAM).get(0);
                Optional<Object> semanticObject = this.objectSearchService.getObject(editingContext, semanticObjectId);
                String fragmentType = parameters.get(FRAGMENT_TYPE_PARAM).get(0);

                semanticObject.filter(OrganizationInformation.class::isInstance)
                        .map(OrganizationInformation.class::cast)
                        .ifPresent(organizationInformation -> {
                            if (fragmentType.equals(DataOwnersTreeItemFragment.TYPE)) {
                                DataOwner dataOwner = OntologyFactory.eINSTANCE.createDataOwner();
                                dataOwner.setName("Data Owner " + (organizationInformation.getDataOwners().size() + 1));
                                createObject.set(dataOwner);
                                organizationInformation.getDataOwners().add((DataOwner) createObject.get());
                            } else if (fragmentType.equals(BusinessDomainsTreeItemFragment.TYPE)) {
                                BusinessDomain businessDomain = OntologyFactory.eINSTANCE.createBusinessDomain();
                                businessDomain.setName("Functional Area " + (organizationInformation.getBusinessDomains().size() + 1));
                                createObject.set(businessDomain);
                                organizationInformation.getBusinessDomains().add((BusinessDomain) createObject.get());
                            } else if (fragmentType.equals(DataSourcesTreeItemFragment.TYPE)) {
                                DataSource dataSource = OntologyFactory.eINSTANCE.createDataSource();
                                dataSource.setName("Data Source " + (organizationInformation.getDataSources().size() + 1));
                                createObject.set(dataSource);
                                organizationInformation.getDataSources().add((DataSource) createObject.get());
                            }
                        });
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Unparsable id {} : {}", treeItem.getId(), e.getCause());
        }

        return createObject.get();
    }

    public String getCreateObjectLabel(TreeItem treeItem) {
        return Optional.ofNullable(this.urlParser.getParameterValues(treeItem.getId()))
                .map(stringListMap -> stringListMap.get(FRAGMENT_TYPE_PARAM))
                .filter(Objects::nonNull)
                .map(strings -> strings.get(0))
                .map(type -> {
                    String label = "";
                    if (DataOwnersTreeItemFragment.TYPE.equals(type)) {
                        label = "New Data Owner";
                    } else if (BusinessDomainsTreeItemFragment.TYPE.equals(type)) {
                        label = "New Functional Area";
                    } else if (DataSourcesTreeItemFragment.TYPE.equals(type)) {
                        label = "New Data Source";
                    }
                    return label;
                })
                .orElse("");
    }

    public boolean canCreateNewSubEntityExplorer(Object self) {
        return self instanceof Entity entity
                && this.entityJavaService.canCreateNewSubEntity(entity);
    }

    public Entity createCoreEntity(Namespace namespace, String name) {
        Entity newCoreEntity = EntityFactory.eINSTANCE.createEntity();
        newCoreEntity.setName(name);
        namespace.getTypes().add(newCoreEntity);

        return newCoreEntity;
    }
}
