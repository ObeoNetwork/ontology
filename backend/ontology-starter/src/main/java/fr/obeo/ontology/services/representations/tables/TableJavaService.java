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
package fr.obeo.ontology.services.representations.tables;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.obeo.ontology.services.representations.EntityJavaService;
import fr.obeo.ontology.services.representations.providers.ViewEntityTableDescriptionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.tables.ColumnFilter;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.StructuredType;
import org.springframework.stereotype.Service;

/**
 * Java Service for the Table view.
 *
 * @author lfasani
 */
@Service
public class TableJavaService {

    private final IIdentityService identityService;

    private final ObjectMapper objectMapper;

    private final EntityJavaService entityJavaService;

    private final ILabelService labelService;

    public TableJavaService(IIdentityService identityService, ObjectMapper objectMapper, EntityJavaService entityJavaService, ILabelService labelService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.entityJavaService = Objects.requireNonNull(entityJavaService);
        this.labelService = Objects.requireNonNull(labelService);
    }

    public List<Entity> getAllOrderedEntities(Namespace namespace, List<Object> expandedIds, String globalFilter, List<ColumnFilter> columnFilters, List<String> activeRowFilterIds) {
        List<Entity> entities = new ArrayList<>();
        namespace.getTypes()
                .stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> entity.getSupertype() == null && this.matchesRowFilters(entity, activeRowFilterIds))
                .forEach(entity -> this.addEntityAndSubTypes(null, entity, entities, expandedIds, globalFilter, columnFilters));
        return entities;
    }

    private boolean matchesRowFilters(Entity entity, List<String> activeRowFilterIds) {
        var entityId = this.identityService.getId(entity);
        return activeRowFilterIds.stream().anyMatch(rowFilterId -> this.contains(rowFilterId, entityId));
    }

    private void addEntityAndSubTypes(Entity parent, Entity entity, List<Entity> entities, List<Object> expandedIds, String globalFilter, List<ColumnFilter> columnFilters) {
        if (!entities.contains(entity)) {

            //If no filter is active, we only display entity if the parent is expanded
            if (!this.filterActivated(globalFilter, columnFilters)) {
                if (this.isAllAncestorsExpanded(parent, expandedIds)) {
                    entities.add(entity);
                }
            } else {
                if (this.matchFilter(entity, globalFilter, columnFilters)) {
                    this.findEntitySuperEntities(entity, entities);
                    entities.add(entity);
                }
            }
            Optional.of(entity)
                    .map(EObject::eContainer)
                    .filter(Namespace.class::isInstance)
                    .map(Namespace.class::cast).stream()
                    .flatMap(namespace -> namespace.getTypes().stream())
                    .filter(Entity.class::isInstance).map(Entity.class::cast)
                    .filter(currentEntity -> EcoreUtil.equals(entity, currentEntity.getSupertype()))
                    .forEach(currentEntity -> this.addEntityAndSubTypes(entity, currentEntity, entities, expandedIds, globalFilter, columnFilters));
        }
    }

    private boolean filterActivated(String globalFilter, List<ColumnFilter> columnFilters) {
        return (globalFilter != null && !globalFilter.isBlank()) || !columnFilters.isEmpty();
    }

    private boolean isAllAncestorsExpanded(Entity parent, List<Object> expandedIds) {
        if (parent == null) {
            return true;
        }
        String parentEntityId = this.identityService.getId(parent);
        return expandedIds.contains(parentEntityId) && this.isAllAncestorsExpanded(this.getSuperEntity(parent), expandedIds);
    }

    private Entity getSuperEntity(Entity parent) {
        return Optional.of(parent)
                .map(Entity::getSupertype)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .orElse(null);
    }

    private boolean matchFilter(Entity entity, String globalFilter, List<ColumnFilter> columnFilters) {
        boolean isValidCandidate = true;
        if (globalFilter != null && !globalFilter.isBlank()) {
            isValidCandidate = entity.getName() != null && this.contains(entity.getName(), globalFilter);
        }

        isValidCandidate = isValidCandidate && columnFilters.stream().allMatch(columnFilter -> {
            String columnFilterId = columnFilter.id();
            String columnFilterValue = columnFilter.value().replace("\"", "").trim();

            return switch (columnFilterId) {
                case ViewEntityTableDescriptionProvider.ENTITY_TABLE_ATTRIBUTES_COLUMN ->
                        entity.getOwnedAttributes() != null && this.isValidAttributesFilter(entity, columnFilterValue);
                case ViewEntityTableDescriptionProvider.ENTITY_TABLE_ATTRIBUTES_COMMENTS ->
                        entity.getMetadatas() != null && this.isValidMetadataFilter(entity, columnFilterValue);
                case ViewEntityTableDescriptionProvider.ENTITY_TABLE_REFERENCES_COLUMN ->
                        entity.getOwnedReferences() != null && this.isValidReferencesFilter(entity, columnFilterValue);
                case ViewEntityTableDescriptionProvider.ENTITY_TABLE_BUSINESSDOMAIN_COLUMN ->
                        this.entityJavaService.getEntityBusinessDomain(entity) != null && this.isValidFunctionalAreaFilter(entity, columnFilterValue);
                case ViewEntityTableDescriptionProvider.ENTITY_TABLE_DATAOWNER_COLUMN ->
                        this.entityJavaService.getEntityDataOwner(entity) != null && this.isValidDataOwnerFilter(entity, columnFilterValue);
                case ViewEntityTableDescriptionProvider.ENTITY_TABLE_DATASOURCES_COLUMN ->
                        this.entityJavaService.getEntityDataSources(entity) != null && this.isValidDataSourcesFilter(entity, columnFilterValue);
                default -> true;
            };
        });

        return isValidCandidate;
    }

    private List<Entity> findEntitySuperEntities(Entity entity, List<Entity> entities) {
        var superType = entity.getSupertype();
        if (superType instanceof Entity superEntity) {
            findEntitySuperEntities(superEntity, entities);
            if (!entities.contains(superEntity)) {
                entities.add(superEntity);
            }
        }
        return entities;
    }

    private boolean isValidAttributesFilter(Entity entity, String columnFilterValue) {
        return entity.getOwnedAttributes().stream().anyMatch(attribute -> this.contains(attribute.getName(), columnFilterValue));
    }

    private boolean isValidMetadataFilter(Entity entity, String filterValue) {
        return entity.getMetadatas().getMetadatas().stream()
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .anyMatch(annotation -> this.contains(annotation.getTitle(), filterValue) || this.contains(annotation.getBody(), filterValue));
    }

    private boolean isValidReferencesFilter(Entity entity, String filterValue) {
        return entity.getOwnedReferences()
                .stream()
                .anyMatch(reference -> this.contains(reference.getName(), filterValue));
    }

    private boolean isValidFunctionalAreaFilter(Entity entity, String filterValue) {
        return this.contains(this.entityJavaService.getEntityBusinessDomain(entity).getName(), filterValue);
    }

    private boolean isValidDataOwnerFilter(Entity entity, String filterValue) {
        var dataOwner = this.entityJavaService.getEntityDataOwner(entity);
        return this.contains(dataOwner.getCode(), filterValue)
                || this.contains(dataOwner.getName(), filterValue) ;
    }

    private boolean isValidDataSourcesFilter(Entity entity, String filterValue) {
        return this.entityJavaService.getEntityDataSources(entity).stream()
                .anyMatch(dataSource -> this.contains(dataSource.getCode(), filterValue) || this.contains(dataSource.getName(), filterValue));
    }

    private boolean contains(String s, String text) {
        return s.toLowerCase().contains(text.toLowerCase());
    }

    public String getLevelLabel(Entity entity) {
        int entityLevel = this.entityJavaService.getEntityLevel(entity);
        String label = "";
        if (entityLevel > 0) {
            label = "[" + entityLevel + "]";
        }
        return label;
    }

    public String getEntityReferencesCellLabel(Entity self) {
        return self.getOwnedReferences().stream()
                .map(object -> this.labelService.getStyledLabel(object).toString() + " " + Optional.ofNullable(object.getReferencedType()).map(StructuredType::getName).orElse(""))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String getAttributesCellLabel(Entity entity) {
        return entity.getOwnedAttributes().stream()
                .map(object -> this.labelService.getStyledLabel(object).toString())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String getBusinessDomainCellLabel(Entity entity) {
        return Optional.ofNullable(this.entityJavaService.getEntityBusinessDomain(entity))
                .map(object -> this.labelService.getStyledLabel(object).toString())
                .orElse("");
    }

    public String getDataOwnerCellLabel(Entity entity) {
        return Optional.ofNullable(this.entityJavaService.getEntityDataOwner(entity))
                .map(object -> this.labelService.getStyledLabel(object).toString())
                .orElse("");
    }

    public String getDataSourcesCellLabel(Entity entity) {
        return this.entityJavaService.getEntityDataSources(entity)
                .stream()
                .map(object -> this.labelService.getStyledLabel(object).toString())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public Entity createSubEntity(Entity entity, String name) {
        return this.entityJavaService.createSubEntity(entity, name);
    }

    public boolean canCreateNewSubEntity(Entity entity) {
        return this.entityJavaService.canCreateNewSubEntity(entity);
    }

    public Entity createSiblingEntry(Entity entity) {
        var entitySupertype = entity.getSupertype();
        Entity newSiblingEntity = null;

        if (Objects.isNull(entitySupertype) && entity.eContainer() instanceof Namespace namespace) {
            newSiblingEntity = EntityFactory.eINSTANCE.createEntity();
            newSiblingEntity.setName("New Core Entity");
            namespace.getTypes().add(newSiblingEntity);

        } else if (entitySupertype instanceof Entity parentEntity) {
            newSiblingEntity = this.createSubEntity(parentEntity, "New Entity");
        }

        return newSiblingEntity;
    }

    public Entity deleteEntity(Entity entity) {
        return this.entityJavaService.deleteEntity(entity);
    }
}
