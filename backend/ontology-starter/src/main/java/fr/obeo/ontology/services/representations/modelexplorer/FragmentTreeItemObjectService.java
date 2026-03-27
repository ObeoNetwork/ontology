/*****************************************************************************
 * Copyright (c) 2026 Obeo.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
package fr.obeo.ontology.services.representations.modelexplorer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IDefaultObjectSearchService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchServiceDelegate;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * IObjectSearchServiceDelegate that handles {@link TreeItemFragment}.
 *
 * @author lfasani
 */
@Service
public class FragmentTreeItemObjectService implements IObjectSearchServiceDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentTreeItemObjectService.class);

    private final IDefaultObjectSearchService objectService;

    private final IURLParser urlParser;

    public FragmentTreeItemObjectService(IDefaultObjectSearchService objectService, IURLParser urlParser) {
        super();
        this.objectService = Objects.requireNonNull(objectService);
        this.urlParser = Objects.requireNonNull(urlParser);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String objectId) {
        return objectId != null && objectId.startsWith(OntologyExplorerServices.FRAGMENT_URI_PREFIX);
    }

    @Override
    public Optional<Object> getObject(IEditingContext editingContext, String objectId) {
        Optional<Object> result = Optional.empty();
        try {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(objectId);
            if (parameters != null) {
                List<String> semanticObjectId = parameters.get(OntologyExplorerServices.SEMANTIC_OBJECT_ID_PARAM);

                if (semanticObjectId != null && !semanticObjectId.isEmpty()) {
                    result = this.objectService.getObject(editingContext, semanticObjectId.get(0));
                }
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Invalid id {} : {}", objectId, e.getCause());
        }

        return result;
    }
}
