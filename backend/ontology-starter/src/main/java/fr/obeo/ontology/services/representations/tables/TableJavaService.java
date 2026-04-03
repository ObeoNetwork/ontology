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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.obeo.ontology.ontologymm.BusinessDomain;
import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.DataSource;
import fr.obeo.ontology.ontologymm.OntologyPackage;
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
            isValidCandidate = isValidCandidate || entity.getOwnedAttributes() != null && this.isValidAttributesFilter(entity, globalFilter);
            isValidCandidate = isValidCandidate || entity.getMetadatas() != null && this.isValidMetadataFilter(entity, globalFilter);
        }

        isValidCandidate = isValidCandidate && columnFilters.stream().allMatch(columnFilter -> {
            boolean isCandidate = true;
            String columnFilterValue = this.getColumnFilterValue(columnFilter);
            if (columnFilter.id().equals(ViewEntityTableDescriptionProvider.ENTITY_TABLE_ATTRIBUTES_COLUMN)) {
                isCandidate = entity.getOwnedAttributes() != null && this.isValidAttributesFilter(entity, columnFilterValue);
            } else if (columnFilter.id().equals(ViewEntityTableDescriptionProvider.ENTITY_TABLE_ATTRIBUTES_COMMENTS)) {
                isCandidate = entity.getMetadatas() != null && this.isValidMetadataFilter(entity, columnFilterValue);
            }
            return isCandidate;
        });

        return isValidCandidate;
    }

    private boolean isValidAttributesFilter(Entity entity, String columnFilterValue) {
        return entity.getOwnedAttributes().stream().anyMatch(attribute -> this.contains(attribute.getName(), columnFilterValue));
    }

    private String getColumnFilterValue(ColumnFilter columnFilter) {
        try {
            return this.objectMapper.readValue(columnFilter.value(), new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            //We do nothing
        }
        return "";
    }

    private boolean isValidMetadataFilter(Entity entity, String filter) {
        return entity.getMetadatas().getMetadatas().stream()
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .anyMatch(annotation -> this.contains(annotation.getTitle(), filter) || this.contains(annotation.getBody(), filter));
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
                .map(object -> labelService.getStyledLabel(object).toString())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String getAttributesCellLabel(Entity entity) {
        return entity.getOwnedAttributes().stream()
                .map(object -> labelService.getStyledLabel(object).toString())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String getBusinessDomainCellLabel(Entity entity) {
        return entityJavaService.objectReferencingEntity(entity, OntologyPackage.eINSTANCE.getBusinessDomain_Entities(), BusinessDomain.class)
                .map(object -> labelService.getStyledLabel(object).toString())
                .orElse("");
    }

    public String getDataOwnerCellLabel(Entity entity) {
        return entityJavaService.objectReferencingEntity(entity, OntologyPackage.eINSTANCE.getDataOwner_Entities(), DataOwner.class)
                .map(object -> labelService.getStyledLabel(object).toString())
                .orElse("");
    }

    public String getDataSourceCellLabel(Entity entity) {
        return entityJavaService.objectsReferencingEntity(entity, OntologyPackage.eINSTANCE.getDataSource_Entities(), DataSource.class)
                .map(object -> labelService.getStyledLabel(object).toString())
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
            newSiblingEntity = this.createSubEntity(parentEntity, "New Sub Entity");
        }

        return newSiblingEntity;
    }

    public Entity deleteEntity(Entity entity) {
        return this.entityJavaService.deleteEntity(entity);
    }
}
