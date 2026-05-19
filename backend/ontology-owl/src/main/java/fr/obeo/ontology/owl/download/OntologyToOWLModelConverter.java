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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.MultiplicityKind;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.Reference;
import org.obeonetwork.dsl.environment.StructuredType;
import org.obeonetwork.dsl.environment.Type;
import org.obeonetwork.dsl.environment.TypesDefinition;
import org.springframework.stereotype.Service;

/**
 * A Service to convert OWL to Ontology and Ontology to OWL.
 * The resulting file is aimed to be imported in Denodo. See constraints https://community.denodo.com/docs/html/document/denodoconnects/latest/en/Denodo%20Model%20Bridge%20-%20User%20Manual
 *
 * @author lfasani
 */
@Service
public class OntologyToOWLModelConverter {

    final String BASE_ENTITY_URI = "http://ontology/entity#";

    private final IIdentityService identityService;

    public OntologyToOWLModelConverter(EntityOWLModelService entityOWLModelService, IIdentityService identityService) {
        Objects.requireNonNull(entityOWLModelService);
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
        Map<Entity, OntClass> objectIdToOWLResourceMap = new HashMap<>();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        model.setNsPrefix("entity", BASE_ENTITY_URI);

        OntClass baseClass = createBaseEntity(model);

        List<Entity> entities = collectEntities(rootOntology);

        entities.forEach(entity -> {
            this.createEntity(entity, model, baseClass, objectIdToOWLResourceMap);
        });
        entities.forEach(entity -> {
            this.createEntityRelations(entity, model, objectIdToOWLResourceMap);
        });

        return model;
    }

    private void createEntityRelations(Entity entity, OntModel model, Map<Entity, OntClass> objectIdToOWLResourceMap) {
        //Create Supertype relation
        OntClass entityClass = objectIdToOWLResourceMap.get(entity);
        Optional.ofNullable(entity.getSupertype())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .map(objectIdToOWLResourceMap::get)
                .ifPresent(entityClass::addSuperClass);

        // Create the relation between Entities
        entity.getOwnedReferences()
                .forEach(reference -> {
                    String referenceId = identityService.getId(reference);
                    if (isManyToManyReference(reference)) {
                        createAssociationClass(entity, reference, referenceId, model, entityClass, objectIdToOWLResourceMap);
                    } else {
                        String relationFullName = encodeString(entity.getName() + "_" + reference.getName() + "_" + reference.getReferencedType().getName() + "_" + referenceId);
                        ObjectProperty objectProperty = model.createObjectProperty(BASE_ENTITY_URI + "Reference_" + relationFullName);
                        Optional.ofNullable(reference.getName()).ifPresent(name -> objectProperty.addLabel(name, "en"));
                        Optional.ofNullable(reference.getDescription()).ifPresent(description -> objectProperty.addComment(description, "en"));
                        objectProperty.addDomain(entityClass);
                        getReferencedEntityClass(reference.getReferencedType(), objectIdToOWLResourceMap).ifPresent(objectProperty::addRange);
                        applyObjectCardinality(model, entityClass, objectProperty, reference.getMultiplicity());
                    }
                });

        // Create the attributes
        entity.getOwnedAttributes()
                .forEach(attribute -> {
                    String attributeFullName = encodeString(entity.getName() + "_" + attribute.getName());
                    DatatypeProperty datatypeProperty = model.createDatatypeProperty(BASE_ENTITY_URI + "Attribute_" + attributeFullName);
                    Optional.ofNullable(attribute.getName()).filter(s -> !s.isBlank()).ifPresent(name -> datatypeProperty.addLabel(name, "en"));
                    Optional.ofNullable(attribute.getDescription()).ifPresent(description -> datatypeProperty.addComment(description, "en"));

                    datatypeProperty.addDomain(entityClass);
                    datatypeProperty.addRange(model.getResource(getDatatypeURI(attribute.getType())));
                    applyObjectCardinality(model, entityClass, datatypeProperty, attribute.getMultiplicity());
                });
    }

    private OntClass createEntity(Entity entity, OntModel model, OntClass baseClass, Map<Entity, OntClass> objectIdToOWLResourceMap) {
        String encodedEntityName = encodeString(entity.getName());
        OntClass entityClass = model.createClass(BASE_ENTITY_URI + encodedEntityName);
        List<Annotation> annotations = Optional.ofNullable(entity.getMetadatas())
                .stream()
                .flatMap(metaDataContainer -> metaDataContainer.getMetadatas().stream())
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .toList();

        for (int i = 0; i < annotations.size(); i++) {
            Annotation annotation = annotations.get(i);
            addAnnotationComment(entityClass, annotation);
        }

        Optional.ofNullable(entity.getName()).ifPresent(entityName -> entityClass.addLabel(entityName, "en"));
        Optional.ofNullable(entity.getDescription()).ifPresent(entityDescription -> entityClass.addComment(entityDescription, "en"));

        entityClass.addSuperClass(baseClass);

        objectIdToOWLResourceMap.put(entity, entityClass);
        return entityClass;
    }

    private String encodeString(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    private OntClass createBaseEntity(OntModel model) {
        OntClass baseClass = model.createClass(BASE_ENTITY_URI + "BaseEntity");

        DatatypeProperty description = model.createDatatypeProperty(BASE_ENTITY_URI + "description");
        description.addDomain(baseClass);
        description.addRange(XSD.xstring);

        return baseClass;
    }

    private static void applyObjectCardinality(OntModel model, OntClass cls, Property prop, MultiplicityKind cardinality) {
        if (cardinality == null) {
            return;
        }
        switch (cardinality) {
            case ONE_LITERAL -> {
                addCardinalityRestriction(model, cls, prop, OWL.cardinality, 1);
            }
            case ONE_STAR_LITERAL -> {
                addCardinalityRestriction(model, cls, prop, OWL.minCardinality, 1);
            }
            case ZERO_ONE_LITERAL -> {
                addCardinalityRestriction(model, cls, prop, OWL.maxCardinality, 1);
            }
            case ZERO_STAR_LITERAL -> {
                // no restriction needed
            }
        }
    }

    private static boolean isManyToManyReference(Reference reference) {
        return MultiplicityKind.ONE_STAR_LITERAL.equals(reference.getMultiplicity()) || MultiplicityKind.ZERO_STAR_LITERAL.equals(reference.getMultiplicity());
    }

    private void createAssociationClass(Entity sourceEntity, Reference reference, String referenceId, OntModel model, OntClass sourceClass,
            Map<Entity, OntClass> entityClassByEntity) {
        getReferencedEntityClass(reference.getReferencedType(), entityClassByEntity)
                .ifPresentOrElse(targetClass -> {
                    String relationFullName = encodeString(sourceEntity.getName() + "_" + reference.getName() + "_" + reference.getReferencedType().getName());
                    OntClass associationClass = model.createClass(BASE_ENTITY_URI + "Association_" + relationFullName);
                    Optional.ofNullable(reference.getName()).ifPresent(name -> associationClass.addLabel(name, "en"));
                    Optional.ofNullable(reference.getDescription()).ifPresent(description -> associationClass.addComment(description, "en"));

                    ObjectProperty sourceProperty = model.createObjectProperty(BASE_ENTITY_URI + "Association_source_" + relationFullName);
                    sourceProperty.addDomain(associationClass);
                    sourceProperty.addRange(sourceClass);
                    addCardinalityRestriction(model, associationClass, sourceProperty, OWL.cardinality, 1);

                    ObjectProperty targetProperty = model.createObjectProperty(BASE_ENTITY_URI + "Association_target_" + relationFullName);
                    targetProperty.addDomain(associationClass);
                    targetProperty.addRange(targetClass);
                    addCardinalityRestriction(model, associationClass, targetProperty, OWL.cardinality, 1);
                }, () -> sourceClass.addComment("Reference '" + getDisplayName(reference.getName(), referenceId)
                        + "' is not exported because its target entity cannot be resolved.", "en"));
    }

    private static void addCardinalityRestriction(OntModel model, OntClass cls, Property prop, Property cardinalityProperty, int cardinality) {
        Restriction restriction = model.createRestriction(prop);
        restriction.addLiteral(cardinalityProperty, model.createTypedLiteral(Integer.toString(cardinality), XSDDatatype.XSDnonNegativeInteger));
        addCardinalityRange(model, prop, cardinalityProperty, restriction);
        cls.addSuperClass(restriction);
    }

    private static void addCardinalityRange(OntModel model, Property prop, Property cardinalityProperty, Restriction restriction) {
        Statement rangeStatement = prop.getProperty(RDFS.range);
        if (rangeStatement == null || !rangeStatement.getObject().isResource()) {
            return;
        }
        if (model.contains(prop, RDF.type, OWL.ObjectProperty)) {
            restriction.addProperty(OWL2.onClass, rangeStatement.getResource());
        } else if (model.contains(prop, RDF.type, OWL.DatatypeProperty)) {
            restriction.addProperty(OWL2.onDataRange, rangeStatement.getResource());
        }
    }

    private static String getDisplayName(String value, String fallback) {
        return Optional.ofNullable(value)
                .filter(name -> !name.isBlank())
                .orElse(fallback);
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

    private Optional<OntClass> getReferencedEntityClass(StructuredType referencedType, Map<Entity, OntClass> entityClassByEntity) {
        return Optional.ofNullable(referencedType)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .map(entityClassByEntity::get);
    }

    private String getDatatypeURI(Type type) {
        if (type != null && type.getName() != null) {
            return switch (type.getName()) {
                case "Boolean" -> XSDDatatype.XSDboolean.getURI();
                case "Int" -> XSDDatatype.XSDint.getURI();
                case "Double" -> XSDDatatype.XSDdouble.getURI();
                case "Date" -> XSDDatatype.XSDdate.getURI();
                default -> XSDDatatype.XSDstring.getURI();
            };
        }
        return XSDDatatype.XSDstring.getURI();
    }

    private void addAnnotationComment(OntClass entityClass, Annotation annotation) {
        String comment = toComment(annotation);
        if (!comment.isBlank()) {
            entityClass.addComment(comment, "en");
        }
    }

    private String toComment(Annotation annotation) {
        String comment = Optional.ofNullable(annotation.getTitle()).orElse("").trim();
        String body = Optional.ofNullable(annotation.getBody()).orElse("").trim();
        if (!body.isBlank()) {
            comment = comment + "\n" + body;
        }
        return comment;
    }
}
