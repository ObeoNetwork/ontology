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

import java.util.List;

import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * This interface represent a custom tree item.
 *
 * @author lfasani
 */
public interface TreeItemFragment {

    String getLabel();

    List<String> getIconURL();

    boolean hasChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds);

    List<Object> getChildren(IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds);

    String getTreeItemId();

    boolean isEditable();

    boolean isDeletable();

    boolean isSelectable();

    String getKind();
}
