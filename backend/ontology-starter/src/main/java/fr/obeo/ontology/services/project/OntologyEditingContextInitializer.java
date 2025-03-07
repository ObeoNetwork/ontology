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
package fr.obeo.ontology.services.project;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.obeonetwork.dsl.entity.EntityPackage;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.technicalid.TechnicalIDPackage;
import org.springframework.stereotype.Service;

/**
 * Used to initialize the editing context of an ontology project.
 *
 * @author lfasani
 */
@Service
public class OntologyEditingContextInitializer implements IEditingContextProcessor {

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    public OntologyEditingContextInitializer(OntologyEditingContextPredicate ontologyEditingContextPredicate) {
        this.ontologyEditingContextPredicate = ontologyEditingContextPredicate;
    }

    @Override
    public void preProcess(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext emfEditingContext && ontologyEditingContextPredicate.test(editingContext)) {
            var packageRegistry = emfEditingContext.getDomain().getResourceSet().getPackageRegistry();
            packageRegistry.put(EntityPackage.eNS_URI, EntityPackage.eINSTANCE);
            packageRegistry.put(EnvironmentPackage.eNS_URI, EnvironmentPackage.eINSTANCE);
            packageRegistry.put(TechnicalIDPackage.eNS_URI, TechnicalIDPackage.eINSTANCE);
        }
    }
}
