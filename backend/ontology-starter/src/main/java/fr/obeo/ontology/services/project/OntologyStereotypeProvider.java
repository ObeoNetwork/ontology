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

import java.util.List;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.application.document.dto.Stereotype;
import org.eclipse.sirius.web.application.document.services.api.IStereotypeProvider;
import org.springframework.stereotype.Service;

/**
 * Used to return the list of stereotypes to create documents.
 *
 * @author lfasani
 */
@Service
public class OntologyStereotypeProvider implements IStereotypeProvider {

    public static final String ONTOLOGY_SAMPLE = "ontology_sample";

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    public OntologyStereotypeProvider(OntologyEditingContextPredicate ontologyEditingContextPredicate) {
        this.ontologyEditingContextPredicate = ontologyEditingContextPredicate;
    }

    @Override
    public List<Stereotype> getStereotypes(IEditingContext editingContext) {
        if (ontologyEditingContextPredicate.test(editingContext)) {
            return List.of(new Stereotype(ONTOLOGY_SAMPLE, "Ontology Initial Model"));
        }
        return List.of();
    }
}
