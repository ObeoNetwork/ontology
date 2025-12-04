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
package fr.obeo.ontology.services.representations.modelexplorer;

import fr.obeo.ontology.services.representations.providers.ViewExplorerTreeDescriptionProvider;

import java.util.List;

import org.eclipse.sirius.web.application.views.explorer.dto.ExplorerDescriptionMetadata;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerTreeDescriptionOrderer;
import org.springframework.stereotype.Service;

/**
 * Sorts the tree descriptions that can be used as explorer in Ontology.
 *
 * @author lfasani
 */
@Service
public class OntologyExplorerTreeDescriptionOrderer implements IExplorerTreeDescriptionOrderer {

    @Override
    public void order(List<ExplorerDescriptionMetadata> explorerDescriptionMetadataList) {
        if (explorerDescriptionMetadataList.stream()
                .map(ExplorerDescriptionMetadata::label)
                .anyMatch(label -> ViewExplorerTreeDescriptionProvider.ONTOLOGY_EXPLORER_DESCRIPTION_NAME.equals(label))) {
            // Remove the default Sirius Web explorer if the Ontology Explorer is available.
//            explorerDescriptionMetadataList.removeIf(metadata -> ExplorerDescriptionProvider.DESCRIPTION_ID.equals(metadata.id()));
        }
    }

}
