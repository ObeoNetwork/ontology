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
package fr.obeo.ontology.services.representations.builders;

import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.table.TableBuilders;

/**
 * Used to provide the view model used to create tables.
 *
 * @author fbarbin
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ViewEntityTableDescriptionBuilder {

    public static final String ENTITY_TABLE_ATTRIBUTES_COLUMN = "Attributes";

    public static final String ENTITY_TABLE_ATTRIBUTES_COMMENTS = "Comments";

    public static final String AQL = "aql:";

    private final TableBuilders tableBuilders = new TableBuilders();

    private final View view;

    public ViewEntityTableDescriptionBuilder(View view) {
        this.view = view;
    }

    public void addRepresentationDescription() {
        RepresentationDescription representationDescription = this.create();

        this.view.getDescriptions().add(representationDescription);
    }

    private RepresentationDescription create() {

        var attributesColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_ATTRIBUTES_COLUMN + "'")
                .headerLabelExpression(ENTITY_TABLE_ATTRIBUTES_COLUMN)
                .initialWidthExpression("450")
                .isResizableExpression(AQL + "true")
                .build();

        var commentsColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_ATTRIBUTES_COMMENTS + "'")
                .headerLabelExpression(ENTITY_TABLE_ATTRIBUTES_COMMENTS)
                .initialWidthExpression("250")
                .isResizableExpression(AQL + "true")
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

        var rowDescription = this.tableBuilders.newRowDescription()
                .semanticCandidatesExpression(AQL + "self.getAllOrderedEntities(expandedIds, globalFilterData, columnFilters)->toPaginatedData(cursor,direction,size)")
                .initialHeightExpression("-1")
                .isResizableExpression(AQL + "false")
                .headerLabelExpression(AQL + "self.name")
                .headerIndexLabelExpression(AQL + "rowIndex + 1")
                .depthLevelExpression(AQL + "self.getEntityLevel()")
                .build();

        return this.tableBuilders.newTableDescription()
                .name("Entities Table")
                .titleExpression("aql:self.name + ' Table'")
                .domainType("entity::Entity")
                .preconditionExpression("aql:self.supertype==null")
                .columnDescriptions(attributesColumnDescription, commentsColumnDescription)
                .cellDescriptions(cellAttributesDescription, cellCommentsDescription)
                .rowDescription(rowDescription)
                .enableSubRows(true)
                .build();
    }
}
