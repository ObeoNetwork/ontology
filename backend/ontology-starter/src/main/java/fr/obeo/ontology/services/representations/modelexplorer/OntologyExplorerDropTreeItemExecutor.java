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
package fr.obeo.ontology.services.representations.modelexplorer;

import fr.obeo.ontology.services.representations.EntityJavaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerDropTreeItemExecutor;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.Namespace;
import org.springframework.stereotype.Service;

/**
 * Executes drop operations in the Ontology explorer tree
 *
 * @author ntinsalhi
 */
@Service
public class OntologyExplorerDropTreeItemExecutor implements IExplorerDropTreeItemExecutor {

    private final IObjectSearchService objectSearchService;

    private final EntityJavaService entityJavaService;

    public OntologyExplorerDropTreeItemExecutor(IObjectSearchService objectSearchService, EntityJavaService entityJavaService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.entityJavaService = Objects.requireNonNull(entityJavaService);
    }

    @Override
    public IStatus drop(IEditingContext editingContext, Tree tree, List<String> droppedElementIds, String targetElementId, int index) {
        boolean atLeastOneSuccessDrop = false;
        List<String> failingDropMessages = new ArrayList<>();
        var optionalTarget = this.objectSearchService.getObject(editingContext, targetElementId);

        if (optionalTarget.isPresent() && optionalTarget.get() instanceof Entity target) {

            List<Entity> droppedEntities = droppedElementIds.stream()
                    .map(droppedElementId -> this.isEntity(editingContext, droppedElementId))
                    .peek(optTarget -> {
                        if (optTarget.isEmpty()) {
                            failingDropMessages.add("Unable to move the element in selected target");
                        }
                    })
                    .flatMap(Optional::stream)
                    .filter(entity -> this.isDropAuthorized(target, entity, failingDropMessages))
                    .toList();

            if (!droppedEntities.isEmpty()) {
                atLeastOneSuccessDrop = true;
                droppedEntities.forEach(droppedEntity -> droppedEntity.setSupertype(target));
            }
        } else if (optionalTarget.isPresent() && optionalTarget.get() instanceof Namespace target) {
            List<Entity> droppedEntities = droppedElementIds.stream()
                    .map(droppedElementId -> this.isEntity(editingContext, droppedElementId))
                    .flatMap(Optional::stream)
                    .toList();

            if (!droppedEntities.isEmpty()) {
                atLeastOneSuccessDrop = true;
                droppedEntities.forEach(droppedEntity -> droppedEntity.setSupertype(null));
            }
        } else {
            failingDropMessages.add("Unable to move the element in selected target");
        }

        if (atLeastOneSuccessDrop) {
            return new Success(failingDropMessages.stream().map(m -> new Message(m, MessageLevel.WARNING)).toList());
        } else {
            return new Failure(failingDropMessages.stream().map(m -> new Message(m, MessageLevel.WARNING)).toList());
        }
    }

    private Optional<Entity> isEntity(IEditingContext editingContext, String targetElementId) {
        return objectSearchService.getObject(editingContext, targetElementId)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast);
    }

    private boolean isDropAuthorized(Entity target, Entity droppedElement, List<String> failingDropMessages) {
        boolean result = true;
        int targetLevel = this.entityJavaService.getEntityLevel(target);
        int droppedElementSubtreeDepth = this.getSubtreeDepth(droppedElement);

        if (droppedElement.getSupertype() == target) {
            result = false;
        } else if (this.isTargetDescendantOf(droppedElement, target)) {
            failingDropMessages.add("The target element cannot be a descendant of the moved element.");
            result = false;
        } else if (targetLevel + droppedElementSubtreeDepth > 3) {
            failingDropMessages.add("The target element cannot accept the dropped element because the maximum depth of "
                    + 3 + " levels would be exceeded.");
            result = false;
        }

        return result;
    }

    private int getSubtreeDepth(Entity entity) {
        List<Entity> subEntities = this.entityJavaService.getSubEntities(entity);
        if (subEntities.isEmpty()) {
            return 1;
        }

        return 1 + subEntities.stream()
                .mapToInt(this::getSubtreeDepth)
                .max()
                .orElse(0);
    }

    private boolean isTargetDescendantOf(Entity droppedElement, Entity target) {
        List<Entity> children = this.entityJavaService.getSubEntities(droppedElement);
        if (children.contains(target)) {
            return true;
        }

        return children.stream().anyMatch(child -> this.isTargetDescendantOf(child, target));
    }
}
