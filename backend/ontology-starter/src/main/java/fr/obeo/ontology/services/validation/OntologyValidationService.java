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
package fr.obeo.ontology.services.validation;

import fr.obeo.ontology.service.validation.OntologyModelValidation.ValidationStatus;
import fr.obeo.ontology.service.validation.api.IOntologyModelValidation;

import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IValidationService;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.obeonetwork.dsl.entity.Root;
import org.springframework.stereotype.Service;

/**
 * Adapts the ontology business validation to the Validation view.
 *
 * @author lfasani
 */
@Service
public class OntologyValidationService implements IValidationService {

    private final IOntologyModelValidation ontologyModelValidation;

    public OntologyValidationService(IOntologyModelValidation ontologyModelValidation) {
        this.ontologyModelValidation = Objects.requireNonNull(ontologyModelValidation);
    }

    @Override
    public List<Object> validate(IEditingContext editingContext) {
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            return emfEditingContext.getDomain().getResourceSet().getResources().stream()
                    .flatMap(resource -> resource.getContents().stream())
                    .filter(Root.class::isInstance)
                    .map(Root.class::cast)
                    .flatMap(root -> this.ontologyModelValidation.validate(root).stream()
                            .map(status -> this.toDiagnostic(root, status)))
                    .map(Object.class::cast)
                    .toList();
        }
        return List.of();
    }

    @Override
    public List<Object> validate(Object object, Object feature) {
        // Business diagnostics are currently exposed only in the Validation view.
        return List.of();
    }

    private Diagnostic toDiagnostic(Root root, ValidationStatus status) {
        int severity = switch (status.severity()) {
            case ERROR -> Diagnostic.ERROR;
            case WARNING -> Diagnostic.WARNING;
            case INFORMATION -> Diagnostic.INFO;
        };
        return new BasicDiagnostic(severity, IOntologyModelValidation.class.getName(), 0, status.message(), new Object[] { root });
    }
}
