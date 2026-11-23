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
import java.util.function.Function;

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
 * Provides read-only tables of an entity's direct and inherited references.
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

    public TableWidgetDescription createOwnedReferencesWidget() {
        return this.createWidget(WIDGET_ID_PREFIX, "Direct References", Entity::getOwnedReferences);
    }

    public TableWidgetDescription createInheritedReferencesWidget() {
        return this.createWidget("entity.inheritedReferences.", "Inherited References", entity -> entity.getReferences().stream()
                .filter(reference -> !entity.getOwnedReferences().contains(reference))
                .toList());
    }

    private TableWidgetDescription createWidget(String idPrefix, String label, Function<Entity, List<Reference>> referencesProvider) {
        return TableWidgetDescription.newTableWidgetDescription(idPrefix)
                .idProvider(variableManager -> idPrefix + this.getTargetObjectId(variableManager))
                .labelProvider(variableManager -> label)
                .targetObjectIdProvider(this::getTargetObjectId)
                .isReadOnlyProvider(variableManager -> true)
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .tableDescription(this.createTableDescription(idPrefix, label, referencesProvider))
                .build();
    }

    private TableDescription createTableDescription(String idPrefix, String label, Function<Entity, List<Reference>> referencesProvider) {
        return TableDescription.newTableDescription(idPrefix + "table")
                .label(label)
                .targetObjectIdProvider(this::getTargetObjectId)
                .targetObjectKindProvider(this::getTargetObjectKind)
                .labelProvider(variableManager -> label)
                .lineDescription(this.createLineDescription(idPrefix, referencesProvider))
                .columnDescriptions(List.of(this.createColumnDescription(idPrefix)))
                .cellDescriptions(List.of(this.createCellDescription(idPrefix)))
                .iconURLsProvider(variableManager -> List.of())
                .isStripeRowPredicate(variableManager -> false)
                .enableSubRows(false)
                .pageSizeOptionsProvider(variableManager -> List.of())
                .defaultPageSizeIndexProvider(variableManager -> 0)
                .build();
    }

    private LineDescription createLineDescription(String idPrefix, Function<Entity, List<Reference>> referencesProvider) {
        return LineDescription.newLineDescription(idPrefix + "line")
                .targetObjectIdProvider(this::getTargetObjectId)
                .targetObjectKindProvider(this::getTargetObjectKind)
                .semanticElementsProvider(variableManager -> this.getReferences(variableManager, referencesProvider))
                .headerLabelProvider(variableManager -> variableManager.get(VariableManager.SELF, Reference.class).map(Reference::getName).orElse(""))
                .headerIconURLsProvider(variableManager -> List.of())
                .headerIndexLabelProvider(variableManager -> "")
                .isResizablePredicate(variableManager -> false)
                .initialHeightProvider(variableManager -> 36)
                .depthLevelProvider(variableManager -> 0)
                .hasChildrenProvider(variableManager -> false)
                .build();
    }

    private PaginatedData getReferences(VariableManager variableManager, Function<Entity, List<Reference>> referencesProvider) {
        List<Object> references = variableManager.get(VariableManager.SELF, Entity.class).stream()
                .flatMap(entity -> referencesProvider.apply(entity).stream())
                .map(Object.class::cast)
                .toList();
        return new PaginatedData(references, false, false, references.size());
    }

    private ColumnDescription createColumnDescription(String idPrefix) {
        return ColumnDescription.newColumnDescription(idPrefix + REFERENCED_TYPE)
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

    private IconLabelCellDescription createCellDescription(String idPrefix) {
        return IconLabelCellDescription.newIconLabelCellDescription(idPrefix + "cell")
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
