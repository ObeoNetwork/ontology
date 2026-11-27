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
package fr.obeo.ontology.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import fr.obeo.ontology.service.validation.OntologyModelValidation;
import fr.obeo.ontology.services.validation.OntologyValidationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Tests the adaptation of real ontology business diagnostics without a database.
 *
 * @author lfasani
 */
@SpringJUnitConfig(classes = { OntologyModelValidation.class, OntologyValidationService.class })
public class OntologyValidationServiceTests {

    @Autowired
    private OntologyValidationService validationService;

    @Test
    @DisplayName("Given an invalid ontology, when it is validated, then messages and all severities are preserved")
    public void givenInvalidOntologyWhenValidatedThenMessagesAndAllSeveritiesArePreserved() {
        var root = EntityFactory.eINSTANCE.createRoot();
        root.setName("Ontology");
        var namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        root.getOwnedNamespaces().add(namespace);
        var entity = EntityFactory.eINSTANCE.createEntity();
        entity.setName("Person");
        namespace.getTypes().add(entity);
        var attribute = EnvironmentFactory.eINSTANCE.createAttribute();
        attribute.setName("age");
        entity.getOwnedAttributes().add(attribute);
        var reference = EnvironmentFactory.eINSTANCE.createReference();
        reference.setName("parent");
        entity.getOwnedReferences().add(reference);
        var metadatas = EnvironmentFactory.eINSTANCE.createMetaDataContainer();
        metadatas.getMetadatas().add(EnvironmentFactory.eINSTANCE.createAnnotation());
        entity.setMetadatas(metadatas);

        var editingContext = this.createEditingContext();
        try {
            var resource = new ResourceImpl(URI.createURI("sirius:///ontology"));
            resource.getContents().add(root);
            editingContext.getDomain().getResourceSet().getResources().add(resource);

            assertThat(this.validationService.validate(editingContext))
                    .allSatisfy(diagnostic -> assertThat(diagnostic).isInstanceOf(Diagnostic.class))
                    .map(Diagnostic.class::cast)
                    .extracting(Diagnostic::getSeverity, Diagnostic::getMessage)
                    .containsExactly(
                            tuple(Diagnostic.WARNING, "Attribute 'age' of entity 'Person' has no type."),
                            tuple(Diagnostic.ERROR, "Reference 'parent' of entity 'Person' has no target entity."),
                            tuple(Diagnostic.INFO, "An annotation of entity 'Person' should have both a title and a body."));
        } finally {
            editingContext.dispose();
        }
    }

    @Test
    @DisplayName("Given several roots and resources, when validated, then each ontology is validated once")
    public void givenSeveralRootsAndResourcesWhenValidatedThenEachOntologyIsValidatedOnce() {
        var editingContext = this.createEditingContext();
        try {
            var firstRoot = EntityFactory.eINSTANCE.createRoot();
            firstRoot.setName("First");
            var entity = EntityFactory.eINSTANCE.createEntity();
            firstRoot.getTypes().add(entity);
            var secondRoot = EntityFactory.eINSTANCE.createRoot();
            secondRoot.setName("Second");
            var firstResource = new ResourceImpl(URI.createURI("sirius:///first"));
            firstResource.getContents().addAll(List.of(firstRoot, secondRoot));
            var thirdRoot = EntityFactory.eINSTANCE.createRoot();
            thirdRoot.setName("Third");
            var thirdEntity = EntityFactory.eINSTANCE.createEntity();
            thirdEntity.setName("Valid");
            thirdRoot.getTypes().add(thirdEntity);
            var secondResource = new ResourceImpl(URI.createURI("sirius:///second"));
            secondResource.getContents().add(thirdRoot);
            editingContext.getDomain().getResourceSet().getResources().addAll(List.of(firstResource, secondResource));

            assertThat(this.validationService.validate(editingContext))
                    .map(Diagnostic.class::cast)
                    .extracting(Diagnostic::getSeverity, Diagnostic::getMessage)
                    .containsExactly(
                            tuple(Diagnostic.ERROR, "An entity has no name."),
                            tuple(Diagnostic.WARNING, "The ontology does not contain any entity."));
        } finally {
            editingContext.dispose();
        }
    }

    @Test
    @DisplayName("Given a non-EMF context, when validated, then no business diagnostic is returned")
    public void givenNonEMFContextWhenValidatedThenNoBusinessDiagnosticIsReturned() {
        IEditingContext editingContext = () -> UUID.randomUUID().toString();
        assertThat(this.validationService.validate(editingContext)).isEmpty();
    }

    @Test
    @DisplayName("Given an empty EMF context, when validated, then no business diagnostic is returned")
    public void givenEmptyEMFContextWhenValidatedThenNoBusinessDiagnosticIsReturned() {
        var editingContext = this.createEditingContext();
        try {
            assertThat(this.validationService.validate(editingContext)).isEmpty();
        } finally {
            editingContext.dispose();
        }
    }

    @Test
    @DisplayName("Given a non-ontology resource, when validated, then no business diagnostic is returned")
    public void givenNonOntologyResourceWhenValidatedThenNoBusinessDiagnosticIsReturned() {
        var editingContext = this.createEditingContext();
        try {
            var resource = new ResourceImpl(URI.createURI("sirius:///other"));
            resource.getContents().add(EnvironmentFactory.eINSTANCE.createNamespace());
            editingContext.getDomain().getResourceSet().getResources().add(resource);

            assertThat(this.validationService.validate(editingContext)).isEmpty();
        } finally {
            editingContext.dispose();
        }
    }

    @Test
    @DisplayName("Given an invalid object, when a field is validated, then no business diagnostic is returned")
    public void givenInvalidObjectWhenFieldValidatedThenNoBusinessDiagnosticIsReturned() {
        var entity = EntityFactory.eINSTANCE.createEntity();
        assertThat(this.validationService.validate(entity, EnvironmentPackage.Literals.TYPE__NAME)).isEmpty();
    }

    private EditingContext createEditingContext() {
        var domain = new AdapterFactoryEditingDomain(new ComposedAdapterFactory(), new BasicCommandStack());
        return new EditingContext(UUID.randomUUID().toString(), domain, Map.of(), List.of());
    }
}
