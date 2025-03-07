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

import java.util.List;

import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.TextStyleDescription;
import org.eclipse.sirius.components.view.TextStylePalette;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.tree.TreeBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.providers.DefaultColorProvider;
import org.eclipse.sirius.components.view.tree.SingleClickTreeItemContextMenuEntry;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.eclipse.sirius.components.view.tree.TreeItemContextMenuEntry;
import org.eclipse.sirius.components.view.tree.TreeItemLabelDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelElementDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelFragmentDescription;

/**
 * Builder of the Explorer tree description.
 *
 * @author lfasani
 */
public class ViewExplorerTreeDescriptionBuilder {

    public static final String ONTOLOGY_EXPLORER_DESCRIPTION_NAME = "Ontology Explorer";

    private static final String AQL_SELF_NAME = "aql:self.name";

    private static final String AQL_SELF = "aql:self";

    private static final int NB_LEVEL = 4;

    public static final String ENTITY_ENTITY = "entity::Entity";

    public static final String BLUE = "lightBlue 500";

    private static final String AQL_TRUE = "aql:true";

    private final DefaultColorProvider colorProvider;

    private final View view;

    private final ColorPalette colorPalette;

    private final TextStylePalette textStylePalette;

    public ViewExplorerTreeDescriptionBuilder(View view) {
        this.view = view;
        colorProvider = new DefaultColorProvider(view);
        colorPalette = view.getColorPalettes().get(0);
        textStylePalette = view.getTextStylePalettes().get(0);
    }

    public void addRepresentationDescription() {
        TreeDescription ontologyExplorerTreeDescription = this.createExplorerTreeDescription();

        view.getDescriptions().add(ontologyExplorerTreeDescription);
    }

    private TreeDescription createExplorerTreeDescription() {
        return new TreeBuilders().newTreeDescription()
                .name(ONTOLOGY_EXPLORER_DESCRIPTION_NAME)
                .childrenExpression("aql:self.getChildren(editingContext, expanded, activeFilterIds))")
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
                .style(this.getTextStyleByName(ViewOntologyPaletteBuilder.BLUE_BOLD_TEXT_STYLE_NAME))
                .build();
    }

//    private TreeItemLabelElementDescription getEntityTreeItemLabelValue() {
//        return new TreeBuilders().newTreeItemLabelFragmentDescription()
//                .labelExpression("aql:aql:self.getLabel()")
//                .style(this.getTextStyleByName(ViewOntologyPaletteBuilder.NORMAL_TEXT_STYLE_NAME))
//                .build();
//    }

    private TextStyleDescription getTextStyleByName(String styleName) {
        return textStylePalette.getStyles().stream().filter(tsd -> tsd.getName().equals(styleName)).findFirst().orElse(null);
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
                .name("Create Sub Entity")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .labelExpression("New Sub Entity")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createSubEntity('Sub Ontology')")
                        .build())
                .build();
        return List.of(deleteEntity, createSubEntity);
    }
}
