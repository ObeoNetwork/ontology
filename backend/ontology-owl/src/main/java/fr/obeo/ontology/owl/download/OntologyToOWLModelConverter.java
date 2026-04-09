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
package fr.obeo.ontology.owl.download;

import fr.obeo.ontology.owl.services.EntityOWLModelService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Namespace;
import org.springframework.stereotype.Service;

/**
 * A Service to convert OWL to Ontology and Ontology to OWL.
 *
 * @author fbarbin
 */
@Service
public class OntologyToOWLModelConverter {

    private final EntityOWLModelService entityOWLModelService;

    private final IIdentityService identityService;

    public OntologyToOWLModelConverter(EntityOWLModelService entityOWLModelService, IIdentityService identityService) {
        this.entityOWLModelService = Objects.requireNonNull(entityOWLModelService);
        this.identityService = Objects.requireNonNull(identityService);
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
