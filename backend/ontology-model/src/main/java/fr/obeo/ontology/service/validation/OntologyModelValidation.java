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
package fr.obeo.ontology.service.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.Reference;
import org.obeonetwork.dsl.environment.StructuredType;
import org.obeonetwork.dsl.environment.Type;
import org.obeonetwork.dsl.environment.TypesDefinition;
import org.springframework.stereotype.Service;

/**
 * Validate the ontology model.
 *
 * @author lfasani
 */
@Service
public class OntologyModelValidation {

    public enum Severity {
        ERROR,
        WARNING,
        INFORMATION
    }

    public record ValidationStatus(Severity severity, String message) {

        public ValidationStatus {
            Objects.requireNonNull(severity);
            Objects.requireNonNull(message);
        }
    }

    public List<ValidationStatus> validate(Root root) {
        List<ValidationStatus> statuses = new ArrayList<>();

        if (root == null) {
            statuses.add(new ValidationStatus(Severity.ERROR, "The ontology root is missing."));
            return statuses;
        }

        if (isBlank(root.getName())) {
            statuses.add(new ValidationStatus(Severity.WARNING, "The ontology root has no name."));
        }

        List<Entity> entities = collectEntities(root);
        if (entities.isEmpty()) {
            statuses.add(new ValidationStatus(Severity.WARNING, "The ontology does not contain any entity."));
        }

        validateEntityNames(entities, statuses);
        entities.forEach(entity -> validateEntity(entity, statuses));

        return List.copyOf(statuses);
    }

    private void validateEntityNames(List<Entity> entities, List<ValidationStatus> statuses) {
        Map<String, Integer> occurrencesByName = new HashMap<>();
        entities.stream()
                .map(Entity::getName)
                .filter(name -> !isBlank(name))
                .map(String::trim)
                .forEach(name -> occurrencesByName.merge(name, 1, Integer::sum));

        occurrencesByName.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> statuses.add(new ValidationStatus(Severity.ERROR, "Several entities are named '" + entry.getKey() + "'.")));
    }

    private void validateEntity(Entity entity, List<ValidationStatus> statuses) {
        String entityName = getDisplayName(entity.getName(), "Unnamed entity");
        if (isBlank(entity.getName())) {
            statuses.add(new ValidationStatus(Severity.ERROR, "An entity has no name" + getSuperTypesMessage(entity) + "."));
        }

        validateAttributeNames(entity.getOwnedAttributes(), statuses, entityName);
        validateReferenceNames(entity.getOwnedReferences(), statuses, entityName);

        entity.getOwnedAttributes().forEach(attribute -> validateAttribute(entityName, attribute, statuses));
        entity.getOwnedReferences().forEach(reference -> validateReference(entityName, reference, statuses));
        validateAnnotations(entityName, entity.getMetadatas(), statuses);
    }

    private void validateReferenceNames(List<Reference> references, List<ValidationStatus> statuses, String entityName) {
        Map<String, Integer> occurrencesByName = new HashMap<>();
        references.stream()
                .map(reference -> "'" + reference.getName() + "' with type '" + Optional.ofNullable(reference.getReferencedType()).map(Type::getName).orElse("") + "'")
                .filter(name -> !isBlank(name))
                .map(String::trim)
                .forEach(name -> occurrencesByName.merge(name, 1, Integer::sum));

        occurrencesByName.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> statuses.add(new ValidationStatus(Severity.ERROR, "Several references are named " + entry.getKey() + " in entity '" + entityName + "'.")));
    }

    private void validateAttributeNames(List<Attribute> attributes, List<ValidationStatus> statuses, String entityName) {
        Map<String, Integer> occurrencesByName = new HashMap<>();
        attributes.stream()
                .map(Attribute::getName)
                .filter(name -> !isBlank(name))
                .map(String::trim)
                .forEach(name -> occurrencesByName.merge(name, 1, Integer::sum));

        occurrencesByName.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> statuses.add(new ValidationStatus(Severity.ERROR, "Several attributes are named '" + entry.getKey() + "' in entity '" + entityName + "'.")));
    }

    private void validateAttribute(String entityName, Attribute attribute, List<ValidationStatus> statuses) {
        String attributeName = getDisplayName(attribute.getName(), "Unnamed attribute");
        if (isBlank(attribute.getName())) {
            statuses.add(new ValidationStatus(Severity.ERROR, "Entity '" + entityName + "' owns an attribute without a name."));
        }
        if (attribute.getType() == null) {
            statuses.add(new ValidationStatus(Severity.WARNING, "Attribute '" + attributeName + "' of entity '" + entityName + "' has no type."));
        }
        if (attribute.getMultiplicity() == null) {
            statuses.add(new ValidationStatus(Severity.INFORMATION, "Attribute '" + attributeName + "' of entity '" + entityName + "' has no cardinality."));
        }
    }

    private void validateReference(String entityName, Reference reference, List<ValidationStatus> statuses) {
        String referenceName = getDisplayName(reference.getName(), "Unnamed reference");
        if (isBlank(reference.getName())) {
            statuses.add(new ValidationStatus(Severity.ERROR, "Entity '" + entityName + "' owns a reference without a name."));
        }
        if (reference.getReferencedType() == null) {
            statuses.add(new ValidationStatus(Severity.ERROR, "Reference '" + referenceName + "' of entity '" + entityName + "' has no target entity."));
        }
        if (reference.getMultiplicity() == null) {
            statuses.add(new ValidationStatus(Severity.INFORMATION, "Reference '" + referenceName + "' of entity '" + entityName + "' has no cardinality."));
        }
    }

    private void validateAnnotations(String entityName, MetaDataContainer metadatas, List<ValidationStatus> statuses) {
        Optional.ofNullable(metadatas)
                .stream()
                .flatMap(metaDataContainer -> metaDataContainer.getMetadatas().stream())
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .filter(annotation -> isBlank(annotation.getTitle()) || isBlank(annotation.getBody()))
                .forEach(annotation -> statuses.add(new ValidationStatus(Severity.INFORMATION,
                        "An annotation of entity '" + entityName + "' should have both a title and a body.")));
    }

    private List<Entity> collectEntities(Root root) {
        List<Entity> entities = new ArrayList<>();
        collectTypes(root, entities);
        root.getOwnedNamespaces().forEach(namespace -> collectNamespace(namespace, entities));
        return entities;
    }

    private void collectNamespace(Namespace namespace, List<Entity> entities) {
        collectTypes(namespace, entities);
        namespace.getOwnedNamespaces().forEach(childNamespace -> collectNamespace(childNamespace, entities));
    }

    private void collectTypes(TypesDefinition typesDefinition, List<Entity> entities) {
        typesDefinition.getTypes().stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .forEach(entities::add);
    }

    private String getDisplayName(String value, String fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        return value.trim();
    }

    private String getSuperTypesMessage(Entity entity) {
        List<String> superTypeNames = new ArrayList<>();
        Set<StructuredType> visitedSuperTypes = new LinkedHashSet<>();
        StructuredType superType = entity.getSupertype();
        while (superType != null && visitedSuperTypes.add(superType)) {
            superTypeNames.add(getDisplayName(superType.getName(), "Unnamed super type"));
            superType = superType.getSupertype();
        }

        if (superTypeNames.isEmpty()) {
            return "";
        }
        return ". Super types: " + String.join(", ", superTypeNames);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
