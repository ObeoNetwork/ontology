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

import fr.obeo.ontology.ontologymm.OrganizationInformation;

import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;

/**
 * This virtual tree item corresponds to a container of {@link fr.obeo.ontology.ontologymm.DataOwner}.
 *
 * @author lfasani
 */
public class DataOwnersTreeItemFragment implements TreeItemFragment {

    private final OrganizationInformation organizationInformation;

    private final IIdentityService identityService;

    static final String ID_PREFIX = "OrganizationsTreeItemFragment";

    public DataOwnersTreeItemFragment(OrganizationInformation organizationInformation, IIdentityService identityService) {
        this.organizationInformation = Objects.requireNonNull(organizationInformation);
        this.identityService = identityService;
    }

    @Override
    public String getLabel() {
        return "Data Owners";
    }

    @Override
    public List<String> getIconURL() {
        return List.of();
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        return !this.organizationInformation.getDataOwners().isEmpty();
    }

    @Override
    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        return this.organizationInformation.getDataOwners().stream().map(Object.class::cast).toList();
    }

    @Override
    public String getTreeItemId() {
        return ID_PREFIX + " " + this.identityService.getId(this.organizationInformation);
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
