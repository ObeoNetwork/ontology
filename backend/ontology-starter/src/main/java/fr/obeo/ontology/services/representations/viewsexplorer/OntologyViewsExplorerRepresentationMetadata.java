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
package fr.obeo.ontology.services.representations.viewsexplorer;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationIconURL;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.SemanticData;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * Transient display projection required by the Views content API's metadata list.
 * Only the label differs from the original aggregate. This projection must never be persisted or updated.
 *
 * @author lfasani
 */
public class OntologyViewsExplorerRepresentationMetadata extends RepresentationMetadata {

    private final RepresentationMetadata original;

    private final String displayLabel;

    public OntologyViewsExplorerRepresentationMetadata(RepresentationMetadata original, String displayLabel) {
        this.original = Objects.requireNonNull(original);
        this.displayLabel = Objects.requireNonNull(displayLabel);
    }

    /**
     * Forwards the legacy persistence key for compatibility only.
     *
     * @deprecated Use getRepresentationMetadataId() for representation identity.
     */
    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public String getId() {
        return this.original.getId();
    }

    @Override
    public UUID getRepresentationMetadataId() {
        return this.original.getRepresentationMetadataId();
    }

    @Override
    public AggregateReference<SemanticData, UUID> getSemanticData() {
        return this.original.getSemanticData();
    }

    @Override
    public String getTargetObjectId() {
        return this.original.getTargetObjectId();
    }

    @Override
    public String getDescriptionId() {
        return this.original.getDescriptionId();
    }

    @Override
    public String getLabel() {
        return this.displayLabel;
    }

    @Override
    public String getKind() {
        return this.original.getKind();
    }

    @Override
    public Instant getCreatedOn() {
        return this.original.getCreatedOn();
    }

    @Override
    public Instant getLastModifiedOn() {
        return this.original.getLastModifiedOn();
    }

    @Override
    public List<RepresentationIconURL> getIconURLs() {
        return this.original.getIconURLs();
    }

    @Override
    public String getDocumentation() {
        return this.original.getDocumentation();
    }

    @Override
    public boolean isNew() {
        return this.original.isNew();
    }
}
