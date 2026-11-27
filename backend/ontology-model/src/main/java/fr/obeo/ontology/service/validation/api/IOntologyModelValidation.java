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
package fr.obeo.ontology.service.validation.api;

import fr.obeo.ontology.service.validation.OntologyModelValidation.ValidationStatus;

import java.util.List;

import org.obeonetwork.dsl.entity.Root;

/**
 * Provides the business validation of an ontology model.
 *
 * @author lfasani
 * @since 2027.02.00
 */
public interface IOntologyModelValidation {

    List<ValidationStatus> validate(Root root);
}
