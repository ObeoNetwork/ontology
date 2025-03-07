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
package fr.obeo.ontology.services.representations;

import fr.obeo.ontology.services.representations.builders.ViewDiagramDescriptionBuilder;

import java.util.List;

import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.IInputPostProcessor;
import org.eclipse.sirius.components.collaborative.api.IInputPreProcessor;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramLayoutDataInput;
import org.eclipse.sirius.components.collaborative.diagrams.dto.LayoutDiagramInput;
import org.eclipse.sirius.components.collaborative.diagrams.dto.NodeLayoutDataInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.layoutdata.Position;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks;

/**
 * Used to override the LayoutDiagramInput so that the diagram Ontology is properly layouted.
 *
 * @author lfasani
 */
@Service
public class DiagramPostProcessor implements IInputPreProcessor, IInputPostProcessor {

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IRepresentationSearchService representationSearchService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationPersistenceService representationPersistenceService;

    private static final int CONTAINER_GAP = 40;

    public DiagramPostProcessor(IRepresentationMetadataSearchService representationMetadataSearchService, IRepresentationSearchService representationSearchService,
            IRepresentationDescriptionSearchService representationDescriptionSearchService,
            IRepresentationPersistenceService representationPersistenceService) {
        this.representationMetadataSearchService = representationMetadataSearchService;
        this.representationSearchService = representationSearchService;
        this.representationDescriptionSearchService = representationDescriptionSearchService;
        this.representationPersistenceService = representationPersistenceService;
    }

    private boolean canHandle(IEditingContext editingContext, Diagram diagram) {
        return representationDescriptionSearchService.findById(editingContext, diagram.getDescriptionId())
                .map(iRepresentationDescription -> iRepresentationDescription.getLabel().equals(ViewDiagramDescriptionBuilder.ONTOLOGY_DIAGRAM_NAME))
                .orElse(false);
    }

    @Override
    public IInput preProcess(IEditingContext editingContext, IInput input, Sinks.Many<ChangeDescription> changeDescriptionSink) {

//        if (input instanceof LayoutDiagramInput layoutDiagramInput) {
//            return representationSearchService.findById(editingContext, layoutDiagramInput.representationId(), Diagram.class)
//                    .filter(diagram -> this.canHandle(editingContext, diagram))
//                    .map(diagram -> {
//                        DiagramLayoutDataInput diagramLayoutData = createUpdatedDiagramLayoutData(editingContext, layoutDiagramInput, diagram);
//
//                        return (IInput) new LayoutDiagramInput(input.id(), editingContext.getId(), layoutDiagramInput.representationId(), diagramLayoutData);
//                    })
//                    .orElse(input);
//        }
        return input;
    }

    private DiagramLayoutDataInput createUpdatedDiagramLayoutData(IEditingContext editingContext, LayoutDiagramInput layoutDiagramInput, Diagram diagram) {

        List<String> nodeIds = diagram.getNodes().stream()
                .map(Node::getId)
                .toList();

        List<NodeLayoutDataInput> nodeLayoutDataInputs = layoutDiagramInput.diagramLayoutData().nodeLayoutData();
        List<NodeLayoutDataInput> orderedNodeLayoutDataInputs = nodeLayoutDataInputs.stream()
                .filter(nodeLayoutDataInput -> nodeIds.contains(nodeLayoutDataInput.id()))
                .sorted((nodeLayoutDataInput1, nodeLayoutDataInput2) -> ((Integer) nodeIds.indexOf(nodeLayoutDataInput1.id())).compareTo(nodeIds.indexOf(nodeLayoutDataInput2.id())))
                .toList();

        List<NodeLayoutDataInput> updatedNodeLayoutDataInputs = nodeLayoutDataInputs.stream()
                .map(nodeLayoutDataInput -> {
                    NodeLayoutDataInput newNodeLayoutDataInput = nodeLayoutDataInput;
                    if (orderedNodeLayoutDataInputs.contains(nodeLayoutDataInput)) {
                        int index = orderedNodeLayoutDataInputs.indexOf(nodeLayoutDataInput);
                        double x = 0;
                        for (int i = 0; i < index; i++) {
                            x = x + orderedNodeLayoutDataInputs.get(i).size().width() + CONTAINER_GAP;
                        }

                        Position position = new Position(x, 0);
                        newNodeLayoutDataInput = new NodeLayoutDataInput(nodeLayoutDataInput.id(), position, nodeLayoutDataInput.size(), nodeLayoutDataInput.resizedByUser());
                    }
                    return newNodeLayoutDataInput;
                })
                .toList();
        return new DiagramLayoutDataInput(updatedNodeLayoutDataInputs, layoutDiagramInput.diagramLayoutData().edgeLayoutData());
    }

    @Override
    public void postProcess(IEditingContext editingContext, IInput input, Sinks.Many<ChangeDescription> changeDescriptionSink) {
//        if (editingContext instanceof EditingContext siriusEditingContext && input instanceof CreateRepresentationInput layoutDiagramInput) {
//
//            Optional<Project> projectId = projectSearchService.findById(new UUIDParser().parse(editingContext.getId()).get());
//
//            Optional<Diagram> updateDiagram = new UUIDParser().parse(editingContext.getId())
//                    .map(projectUuid -> {
//                        AggregateReference<Project, UUID> objectStringAggregateReference = AggregateReference.to(projectUuid);
//                        return objectStringAggregateReference;
//                    })
//                    .map(projectStringAggregateReference -> this.representationMetadataSearchService.findAllMetadataByProjectAndTargetObjectId(projectStringAggregateReference,
//                            layoutDiagramInput.objectId()))
//                    .orElse(new ArrayList<>())
//                    .stream()
//                    .filter(representationMetadata -> representationMetadata.getTargetObjectId().equals(layoutDiagramInput.objectId()))
//                    .filter(representationMetadata -> representationMetadata.getDescriptionId().equals(layoutDiagramInput.representationDescriptionId()))
//                    .findFirst()
//                    .flatMap(representationMetadata -> representationSearchService.findById(editingContext, representationMetadata.getId().toString(), Diagram.class))
//                    .map(this::createUpdatedDiagram);
//
//            this.representationPersistenceService.save(layoutDiagramInput, editingContext, updateDiagram.get());
//        }
    }

//    private Diagram createUpdatedDiagram(Diagram diagram) {
//
//        List<String> nodeIds = new ArrayList<>();
//        List<Node> nodes = diagram.getNodes().stream()
//                .map(node -> {
//                    nodeIds.add(node.getId());
//                    return Node.newNode(node)
//                            .pinned(true)
//                            .build();
//                })
//                .toList();
//
//        return Diagram.newDiagram(diagram)
//                .nodes(nodes)
//                .build();
//    }
}
