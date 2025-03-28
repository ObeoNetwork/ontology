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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerServices;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.MetaDataContainer;

/**
 * This virtual tree item corresponds to a container of comments.
 *
 * @author lfasani
 */
public class CommentsTreeItemFragment implements TreeItemFragment {

    private final Entity entity;

    private final String id = UUID.nameUUIDFromBytes("CommentsTreeItemFragment".getBytes()).toString();

    private final IObjectService objectService;

    private final IExplorerServices explorerServices;

    public CommentsTreeItemFragment(Entity entity, IObjectService objectService, IExplorerServices explorerServices) {
        this.entity = Objects.requireNonNull(entity);
        this.objectService = objectService;
        this.explorerServices = explorerServices;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public String getLabel() {
        return "Comments";
    }

    @Override
    public List<String> getIconURL() {
        return this.explorerServices.getImageURL(EnvironmentFactory.eINSTANCE.createMetaDataContainer());
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        boolean result = Optional.of(this.entity)
                .map(entity -> entity.getMetadatas())
                .filter(MetaDataContainer.class::isInstance)
                .map(dataContainer -> dataContainer)
                .map(metaDataContainer -> metaDataContainer.getMetadatas())
                .stream()
                .flatMap(metaDataContainer -> metaDataContainer.stream())
                .findFirst()
                .isPresent();
        return result;
    }

    @Override
    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        List<Object> result = new ArrayList<>();

        result.addAll(Optional.of(this.entity)
                .map(entity -> entity.getMetadatas())
                .filter(MetaDataContainer.class::isInstance)
                .map(dataContainer -> dataContainer)
                .map(metaDataContainer -> metaDataContainer.getMetadatas())
                .stream()
                .flatMap(metaDataContainer -> metaDataContainer.stream())
                .toList());

        return result;
    }

    @Override
    public String getTreeItemId() {
        return "CommentsTreeItemFragment " + this.objectService.getId(this.entity);
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
