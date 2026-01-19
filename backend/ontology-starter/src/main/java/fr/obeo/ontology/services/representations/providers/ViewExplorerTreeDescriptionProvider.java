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
import org.eclipse.sirius.components.view.TextStyleDescription;
import org.eclipse.sirius.components.view.TextStylePalette;
import org.eclipse.sirius.components.view.builder.generated.tree.TreeBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.tree.SingleClickTreeItemContextMenuEntry;
import org.eclipse.sirius.components.view.tree.TreeItemContextMenuEntry;
import org.eclipse.sirius.components.view.tree.TreeItemLabelDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelElementDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelFragmentDescription;
import org.springframework.stereotype.Service;

/**
 * Builder of the Explorer tree description.
 *
 * @author lfasani
 */
@Service
public class ViewExplorerTreeDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String ONTOLOGY_EXPLORER_DESCRIPTION_NAME = "Ontology Explorer";

    private final TextStylePalette textStylePalette;

    public ViewExplorerTreeDescriptionProvider() {
        this.textStylePalette = new ViewOntologyPaletteFactory().createTextStylePalette();
    }

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        return new TreeBuilders().newTreeDescription()
                .name(ONTOLOGY_EXPLORER_DESCRIPTION_NAME)
                .childrenExpression("aql:self.getChildren(editingContext, expanded, activeFilterIds, existingRepresentations)")
                .deletableExpression("aql:self.isDeletable()")
                .editableExpression("aql:self.isEditable()")
                .elementsExpression("aql:editingContext.getElements()")
                .hasChildrenExpression("aql:self.hasChildren(editingContext, expanded, activeFilterIds)")
                .treeItemIconExpression("aql:self.getImageURL()")
                .kindExpression("aql:self.getKind()")
                .parentExpression("aql:self.getParent(id, editingContext)")
                // This predicate will NOT be used while creating the explorer, but we don't want to see the description
                // of the explorer in the list of representations that can be created. Thus, we will return false all
                // the time.
                .preconditionExpression("aql:false")
                .selectableExpression("aql:self.isSelectable()")
                .titleExpression("Ontology Explorer")
                .treeItemIdExpression("aql:self.getTreeItemId()")
                .treeItemObjectExpression("aql:id.getTreeItemObject(editingContext)")
                .treeItemLabelDescriptions(this.createEntityTreeItemLabel(), this.createDefaultStyle())
                .contextMenuEntries(this.createContextMenuEntries().toArray(new TreeItemContextMenuEntry[] {}))
                .build();
    }

    private TreeItemLabelDescription createDefaultStyle() {
        return new TreeBuilders()
                .newTreeItemLabelDescription()
                .name("Default style")
                .preconditionExpression("aql:true")
                .children(this.getDefaultLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelFragmentDescription getDefaultLabelFragmentDescription() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression("aql:self.getLabel()")
                .build();
    }

    private TreeItemLabelDescription defaultStyle() {
        return new TreeBuilders()
                .newTreeItemLabelDescription()
                .name("default style")
                .preconditionExpression("aql:true")
                .children(new TreeBuilders().newTreeItemLabelFragmentDescription()
                        .labelExpression("aql:self.getTreeItemLabel()")
                        // no style specified => default one will be chosen
                        .build()).build();
    }

    private TreeItemLabelDescription createEntityTreeItemLabel() {
        return new TreeBuilders().newTreeItemLabelDescription()
                .name("entityFragment style")
                .preconditionExpression("aql:self.isEntityTreeItemElement()")
                .children(this.getEntityTreeItemLabelPrefix(), this.getDefaultLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelElementDescription getEntityTreeItemLabelPrefix() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression("aql:self.getEntityTreeItemLabelPrefix()")
                .style(this.getTextStyleByName(ViewOntologyPaletteFactory.BLUE_BOLD_TEXT_STYLE_NAME))
                .build();
    }

//    private TreeItemLabelElementDescription getEntityTreeItemLabelValue() {
//        return new TreeBuilders().newTreeItemLabelFragmentDescription()
//                .labelExpression("aql:aql:self.getLabel()")
//                .style(this.getTextStyleByName(ViewOntologyPaletteBuilder.NORMAL_TEXT_STYLE_NAME))
//                .build();
//    }

    private TextStyleDescription getTextStyleByName(String styleName) {
        return this.textStylePalette.getStyles().stream().filter(tsd -> tsd.getName().equals(styleName)).findFirst().orElse(null);
    }

    private List<TreeItemContextMenuEntry> createContextMenuEntries() {
        SingleClickTreeItemContextMenuEntry deleteEntity = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("Delete Entity Entry")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .labelExpression("Delete")
                .iconURLExpression("/customImages/delete.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.deleteEntity()")
                        .build())
                .build();
        SingleClickTreeItemContextMenuEntry createSubEntity = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("New Sub Entity")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .labelExpression("New Sub Entity")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createSubEntity('New Sub Entity')")
                        .build())
                .build();
        return List.of(deleteEntity, createSubEntity);
    }
}
