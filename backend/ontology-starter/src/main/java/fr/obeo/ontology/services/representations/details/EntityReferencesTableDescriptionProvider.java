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
package fr.obeo.ontology.services.representations.details;

import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.tables.descriptions.ColumnDescription;
import org.eclipse.sirius.components.tables.descriptions.IconLabelCellDescription;
import org.eclipse.sirius.components.tables.descriptions.LineDescription;
import org.eclipse.sirius.components.tables.descriptions.PaginatedData;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.eclipse.sirius.components.widget.table.TableWidgetDescription;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Reference;
import org.obeonetwork.dsl.environment.Type;
import org.springframework.stereotype.Service;

/**
 * Provides the read-only table of an entity's directly owned references.
 *
 * @author lfasani
 */
@Service
public class EntityReferencesTableDescriptionProvider {

    public static final String WIDGET_ID_PREFIX = "entity.ownedReferences.";

    private static final String REFERENCED_TYPE = "referencedType";

    private final IIdentityService identityService;

    public EntityReferencesTableDescriptionProvider(IIdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);
    }

    public TableWidgetDescription create() {
        return TableWidgetDescription.newTableWidgetDescription(WIDGET_ID_PREFIX)
                .idProvider(variableManager -> WIDGET_ID_PREFIX + this.getTargetObjectId(variableManager))
                .labelProvider(variableManager -> "")
                .targetObjectIdProvider(this::getTargetObjectId)
                .isReadOnlyProvider(variableManager -> true)
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .tableDescription(this.createTableDescription())
                .build();
    }

    private TableDescription createTableDescription() {
        return TableDescription.newTableDescription("entityReferencesTable")
                .label("Entity references")
                .targetObjectIdProvider(this::getTargetObjectId)
                .targetObjectKindProvider(this::getTargetObjectKind)
                .labelProvider(variableManager -> "References")
                .lineDescription(this.createLineDescription())
                .columnDescriptions(List.of(this.createColumnDescription()))
                .cellDescriptions(List.of(this.createCellDescription()))
                .iconURLsProvider(variableManager -> List.of())
                .isStripeRowPredicate(variableManager -> false)
                .enableSubRows(false)
                .pageSizeOptionsProvider(variableManager -> List.of())
                .defaultPageSizeIndexProvider(variableManager -> 0)
                .build();
    }

    private LineDescription createLineDescription() {
        return LineDescription.newLineDescription("entityReferencesLine")
                .targetObjectIdProvider(this::getTargetObjectId)
                .targetObjectKindProvider(this::getTargetObjectKind)
                .semanticElementsProvider(this::getReferences)
                .headerLabelProvider(variableManager -> variableManager.get(VariableManager.SELF, Reference.class).map(Reference::getName).orElse(""))
                .headerIconURLsProvider(variableManager -> List.of())
                .headerIndexLabelProvider(variableManager -> "")
                .isResizablePredicate(variableManager -> false)
                .initialHeightProvider(variableManager -> 36)
                .depthLevelProvider(variableManager -> 0)
                .hasChildrenProvider(variableManager -> false)
                .build();
    }

    private PaginatedData getReferences(VariableManager variableManager) {
        List<Object> references = variableManager.get(VariableManager.SELF, Entity.class).stream()
                .flatMap(entity -> entity.getOwnedReferences().stream())
                .map(Object.class::cast)
                .toList();
        return new PaginatedData(references, false, false, references.size());
    }

    private ColumnDescription createColumnDescription() {
        return ColumnDescription.newColumnDescription(REFERENCED_TYPE)
                .semanticElementsProvider(variableManager -> List.of(REFERENCED_TYPE))
                .targetObjectIdProvider(variableManager -> REFERENCED_TYPE)
                .targetObjectKindProvider(variableManager -> "")
                .headerLabelProvider(variableManager -> "Referenced Type")
                .headerIconURLsProvider(variableManager -> List.of())
                .headerIndexLabelProvider(variableManager -> "")
                .shouldRenderPredicate(variableManager -> true)
                .initialWidthProvider(variableManager -> 200)
                .isResizablePredicate(variableManager -> false)
                .filterVariantProvider(variableManager -> "text")
                .isSortablePredicate(variableManager -> false)
                .build();
    }

    private IconLabelCellDescription createCellDescription() {
        return IconLabelCellDescription.newIconLabelCellDescription("entityReferencedTypeCell")
                .canCreatePredicate(variableManager -> variableManager.get(VariableManager.SELF, Reference.class).map(Reference::getReferencedType).isPresent())
                .targetObjectIdProvider(variableManager -> variableManager.get(VariableManager.SELF, Reference.class)
                        .map(Reference::getReferencedType).map(this.identityService::getId).orElse(""))
                .targetObjectKindProvider(variableManager -> variableManager.get(VariableManager.SELF, Reference.class)
                        .map(Reference::getReferencedType).map(this.identityService::getKind).orElse(""))
                .cellValueProvider((variableManager, column) -> variableManager.get(VariableManager.SELF, Reference.class)
                        .map(Reference::getReferencedType).map(Type::getName).orElse(""))
                .cellIconURLsProvider((variableManager, column) -> List.of())
                .cellTooltipValueProvider((variableManager, column) -> "")
                .build();
    }

    private String getTargetObjectId(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, Object.class).map(this.identityService::getId).orElse("");
    }

    private String getTargetObjectKind(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, Object.class).map(this.identityService::getKind).orElse("");
    }
}
