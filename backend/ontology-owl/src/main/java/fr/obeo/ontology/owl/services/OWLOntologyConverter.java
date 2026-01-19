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
package fr.obeo.ontology.owl.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.Namespace;
import org.springframework.stereotype.Service;

/**
 * A Service to convert OWL to Ontology and Ontology to OWL.
 *
 * @author fbarbin
 */
@Service
public class OWLOntologyConverter {

    private final EntityOWLModelService entityOWLModelService;

    private final IIdentityService identityService;

    public OWLOntologyConverter(EntityOWLModelService entityOWLModelService, IIdentityService identityService) {
        this.entityOWLModelService = Objects.requireNonNull(entityOWLModelService);
        this.identityService = Objects.requireNonNull(identityService);
    }

    /**
     * Convert the given OWL Jena {@link Model} into the Entity model.
     *
     * @param loadedModel
     *         the Jena {@link Model}.
     * @return The {@link Root} element of the converted entity model.
     */
    public Root convertToOntology(Model loadedModel) {
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        String currentDateTime = getCurrentDateTime();
        namespace.setName("Core entities imported on " + currentDateTime);
        Root root = EntityFactory.eINSTANCE.createRoot();
        root.getOwnedNamespaces().add(namespace);
        Model model = this.entityOWLModelService.createBaseModel();
        StmtIterator iterator = loadedModel.listStatements(null, RDF.type, this.entityOWLModelService.getEntityClass(model));
        //The map use to store already converted entity
        Map<String, Entity> uriToEntityMap = new HashMap<>();
        while (iterator.hasNext()) {
            org.apache.jena.rdf.model.Resource entityResource = iterator.next().getSubject();
            this.convertOWLEntity(loadedModel, entityResource, uriToEntityMap, namespace);
        }
        return root;
    }

    private static String getCurrentDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return localDateTime.format(formatter);
    }

    /**
     * Convert or retrieve (if already converted) the OWL Entity resource to {@link Entity}.
     *
     * @param loadedModel
     *         the Jena OWL model.
     * @param entityResource
     *         the current {@link Resource} to convert to {@link Entity}
     * @param uriToEntityMap
     *         the map containing already converted entities.
     * @param namespace
     *         the {@link Namespace} where the entity should be added.
     * @return the converted {@link Entity}.
     */
    private Entity convertOWLEntity(Model loadedModel, Resource entityResource, Map<String, Entity> uriToEntityMap, Namespace namespace) {
        Entity entity = uriToEntityMap.get(entityResource.getURI());
        if (entity == null) {
            String entityName = entityResource.getProperty(this.entityOWLModelService.getNameProperty(loadedModel)).getString();
            String description = entityResource.getProperty(this.entityOWLModelService.getDescriptionProperty(loadedModel)).getString();
            Optional<Entity> optionalSuperTypeEntity = this.getSuperTypeEntity(entityResource, loadedModel, uriToEntityMap, namespace);
            entity = EntityFactory.eINSTANCE.createEntity();
            entity.setName(entityName);
            entity.setDescription(description);
            optionalSuperTypeEntity.ifPresent(entity::setSupertype);
            namespace.getTypes().add(entity);
            uriToEntityMap.put(entityResource.getURI(), entity);
        }
        return entity;
    }

    private Optional<Entity> getSuperTypeEntity(Resource entityResource, Model model, Map<String, Entity> uriToEntityMap, Namespace namespace) {
        Optional<Entity> optionalEntity = Optional.empty();
        if (entityResource.hasProperty(RDFS.subClassOf)) {
            Resource resource = entityResource.getProperty(RDFS.subClassOf).getObject().asResource();
            optionalEntity = Optional.of(this.convertOWLEntity(model, resource, uriToEntityMap, namespace));
        }
        return optionalEntity;
    }

    /**
     * Convert the given Entity model (the {@link Root} element) into a Jena {@link Model}.
     *
     * @param rootOntology
     *         the {@link Root} element.
     * @return the new {@link Model}
     */
    public Model convertToOWLModel(Root rootOntology) {
        Map<String, Resource> objectIdToOWLResourceMap = new HashMap<>();
        Model model = this.entityOWLModelService.createBaseModel();
        rootOntology.getTypes().stream().filter(Entity.class::isInstance).forEach(type -> this.convert((Entity) type, model, objectIdToOWLResourceMap));
        rootOntology.getOwnedNamespaces().forEach(namespace -> this.convert(namespace, model, objectIdToOWLResourceMap));
        return model;
    }

    private void convert(Entity entity, Model model, Map<String, Resource> objectIdToOWLResourceMap) {
        this.getOrCreate(entity, model, objectIdToOWLResourceMap);
    }

    private Resource getOrCreate(Entity entity, Model model, Map<String, Resource> objectIdToOWLResourceMap) {
        String objectId = this.identityService.getId(entity);
        Resource resource = objectIdToOWLResourceMap.get(objectId);
        return Optional.ofNullable(resource).orElseGet(() -> this.createEntityResource(entity, model, objectIdToOWLResourceMap));
    }

    private Resource createEntityResource(Entity entity, Model model, Map<String, Resource> objectIdToOWLResourceMap) {
        Resource resource = model.createResource(this.createURI(entity))
                .addProperty(RDF.type, this.entityOWLModelService.getEntityClass(model))
                .addProperty(this.entityOWLModelService.getNameProperty(model), Optional.ofNullable(entity.getName()).orElse(""))
                .addProperty(this.entityOWLModelService.getDescriptionProperty(model), Optional.ofNullable(entity.getDescription()).orElse(""));
        if (entity.getSupertype() instanceof Entity superType) {
            resource.addProperty(RDFS.subClassOf, this.getOrCreate(superType, model, objectIdToOWLResourceMap));
        }
        return resource;
    }

    private void convert(Namespace namespace, Model model, Map<String, Resource> objectIdToOWLResourceMap) {
        namespace.getOwnedNamespaces().forEach(currentNamespace -> this.convert(currentNamespace, model, objectIdToOWLResourceMap));
        namespace.getTypes().stream().filter(Entity.class::isInstance).forEach(type -> this.getOrCreate((Entity) type, model, objectIdToOWLResourceMap));
    }

    private String createURI(EObject eObject) {
        return EntityOWLModelService.BASE_URI + this.identityService.getId(eObject);
    }
}
