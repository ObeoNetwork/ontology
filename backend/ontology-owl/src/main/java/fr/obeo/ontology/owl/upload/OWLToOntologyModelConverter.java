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
package fr.obeo.ontology.owl.upload;

import fr.obeo.ontology.owl.services.EntityOWLModelService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.DataType;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.obeonetwork.dsl.environment.MultiplicityKind;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.PrimitiveType;
import org.obeonetwork.dsl.environment.Reference;
import org.springframework.stereotype.Service;

/**
 * A Service to convert OWL to Ontology.
 *
 * @author lfasani
 */
@Service
public class OWLToOntologyModelConverter {

    private static final String BASE_ENTITY_URI = "http://ontology/entity#";

    private static final String BASE_ENTITY_NAME = "BaseEntity";

    private static final String ASSOCIATION_PREFIX = "Association_";

    private final EntityOWLModelService entityOWLModelService;

    private final IIdentityService identityService;

    public OWLToOntologyModelConverter(EntityOWLModelService entityOWLModelService, IIdentityService identityService) {
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

        Map<Resource, Entity> entityByClass = this.createEntities(loadedModel, namespace);
        this.applySupertypes(loadedModel, entityByClass);
        this.createReferences(loadedModel, entityByClass);
        this.createAttributes(loadedModel, entityByClass);
        this.createAssociationReferences(loadedModel, entityByClass);
        return root;
    }

    private String getCurrentDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return localDateTime.format(formatter);
    }

    private Map<Resource, Entity> createEntities(Model model, Namespace namespace) {
        Map<Resource, Entity> entityByClass = new HashMap<>();
        this.listOWLClasses(model).stream()
                .filter(this::isExportedEntityClass)
                .sorted((left, right) -> getName(model, left).compareTo(getName(model, right)))
                .forEach(resource -> {
                    Entity entity = EntityFactory.eINSTANCE.createEntity();
                    entity.setName(getName(model, resource));
                    addDescriptionAndAnnotations(model, resource, entity);
                    namespace.getTypes().add(entity);
                    entityByClass.put(resource, entity);
                });
        return entityByClass;
    }

    private List<Resource> listOWLClasses(Model model) {
        return model.listSubjectsWithProperty(org.apache.jena.vocabulary.RDF.type, OWL.Class)
                .toList()
                .stream()
                .filter(Resource::isURIResource)
                .toList();
    }

    private boolean isExportedEntityClass(Resource resource) {
        String localName = resource.getLocalName();
        return resource.getURI().startsWith(BASE_ENTITY_URI)
                && !BASE_ENTITY_NAME.equals(localName)
                && !localName.startsWith(ASSOCIATION_PREFIX);
    }

    private boolean isAssociationClass(Resource resource) {
        return resource.isURIResource()
                && resource.getURI().startsWith(BASE_ENTITY_URI + ASSOCIATION_PREFIX);
    }

    private static String getName(Model model, Resource resource) {
        return Optional.ofNullable(resource.getProperty(RDFS.label))
                .map(Statement::getString)
                .orElseGet(() -> URLDecoder.decode(resource.getLocalName(), StandardCharsets.UTF_8));
    }

    private static void addDescriptionAndAnnotations(Model model, Resource resource, Entity entity) {
        model.listObjectsOfProperty(resource, RDFS.comment)
                .toList()
                .stream()
                .filter(RDFNode::isLiteral)
                .map(node -> node.asLiteral().getString())
                .forEach(comment -> {
                    if (comment.contains("\n")) {
                        addAnnotation(entity, comment);
                    } else {
                        entity.setDescription(comment);
                    }
                });
    }

    private static void addAnnotation(Entity entity, String comment) {
        String[] parts = comment.split("\\R", 2);
        Annotation annotation = EnvironmentFactory.eINSTANCE.createAnnotation();
        annotation.setTitle(parts[0]);
        if (parts.length > 1) {
            annotation.setBody(parts[1]);
        }
        MetaDataContainer metadatas = entity.getMetadatas();
        if (metadatas == null) {
            metadatas = EnvironmentFactory.eINSTANCE.createMetaDataContainer();
            entity.setMetadatas(metadatas);
        }
        metadatas.getMetadatas().add(annotation);
    }

    private void applySupertypes(Model model, Map<Resource, Entity> entityByClass) {
        entityByClass.forEach((entityClass, entity) -> model.listObjectsOfProperty(entityClass, RDFS.subClassOf)
                .toList()
                .stream()
                .filter(RDFNode::isResource)
                .map(RDFNode::asResource)
                .map(entityByClass::get)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(entity::setSupertype));
    }

    private void createReferences(Model model, Map<Resource, Entity> entityByClass) {
        model.listSubjectsWithProperty(org.apache.jena.vocabulary.RDF.type, OWL.ObjectProperty)
                .toList()
                .stream()
                .filter(Resource::isURIResource)
                .filter(resource -> resource.getURI().startsWith(BASE_ENTITY_URI + "Reference_"))
                .forEach(resource -> this.createReference(model, resource, entityByClass));
    }

    private void createReference(Model model, Resource propertyResource, Map<Resource, Entity> entityByClass) {
        Optional<Entity> domain = getResourceProperty(model, propertyResource, RDFS.domain).map(entityByClass::get);
        Optional<Entity> range = getResourceProperty(model, propertyResource, RDFS.range).map(entityByClass::get);
        if (domain.isPresent() && range.isPresent()) {
            Reference reference = EnvironmentFactory.eINSTANCE.createReference();
            reference.setName(getName(model, propertyResource));
            reference.setDescription(getOptionalLiteral(model, propertyResource, RDFS.comment).orElse(null));
            reference.setReferencedType(range.get());
            reference.setMultiplicity(getMultiplicity(model, domain.get(), propertyResource).orElse(MultiplicityKind.ZERO_STAR_LITERAL));
            domain.get().getOwnedReferences().add(reference);
        }
    }

    private void createAttributes(Model model, Map<Resource, Entity> entityByClass) {
        model.listSubjectsWithProperty(org.apache.jena.vocabulary.RDF.type, OWL.DatatypeProperty)
                .toList()
                .stream()
                .filter(Resource::isURIResource)
                .filter(resource -> resource.getURI().startsWith(BASE_ENTITY_URI + "Attribute_"))
                .forEach(resource -> this.createAttribute(model, resource, entityByClass));
    }

    private void createAttribute(Model model, Resource propertyResource, Map<Resource, Entity> entityByClass) {
        Optional<Entity> domain = getResourceProperty(model, propertyResource, RDFS.domain).map(entityByClass::get);
        if (domain.isPresent()) {
            Attribute attribute = EnvironmentFactory.eINSTANCE.createAttribute();
            attribute.setName(getName(model, propertyResource));
            attribute.setDescription(getOptionalLiteral(model, propertyResource, RDFS.comment).orElse(null));
            attribute.setType(getResourceProperty(model, propertyResource, RDFS.range).map(this::toDataType).orElseGet(this::stringDataType));
            attribute.setMultiplicity(getMultiplicity(model, domain.get(), propertyResource).orElse(MultiplicityKind.ZERO_STAR_LITERAL));
            domain.get().getOwnedAttributes().add(attribute);
        }
    }

    private void createAssociationReferences(Model model, Map<Resource, Entity> entityByClass) {
        this.listOWLClasses(model).stream()
                .filter(this::isAssociationClass)
                .forEach(associationClass -> this.createAssociationReference(model, associationClass, entityByClass));
    }

    private void createAssociationReference(Model model, Resource associationClass, Map<Resource, Entity> entityByClass) {
        List<Resource> ranges = model.listSubjectsWithProperty(RDFS.domain, associationClass)
                .toList()
                .stream()
                .filter(Resource::isURIResource)
                .sorted((left, right) -> left.getURI().compareTo(right.getURI()))
                .map(property -> getResourceProperty(model, property, RDFS.range))
                .flatMap(Optional::stream)
                .toList();
        if (ranges.size() == 2) {
            Entity source = entityByClass.get(ranges.get(0));
            Entity target = entityByClass.get(ranges.get(1));
            if (source != null && target != null) {
                Reference reference = EnvironmentFactory.eINSTANCE.createReference();
                reference.setName(getName(model, associationClass));
                reference.setDescription(getOptionalLiteral(model, associationClass, RDFS.comment).orElse(null));
                reference.setReferencedType(target);
                reference.setMultiplicity(MultiplicityKind.ZERO_STAR_LITERAL);
                source.getOwnedReferences().add(reference);
            }
        }
    }

    private Optional<MultiplicityKind> getMultiplicity(Model model, Entity domain, Resource property) {
        return model.listObjectsOfProperty(toResource(model, domain), RDFS.subClassOf)
                .toList()
                .stream()
                .filter(RDFNode::isResource)
                .map(RDFNode::asResource)
                .filter(restriction -> model.contains(restriction, OWL.onProperty, property))
                .map(restriction -> getRestrictionMultiplicity(restriction))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Resource toResource(Model model, Entity entity) {
        return model.getResource(BASE_ENTITY_URI + java.net.URLEncoder.encode(entity.getName(), StandardCharsets.UTF_8));
    }

    private Optional<MultiplicityKind> getRestrictionMultiplicity(Resource restriction) {
        if (restriction.hasProperty(OWL.cardinality)) {
            return Optional.of(MultiplicityKind.ONE_LITERAL);
        } else if (restriction.hasProperty(OWL.minCardinality)) {
            return Optional.of(MultiplicityKind.ONE_STAR_LITERAL);
        } else if (restriction.hasProperty(OWL.maxCardinality)) {
            return Optional.of(MultiplicityKind.ZERO_ONE_LITERAL);
        }
        return Optional.empty();
    }

    private Optional<Resource> getResourceProperty(Model model, Resource subject, Property property) {
        return model.listObjectsOfProperty(subject, property)
                .toList()
                .stream()
                .filter(RDFNode::isResource)
                .map(RDFNode::asResource)
                .findFirst();
    }

    private Optional<String> getOptionalLiteral(Model model, Resource subject, Property property) {
        return model.listObjectsOfProperty(subject, property)
                .toList()
                .stream()
                .filter(RDFNode::isLiteral)
                .map(node -> node.asLiteral().getString())
                .findFirst();
    }

    private DataType toDataType(Resource range) {
        String uri = range.getURI();
        if (XSDDatatype.XSDboolean.getURI().equals(uri)) {
            return primitiveType("Boolean");
        } else if (XSDDatatype.XSDint.getURI().equals(uri)) {
            return primitiveType("Int");
        } else if (XSDDatatype.XSDdouble.getURI().equals(uri)) {
            return primitiveType("Double");
        }
        return stringDataType();
    }

    private DataType stringDataType() {
        return primitiveType("String");
    }

    private static PrimitiveType primitiveType(String name) {
        PrimitiveType primitiveType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        primitiveType.setName(name);
        return primitiveType;
    }
}
