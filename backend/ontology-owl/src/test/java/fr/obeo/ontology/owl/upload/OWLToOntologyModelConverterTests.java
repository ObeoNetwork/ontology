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
package fr.obeo.ontology.owl.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.obeo.ontology.owl.download.OntologyToOWLModelConverter;
import fr.obeo.ontology.owl.services.EntityOWLModelService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import org.obeonetwork.dsl.environment.PrimitiveTypeKind;
import org.obeonetwork.dsl.environment.Reference;

class OWLToOntologyModelConverterTests {

    private final TestIdentityService identityService = new TestIdentityService();

    private final EntityOWLModelService entityOWLModelService = new EntityOWLModelService();

    private final OntologyToOWLModelConverter owlExporter = new OntologyToOWLModelConverter(this.entityOWLModelService, this.identityService);

    private final OWLToOntologyModelConverter owlImporter = new OWLToOntologyModelConverter(this.entityOWLModelService, this.identityService);

    @Test
    void convertExportedOWLModelToOntologyRestoresTheExpectedRoot() {
        Root expectedRoot = EntityFactory.eINSTANCE.createRoot();
        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        expectedRoot.getOwnedNamespaces().add(namespace);
        Entity alpha = entity("Alpha", "alpha", "Alpha description");
        Entity beta = entity("Beta", "beta", "Beta description");
        beta.setSupertype(alpha);

        Attribute active = EnvironmentFactory.eINSTANCE.createAttribute();
        active.setName("active");
        active.setDescription("Active flag");
        active.setType(primitiveType("Boolean"));
        active.setMultiplicity(MultiplicityKind.ONE_LITERAL);
        this.identityService.register(active, "active");
        alpha.getOwnedAttributes().add(active);

        Attribute createdOn = EnvironmentFactory.eINSTANCE.createAttribute();
        createdOn.setName("createdOn");
        createdOn.setDescription("Creation date");
        createdOn.setType(primitiveType("Date"));
        createdOn.setMultiplicity(MultiplicityKind.ZERO_ONE_LITERAL);
        this.identityService.register(createdOn, "createdOn");
        alpha.getOwnedAttributes().add(createdOn);

        Reference betaReference = EnvironmentFactory.eINSTANCE.createReference();
        betaReference.setName("mainBeta");
        betaReference.setDescription("Main beta relation");
        betaReference.setReferencedType(beta);
        betaReference.setMultiplicity(MultiplicityKind.ZERO_ONE_LITERAL);
        this.identityService.register(betaReference, "mainBeta");
        alpha.getOwnedReferences().add(betaReference);

        Reference alphaReference = EnvironmentFactory.eINSTANCE.createReference();
        alphaReference.setName("alphas");
        alphaReference.setDescription("Alpha collection");
        alphaReference.setReferencedType(alpha);
        alphaReference.setMultiplicity(MultiplicityKind.ZERO_STAR_LITERAL);
        this.identityService.register(alphaReference, "alphas");
        beta.getOwnedReferences().add(alphaReference);

        MetaDataContainer metaDataContainer = EnvironmentFactory.eINSTANCE.createMetaDataContainer();
        Annotation annotation = EnvironmentFactory.eINSTANCE.createAnnotation();
        annotation.setTitle("Governance");
        annotation.setBody("Validated by data office");
        metaDataContainer.getMetadatas().add(annotation);
        alpha.setMetadatas(metaDataContainer);

        namespace.getTypes().add(alpha);
        namespace.getTypes().add(beta);

        Model exportedModel = this.owlExporter.convertToOWLModel(expectedRoot);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportedModel.write(outputStream, "RDF/XML-ABBREV");

        Model loadedModel = ModelFactory.createDefaultModel();
        loadedModel.read(new ByteArrayInputStream(outputStream.toByteArray()), null, "RDF/XML");
        Root currentRoot = this.owlImporter.convertToOntology(loadedModel);

        assertPrimitiveTypes(currentRoot.getOwnedNamespaces().get(0));
        assertTrue(rootsAreEqual(expectedRoot, currentRoot));
    }

    @Test
    void convertOntologyForTestOWLToOntologyCreatesTheExpectedModel() {
        Model loadedModel = ModelFactory.createDefaultModel();
        try (InputStream inputStream = getClass().getResourceAsStream("/OntologyForTest.owl")) {
            assertNotNull(inputStream);
            loadedModel.read(inputStream, null, "RDF/XML");
        } catch (Exception exception) {
            throw new AssertionError("Unable to read OntologyForTest.owl", exception);
        }

        Root root = this.owlImporter.convertToOntology(loadedModel);
        Namespace namespace = root.getOwnedNamespaces().get(0);

        assertEquals(12, namespace.getTypes().size());
        assertPrimitiveTypes(namespace);

        Entity coreEntity = findEntity(namespace, "Core Entity");
        Entity entity1 = findEntity(namespace, "Entity 1");
        Entity entity2 = findEntity(namespace, "Entity 2");
        Entity entity11 = findEntity(namespace, "Entity 11");
        Entity entity12 = findEntity(namespace, "Entity 12");
        Entity entity13 = findEntity(namespace, "Entity 13");
        Entity entity21 = findEntity(namespace, "Entity 21");

        assertEquals("description of entity 1", entity1.getDescription());
        assertEquals("Core Entity", entity1.getSupertype().getName());
        assertEquals("Core Entity", entity2.getSupertype().getName());
        assertEquals("Entity 1", entity11.getSupertype().getName());
        assertEquals("Entity 1", entity12.getSupertype().getName());
        assertEquals("Entity 1", entity13.getSupertype().getName());
        assertEquals("Entity 2", entity21.getSupertype().getName());
        assertNotNull(coreEntity);

        assertEquals(3, entity11.getOwnedAttributes().size());
        assertAttribute(entity11, "attr1_0_1", "Int", MultiplicityKind.ZERO_ONE_LITERAL);
        assertAttribute(entity11, "attr2_ONLY_1", "Boolean", MultiplicityKind.ONE_LITERAL);
        assertAttribute(entity11, "attr3_0_N", "Double", MultiplicityKind.ZERO_STAR_LITERAL);

        assertEquals(3, entity11.getOwnedReferences().size());
        assertReference(entity11, "ref1_0_1", "Entity 13", MultiplicityKind.ZERO_ONE_LITERAL);
        assertReference(entity11, "ref2_ONLY_1", "Entity 12", MultiplicityKind.ONE_LITERAL);
        assertReference(entity11, "ref3_0_N", "Entity 12", MultiplicityKind.ZERO_STAR_LITERAL);

        assertEquals(3, entity11.getMetadatas().getMetadatas().size());
        assertAnnotation(entity11, "title1", "body line1\nbody line2");
        assertAnnotation(entity11, "title2", "body2");
        assertAnnotation(entity11, "", "body3");
    }

    private Entity entity(String name, String id, String description) {
        Entity entity = EntityFactory.eINSTANCE.createEntity();
        entity.setName(name);
        entity.setDescription(description);
        this.identityService.register(entity, id);
        return entity;
    }

    private static PrimitiveType primitiveType(String name) {
        PrimitiveType primitiveType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        primitiveType.setName(name);
        return primitiveType;
    }

    private static boolean rootsAreEqual(Root expectedRoot, Root currentRoot) {
        Namespace expectedNamespace = expectedRoot.getOwnedNamespaces().get(0);
        Namespace currentNamespace = currentRoot.getOwnedNamespaces().get(0);
        long expectedEntityCount = expectedNamespace.getTypes().stream().filter(Entity.class::isInstance).count();
        long currentEntityCount = currentNamespace.getTypes().stream().filter(Entity.class::isInstance).count();
        if (expectedEntityCount != currentEntityCount) {
            return false;
        }
        for (Entity expectedEntity : expectedNamespace.getTypes().stream().filter(Entity.class::isInstance).map(Entity.class::cast).toList()) {
            Entity currentEntity = findEntity(currentNamespace, expectedEntity.getName());
            if (!entitiesAreEqual(expectedEntity, currentEntity)) {
                return false;
            }
        }
        return true;
    }

    private static boolean entitiesAreEqual(Entity expectedEntity, Entity currentEntity) {
        return Objects.equals(expectedEntity.getName(), currentEntity.getName())
                && Objects.equals(expectedEntity.getDescription(), currentEntity.getDescription())
                && Objects.equals(getEntityName(expectedEntity.getSupertype()), getEntityName(currentEntity.getSupertype()))
                && annotationsAreEqual(expectedEntity, currentEntity)
                && attributesAreEqual(expectedEntity, currentEntity)
                && referencesAreEqual(expectedEntity, currentEntity);
    }

    private static boolean annotationsAreEqual(Entity expectedEntity, Entity currentEntity) {
        if (expectedEntity.getMetadatas() == null || currentEntity.getMetadatas() == null) {
            return expectedEntity.getMetadatas() == currentEntity.getMetadatas();
        }
        if (expectedEntity.getMetadatas().getMetadatas().size() != currentEntity.getMetadatas().getMetadatas().size()) {
            return false;
        }
        for (int i = 0; i < expectedEntity.getMetadatas().getMetadatas().size(); i++) {
            Annotation expectedAnnotation = (Annotation) expectedEntity.getMetadatas().getMetadatas().get(i);
            Annotation currentAnnotation = (Annotation) currentEntity.getMetadatas().getMetadatas().get(i);
            if (!Objects.equals(expectedAnnotation.getTitle(), currentAnnotation.getTitle())
                    || !Objects.equals(expectedAnnotation.getBody(), currentAnnotation.getBody())) {
                return false;
            }
        }
        return true;
    }

    private static boolean attributesAreEqual(Entity expectedEntity, Entity currentEntity) {
        if (expectedEntity.getOwnedAttributes().size() != currentEntity.getOwnedAttributes().size()) {
            return false;
        }
        for (Attribute expectedAttribute : expectedEntity.getOwnedAttributes()) {
            Optional<Attribute> optionalCurrentAttribute = currentEntity.getOwnedAttributes().stream()
                    .filter(attribute -> Objects.equals(expectedAttribute.getName(), attribute.getName()))
                    .findFirst();
            if (optionalCurrentAttribute.isEmpty()) {
                return false;
            }
            Attribute currentAttribute = optionalCurrentAttribute.get();
            if (!Objects.equals(expectedAttribute.getName(), currentAttribute.getName())
                    || !Objects.equals(expectedAttribute.getDescription(), currentAttribute.getDescription())
                    || !Objects.equals(expectedAttribute.getMultiplicity(), currentAttribute.getMultiplicity())
                    || !Objects.equals(getEntityName(expectedAttribute.getType()), getEntityName(currentAttribute.getType()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean referencesAreEqual(Entity expectedEntity, Entity currentEntity) {
        if (expectedEntity.getOwnedReferences().size() != currentEntity.getOwnedReferences().size()) {
            return false;
        }
        for (Reference expectedReference : expectedEntity.getOwnedReferences()) {
            Optional<Reference> optionalCurrentReference = currentEntity.getOwnedReferences().stream()
                    .filter(reference -> Objects.equals(expectedReference.getName(), reference.getName()))
                    .findFirst();
            if (optionalCurrentReference.isEmpty()) {
                return false;
            }
            Reference currentReference = optionalCurrentReference.get();
            if (!Objects.equals(expectedReference.getName(), currentReference.getName())
                    || !Objects.equals(expectedReference.getDescription(), currentReference.getDescription())
                    || !Objects.equals(expectedReference.getMultiplicity(), currentReference.getMultiplicity())
                    || !Objects.equals(getEntityName(expectedReference.getReferencedType()), getEntityName(currentReference.getReferencedType()))) {
                return false;
            }
        }
        return true;
    }

    private static String getEntityName(Object object) {
        if (object instanceof Entity entity) {
            return entity.getName();
        } else if (object instanceof PrimitiveType primitiveType) {
            return primitiveType.getName();
        }
        return null;
    }

    private static Entity findEntity(Namespace namespace, String name) {
        return namespace.getTypes().stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> Objects.equals(name, entity.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing entity " + name));
    }

    private static void assertPrimitiveTypes(Namespace namespace) {
        assertPrimitiveType(namespace, "String", PrimitiveTypeKind.TEXT);
        assertPrimitiveType(namespace, "Boolean", PrimitiveTypeKind.OTHER);
        assertPrimitiveType(namespace, "Int", PrimitiveTypeKind.NUMBER);
        assertPrimitiveType(namespace, "Double", PrimitiveTypeKind.NUMBER);
        assertPrimitiveType(namespace, "Date", PrimitiveTypeKind.TEXT);
    }

    private static void assertPrimitiveType(Namespace namespace, String name, PrimitiveTypeKind kind) {
        PrimitiveType primitiveType = namespace.getTypes().stream()
                .filter(PrimitiveType.class::isInstance)
                .map(PrimitiveType.class::cast)
                .filter(candidate -> Objects.equals(name, candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing primitive type " + name));
        assertEquals(kind, primitiveType.getKind());
    }

    private static void assertAttribute(Entity entity, String name, String typeName, MultiplicityKind multiplicity) {
        Attribute attribute = entity.getOwnedAttributes().stream()
                .filter(candidate -> Objects.equals(name, candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing attribute " + name));
        assertEquals(typeName, getEntityName(attribute.getType()));
        assertEquals(multiplicity, attribute.getMultiplicity());
    }

    private static void assertReference(Entity entity, String name, String targetName, MultiplicityKind multiplicity) {
        Reference reference = entity.getOwnedReferences().stream()
                .filter(candidate -> Objects.equals(name, candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reference " + name));
        assertEquals(targetName, getEntityName(reference.getReferencedType()));
        assertEquals(multiplicity, reference.getMultiplicity());
    }

    private static void assertAnnotation(Entity entity, String title, String body) {
        Optional<Annotation> annotation = entity.getMetadatas().getMetadatas().stream()
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .filter(candidate -> Objects.equals(title, candidate.getTitle()))
                .findFirst();
        assertTrue(annotation.isPresent());
        assertEquals(body, annotation.get().getBody());
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
