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
import org.eclipse.sirius.components.view.emf.tree.ITreeIdProvider;
import org.eclipse.sirius.components.view.tree.SingleClickTreeItemContextMenuEntry;
import org.eclipse.sirius.components.view.tree.TreeDescription;
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

    private final ITreeIdProvider treeIdProvider;

    private TreeDescription treeDescription;

    public ViewExplorerTreeDescriptionProvider(ITreeIdProvider treeIdProvider) {
        this.treeIdProvider = treeIdProvider;
        this.textStylePalette = new ViewOntologyPaletteFactory().createTextStylePalette();
    }

    public String getRepresentationDescriptionId() {
        return this.treeIdProvider.getId(this.treeDescription);
    }

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        this.treeDescription = new TreeBuilders().newTreeDescription()
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
                .treeItemLabelDescriptions(this.createEntityLabel(), this.createDefaultStyle())
                .contextMenuEntries(this.createContextMenuEntries().toArray(new TreeItemContextMenuEntry[] {}))
                .build();

        return this.treeDescription;
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

    private TreeItemLabelDescription createEntityLabel() {
        return new TreeBuilders().newTreeItemLabelDescription()
                .name("entityFragment style")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .children(this.getEntityLabelPrefix(), this.getDefaultLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelElementDescription getEntityLabelPrefix() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression("aql:self.getEntityLabelPrefix()")
                .style(this.getTextStyleByName(ViewOntologyPaletteFactory.BLUE_BOLD_TEXT_STYLE_NAME))
                .build();
    }

    private TextStyleDescription getTextStyleByName(String styleName) {
        return this.textStylePalette.getStyles().stream().filter(tsd -> tsd.getName().equals(styleName)).findFirst().orElse(null);
    }

    private List<TreeItemContextMenuEntry> createContextMenuEntries() {
        // TODO It is currently not possible to hide default Rename et Delete menu entry
        // So we must not add our own contextual menu
/*        SingleClickTreeItemContextMenuEntry deleteOthers = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("Delete Entity Entry")
                .preconditionExpression("aql:self.isDeleteAuthorized()")
                .labelExpression("Delete")
                .iconURLExpression("/customImages/delete.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.deleteObject()")
                        .build())
                .build();*/

        SingleClickTreeItemContextMenuEntry createSubEntity = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("New Sub Entity")
                .preconditionExpression("aql:self.canCreateNewSubEntityExplorer()")
                .labelExpression("New Sub Entity")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createSubEntity('New Entity')")
                        .build())
                .build();

        SingleClickTreeItemContextMenuEntry createCoreEntity = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("New Sub Entity")
                .preconditionExpression("aql:self.oclIsKindOf(environment::Namespace)")
                .labelExpression("New Core Entity")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createCoreEntity('New Core Entity')")
                        .build())
                .build();

        SingleClickTreeItemContextMenuEntry createOrganizationObject = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("Create Object")
                .preconditionExpression("aql:selectedTreeItem.isCreateObjectAllowed()")
                .labelExpression("aql:selectedTreeItem.getCreateObjectLabel()")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:editingContext.createObject(selectedTreeItem)")
                        .build())
                .build();


        SingleClickTreeItemContextMenuEntry createComment = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("New Comment")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .labelExpression("New Comment")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createComment('New Comment')")
                        .build())
                .build();

        SingleClickTreeItemContextMenuEntry createAttribute = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("New Attribute")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .labelExpression("New Attribute")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createAttribute('New Attribute')")
                        .build())
                .build();

        SingleClickTreeItemContextMenuEntry createReference = new TreeBuilders().newSingleClickTreeItemContextMenuEntry()
                .name("New Reference")
                .preconditionExpression("aql:self.oclIsKindOf(entity::Entity)")
                .labelExpression("New Reference")
                .iconURLExpression("/customImages/create.svg")
                .body(new ChangeContextBuilder()
                        .expression("aql:self.createReference('New Reference')")
                        .build())
                .build();

        return List.of(createSubEntity, createCoreEntity, createOrganizationObject, createComment, createAttribute, createReference);
    }
}
