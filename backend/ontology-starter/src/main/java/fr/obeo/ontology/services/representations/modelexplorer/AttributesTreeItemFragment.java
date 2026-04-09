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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This virtual tree item corresponds to a container of attributes.
 *
 * @author lfasani
 */
public class AttributesTreeItemFragment implements TreeItemFragment {

    static final String TYPE = "AttributesTreeItemFragment";

    private final Entity entity;

    private final IIdentityService identityService;

    private final ILabelService labelService;

    public AttributesTreeItemFragment(Entity entity, IIdentityService identityService, ILabelService labelService) {
        this.entity = Objects.requireNonNull(entity);
        this.identityService = identityService;
        this.labelService = labelService;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public String getLabel() {
        return "Attributes";
    }

    @Override
    public List<String> getIconURL() {
        return this.labelService.getImagePaths(EnvironmentFactory.eINSTANCE.createAttribute());
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        boolean result = Optional.of(this.entity)
                .map(entity -> entity.getOwnedAttributes())
                .stream()
                .flatMap(attributes -> attributes.stream())
                .findFirst()
                .isPresent();
        return result;
    }

    @Override
    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        List<Object> result = new ArrayList<>();

        result.addAll(Optional.of(this.entity)
                .map(entity -> entity.getOwnedAttributes())
                .stream()
                .flatMap(attributes -> attributes.stream())
                .toList());

        return result;
    }

    @Override
    public String getTreeItemId() {
        return UriComponentsBuilder.fromUriString(ExplorerJavaService.FRAGMENT_URI_PREFIX)
                .queryParam(ExplorerJavaService.FRAGMENT_TYPE_PARAM, TYPE)
                .queryParam(ExplorerJavaService.SEMANTIC_OBJECT_ID_PARAM, this.identityService.getId(this.entity))
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
