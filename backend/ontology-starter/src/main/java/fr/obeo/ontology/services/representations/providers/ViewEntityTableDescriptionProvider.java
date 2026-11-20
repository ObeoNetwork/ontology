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

import java.util.List;

import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.generated.table.TableBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.table.CellDescription;
import org.eclipse.sirius.components.view.table.ColumnDescription;
import org.eclipse.sirius.components.view.table.RowContextMenuEntry;
import org.eclipse.sirius.components.view.table.RowDescription;
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

    public static final String ENTITY_TABLE_REFERENCES_COLUMN = "References";

    public static final String ENTITY_TABLE_BUSINESSDOMAIN_COLUMN = "Business Domain";

    public static final String ENTITY_TABLE_DATAOWNER_COLUMN = "Data Owner";

    public static final String ENTITY_TABLE_DATASOURCES_COLUMN = "Data Sources";

    public static final String AQL = "aql:";

    public static final String ENTITIES_TABLE_NAME = "Entities Table";

    private final TableBuilders tableBuilders = new TableBuilders();

    private final ViewBuilders viewBuilders = new ViewBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        List<ColumnDescription> columnDescriptions = this.createColumnDescriptions();
        List<CellDescription> cellDescriptions = this.createCellDescriptions();
        RowDescription rowDescription = this.createRowDescription();

        return this.tableBuilders.newTableDescription()
                .name(ENTITIES_TABLE_NAME)
                .titleExpression("aql:self.name + ' Table'")
                .domainType("environment::Namespace")
                .columnDescriptions(columnDescriptions.toArray(ColumnDescription[]::new))
                .cellDescriptions(cellDescriptions.toArray(CellDescription[]::new))
                .rowDescription(rowDescription)
                .enableSubRows(true)
                .useStripedRowsExpression("true")
                .pageSizeOptionsExpression("aql:Sequence{10,20,50}")
                .build();
    }

    private List<ColumnDescription> createColumnDescriptions() {
        var attributesColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_ATTRIBUTES_COLUMN + "'")
                .headerLabelExpression(ENTITY_TABLE_ATTRIBUTES_COLUMN)
                .initialWidthExpression("250")
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

        var referencesColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_REFERENCES_COLUMN + "'")
                .headerLabelExpression(ENTITY_TABLE_REFERENCES_COLUMN)
                .initialWidthExpression("250")
                .isResizableExpression(AQL + "true")
                .isSortableExpression(AQL + "false")
                .build();

        var businessDomainColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_BUSINESSDOMAIN_COLUMN + "'")
                .headerLabelExpression("Functional Area")
                .initialWidthExpression("250")
                .isResizableExpression(AQL + "true")
                .isSortableExpression(AQL + "false")
                .build();

        var dataOwnerColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_DATAOWNER_COLUMN + "'")
                .headerLabelExpression(ENTITY_TABLE_DATAOWNER_COLUMN)
                .initialWidthExpression("250")
                .isResizableExpression(AQL + "true")
                .isSortableExpression(AQL + "false")
                .build();

        var dataSourceColumnDescription = this.tableBuilders.newColumnDescription()
                .semanticCandidatesExpression(AQL + "'" + ENTITY_TABLE_DATASOURCES_COLUMN + "'")
                .headerLabelExpression(ENTITY_TABLE_DATASOURCES_COLUMN)
                .initialWidthExpression("250")
                .isResizableExpression(AQL + "true")
                .isSortableExpression(AQL + "false")
                .build();
        return List.of(attributesColumnDescription, commentsColumnDescription, referencesColumnDescription, businessDomainColumnDescription,
                dataOwnerColumnDescription, dataSourceColumnDescription);
    }

    private List<CellDescription> createCellDescriptions() {
        var cellAttributesDescription = this.tableBuilders.newCellDescription()
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_ATTRIBUTES_COLUMN + "'")
                .valueExpression("aql:self.getAttributesCellLabel()")
                .cellWidgetDescription(this.tableBuilders.newCellTextareaWidgetDescription().build())
                .build();

        var cellCommentsDescription = this.tableBuilders.newCellDescription()
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_ATTRIBUTES_COMMENTS + "'")
                .valueExpression(
                        "aql:if self.metadatas.oclIsKindOf(environment::MetaDataContainer) then self.metadatas.metadatas->collect(m | '[' + m.title + ',' + m.body + ']')->sep('','\n','')->toString() else '' endif")
                .cellWidgetDescription(this.tableBuilders.newCellTextareaWidgetDescription().build())
                .build();

        var cellReferencesDescription = this.tableBuilders.newCellDescription()
                .name("reference-cell-description")
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_REFERENCES_COLUMN + "'")
                .valueExpression("aql:self.getEntityReferencesCellLabel()")
                .cellWidgetDescription(this.tableBuilders.newCellTextareaWidgetDescription().build())
                .build();

        var cellBusinessDomainDescription = this.tableBuilders.newCellDescription()
                .name("businessDomain-cell-description")
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_BUSINESSDOMAIN_COLUMN + "'")
                .valueExpression("aql:self.getBusinessDomainCellLabel()")
                .cellWidgetDescription(this.tableBuilders.newCellLabelWidgetDescription().build())
                .build();

        var cellDataOwnerDescription = this.tableBuilders.newCellDescription()
                .name("dataOwner-cell-description")
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_DATAOWNER_COLUMN + "'")
                .valueExpression(AQL + "self.getDataOwnerCellLabel()")
                .cellWidgetDescription(this.tableBuilders.newCellLabelWidgetDescription().build())
                .build();

        var cellDataSourcesDescription = this.tableBuilders.newCellDescription()
                .name("dataSources-cell-description")
                .preconditionExpression(AQL + "columnTargetObject == '" + ENTITY_TABLE_DATASOURCES_COLUMN + "'")
                .valueExpression("aql:self.getDataSourcesCellLabel()")
                .cellWidgetDescription(this.tableBuilders.newCellTextareaWidgetDescription().build())
                .build();
        return List.of(cellAttributesDescription, cellCommentsDescription, cellReferencesDescription, cellBusinessDomainDescription,
                cellDataOwnerDescription, cellDataSourcesDescription);
    }

    private List<RowContextMenuEntry> createContextMenuEntries() {
        RowContextMenuEntry newSiblingEntity = this.tableBuilders.newRowContextMenuEntry()
                .name("new-sibling-entity-entry")
                .labelExpression("New Sibling Entity")
                .iconURLExpression("customImages/create.svg")
                .body(this.viewBuilders.newChangeContext()
                        .expression(AQL + "self.createSiblingEntry()")
                        .build())
                .build();

        RowContextMenuEntry newSubEntityEntry = this.tableBuilders.newRowContextMenuEntry()
                .name("new-sub-entity-entry")
                .labelExpression("New Sub Entity")
                .preconditionExpression(AQL + "self.canCreateNewSubEntity()")
                .iconURLExpression("customImages/create-sub.svg")
                .body(this.viewBuilders.newChangeContext()
                        .expression(AQL + "self.createSubEntity('New Entity')")
                        .build())
                .build();

        RowContextMenuEntry deleteEntityEntry = this.tableBuilders.newRowContextMenuEntry()
                .name("delete-entity-entry")
                .labelExpression("Delete")
                .iconURLExpression("customImages/delete.svg")
                .body(this.viewBuilders.newChangeContext()
                        .expression(AQL + "self.deleteEntity()")
                        .build())
                .build();
        return List.of(newSubEntityEntry, deleteEntityEntry, newSiblingEntity);
    }

    private RowDescription createRowDescription() {
        List<RowContextMenuEntry> contextMenuEntries = this.createContextMenuEntries();
        //TODO traiter la variable ExplandAll
        return this.tableBuilders.newRowDescription()
                .semanticCandidatesExpression(AQL + "self.getAllOrderedEntities(expandedIds, globalFilterData, columnFilters, activeRowFilterIds)->toPaginatedData(cursor,direction,size)")
                .initialHeightExpression("-1")
                .isResizableExpression(AQL + "true")
                .headerLabelExpression(AQL + "self.name")
                .headerIndexLabelExpression(AQL + "self.getLevelLabel()")
                .depthLevelExpression(AQL + "self.getEntityLevel()")
                .initialHeightExpression("55")
                .contextMenuEntries(contextMenuEntries.toArray(RowContextMenuEntry[]::new))
                .build();
    }
}
