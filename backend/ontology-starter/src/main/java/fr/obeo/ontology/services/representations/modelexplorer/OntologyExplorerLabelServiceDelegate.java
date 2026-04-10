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

import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerLabelServiceDelegate;
import org.obeonetwork.dsl.entity.Entity;
import org.springframework.stereotype.Service;

@Service
public class OntologyExplorerLabelServiceDelegate implements IExplorerLabelServiceDelegate {

    @Override
    public boolean canHandle(Object object) {
        return object instanceof Entity;
    }

    @Override
    public boolean isEditable(Object self) {
        return self instanceof Entity;
    }

    @Override
    public void editLabel(Object self, String newValue) {
        if (self instanceof Entity entity) {
            String cleanedValue = newValue
                    .replaceAll("\\s*\\[\\d+]", "")
                    .trim();
            entity.setName(cleanedValue);
        }
    }
}
