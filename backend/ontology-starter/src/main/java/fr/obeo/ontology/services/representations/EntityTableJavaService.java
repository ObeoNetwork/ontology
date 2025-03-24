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
package fr.obeo.ontology.services.representations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.obeo.ontology.services.representations.builders.ViewEntityTableDescriptionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.tables.ColumnFilter;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Namespace;

/**
 * Java services for Entity Table.
 *
 * @author fbarbin
 */
public class EntityTableJavaService {

    private final IObjectService objectService;

    private final ObjectMapper objectMapper;

    public EntityTableJavaService(IObjectService objectService, ObjectMapper objectMapper) {
        this.objectService = Objects.requireNonNull(objectService);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    public List<Entity> getAllOrderedEntities(Entity coreObject, List<Object> expandedIds, String globalFilter, List<ColumnFilter> columnFilters) {
        List<Entity> entities = new ArrayList<>();
        Optional.of(coreObject)
                .map(EObject::eContainer)
                .filter(Namespace.class::isInstance)
                .map(Namespace.class::cast)
                .stream()
                .flatMap(namespace -> namespace.getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> entity.getSupertype() == null)
                .forEach(entity -> this.addEntityAndSubTypes(null, entity, entities, expandedIds, globalFilter, columnFilters));
        return entities;
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
        String parentEntityId = this.objectService.getId(parent);
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
            if (columnFilter.id().equals(ViewEntityTableDescriptionBuilder.ENTITY_TABLE_ATTRIBUTES_COLUMN)) {
                isCandidate = entity.getOwnedAttributes() != null && this.isValidAttributesFilter(entity, columnFilterValue);
            } else if (columnFilter.id().equals(ViewEntityTableDescriptionBuilder.ENTITY_TABLE_ATTRIBUTES_COMMENTS)) {
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
}
