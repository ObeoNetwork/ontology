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
package fr.obeo.ontology.services.representations.modelexplorer;

import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Virtual tree item representing references container.
 *
 * @author ntinsalhi
 */
public class ReferencesTreeItemFragment implements TreeItemFragment {

    public final static String TYPE = "ReferencesTreeItemFragment";

    private final Entity entity;

    private final IIdentityService identityService;

    private final ILabelService labelService;

    public ReferencesTreeItemFragment(Entity entity, IIdentityService identityService, ILabelService labelService) {
        this.entity = Objects.requireNonNull(entity);
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public String getLabel() {
        return "References";
    }

    @Override
    public List<String> getIconURL() {
        return this.labelService.getImagePaths(EnvironmentFactory.eINSTANCE.createReference());
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        return !this.entity.getOwnedReferences().isEmpty();
    }

    @Override
    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        return this.entity.getOwnedReferences()
                .stream()
                .map(Object.class::cast)
                .toList();
    }

    @Override
    public String getTreeItemId() {
        return UriComponentsBuilder.fromUriString(OntologyExplorerServices.FRAGMENT_URI_PREFIX)
                .queryParam(OntologyExplorerServices.FRAGMENT_TYPE_PARAM, TYPE)
                .queryParam(OntologyExplorerServices.SEMANTIC_OBJECT_ID_PARAM, this.identityService.getId(this.entity))
                .encode()
                .build().toUri().toString();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isDeletable() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public String getKind() {
        return "";
    }
}
