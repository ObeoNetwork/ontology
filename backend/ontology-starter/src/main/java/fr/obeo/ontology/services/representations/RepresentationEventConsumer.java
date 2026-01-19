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
package fr.obeo.ontology.services.representations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.layoutdata.EdgeLayoutData;
import org.eclipse.sirius.components.diagrams.layoutdata.HandleLayoutData;
import org.eclipse.sirius.components.diagrams.layoutdata.HandleType;
import org.eclipse.sirius.components.diagrams.layoutdata.NodeLayoutData;
import org.eclipse.sirius.components.diagrams.layoutdata.Position;
import org.eclipse.sirius.components.diagrams.layoutdata.Size;
import org.eclipse.sirius.components.representations.IRepresentation;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.events.RepresentationContentCreatedEvent;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.events.RepresentationContentUpdatedEvent;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.repositories.IRepresentationContentRepository;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationContentUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Used to pin node at diagram creation
 *
 * @author lfasani
 */
@Service
public class RepresentationEventConsumer {
    private static final int CONTAINER_WIDTH = 140;

    private static final int CONTAINER_GAP = 30;

    private final IRepresentationSearchService representationSearchService;

    private final IRepresentationContentRepository representationContentRepository;

    private final IRepresentationPersistenceService representationPersistenceService;

    private final IRepresentationContentUpdateService representationContentUpdateService;

    private Map<String, NodeLayoutData> idToNodeLayoutDataMap;

    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(RepresentationEventConsumer.class);

    public RepresentationEventConsumer(IRepresentationSearchService representationSearchService, IRepresentationContentRepository representationContentRepository,
            IRepresentationPersistenceService representationPersistenceService, IRepresentationContentUpdateService representationContentUpdateService, ObjectMapper objectMapper) {
        this.representationSearchService = representationSearchService;
        this.representationContentRepository = representationContentRepository;
        this.representationPersistenceService = representationPersistenceService;
        this.representationContentUpdateService = representationContentUpdateService;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener
    public void RepresentationContentCreatedEvent(RepresentationContentCreatedEvent domainEvent) {
        // TODO Check if this code is still useful
        this.toRepresentation(domainEvent.representationContent().getContent())
                .filter(Diagram.class::isInstance)
                .map(Diagram.class::cast)
                .map(this::updateLayout)
                .ifPresent(diagram -> {
                    var optionalRepresentationId = new UUIDParser().parse(diagram.getId());
                    if (optionalRepresentationId.isPresent()) {
                        this.representationContentUpdateService.updateContentByRepresentationId(domainEvent.causedBy(), optionalRepresentationId.get(), this.toString(diagram));
                    }
                });
    }

    @TransactionalEventListener
    public void onRepresentationContentUpdatedEvent(RepresentationContentUpdatedEvent updateEvent) {
        // TODO Check if this code is still useful
        // The change is not taken into account in DiagramEventProcessor.handleEvent
        // Consequently the layout update is done in preProcess override
        this.toRepresentation(updateEvent.representationContent().getContent())
                .filter(Diagram.class::isInstance)
                .map(Diagram.class::cast)
                .map(this::updateLayout)
                .ifPresent(diagram -> {
                    var optionalRepresentationId = new UUIDParser().parse(diagram.getId());
                    optionalRepresentationId.ifPresent(uuid -> this.representationContentUpdateService.updateContentByRepresentationId(updateEvent.causedBy(), uuid, this.toString(diagram)));
                });
    }

    private String toString(IRepresentation representation) {
        String content = "";
        try {
            content = this.objectMapper.writeValueAsString(representation);
        } catch (JsonProcessingException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }

        return content;
    }

    private Optional<IRepresentation> toRepresentation(String content) {
        Optional<IRepresentation> optionalRepresentation = Optional.empty();

        try {
            IRepresentation representation = this.objectMapper.readValue(content, IRepresentation.class);
            optionalRepresentation = Optional.of(representation);
        } catch (JsonProcessingException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }
        return optionalRepresentation;
    }

    private Diagram updateLayout(Diagram diagram) {
        this.idToNodeLayoutDataMap = diagram.getLayoutData().nodeLayoutData();

        diagram = this.updateDiagramWithPinnedNodes(diagram);
        // TODO Check if this code is still useful
        // Diagram Layout is supposed to be done in DiagramProcessor
        this.updateContainerLayout(diagram);
        this.updateContainerContentLayout(diagram);
        this.updateEdges(diagram);

        return diagram;
    }

    private void updateEdges(Diagram diagram) {
        Map<String, EdgeLayoutData> edgeIdLayoutDataMap = diagram.getLayoutData().edgeLayoutData();
        diagram.getEdges().forEach(edge -> {
            edgeIdLayoutDataMap.put(edge.getId(), new EdgeLayoutData(edge.getId(), List.of(), List.of()));
        });
    }

    private void updateContainerLayout(Diagram diagram) {
        List<String> nodeIds = diagram.getNodes().stream()
                .map(Node::getId)
                .toList();

        this.idToNodeLayoutDataMap.forEach((id, nodeLayoutData) -> {
            if (nodeIds.contains(id)) {
                int index = nodeIds.indexOf(id);
                double x = 0;
                for (int i = 0; i < index; i++) {
                    x = x + this.idToNodeLayoutDataMap.get(nodeIds.get(i)).size().width() + CONTAINER_GAP;
                }

                Position position = new Position(x, 0);
                this.idToNodeLayoutDataMap.put(id,
                        new NodeLayoutData(nodeLayoutData.id(), position, nodeLayoutData.size(), nodeLayoutData.resizedByUser(), nodeLayoutData.movedByUser(), nodeLayoutData.handleLayoutData(),
                                new Size(0, 0)));
            }
        });
    }

    private void updateContainerContentLayout(Diagram diagram) {
        Map<String, Node> idToNode = new LinkedHashMap<>();
        Map<Node, Node> borderNodeToOwningNode = new LinkedHashMap<>();
        Map<Integer, List<Node>> levelToNodes = new LinkedHashMap<>();
        this.initializeDataForDiagram(diagram, idToNode, borderNodeToOwningNode);

        // update nodes position in level containers
        Map<String, NodeLayoutData> nodeLayoutDataMap = diagram.getLayoutData().nodeLayoutData();
        for (int level = 1; level <= 4; level++) {
            List<Node> nodesToReorder = new ArrayList<>();
            if (level > 1) {
                Optional.ofNullable(levelToNodes.get(level - 1)).stream()
                        .flatMap(Collection::stream)
                        .forEach(parentNode -> {
                            nodesToReorder.addAll(diagram.getEdges().stream()
                                    .filter(edge -> edge.getSourceId().equals(parentNode.getId()))
                                    .map(edge -> borderNodeToOwningNode.get(idToNode.get(edge.getTargetId())))
                                    .toList());
                        });

                levelToNodes.put(level, nodesToReorder);
            } else if (level == 1) {
                nodesToReorder.addAll(diagram.getNodes().get(1).getChildNodes());
                levelToNodes.put(level, nodesToReorder);
            }

            // update layoutData of nodes
            for (int i = 0; i < nodesToReorder.size(); i++) {
                String nodeId = nodesToReorder.get(i).getId();
                Size size = Optional.ofNullable(nodeLayoutDataMap.get(nodeId)).map(NodeLayoutData::size).orElse(new Size(10, 10));
                nodeLayoutDataMap.put(nodeId, new NodeLayoutData(nodeId, new Position(20, 50 + i * 70), size, false, false, List.of(), new Size(0, 0)));
            }
        }

        //update border node position
        for (Node borderNode : borderNodeToOwningNode.keySet()) {
            boolean isCoreEntityBorderNode = borderNodeToOwningNode.get(borderNode).equals(diagram.getNodes().get(0));
            if (!isCoreEntityBorderNode) {
                Size size = Optional.ofNullable(nodeLayoutDataMap.get(borderNode.getId())).map(NodeLayoutData::size).orElse(new Size(20, 20));
                nodeLayoutDataMap.put(borderNode.getId(), new NodeLayoutData(borderNode.getId(), new Position(-15, 8), size, false, false, List.of(), new Size(0, 0)));
            }
        }

        //update source edge position
        diagram.getEdges().forEach(edge -> {
            String edgeId = edge.getId();
            String sourceNodeId = edge.getSourceId();
            NodeLayoutData nodeLayoutData = this.idToNodeLayoutDataMap.get(sourceNodeId);
            if (nodeLayoutData != null) {
                boolean isCoreEntityBorderNode = nodeLayoutData.id().equals(diagram.getNodes().get(0).getBorderNodes().get(0));
                if (!isCoreEntityBorderNode) {
                    List<HandleLayoutData> handleLayoutDatas = new ArrayList<>(nodeLayoutData.handleLayoutData());
                    // HARD CODED VALUE because of strange SW edge source positioning
                    handleLayoutDatas.add(new HandleLayoutData(edgeId, new Position(-6, 15), "right", HandleType.source));
                    this.idToNodeLayoutDataMap.put(sourceNodeId,
                            new NodeLayoutData(nodeLayoutData.id(), nodeLayoutData.position(), nodeLayoutData.size(), nodeLayoutData.resizedByUser(), nodeLayoutData.movedByUser(), handleLayoutDatas,
                                    new Size(0, 0)));
                }
            }
        });
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
                .map(this::createPinnedNode)
                .toList();

        return Diagram.newDiagram(diagram)
                .nodes(nodes)
                .build();
    }

    private Node createPinnedNode(Node node) {
        List<Node> childNodes = node.getChildNodes().stream()
                .map(this::createPinnedNode)
                .toList();

        List<Node> borderNodes = node.getBorderNodes().stream()
                .map(this::createPinnedNode)
                .toList();

        return Node.newNode(node)
                .childNodes(childNodes)
                .borderNodes(borderNodes)
                .pinned(true)
                .build();
    }
}
