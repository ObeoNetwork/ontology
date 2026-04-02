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
package fr.obeo.ontology.services.representations.tables;

import fr.obeo.ontology.services.representations.providers.ViewEntityTableDescriptionProvider;

import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.collaborative.tables.api.IRowFilterProvider;
import org.eclipse.sirius.components.collaborative.tables.api.RowFilter;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Namespace;
import org.springframework.stereotype.Service;

/**
 * Provides row filters used in entity table.
 *
 * @author ntinsalhi
 */
@Service
public class EntityTableFiltersProvider implements IRowFilterProvider {

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    public EntityTableFiltersProvider(IObjectSearchService objectSearchService, IIdentityService identityService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = identityService;
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, TableDescription tableDescription, String representationId) {
        return Objects.equals(tableDescription.getLabel(), ViewEntityTableDescriptionProvider.ENTITIES_TABLE_NAME);
    }

    @Override
    public List<RowFilter> get(IEditingContext editingContext, TableDescription tableDescription, String representationId) {
        List<RowFilter> rowFilters = List.of();
        var representationMetadata = this.objectSearchService.getObject(editingContext, representationId)
                .filter(RepresentationMetadata.class::isInstance)
                .map(RepresentationMetadata.class::cast)
                .orElse(null);

        if (Objects.nonNull(representationMetadata)) {
            var optTargetObject = this.objectSearchService.getObject(editingContext, representationMetadata.getTargetObjectId());

            if (optTargetObject.isPresent() && optTargetObject.get() instanceof Namespace namespace) {
                List<Entity> coreEntities = namespace.getTypes()
                        .stream()
                        .filter(Entity.class::isInstance)
                        .map(Entity.class::cast)
                        .filter(entity -> Objects.isNull(entity.getSupertype()))
                        .toList();

                rowFilters = coreEntities.stream()
                        .map(coreEntity -> {
                            var rowFilterId = this.identityService.getId(coreEntity) + "-filter-id";
                            return new RowFilter(rowFilterId, coreEntity.getName(), true);
                        })
                        .toList();
            }
        }

        return rowFilters;
    }
}
