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

import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateInitializer;
import org.springframework.stereotype.Service;

/**
 * Provides Ontology specific project template initializers.
 *
 * @author lfasani
 */
@Service
public class OntologyProjectTemplateInitializer implements IProjectTemplateInitializer {
    @Override
    public boolean canHandle(String projectTemplateId) {
        return OntologyProjectTemplateProvider.ONTOLOGY_EXAMPLE_TEMPLATE_ID.equals(projectTemplateId);
    }

    @Override
    public Optional<RepresentationMetadata> handle(ICause cause, String projectTemplateId, IEditingContext editingContext) {
        if (OntologyProjectTemplateProvider.ONTOLOGY_EXAMPLE_TEMPLATE_ID.equals(projectTemplateId)) {
            return this.initializeOntologyModel(editingContext);
        }
        return Optional.empty();
    }

    private Optional<RepresentationMetadata> initializeOntologyModel(IEditingContext editingContext) {
        Optional<RepresentationMetadata> result = Optional.empty();
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            var documentId = UUID.randomUUID();
            var resource = new JSONResourceFactory().createResourceFromPath(documentId.toString());
            var resourceMetadataAdapter = new ResourceMetadataAdapter("Ontology");
            resource.eAdapters().add(resourceMetadataAdapter);
            emfEditingContext.getDomain().getResourceSet().getResources().add(resource);

            resource.getContents().addAll(new OntologySampleBuilder().getEmptySampleContent());
        }
        return result;
    }
}
