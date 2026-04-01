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
package fr.obeo.ontology.services.representations.diagrams;

import fr.obeo.ontology.services.representations.providers.ViewOntologyPaletteFactory;

import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.NodePaletteBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.INodeDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DeleteTool;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;

/**
 * Abstract class for relations diagram node description providers.
 *
 * @author ntinsalhi
 */
public abstract class AbstractRelationsNodeDescriptionProvider extends AbstractDescriptionProvider implements INodeDescriptionProvider {
    public static final int DEFAULT_ENTITY_NODE_HEIGHT = 180;

    public static final int DEFAULT_ENTITY_NODE_WIDTH = 100;

    public static final String ATTRIBUTE_ITEM_NODE = "AttributeItemNode";

    public static final String ATTRIBUTE_CONTAINER_NODE = "AttributeContainerNode";

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    protected final ViewBuilders viewBuilderHelper = new ViewBuilders();

    protected NodePaletteBuilder createEntityNodePaletteBuilder() {
        return this.diagramBuilderHelper.newNodePalette()
                .labelEditTool(new DiagramBuilders().newLabelEditTool()
                        .name("Label Edit Tool")
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.defaultEditLabel(newLabel)")
                                .build())
                        .build())
                .deleteTool(this.deleteFromModelTool());
    }

    private DeleteTool deleteFromModelTool() {
        var changeContext = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.deleteEntity()")
                .build();

        var isOtherEntityIf = this.viewBuilderHelper.newIf()
                .conditionExpression("aql:self.canUseDeleteFromModelTool(diagramContext)")
                .children(changeContext)
                .build();

        return new DiagramBuilders().newDeleteTool()
                .body(isOtherEntityIf)
                .build();
    }

    protected InsideLabelDescription entityInsideLabelDescription() {
        var insideLabelStyle = this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.IF_CHILDREN)
                .withHeader(true)
                .build();

        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("aql:self.name")
                .style(insideLabelStyle)
                .position(InsideLabelPosition.TOP_CENTER)
                .textAlign(LabelTextAlign.CENTER)
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .build();
    }

    protected ConditionalNodeStyle mainEntityConditionalNodeStyle(IColorProvider colorProvider) {
        var entityLayoutChildrenNodesLayoutStrategy = this.diagramBuilderHelper
                .newListLayoutStrategyDescription()
                .topGapExpression("2")
                .build();

        RectangularNodeStyleDescription mainEntityRectangularNodeStyleDescription = diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(colorProvider.getColor(ViewOntologyPaletteFactory.BLUE_GREY))
                .borderSize(2)
                .childrenLayoutStrategy(entityLayoutChildrenNodesLayoutStrategy)
                .build();

        return this.diagramBuilderHelper
                .newConditionalNodeStyle()
                .condition("aql:self.isMainEntity(diagramContext)")
                .style(mainEntityRectangularNodeStyleDescription)
                .build();
    }

    protected RectangularNodeStyleDescription entityRectangularNodeStyleDescription(IColorProvider colorProvider) {
        var entityLayoutChildrenNodesLayoutStrategy = this.diagramBuilderHelper
                .newListLayoutStrategyDescription()
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(colorProvider.getColor(BACKGROUND_COLOR))
                .borderSize(1)
                .childrenLayoutStrategy(entityLayoutChildrenNodesLayoutStrategy)
                .build();
    }

    private NodeDescription attributeItemNodeDescription() {
        var attributeInsideLabelStyle = new DiagramBuilders()
                .newInsideLabelStyle()
                .fontSize(12)
                .showIconExpression("aql:true")
                .borderSize(0)
                .build();

        var attributeItemLabel = this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("aql:self.getAttributeItemLabel()")
                .textAlign(LabelTextAlign.LEFT)
                .position(InsideLabelPosition.MIDDLE_LEFT)
                .style(attributeInsideLabelStyle)
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .build();

        var nodeStyle = this.diagramBuilderHelper.newIconLabelNodeStyleDescription().build();

        return this.diagramBuilderHelper.newNodeDescription()
                .name(ATTRIBUTE_ITEM_NODE)
                .domainType(ENVIRONMENT_ATTRIBUTE)
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .semanticCandidatesExpression("aql:self.getEntityAttributes()")
                .insideLabel(attributeItemLabel)
                .style(nodeStyle)
                .build();
    }

    protected NodeDescription attributesContainerNodeDescription(IColorProvider colorProvider) {
        var attributeItemNodeDescription = this.attributeItemNodeDescription();

        var containerInsideLabelStyle = new DiagramBuilders()
                .newInsideLabelStyle()
                .fontSize(13)
                .borderSize(0)
                .italic(true)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .withHeader(true)
                .showIconExpression("aql:false")
                .build();

        var containerItemLabel = this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("Attributes")
                .textAlign(LabelTextAlign.CENTER)
                .position(InsideLabelPosition.TOP_CENTER)
                .style(containerInsideLabelStyle)
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .build();

        var containerChildrenNodesLayoutStrategy = this.diagramBuilderHelper
                .newListLayoutStrategyDescription()
                .bottomGapExpression("10")
                .build();

        var nodeStyle = this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(colorProvider.getColor(ViewOntologyPaletteFactory.COLOR_TRANSPARENT))
                .childrenLayoutStrategy(containerChildrenNodesLayoutStrategy)
                .build();

        return this.diagramBuilderHelper.newNodeDescription()
                .name(ATTRIBUTE_CONTAINER_NODE)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression("aql:self")
                .childrenDescriptions(attributeItemNodeDescription)
                .insideLabel(containerItemLabel)
                .defaultWidthExpression(String.valueOf(DEFAULT_ENTITY_NODE_HEIGHT))
                .defaultHeightExpression(String.valueOf(DEFAULT_ENTITY_NODE_WIDTH))
                .style(nodeStyle)
                .build();
    }
}
