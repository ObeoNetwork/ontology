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
package fr.obeo.ontology.services.project;

import java.util.List;
import java.util.UUID;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextPersistenceService;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.project.dto.CreateProjectInput;
import org.eclipse.sirius.web.application.project.services.api.ISemanticDataInitializer;
import org.springframework.stereotype.Service;

/**
 * Provides Ontology specific project template initializers.
 *
 * @author lfasani
 */
@Service
public class OntologyProjectTemplateInitializer implements ISemanticDataInitializer {
    private final IEditingContextPersistenceService editingContextPersistenceService;

    public OntologyProjectTemplateInitializer(IEditingContextPersistenceService editingContextPersistenceService) {
        this.editingContextPersistenceService = editingContextPersistenceService;
    }

    @Override
    public boolean canHandle(String projectTemplateId) {
        return OntologyProjectTemplateProvider.ONTOLOGY_EXAMPLE_TEMPLATE_ID.equals(projectTemplateId);
    }

    @Override
    public void handle(ICause cause, IEditingContext editingContext, String projectTemplateId) {
        if (OntologyProjectTemplateProvider.ONTOLOGY_EXAMPLE_TEMPLATE_ID.equals(projectTemplateId)) {
            this.initializeOntologyModel(editingContext);

            this.editingContextPersistenceService.persist(new CreateProjectInput(UUID.randomUUID(), "Ontology", OntologyProjectTemplateProvider.ONTOLOGY_EXAMPLE_TEMPLATE_ID, List.of()),
                    editingContext);
        }
    }

    private void initializeOntologyModel(IEditingContext editingContext) {
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            var documentId = UUID.randomUUID();
            var resource = new JSONResourceFactory().createResourceFromPath(documentId.toString());
            var resourceMetadataAdapter = new ResourceMetadataAdapter("Ontology");
            resource.eAdapters().add(resourceMetadataAdapter);
            emfEditingContext.getDomain().getResourceSet().getResources().add(resource);

            resource.getContents().addAll(new OntologySampleBuilder().getEmptySampleContent());
        }
    }
}
