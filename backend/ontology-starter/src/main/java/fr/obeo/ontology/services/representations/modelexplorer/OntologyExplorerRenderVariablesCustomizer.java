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
package fr.obeo.ontology.services.representations.modelexplorer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.representations.IRepresentationRenderVariableCustomizer;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class OntologyExplorerRenderVariablesCustomizer implements IRepresentationRenderVariableCustomizer {
    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    public OntologyExplorerRenderVariablesCustomizer(IRepresentationMetadataSearchService representationMetadataSearchService) {
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
    }

    public VariableManager customize(IRepresentationDescription representationDescription, VariableManager variableManager) {
        if (!"explorer_tree_description".equals(representationDescription.getId())) {
            Optional<IEditingContext> optionalEditingContext = variableManager.get("editingContext", IEditingContext.class);
            if (optionalEditingContext.isPresent()) {
                VariableManager customizedVariableManager = variableManager.createChild();
                String editingContextId = optionalEditingContext.get().getId();
                Optional<UUID> optionalSemanticDataId = (new UUIDParser()).parse(editingContextId);
                List<RepresentationMetadata> allRepresentationMetadata = this.representationMetadataSearchService.findAllRepresentationMetadataBySemanticData(
                        AggregateReference.to(optionalSemanticDataId.get()));
                customizedVariableManager.put("existingRepresentations", allRepresentationMetadata);
                return customizedVariableManager;
            }
        }

        return variableManager;
    }
}
