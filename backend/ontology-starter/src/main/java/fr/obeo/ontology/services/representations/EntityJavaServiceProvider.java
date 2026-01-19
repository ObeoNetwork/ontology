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
package fr.obeo.ontology.services.representations;

import fr.obeo.ontology.services.representations.modelexplorer.OntologyExplorerServices;

import java.util.List;

import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.springframework.stereotype.Service;

/**
 * Used to provide services for the Entity view.
 *
 * @author jmallet
 */
@Service
public class EntityJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        boolean isEntityView = view.getDescriptions().stream()
                .anyMatch(representationDescription -> representationDescription.getDomainType().equals("entity::Entity"));
        if (isEntityView) {
            return List.of(EntityJavaService.class, OntologyExplorerServices.class, EntityTableJavaService.class);
        }
        return List.of();
    }

}
