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
package fr.obeo.ontology.services.representations.diagrams;

/**
 * Abstract class for Description Providers.
 *
 * @author lfasani
 */
public abstract class AbstractDescriptionProvider {
    public static final String ENTITY_ENTITY = "entity::Entity";

    public static final String BORDER_COLOR = "lightBlue 500";

    public static final String BACKGROUND_COLOR = "white";

    protected static final String AQL_SELF = "aql:self";

    public static final int NB_LEVEL = 3;
}
