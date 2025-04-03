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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.providers.DefaultColorProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.FreeFormLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;

/**
 * Builder of the "Diagram" view description.
 *
 * @author lfasani
 */
public class ViewDiagramDescriptionBuilder {

    public static final String ONTOLOGY_DIAGRAM_NAME = "Ontology Diagram";

    public static final String ENTITY_ENTITY = "entity::Entity";

    public static final String BLUE = "lightBlue 500";

    public static final String TRANSPARENT = "transparent";

    public static final String ZERO = "0";

    public static final String WHITE = "white";

    private static final String AQL_SELF_NAME = "aql:self.name";

    private static final String AQL_SELF = "aql:self";

    private static final int NB_LEVEL = 3;

    private final DefaultColorProvider colorProvider;

    private final View view;

    public ViewDiagramDescriptionBuilder(View view) {
        this.view = view;
        this.colorProvider = new DefaultColorProvider(view);
    }

    public void addRepresentationDescription() {
        DiagramDescription ontologyDiagramDescription = this.createOntologyDiagramDescription();

        this.view.getDescriptions().add(ontologyDiagramDescription);
    }

    private DiagramDescription createOntologyDiagramDescription() {
        List<NodeDescription> nodeDescriptions = new ArrayList<>();
        NodeDescription coreEntityNodeDescription = this.createCoreEntityNodeDescription();
        nodeDescriptions.add(coreEntityNodeDescription);
        List<NodeDescription> levelContainerDescriptions = this.createLevelContainerDescriptions();
        nodeDescriptions.addAll(levelContainerDescriptions);

        EdgeDescription coreToLevel1Edge = this.createCoreToLevel1Edge(coreEntityNodeDescription, levelContainerDescriptions.get(0));
        List<EdgeDescription> levelToNextLevelEdges = this.createLevelToNextLevelEdges(levelContainerDescriptions);
        List<EdgeDescription> edgeDescriptions = new ArrayList<>();
        edgeDescriptions.add(coreToLevel1Edge);
        edgeDescriptions.addAll(levelToNextLevelEdges);

        this.addEdgeTools(coreEntityNodeDescription, levelContainerDescriptions);

        return new DiagramBuilders().newDiagramDescription()
                .name(ONTOLOGY_DIAGRAM_NAME)
                .domainType(ENTITY_ENTITY)
                .titleExpression(AQL_SELF_NAME)
                .preconditionExpression("aql:self.supertype==null")
                .nodeDescriptions(nodeDescriptions.toArray(new NodeDescription[] {}))
                .edgeDescriptions(edgeDescriptions.toArray(new EdgeDescription[0]))
                .arrangeLayoutDirection(ArrangeLayoutDirection.DOWN)
                .autoLayout(true)
                .build();
    }

    private void addEdgeTools(NodeDescription coreEntityNodeDescription, List<NodeDescription> levelContainerDescriptions) {
        this.addEdgeToolToPalette(coreEntityNodeDescription, levelContainerDescriptions.get(0));

        for (int level = 1; level < levelContainerDescriptions.size(); level++) {
            this.addEdgeToolToPalette(levelContainerDescriptions.get(level - 1).getChildrenDescriptions().get(0), levelContainerDescriptions.get(level));
        }
    }

    private void addEdgeToolToPalette(NodeDescription sourceNodeDescription, NodeDescription targetNodeDescription) {
        sourceNodeDescription.getPalette().getEdgeTools().add(
                new DiagramBuilders().newEdgeTool()
                        .targetElementDescriptions(targetNodeDescription)
                        .body(new ChangeContextBuilder()
                                .expression("aql:semanticEdgeSource.createSubEntity('New Entity')")
                                .build())
                        .build());
    }

    private EdgeDescription createCoreToLevel1Edge(NodeDescription coreEntityNodeDescription, NodeDescription levelContainerDescription) {
        EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                .color(this.colorProvider.getColor(BLUE))
                .build();
        return new DiagramBuilders().newEdgeDescription()
                .name("CoreToLevel1Edge")
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(AQL_SELF)
                .sourceDescriptions(coreEntityNodeDescription.getBorderNodesDescriptions().get(0))
                .targetDescriptions(levelContainerDescription.getChildrenDescriptions().get(0).getBorderNodesDescriptions().get(0))
                .sourceExpression(AQL_SELF)
                .targetExpression("aql:self.getSubEntities()")
                .style(edgeStyle)
                .centerLabelExpression("")
                .build();
    }

    NodePalette createEntityNodePalette(int level) {
        return new DiagramBuilders().newNodePalette()
                .labelEditTool(new DiagramBuilders().newLabelEditTool()
                        .name("Label Edit Tool " + level)
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.defaultEditLabel(newLabel)")
                                .build())
                        .build())
                .deleteTool(new DiagramBuilders().newDeleteTool()
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.deleteEntity()")
                                .build())
                        .build())
                .nodeTools(new DiagramBuilders().newNodeTool()
                        .name("New Sub Entity")
                        .body(new ChangeContextBuilder()
                                .expression("aql:self.createSubEntity('New Sub Entity')")
                                .build())
                        .build())
                .build();
    }

    private List<EdgeDescription> createLevelToNextLevelEdges(List<NodeDescription> levelContainerDescriptions) {
        List<EdgeDescription> edgeDescriptions = new ArrayList<>();
        for (int level = 0; level < levelContainerDescriptions.size() - 1; level++) {
            EdgeStyle edgeStyle = new DiagramBuilders().newEdgeStyle()
                    .color(this.colorProvider.getColor(BLUE))
                    .build();
            edgeDescriptions.add(new DiagramBuilders().newEdgeDescription()
                    .name(String.format("Level%sToLevel%sEdge", level + 1, level + 2))
                    .domainType(ENTITY_ENTITY)
                    .semanticCandidatesExpression(AQL_SELF)
                    .sourceDescriptions(levelContainerDescriptions.get(level).getChildrenDescriptions().get(0))
                    .targetDescriptions(levelContainerDescriptions.get(level + 1).getChildrenDescriptions().get(0).getBorderNodesDescriptions().get(0))
                    .sourceExpression(AQL_SELF)
                    .targetExpression("aql:self.getSubEntities()")
                    .style(edgeStyle)
                    .centerLabelExpression("")
                    .build());
        }
        return edgeDescriptions;
    }

    private NodeDescription createCoreEntityNodeDescription() {
        RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(WHITE))
                .borderColor(this.colorProvider.getColor(BLUE))
                .borderSize(3)
                .build();

        InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                .labelExpression(AQL_SELF_NAME)
                .style(new DiagramBuilders().newInsideLabelStyle().borderSize(0).build())
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name("CoreEntityNode")
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(AQL_SELF)
                .style(rectangularNodeStyleDescription)
                .borderNodesDescriptions(this.createBorderNodeDescription(0))
                .insideLabel(insideLabelDescription)
                .defaultWidthExpression(ZERO)
                .defaultHeightExpression(ZERO)
                .palette(this.createEntityNodePalette(0))
                .build();
    }

    private List<NodeDescription> createLevelContainerDescriptions() {
        List<NodeDescription> containerDescriptions = new ArrayList<>();
        for (int level = 1; level <= NB_LEVEL; level++) {
            FreeFormLayoutStrategyDescription freeFormLayoutStrategyDescription = new DiagramBuilders().newFreeFormLayoutStrategyDescription().build();

            RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                    .background(this.colorProvider.getColor(WHITE))
                    .borderSize(3)
                    .build();

            InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                    .labelExpression("Level " + level)
                    .style(new DiagramBuilders().newInsideLabelStyle()
                            .withHeader(true)
                            .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.ALWAYS)
                            .borderSize(0)
                            .build())
                    .build();

            NodeDescription containerNodeDescription = new DiagramBuilders().newNodeDescription()
                    .name("ContainerNodeLevel" + level)
                    .domainType(ENTITY_ENTITY)
                    .semanticCandidatesExpression(AQL_SELF)
                    .childrenLayoutStrategy(freeFormLayoutStrategyDescription)
                    .style(rectangularNodeStyleDescription)
                    .insideLabel(insideLabelDescription)
                    .childrenDescriptions(this.createEntityNodeDescription(level))
                    .build();
            containerDescriptions.add(containerNodeDescription);
        }
        return containerDescriptions;
    }

    private NodeDescription createEntityNodeDescription(int level) {
        RectangularNodeStyleDescription rectangularNodeStyleDescription = new DiagramBuilders().newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(WHITE))
                .borderColor(this.colorProvider.getColor(BLUE))
                .build();

        InsideLabelDescription insideLabelDescription = new DiagramBuilders().newInsideLabelDescription()
                .labelExpression(AQL_SELF_NAME)
                .style(new DiagramBuilders().newInsideLabelStyle().borderSize(0).build())
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name("EntityNodeLevel" + level)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(String.format("aql:self.getEntitiesOfLevel(%s)", level))
                .style(rectangularNodeStyleDescription)
                .borderNodesDescriptions(this.createBorderNodeDescription(level))
                .insideLabel(insideLabelDescription)
                .defaultWidthExpression(ZERO)
                .defaultHeightExpression(ZERO)
                .palette(this.createEntityNodePalette(level))
                .build();
    }

    private NodeDescription createBorderNodeDescription(int level) {
        ImageNodeStyleDescription imageNodeStyleDescription = new DiagramBuilders().newImageNodeStyleDescription()
                .shape("/customImages/blueRing.svg")
                .borderSize(0)
                .build();

        return new DiagramBuilders().newNodeDescription()
                .name("BorderNodeLevel" + level)
                .domainType(ENTITY_ENTITY)
                .semanticCandidatesExpression(AQL_SELF)
                .style(imageNodeStyleDescription)
                .defaultHeightExpression("20")
                .defaultWidthExpression("20")
                .keepAspectRatio(true)
                .userResizable(UserResizableDirection.NONE)
                .build();
    }
}
