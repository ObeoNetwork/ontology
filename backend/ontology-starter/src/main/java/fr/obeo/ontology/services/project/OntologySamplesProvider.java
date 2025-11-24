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
import java.util.UUID;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.migration.MigrationService;
import org.eclipse.sirius.components.emf.migration.api.IMigrationParticipant;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.springframework.stereotype.Service;

/**
 * Used to provide an example of Ontology.
 *
 * @author lfasani
 */
@Service
public class OntologySamplesProvider {

    private final List<IMigrationParticipant> migrationParticipants;

    public OntologySamplesProvider(List<IMigrationParticipant> migrationParticipants) {
        this.migrationParticipants = Objects.requireNonNull(migrationParticipants);
    }

    public UUID addEmptyOntology(ResourceSet resourceSet, String resourceName) {
        var documentId = UUID.randomUUID();
        var resource = new JSONResourceFactory().createResourceFromPath(documentId.toString());

        var resourceMetadataAdapter = new ResourceMetadataAdapter(resourceName);
        var migrationService = new MigrationService(this.migrationParticipants);

        resourceMetadataAdapter.addMigrationData(migrationService.getMostRecentParticipantMigrationData());

        resource.eAdapters().add(resourceMetadataAdapter);
        resourceSet.getResources().add(resource);

        resource.getContents().addAll(new OntologySampleBuilder().getEmptySampleContent());

        return documentId;
    }
}
