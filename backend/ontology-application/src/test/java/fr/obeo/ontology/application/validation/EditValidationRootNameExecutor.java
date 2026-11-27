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
package fr.obeo.ontology.application.validation;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.forms.dto.EditTextfieldInput;
import org.eclipse.sirius.components.forms.tests.graphql.EditTextfieldMutationRunner;
import org.springframework.stereotype.Service;

/**
 * Executes root name edits through the existing form mutation.
 *
 * @author lfasani
 */
@Service
public class EditValidationRootNameExecutor {

    private final EditTextfieldMutationRunner editTextfieldMutationRunner;

    public EditValidationRootNameExecutor(EditTextfieldMutationRunner editTextfieldMutationRunner) {
        this.editTextfieldMutationRunner = Objects.requireNonNull(editTextfieldMutationRunner);
    }

    public EditValidationRootNameAssert execute(String editingContextId, String formId, String textfieldId, String value) {
        var input = new EditTextfieldInput(UUID.randomUUID(), editingContextId, formId, textfieldId, value);
        return new EditValidationRootNameAssert(this.editTextfieldMutationRunner.run(input));
    }
}
