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

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;

import java.util.Objects;

import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLResult;

/**
 * Asserts the GraphQL outcome of a root name edit.
 *
 * @author lfasani
 */
public class EditValidationRootNameAssert {

    private final GraphQLResult result;

    public EditValidationRootNameAssert(GraphQLResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public EditValidationRootNameAssert isSuccess() {
        assertThat(this.result.errors()).isEmpty();
        String typename = JsonPath.read(this.result.data(), "$.data.editTextfield.__typename");
        assertThat(typename).isEqualTo(SuccessPayload.class.getSimpleName());
        return this;
    }
}
