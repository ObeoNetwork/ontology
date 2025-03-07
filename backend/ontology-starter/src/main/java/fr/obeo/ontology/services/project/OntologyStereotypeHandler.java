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
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.web.application.document.dto.DocumentDTO;
import org.eclipse.sirius.web.application.document.services.api.IStereotypeHandler;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * Used to create documents from a stereotype.
 *
 * @author lfasani
 */
@Service
public class OntologyStereotypeHandler implements IStereotypeHandler {

    private final OntologySamplesProvider ontologySamplesProvider;

    public OntologyStereotypeHandler(OntologySamplesProvider ontologySamplesProvider) {
        this.ontologySamplesProvider = Objects.requireNonNull(ontologySamplesProvider);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String stereotypeId) {
        return List.of(OntologyStereotypeProvider.ONTOLOGY_SAMPLE).contains(stereotypeId);
    }

    @Override
    public Optional<DocumentDTO> handle(IEditingContext editingContext, String stereotypeId, String name) {
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            return switch (stereotypeId) {
                case OntologyStereotypeProvider.ONTOLOGY_SAMPLE -> createSampleOntologyDocument(emfEditingContext, name);
                default -> Optional.empty();
            };
        }
        return Optional.empty();
    }

    private Optional<DocumentDTO> createSampleOntologyDocument(IEMFEditingContext editingContext, String name) {
        var documentId = this.ontologySamplesProvider.addEmptyOntology(editingContext.getDomain().getResourceSet(), name);
        return Optional.of(new DocumentDTO(documentId, name, ExplorerDescriptionProvider.DOCUMENT_KIND));
    }
}
