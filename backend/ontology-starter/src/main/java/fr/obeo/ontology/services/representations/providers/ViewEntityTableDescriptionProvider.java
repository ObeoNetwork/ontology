/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package fr.obeo.ontology.services.representations.providers;

import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.generated.table.TableBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * Used to provide the view model used to create tables.
 *
 * @author fbarbin
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@Service
public class ViewEntityTableDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String ENTITY_TABLE_ATTRIBUTES_COLUMN = "Attributes";

    public static final String ENTITY_TABLE_ATTRIBUTES_COMMENTS = "Comments";

    public static final String AQL = "aql:";

    public static final String ENTITIES_TABLE_NAME = "Entities Table";

    private final TableBuilders tableBuilders = new TableBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {

        var attributesColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_ATTRIBUTES_COLUMN + "'")
                .headerLabelExpression(ENTITY_TABLE_ATTRIBUTES_COLUMN)
                .initialWidthExpression("450")
                .isResizableExpression(AQL + "true")
                .isSortableExpression(AQL + "false")
                .build();

        var commentsColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_ATTRIBUTES_COMMENTS + "'")
                .headerLabelExpression(ENTITY_TABLE_ATTRIBUTES_COMMENTS)
                .initialWidthExpression("250")
                .isResizableExpression(AQL + "true")
                .isSortableExpression(AQL + "false")
                .build();

        var cellAttributesDescription = this.tableBuilders.newCellDescription()
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_ATTRIBUTES_COLUMN + "'")
                .valueExpression(AQL + "self.owned" + ENTITY_TABLE_ATTRIBUTES_COLUMN + ".name->sep(', ')->toString()")
                .cellWidgetDescription(this.tableBuilders.newCellLabelWidgetDescription().build())
                .build();

        var cellCommentsDescription = this.tableBuilders.newCellDescription()
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_ATTRIBUTES_COMMENTS + "'")
                .valueExpression(
                        AQL + "if self.metadatas.oclIsKindOf(environment::MetaDataContainer) then self.metadatas.metadatas->collect(m | '[' + m.title + ',' + m.body + ']')->toString() else '' endif")
                .cellWidgetDescription(this.tableBuilders.newCellLabelWidgetDescription().build())
                .build();

        //TODO traiter la variable ExplandAll
        var rowDescription = this.tableBuilders.newRowDescription()
                .semanticCandidatesExpression(AQL + "self.getAllOrderedEntities(expandedIds, globalFilterData, columnFilters)->toPaginatedData(cursor,direction,size)")
                .initialHeightExpression("-1")
                .isResizableExpression(AQL + "true")
                .headerLabelExpression(AQL + "self.name")
                .headerIndexLabelExpression(AQL + "self.getLevelLabel()")
                .depthLevelExpression(AQL + "self.getEntityLevel()")
                .initialHeightExpression("35")
                .build();

        return this.tableBuilders.newTableDescription()
                .name(ENTITIES_TABLE_NAME)
                .titleExpression("aql:self.name + ' Table'")
                .domainType("environment::Namespace")
                .columnDescriptions(attributesColumnDescription, commentsColumnDescription)
                .cellDescriptions(cellAttributesDescription, cellCommentsDescription)
                .rowDescription(rowDescription)
                .enableSubRows(true)
                .useStripedRowsExpression("true")
                .build();
    }
}
