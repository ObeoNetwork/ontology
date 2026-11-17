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

import fr.obeo.ontology.services.representations.EntityJavaService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Reference;

/**
 * Java service for Diagram.
 *
 * @author lfasani
 */
public class DiagramJavaService {
    private final EntityJavaService entityJavaService;
    private final IObjectSearchService objectSearchService;
    private final IIdentityService identityService;

    public DiagramJavaService(EntityJavaService entityJavaService, IObjectSearchService objectSearchService, IIdentityService identityService) {
        this.entityJavaService = entityJavaService;
        this.objectSearchService = objectSearchService;
        this.identityService = identityService;
    }

    /**
     * Returns all semantic elements contained in diagram.
     */
    public List<Entity> getAllEntitiesInDiagram(IEditingContext editingContext, DiagramContext diagramContext) {
        return getCurrentEntityIdsInDiagram(diagramContext).stream()
                .flatMap(targetObjectId -> objectSearchService.getObject(editingContext, targetObjectId).stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .toList();
    }

    private Set<String> getCurrentEntityIdsInDiagram(DiagramContext diagramContext) {
        List<String> viewDeletionIds = diagramContext.viewDeletionRequests().stream()
                .map(viewDeletionRequest -> viewDeletionRequest.getElementId())
                .toList();
        Set<String> entityIds = diagramContext.getDiagram().getNodes().stream()
                .filter(node -> !viewDeletionIds.contains(node.getId()))
                .map(Node::getTargetObjectId)
                .collect(Collectors.toSet());

        entityIds.addAll(diagramContext.viewCreationRequests().stream()
                .map(ViewCreationRequest::getTargetObjectId)
                .toList());

        return entityIds;
    }

    /**
     * Returns references of all semantic elements contained in diagram.
     */
    public Set<Reference> getReferencesOfAllEntitiesInDiagram(IEditingContext editingContext, DiagramContext diagramContext) {
        return getAllEntitiesInDiagram(editingContext, diagramContext).stream()
                .flatMap(entity -> entity.getReferences().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Find and return, from the Reference source element, the lowest possible level entity among inherited entities represented as a node in the diagram.
     */
    public Object getInheritedReferenceEdgeSourceInDiagram(Reference reference, DiagramContext diagramContext) {
        Set<String> nodeTargetObjectIds = getCurrentEntityIdsInDiagram(diagramContext);
        if (reference.getContainingType() instanceof Entity entity) {
            Object foundEntity = searchEntityFromSubTypes(entity, nodeTargetObjectIds);
            // ignore direct reference
            if (!reference.getContainingType().equals(foundEntity)) {
                return foundEntity;
            }
        }
        return null;
    }

    private Object searchEntityFromSubTypes(Entity entity, Set<String> nodeTargetObjectIds) {
        if (nodeTargetObjectIds.contains(identityService.getId(entity))) {
            return entity;
        }

        for (Entity subEntity : entityJavaService.getSubEntities(entity)) {
            Object obj = searchEntityFromSubTypes(subEntity, nodeTargetObjectIds);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }
}
