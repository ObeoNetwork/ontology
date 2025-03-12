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
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.trees.api.ITreeFilterProvider;
import org.eclipse.sirius.components.collaborative.trees.api.TreeFilter;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.springframework.stereotype.Service;

/**
 * Specific tree filter provider for Ontology explorer.
 *
 * @author lfasani
 */
@Service
public class OntologyTreeFilterProvider implements ITreeFilterProvider {

    public static final String HIDE_COMMENTS_TREE_ITEM_FILTER_ID = UUID.nameUUIDFromBytes("OntologyCommentsTreeItemFilter".getBytes()).toString();

    public static final String HIDE_ATTRIBUTES_TREE_FILTER_ID = UUID.nameUUIDFromBytes("OntologyAttributesTreeItemFilter".getBytes()).toString();

    @Override
    public List<TreeFilter> get(String editingContextId, TreeDescription treeDescription, String representationId) {
        List<TreeFilter> filters = new ArrayList<>();
//        if (treeDescription.getLabel().equals(ViewExplorerTreeDescriptionBuilder.ONTOLOGY_EXPLORER_DESCRIPTION_NAME)) {
        filters.add(new TreeFilter(HIDE_COMMENTS_TREE_ITEM_FILTER_ID, "Hide Comments", true));
        filters.add(new TreeFilter(HIDE_ATTRIBUTES_TREE_FILTER_ID, "Hide Attributes", true));
//        }

        return filters;
    }
}
