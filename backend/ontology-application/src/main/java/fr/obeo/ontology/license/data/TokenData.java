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
package fr.obeo.ontology.license.data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Used to perform some temporary test of the license support.
 *
 * @author sbegaudeau
 */
public record TokenData(UUID id, String title, String description, List<PrivilegeData> privileges, int totalTokenCount, Instant startDate, Instant endDate) {
}
