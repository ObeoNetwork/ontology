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
package fr.obeo.ontology.authorization.services;

import fr.obeo.ocp.domain.boundedcontexts.authorization.services.api.IAuthorizationAdviser;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Used to update the result of the authorization process.
 *
 * @author sbegaudeau
 */
@Service
public class AuthorizationAdviser implements IAuthorizationAdviser {
    @Override
    public Set<String> getUserAccessLevels(UUID accountId, Set<String> defaultAccessLevels) {
        return defaultAccessLevels;
    }
}
