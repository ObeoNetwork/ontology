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
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This virtual tree item corresponds to a container of {@link fr.obeo.ontology.ontologymm.BusinessDomain}.
 *
 * @author lfasani
 */
public class BusinessDomainsTreeItemFragment implements TreeItemFragment {

    private final OrganizationInformation organizationInformation;

    static final String TYPE = "BusinessDomainsTreeItemFragment";

    final IIdentityService identityService;

    public BusinessDomainsTreeItemFragment(OrganizationInformation organizationInformation, IIdentityService identityService) {
        this.organizationInformation = Objects.requireNonNull(organizationInformation);
        this.identityService = identityService;
    }

    @Override
    public String getLabel() {
        return "Functional Areas";
    }

    @Override
    public List<String> getIconURL() {
        return List.of();
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        return !this.organizationInformation.getBusinessDomains().isEmpty();
    }

    @Override
    public List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds) {
        return this.organizationInformation.getBusinessDomains().stream().map(Object.class::cast).toList();
    }

    @Override
    public String getTreeItemId() {
        return UriComponentsBuilder.fromUriString(OntologyExplorerServices.FRAGMENT_URI_PREFIX)
                .queryParam(OntologyExplorerServices.FRAGMENT_TYPE_PARAM, TYPE)
                .queryParam(OntologyExplorerServices.SEMANTIC_OBJECT_ID_PARAM, this.identityService.getId(this.organizationInformation))
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
