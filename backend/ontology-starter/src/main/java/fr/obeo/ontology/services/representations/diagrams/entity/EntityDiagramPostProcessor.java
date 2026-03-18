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
package fr.obeo.ontology.services.representations.diagrams.entity;

import fr.obeo.ontology.services.representations.diagrams.entity.nodes.AbstractNodeDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewDiagramDescriptionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramPostProcessor;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.layoutdata.HandleLayoutData;
import org.eclipse.sirius.components.diagrams.layoutdata.HandleType;
import org.eclipse.sirius.components.diagrams.layoutdata.NodeLayoutData;
import org.eclipse.sirius.components.diagrams.layoutdata.Position;
import org.eclipse.sirius.components.diagrams.layoutdata.Size;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.springframework.stereotype.Service;

/**
 * This service allows to customize the entity digram layout.
 *
 * @author lfasani
 */
@Service
public class EntityDiagramPostProcessor implements IDiagramPostProcessor {

    private static final int CONTAINER_GAP = 30;

    public static final int NODE_XPOSITION_IN_CONTAINER = 25;

    public static final int MINIMAL_NODE_YPOSITION_IN_CONTAINER = 50;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private Map<String, NodeLayoutData> idToNodeLayoutDataMap;

    public EntityDiagramPostProcessor(IRepresentationDescriptionSearchService representationDescriptionSearchService) {
        this.representationDescriptionSearchService = representationDescriptionSearchService;
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, DiagramContext diagramContext) {
        boolean canHandle = this.representationDescriptionSearchService.findById(editingContext, diagramContext.getDiagram().getDescriptionId())
                .map(IRepresentationDescription::getLabel)
                .filter(ViewDiagramDescriptionProvider.ONTOLOGY_DIAGRAM_NAME::equals)
                .isPresent();
        return canHandle;
    }

    @Override
    public Optional<Diagram> postProcess(IEditingContext editingContext, DiagramContext diagramContext) {
        return Optional.ofNullable(diagramContext.getDiagram())
                .map(this::updateLayout);
    }

    private Diagram updateLayout(Diagram diagram) {
        this.idToNodeLayoutDataMap = diagram.getLayoutData().nodeLayoutData();

        diagram = this.updateDiagramWithPinnedNodes(diagram);
        this.updateContainerContentLayout(diagram);
        this.updateContainerLayout(diagram);
        this.updateEdges(diagram);

        return diagram;
    }

    private void updateEdges(Diagram diagram) {
        Map<NodeLayoutData, List<String>> sourceNodeLayoutDataToEdgesId = new LinkedHashMap<>();
        Map<NodeLayoutData, List<String>> targetNodeLayoutDataToEdgesId = new LinkedHashMap<>();
        diagram.getEdges().forEach(edge -> {
            String edgeId = edge.getId();
            String sourceNodeId = edge.getSourceId();
            String targetNodeId = edge.getTargetId();
            NodeLayoutData sourceNodeLayoutData = this.idToNodeLayoutDataMap.get(sourceNodeId);
            NodeLayoutData targetNodeLayoutData = this.idToNodeLayoutDataMap.get(targetNodeId);
            if (sourceNodeLayoutData != null) {
                sourceNodeLayoutDataToEdgesId.computeIfAbsent(sourceNodeLayoutData, key -> new ArrayList<>())
                        .add(edgeId);
            }

            if (targetNodeLayoutData != null) {
                targetNodeLayoutDataToEdgesId.computeIfAbsent(targetNodeLayoutData, key -> new ArrayList<>())
                        .add(edgeId);
            }
        });

        String coreEntityBorderNodeId = diagram.getNodes().get(0).getBorderNodes().get(0).getId();
        Predicate<NodeLayoutData> isCoreEntityBorderNode = (NodeLayoutData nodeLayoutData) -> nodeLayoutData.id().equals(coreEntityBorderNodeId);

        sourceNodeLayoutDataToEdgesId.forEach((nodeLayoutData, edgeIds) -> {
            List<HandleLayoutData> handleLayoutDatas = new ArrayList<>();
            for (String edgeId : edgeIds) {
                double height = Optional.of(10.)
                        .filter(h -> isCoreEntityBorderNode.test(nodeLayoutData))
                        .orElse(20.);
                handleLayoutDatas.add(new HandleLayoutData(edgeId, new Position(0, height - 2), "right", HandleType.source));
            }
            this.idToNodeLayoutDataMap.put(nodeLayoutData.id(),
                    new NodeLayoutData(nodeLayoutData.id(), nodeLayoutData.position(), nodeLayoutData.size(), nodeLayoutData.resizedByUser(),
                            nodeLayoutData.movedByUser(), handleLayoutDatas, nodeLayoutData.minComputedSize()));
        });

        targetNodeLayoutDataToEdgesId.forEach((nodeLayoutData, edgeIds) -> {
            List<HandleLayoutData> handleLayoutDatas = new ArrayList<>();
            for (String edgeId : edgeIds) {
                handleLayoutDatas.add(new HandleLayoutData(edgeId, new Position(0, 8), "left", HandleType.target));
            }
            this.idToNodeLayoutDataMap.put(nodeLayoutData.id(),
                    new NodeLayoutData(nodeLayoutData.id(), nodeLayoutData.position(), nodeLayoutData.size(), nodeLayoutData.resizedByUser(),
                            nodeLayoutData.movedByUser(), handleLayoutDatas, nodeLayoutData.minComputedSize()));
        });
    }

    private void updateContainerLayout(Diagram diagram) {
        List<String> nodeIds = diagram.getNodes().stream()
                .map(Node::getId)
                .toList();

        for (int level = 1; level <= 3; level++) {
            Node node = diagram.getNodes().get(level);
            String nodeId = node.getId();
            NodeLayoutData nodeLayoutData = this.idToNodeLayoutDataMap.get(nodeId);

            if (nodeLayoutData != null) {
                double x = 0;
                for (int i = 0; i < level; i++) {
                    x = x + this.idToNodeLayoutDataMap.get(nodeIds.get(i)).size().width() + CONTAINER_GAP;
                }
                Position position = new Position(x, 0);

                double height = node.getChildNodes().stream()
                        .map(subNode -> this.idToNodeLayoutDataMap.get(subNode.getId()).position())
                        .mapToDouble(Position::y)
                        .max()
                        .orElse(MINIMAL_NODE_YPOSITION_IN_CONTAINER) + AbstractNodeDescriptionProvider.DEFAULT_NODE_HEIGHT + 20;

                // We can't let react flow calculate the width.
                // A typical case, when the user drag an entity from level n to level n-1, the target container is resized
                // to include the dragged entity node as if it was displayed where it was dropped thus practically doubling the width in some case
                double width = node.getChildNodes().stream()
                        .map(subNode -> this.idToNodeLayoutDataMap.get(subNode.getId()).size())
                        // 32 is the empiric value that corresponds to the one computed by react flow
                        // Using this value avoids the blinks between the moment react flow updates the diagram and the moment when the front get the refreshed diagram
                        .mapToDouble(size -> size.width() + 32)
                        .max()
                        .orElse(300);
                Size size = new Size(width, height);

                this.idToNodeLayoutDataMap.put(nodeId,
                        new NodeLayoutData(nodeLayoutData.id(), position, size, nodeLayoutData.resizedByUser(), nodeLayoutData.movedByUser(), nodeLayoutData.handleLayoutData(),
                                new Size(0, 0)));
            }
        }
    }

    private void updateContainerContentLayout(Diagram diagram) {
        Map<String, Node> idToNode = new LinkedHashMap<>();
        Map<Node, Node> borderNodeToOwningNode = new LinkedHashMap<>();
        Map<Integer, List<Node>> levelToNodesToReorder = new LinkedHashMap<>();
        this.initializeDataForDiagram(diagram, idToNode, borderNodeToOwningNode);

        // -----------------------------------------
        // update nodes position in level containers
        Map<Node, List<Node>> nodeToSubNodes = new LinkedHashMap<>();
        for (int level = 0; level <= 3; level++) {
            if (level > 0) {
                List<Node> nodesToReorder = levelToNodesToReorder.computeIfAbsent(level, k -> new ArrayList<>());
                Optional.ofNullable(levelToNodesToReorder.get(level - 1)).stream()
                        .flatMap(Collection::stream)
                        .forEach(parentNode -> {
                            List<Node> subNodes = diagram.getEdges().stream()
                                    .filter(edge -> {
                                        return edge.getSourceId().equals(parentNode.getId()) // parent node if of level > 1
                                                || edge.getSourceId().equals(parentNode.getBorderNodes().get(0).getId()); // parent node is core entity
                                    })
                                    .map(edge -> borderNodeToOwningNode.get(idToNode.get(edge.getTargetId())))
                                    .toList();
                            nodeToSubNodes.computeIfAbsent(parentNode, k -> new ArrayList<>())
                                    .addAll(subNodes);
                            nodesToReorder.addAll(subNodes);
                        });
            } else if (level == 0) {
                levelToNodesToReorder.put(level, List.of(diagram.getNodes().get(0)));
            }
        }

        Map<Integer, Integer> levelToCurrentMaxYPosition = new LinkedHashMap<>();
        for (int level = 0; level <= 3; level++) {
            levelToCurrentMaxYPosition.put(level, MINIMAL_NODE_YPOSITION_IN_CONTAINER);
        }
        this.updateNodePosition(diagram.getNodes().get(0), nodeToSubNodes, 0, levelToCurrentMaxYPosition);

        // ----------------------------------
        // update border nodes position
        List<Node> borderNodes = borderNodeToOwningNode.keySet().stream().toList();
        for (int i = 0; i < borderNodes.size(); i++) {
            String borderNodeId = borderNodes.get(i).getId();
            double x = -AbstractNodeDescriptionProvider.BORDER_NODE_SIZE;
            NodeLayoutData borderNodeLayoutData = this.idToNodeLayoutDataMap.get(borderNodeId);
            if (borderNodeLayoutData != null) {
                if (i == 0) {
                    x = this.idToNodeLayoutDataMap.get(borderNodeToOwningNode.get(borderNodes.get(i)).getId()).size().width() - 5;
                }
                double y = (AbstractNodeDescriptionProvider.DEFAULT_NODE_HEIGHT - AbstractNodeDescriptionProvider.BORDER_NODE_SIZE) / 2.;
                this.idToNodeLayoutDataMap.put(borderNodeId, new NodeLayoutData(borderNodeId, new Position(x, y), borderNodeLayoutData.size(), false, true, List.of(), new Size(0, 0)));
            }
        }
    }

    private void updateNodePosition(Node node, Map<Node, List<Node>> nodeToSubNodes, Integer currentLevel, Map<Integer, Integer> levelToCurrentMaxYPosition) {
        List<Node> subNodes = nodeToSubNodes.get(node);
        if (subNodes != null) {
//            // First we need to keep the order based on the render algorithm which is based on the semantic order
//            // It is necessary to reorder node of same super type and hence having a deterministic order
//            Collections.sort(subNodes, (node1, node2) -> {
//                NodeLayoutData layoutData1 = this.idToNodeLayoutDataMap.get(node1.getId());
//                NodeLayoutData layoutData2 = this.idToNodeLayoutDataMap.get(node2.getId());
//                if (layoutData1 != null && layoutData2 != null) {
//                    double yNode1 = layoutData1.position().y();
//                    double yNode2 = layoutData2.position().y();
//                    return Integer.valueOf((int) Math.round(yNode1)).compareTo(Integer.valueOf((int) Math.round(yNode2)));
//                }
//                return 0;
//            });
            subNodes.forEach(subNode -> this.updateNodePosition(subNode, nodeToSubNodes, currentLevel + 1, levelToCurrentMaxYPosition));
        }

        int nbChildren = subNodes != null ? subNodes.size() : 0;

        Integer y;
        if (nbChildren > 0) {
            // If a node has children, it is positioned vertically at the center of the children bounding box
            double yFirstSubNode = this.idToNodeLayoutDataMap.get(subNodes.get(0).getId()).position().y();
            double yLastSubNode = this.idToNodeLayoutDataMap.get(subNodes.get(nbChildren - 1).getId()).position().y();
            int centerPosition = Math.toIntExact(Math.round((yFirstSubNode + yLastSubNode) / 2));
            // The current node may have previous brother enough so that it can't be positioned at the center of its children bounding box without overloading the previous brothers.
            // In that particular case, all its children must be shifted lower.
            y = Math.max(centerPosition, levelToCurrentMaxYPosition.get(currentLevel));
            if (centerPosition < y) {
                int yShift = Math.toIntExact(Math.round(y - centerPosition));
                this.shiftChildren(node, nodeToSubNodes, yShift);
                for (int level = currentLevel + 1; level <= 3; level++) {
                    levelToCurrentMaxYPosition.put(level, levelToCurrentMaxYPosition.get(level) + yShift);
                }
            }
        } else {
            y = levelToCurrentMaxYPosition.get(currentLevel);
        }
        String nodeId = node.getId();
        NodeLayoutData layoutData = this.idToNodeLayoutDataMap.get(nodeId);
        Size size = Optional.ofNullable(layoutData).map(NodeLayoutData::size).orElse(new Size(10, 10));
        this.idToNodeLayoutDataMap.put(nodeId,
                new NodeLayoutData(nodeId, new Position(currentLevel == 0 ? 0 : NODE_XPOSITION_IN_CONTAINER, y), size, false, true,
                        Optional.ofNullable(layoutData).map(NodeLayoutData::handleLayoutData).orElseGet(List::of),
                        new Size(0, 0)));

        levelToCurrentMaxYPosition.put(currentLevel, y + AbstractNodeDescriptionProvider.DEFAULT_NODE_HEIGHT + CONTAINER_GAP);

    }

    private void shiftChildren(Node node, Map<Node, List<Node>> nodeToSubNodes, int yShift) {
        if (nodeToSubNodes.get(node) != null) {
            for (Node subNode : nodeToSubNodes.get(node)) {
                this.shiftChildren(subNode, nodeToSubNodes, yShift);
                String nodeId = subNode.getId();
                NodeLayoutData nodeLayoutData = this.idToNodeLayoutDataMap.get(nodeId);
                Size size = Optional.ofNullable(nodeLayoutData).map(NodeLayoutData::size).orElse(new Size(10, 10));
                this.idToNodeLayoutDataMap.put(nodeId,
                        new NodeLayoutData(nodeId, new Position(NODE_XPOSITION_IN_CONTAINER, nodeLayoutData.position().y() + yShift), size, false, true, List.of(), new Size(0, 0)));
            }
        }
    }

    private void initializeDataForDiagram(Diagram diagram, Map<String, Node> idToNode, Map<Node, Node> borderNodeToOwningNode) {
        diagram.getNodes().forEach(node -> {
            idToNode.put(node.getId(), node);
            this.initializeDataForNodes(node, idToNode, borderNodeToOwningNode);
        });
    }

    private void initializeDataForNodes(Node node, Map<String, Node> idToNode, Map<Node, Node> borderNodeToOwningNode) {
        node.getChildNodes().forEach(childNode -> {
            idToNode.put(node.getId(), node);
            this.initializeDataForNodes(childNode, idToNode, borderNodeToOwningNode);
        });
        node.getBorderNodes().forEach(borderNode -> {
            idToNode.put(borderNode.getId(), borderNode);
            borderNodeToOwningNode.put(borderNode, node);
            this.initializeDataForNodes(borderNode, idToNode, borderNodeToOwningNode);
        });
    }

    private Diagram updateDiagramWithPinnedNodes(Diagram diagram) {
        List<Node> nodes = diagram.getNodes().stream()
                .map(n -> this.createPinnedNode(n, true))
                .toList();

        return Diagram.newDiagram(diagram)
                .nodes(nodes)
                .build();
    }

    private Node createPinnedNode(Node node, boolean pin) {
        // Sub nodes must be left unpinned to authorize the drop
        List<Node> childNodes = node.getChildNodes().stream()
                .map(n -> this.createPinnedNode(n, false))
                .toList();

        List<Node> borderNodes = node.getBorderNodes().stream()
                .map(n -> this.createPinnedNode(n, true))
                .toList();

        return Node.newNode(node)
                .childNodes(childNodes)
                .borderNodes(borderNodes)
                .pinned(pin)
                .build();
    }
}
