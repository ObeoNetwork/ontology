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
package fr.obeo.ontology.owl.download;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.obeo.ontology.owl.services.EntityOWLModelService;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.junit.jupiter.api.Test;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.obeonetwork.dsl.environment.MultiplicityKind;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.PrimitiveType;
import org.obeonetwork.dsl.environment.Reference;

class OntologyToOWLModelConverterTests {

    private static final String BASE_URI = "http://ontology/entity#";

    private final TestIdentityService identityService = new TestIdentityService();

    private final OntologyToOWLModelConverter converter = new OntologyToOWLModelConverter(new EntityOWLModelService(), this.identityService);

    @Test
    void convertToOWLModelCreatesEntitiesRecursivelyWithBaseAndSupertype() {
        Root root = EntityFactory.eINSTANCE.createRoot();
        Entity parent = entity("Parent entity", "parent", "Parent description");
        Entity child = entity("Child entity", "child", "Child description");
        child.setSupertype(parent);

        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        namespace.getTypes().add(parent);
        Namespace childNamespace = EnvironmentFactory.eINSTANCE.createNamespace();
        childNamespace.getTypes().add(child);
        namespace.getOwnedNamespaces().add(childNamespace);
        root.getOwnedNamespaces().add(namespace);

        Model model = this.converter.convertToOWLModel(root);

        Resource parentClass = model.getResource(BASE_URI + "Parent+entity");
        Resource childClass = model.getResource(BASE_URI + "Child+entity");
        Resource baseClass = model.getResource(BASE_URI + "BaseEntity");

        assertTrue(model.contains(parentClass, RDF.type, OWL.Class));
        assertTrue(model.contains(childClass, RDF.type, OWL.Class));
        assertTrue(model.contains(parentClass, RDFS.subClassOf, baseClass));
        assertTrue(model.contains(childClass, RDFS.subClassOf, baseClass));
        assertTrue(model.contains(childClass, RDFS.subClassOf, parentClass));
        assertTrue(hasLiteral(model, parentClass, RDFS.label, "Parent entity"));
        assertTrue(hasLiteral(model, parentClass, RDFS.comment, "Parent description"));
    }

    @Test
    void convertToOWLModelEncodesEntityNamesInClassUris() {
        Root root = EntityFactory.eINSTANCE.createRoot();
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        Entity entity = entity("Order / Customer", "orderCustomer", null);
        namespace.getTypes().add(entity);
        root.getOwnedNamespaces().add(namespace);

        Model model = this.converter.convertToOWLModel(root);

        Resource entityClass = model.getResource(BASE_URI + "Order+%2F+Customer");

        assertTrue(model.contains(entityClass, RDF.type, OWL.Class));
        assertTrue(hasLiteral(model, entityClass, RDFS.label, "Order / Customer"));
    }

    @Test
    void convertToOWLModelCreatesReferencesAttributesAndCardinalityRestrictions() {
        Root root = EntityFactory.eINSTANCE.createRoot();
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        Entity source = entity("Source", "source", null);
        Entity target = entity("Target", "target", null);

        Reference reference = EnvironmentFactory.eINSTANCE.createReference();
        reference.setName("relatedTo");
        reference.setDescription("Target relation");
        reference.setReferencedType(target);
        reference.setMultiplicity(MultiplicityKind.ZERO_ONE_LITERAL);
        this.identityService.register(reference, "reference");
        source.getOwnedReferences().add(reference);

        Attribute attribute = EnvironmentFactory.eINSTANCE.createAttribute();
        attribute.setName("active");
        attribute.setDescription("Active flag");
        attribute.setType(primitiveType("Boolean"));
        attribute.setMultiplicity(MultiplicityKind.ONE_LITERAL);
        this.identityService.register(attribute, "attribute");
        source.getOwnedAttributes().add(attribute);

        namespace.getTypes().add(source);
        namespace.getTypes().add(target);
        root.getOwnedNamespaces().add(namespace);

        Model model = this.converter.convertToOWLModel(root);

        Resource sourceClass = model.getResource(BASE_URI + "Source");
        Resource targetClass = model.getResource(BASE_URI + "Target");
        Resource objectProperty = model.getResource(BASE_URI + "Reference_Source_relatedTo_Target_reference");
        Resource datatypeProperty = model.getResource(BASE_URI + "Attribute_Source_active");

        assertTrue(model.contains(objectProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(objectProperty, RDFS.domain, sourceClass));
        assertTrue(model.contains(objectProperty, RDFS.range, targetClass));
        assertTrue(hasLiteral(model, objectProperty, RDFS.label, "relatedTo"));
        assertTrue(hasRestriction(model, objectProperty, OWL.maxCardinality, 1));
        assertFalse(model.contains(objectProperty, RDF.type, OWL.FunctionalProperty));

        assertTrue(model.contains(datatypeProperty, RDF.type, OWL.DatatypeProperty));
        assertTrue(model.contains(datatypeProperty, RDFS.domain, sourceClass));
        assertTrue(model.contains(datatypeProperty, RDFS.range, model.getResource(XSDDatatype.XSDboolean.getURI())));
        assertTrue(hasLiteral(model, datatypeProperty, RDFS.comment, "Active flag"));
        assertTrue(hasDataCardinalityRestriction(model, datatypeProperty, OWL.cardinality, model.getResource(XSDDatatype.XSDboolean.getURI()), 1));
        assertFalse(model.contains(datatypeProperty, RDF.type, OWL.FunctionalProperty));
    }

    @Test
    void convertToOWLModelExportsZeroStarReferencesAsOptionalAssociationClasses() {
        Root root = EntityFactory.eINSTANCE.createRoot();
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        Entity parent = entity("Parent", "parent", null);
        Entity child = entity("Child", "child", null);

        Reference childrenReference = EnvironmentFactory.eINSTANCE.createReference();
        childrenReference.setName("children");
        childrenReference.setReferencedType(child);
        childrenReference.setMultiplicity(MultiplicityKind.ZERO_STAR_LITERAL);
        this.identityService.register(childrenReference, "childrenReference");
        parent.getOwnedReferences().add(childrenReference);

        Reference parentReference = EnvironmentFactory.eINSTANCE.createReference();
        parentReference.setName("parent");
        parentReference.setReferencedType(parent);
        parentReference.setMultiplicity(MultiplicityKind.ONE_LITERAL);
        parentReference.setOppositeOf(childrenReference);
        this.identityService.register(parentReference, "parentReference");
        child.getOwnedReferences().add(parentReference);

        namespace.getTypes().add(parent);
        namespace.getTypes().add(child);
        root.getOwnedNamespaces().add(namespace);

        Model model = this.converter.convertToOWLModel(root);

        Resource parentClass = model.getResource(BASE_URI + "Parent");
        Resource childClass = model.getResource(BASE_URI + "Child");
        Resource childrenProperty = model.getResource(BASE_URI + "Reference_Parent_children_Child_childrenReference");
        Resource parentProperty = model.getResource(BASE_URI + "Reference_Child_parent_Parent_parentReference");
        Resource childrenAssociationClass = model.getResource(BASE_URI + "Association_Parent_children_Child");
        Resource childrenAssociationSourceProperty = model.getResource(BASE_URI + "Association_source_Parent_children_Child");
        Resource childrenAssociationTargetProperty = model.getResource(BASE_URI + "Association_target_Parent_children_Child");

        assertFalse(model.contains(childrenProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(childrenAssociationClass, RDF.type, OWL.Class));
        assertTrue(model.contains(childrenAssociationSourceProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(childrenAssociationSourceProperty, RDFS.domain, childrenAssociationClass));
        assertTrue(model.contains(childrenAssociationSourceProperty, RDFS.range, parentClass));
        assertTrue(hasObjectCardinalityRestriction(model, childrenAssociationSourceProperty, OWL.cardinality, parentClass, 1));
        assertFalse(model.contains(childrenAssociationSourceProperty, RDF.type, OWL.FunctionalProperty));
        assertTrue(model.contains(childrenAssociationTargetProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(childrenAssociationTargetProperty, RDFS.domain, childrenAssociationClass));
        assertTrue(model.contains(childrenAssociationTargetProperty, RDFS.range, childClass));
        assertTrue(hasObjectCardinalityRestriction(model, childrenAssociationTargetProperty, OWL.cardinality, childClass, 1));
        assertFalse(model.contains(childrenAssociationTargetProperty, RDF.type, OWL.FunctionalProperty));

        assertTrue(model.contains(parentProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(parentProperty, RDFS.domain, childClass));
        assertTrue(model.contains(parentProperty, RDFS.range, parentClass));
        assertTrue(hasObjectCardinalityRestriction(model, parentProperty, OWL.cardinality, parentClass, 1));
        assertFalse(model.contains(parentProperty, RDF.type, OWL.FunctionalProperty));
    }

    @Test
    void convertToOWLModelExportsManyToManyReferencesAsAssociationClasses() {
        Root root = EntityFactory.eINSTANCE.createRoot();
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        Entity source = entity("Source", "source", null);
        Entity target = entity("Target", "target", null);

        Reference sourceToTargets = EnvironmentFactory.eINSTANCE.createReference();
        sourceToTargets.setName("targets");
        sourceToTargets.setReferencedType(target);
        sourceToTargets.setMultiplicity(MultiplicityKind.ZERO_STAR_LITERAL);
        this.identityService.register(sourceToTargets, "sourceToTargets");
        source.getOwnedReferences().add(sourceToTargets);

        Reference targetToSources = EnvironmentFactory.eINSTANCE.createReference();
        targetToSources.setName("sources");
        targetToSources.setReferencedType(source);
        targetToSources.setMultiplicity(MultiplicityKind.ONE_STAR_LITERAL);
        this.identityService.register(targetToSources, "targetToSources");
        target.getOwnedReferences().add(targetToSources);

        sourceToTargets.setOppositeOf(targetToSources);
        targetToSources.setOppositeOf(sourceToTargets);

        namespace.getTypes().add(source);
        namespace.getTypes().add(target);
        root.getOwnedNamespaces().add(namespace);

        Model model = this.converter.convertToOWLModel(root);

        Resource sourceClass = model.getResource(BASE_URI + "Source");
        Resource targetClass = model.getResource(BASE_URI + "Target");
        Resource directSourceToTargetsProperty = model.getResource(BASE_URI + "Reference_Source_targets_Target_sourceToTargets");
        Resource directTargetToSourcesProperty = model.getResource(BASE_URI + "Reference_Target_sources_Source_targetToSources");
        Resource sourceAssociationClass = model.getResource(BASE_URI + "Association_Source_targets_Target");
        Resource sourceAssociationSourceProperty = model.getResource(BASE_URI + "Association_source_Source_targets_Target");
        Resource sourceAssociationTargetProperty = model.getResource(BASE_URI + "Association_target_Source_targets_Target");
        Resource targetAssociationClass = model.getResource(BASE_URI + "Association_Target_sources_Source");
        Resource targetAssociationSourceProperty = model.getResource(BASE_URI + "Association_source_Target_sources_Source");
        Resource targetAssociationTargetProperty = model.getResource(BASE_URI + "Association_target_Target_sources_Source");

        assertFalse(model.contains(directSourceToTargetsProperty, RDF.type, OWL.ObjectProperty));
        assertFalse(model.contains(directTargetToSourcesProperty, RDF.type, OWL.ObjectProperty));

        assertTrue(model.contains(sourceAssociationClass, RDF.type, OWL.Class));
        assertTrue(hasLiteral(model, sourceAssociationClass, RDFS.label, "targets"));
        assertTrue(model.contains(sourceAssociationSourceProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(sourceAssociationSourceProperty, RDFS.domain, sourceAssociationClass));
        assertTrue(model.contains(sourceAssociationSourceProperty, RDFS.range, sourceClass));
        assertTrue(hasObjectCardinalityRestriction(model, sourceAssociationSourceProperty, OWL.cardinality, sourceClass, 1));
        assertFalse(model.contains(sourceAssociationSourceProperty, RDF.type, OWL.FunctionalProperty));
        assertTrue(model.contains(sourceAssociationTargetProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(sourceAssociationTargetProperty, RDFS.domain, sourceAssociationClass));
        assertTrue(model.contains(sourceAssociationTargetProperty, RDFS.range, targetClass));
        assertTrue(hasObjectCardinalityRestriction(model, sourceAssociationTargetProperty, OWL.cardinality, targetClass, 1));
        assertFalse(model.contains(sourceAssociationTargetProperty, RDF.type, OWL.FunctionalProperty));

        assertTrue(model.contains(targetAssociationClass, RDF.type, OWL.Class));
        assertTrue(model.contains(targetAssociationSourceProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(targetAssociationSourceProperty, RDFS.domain, targetAssociationClass));
        assertTrue(model.contains(targetAssociationSourceProperty, RDFS.range, targetClass));
        assertTrue(hasObjectCardinalityRestriction(model, targetAssociationSourceProperty, OWL.cardinality, targetClass, 1));
        assertFalse(model.contains(targetAssociationSourceProperty, RDF.type, OWL.FunctionalProperty));
        assertTrue(model.contains(targetAssociationTargetProperty, RDF.type, OWL.ObjectProperty));
        assertTrue(model.contains(targetAssociationTargetProperty, RDFS.domain, targetAssociationClass));
        assertTrue(model.contains(targetAssociationTargetProperty, RDFS.range, sourceClass));
        assertTrue(hasObjectCardinalityRestriction(model, targetAssociationTargetProperty, OWL.cardinality, sourceClass, 1));
        assertFalse(model.contains(targetAssociationTargetProperty, RDF.type, OWL.FunctionalProperty));
    }

    @Test
    void convertToOWLModelExportsAnnotationsAsCommentsAndStructuredMetadata() {
        Root root = EntityFactory.eINSTANCE.createRoot();
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        Entity entity = entity("Annotated", "annotated", null);

        MetaDataContainer metaDataContainer = EnvironmentFactory.eINSTANCE.createMetaDataContainer();
        Annotation annotation = EnvironmentFactory.eINSTANCE.createAnnotation();
        annotation.setTitle("Governance");
        annotation.setBody("Validated by data office");
        metaDataContainer.getMetadatas().add(annotation);
        entity.setMetadatas(metaDataContainer);

        namespace.getTypes().add(entity);
        root.getOwnedNamespaces().add(namespace);

        Model model = this.converter.convertToOWLModel(root);

        Resource entityClass = model.getResource(BASE_URI + "Annotated");

        assertTrue(hasLiteral(model, entityClass, RDFS.comment, "Governance\nValidated by data office"));
        assertDoesNotThrow(() -> model.write(new ByteArrayOutputStream(), "RDF/XML-ABBREV"));
    }

    private Entity entity(String name, String id, String description) {
        Entity entity = EntityFactory.eINSTANCE.createEntity();
        entity.setName(name);
        entity.setDescription(description);
        this.identityService.register(entity, id);
        return entity;
    }

    private PrimitiveType primitiveType(String name) {
        PrimitiveType primitiveType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        primitiveType.setName(name);
        return primitiveType;
    }

    private static boolean hasLiteral(Model model, Resource subject, Property property, String value) {
        return model.listObjectsOfProperty(subject, property)
                .toList()
                .stream()
                .filter(RDFNode::isLiteral)
                .anyMatch(node -> value.equals(node.asLiteral().getString()));
    }

    private static boolean hasRestriction(Model model, Resource property, Property cardinalityProperty, int cardinality) {
        return model.listStatements(null, OWL.onProperty, property)
                .toList()
                .stream()
                .map(Statement::getSubject)
                .anyMatch(restriction -> {
                    Statement cardinalityStatement = restriction.getProperty(cardinalityProperty);
                    return cardinalityStatement != null
                            && cardinalityStatement.getObject().isLiteral()
                            && XSDDatatype.XSDnonNegativeInteger.equals(cardinalityStatement.getLiteral().getDatatype())
                            && cardinalityStatement.getInt() == cardinality;
                });
    }

    private static boolean hasObjectCardinalityRestriction(Model model, Resource property, Property cardinalityProperty, Resource range, int cardinality) {
        return hasCardinalityRestriction(model, property, cardinalityProperty, OWL2.onClass, range, cardinality);
    }

    private static boolean hasDataCardinalityRestriction(Model model, Resource property, Property cardinalityProperty, Resource range, int cardinality) {
        return hasCardinalityRestriction(model, property, cardinalityProperty, OWL2.onDataRange, range, cardinality);
    }

    private static boolean hasCardinalityRestriction(Model model, Resource property, Property cardinalityProperty, Property rangeProperty, Resource range, int cardinality) {
        return model.listStatements(null, OWL.onProperty, property)
                .toList()
                .stream()
                .map(Statement::getSubject)
                .anyMatch(restriction -> {
                    Statement cardinalityStatement = restriction.getProperty(cardinalityProperty);
                    return cardinalityStatement != null
                            && cardinalityStatement.getObject().isLiteral()
                            && XSDDatatype.XSDnonNegativeInteger.equals(cardinalityStatement.getLiteral().getDatatype())
                            && cardinalityStatement.getInt() == cardinality
                            && model.contains(restriction, rangeProperty, range);
                });
    }

    private static final class TestIdentityService implements IIdentityService {

        private final Map<Object, String> ids = new HashMap<>();

        void register(Object object, String id) {
            this.ids.put(object, id);
        }

        @Override
        public String getId(Object object) {
            return this.ids.get(object);
        }

        @Override
        @SuppressWarnings("removal")
        public String getKind(Object object) {
            return object.getClass().getName();
        }
    }
}
