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

import fr.obeo.ontology.services.representations.diagrams.relationsoverview.nodes.EntityUnsynchronizedNodeDescriptionProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.interpreter.SimpleCrossReferenceProvider;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.web.services.FeedbackMessageService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.Reference;
import org.obeonetwork.dsl.environment.StructuredType;
import org.obeonetwork.dsl.environment.TypesDefinition;
import org.springframework.stereotype.Service;

/**
 * Java Service for the Entity view.
 *
 * @author lfasani
 */
@Service
public class EntityJavaService {

    private final IEditService editService;

    private final int NB_LEVEL = 3;

    private final IFeedbackMessageService feedbackMessageService;

    private final IIdentityService identityService;

    public EntityJavaService(IEditService editService, FeedbackMessageService feedbackMessageService, IIdentityService identityService) {
        this.editService = Objects.requireNonNull(editService);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.identityService = Objects.requireNonNull(identityService);
    }

    public boolean canCreateEntityDiagram(Entity entity) {
        // TODO : condition de création
        return true;
    }

    public boolean isDropableInThisContainer(EObject eObject) {
        return true;
    }

    public void dropEntity(Entity droppedElement, Node targetNode, DiagramContext diagramContext) {
        List<String> nodeIds = diagramContext.diagram().getNodes().stream()
                .map(Node::getId)
                .toList();

        int targetLevel = nodeIds.indexOf(targetNode.getId());
        int sourceLevel = this.getEntityLevel(droppedElement);

        StructuredType superType = droppedElement.getSupertype();
        if (targetLevel == (sourceLevel + 1) && superType instanceof Entity superEntity) {
            if (this.isDropAuthorized(droppedElement, sourceLevel)) {
                // We create an intermediary entity between the super entity and the dropped entity
                Entity newEntity = this.createSubEntity(superEntity, "New entity");
                droppedElement.setSupertype(newEntity);
            }

        } else if (targetLevel == (sourceLevel - 1) && superType instanceof Entity superEntity) {
            // the super entity is now the super entity of the super entity
            droppedElement.setSupertype(superType.getSupertype());
        }
    }

    /**
     * Send a message is the drop is forbidden. The drop is authorized if the "moved" tree of entities results in entities with level NB_LEVEL at maximum.
     */
    private boolean isDropAuthorized(Entity entity, int currentLevel) {
        int subEntitiesDepth = 0;
        List<Entity> subEntities = this.getSubEntities(entity);
        while (!subEntities.isEmpty()) {
            subEntitiesDepth = subEntitiesDepth + 1;
            if (subEntitiesDepth + currentLevel >= this.NB_LEVEL) {
                break;
            }
            subEntities = subEntities.stream()
                    .flatMap(subEntity -> this.getSubEntities(subEntity).stream())
                    .toList();
        }

        if (subEntitiesDepth + currentLevel >= this.NB_LEVEL) {
            var message = "The operation is not authorized because the maximum depth of " + NB_LEVEL + " levels would be exceeded";
            this.feedbackMessageService.addFeedbackMessage(new Message(message, MessageLevel.INFO));
            return false;
        }
        return true;
    }

    public List<Entity> getEntitiesOfLevel(Entity coreObject, int level) {
        List<Entity> entities = Optional.of(coreObject)
                .map(EObject::eContainer)
                .filter(Namespace.class::isInstance)
                .map(Namespace.class::cast)
                .stream()
                .flatMap(namespace -> namespace.getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> this.isEntityOfLevelForCoreEntity(coreObject, entity, level))
                .toList();

        if (level > 1) {
            entities = this.orderEntities(coreObject, level, entities);
        }
        return entities;
    }

    private List<Entity> orderEntities(Entity coreObject, int level, List<Entity> entities) {
        List<Entity> entitiesOfLowerLevel = this.getEntitiesOfLevel(coreObject, level - 1);
        return entities.stream()
                .sorted((e1, e2) -> {
                    int value = Integer.valueOf(entitiesOfLowerLevel.indexOf(e2.getSupertype())).compareTo(Integer.valueOf(entitiesOfLowerLevel.indexOf(e1.getSupertype())));
                    if (value == 0) {
                        List<Entity> allEntities = Optional.ofNullable(coreObject.eContainer())
                                .filter(Namespace.class::isInstance)
                                .stream()
                                .flatMap(namespace -> ((Namespace) namespace).getTypes().stream())
                                .filter(Entity.class::isInstance)
                                .map(Entity.class::cast)
                                .toList();
                        value = Integer.valueOf(allEntities.indexOf(e2)).compareTo(Integer.valueOf(allEntities.indexOf(e1)));
                    }
                    return value;
                })
                .toList();
    }

    boolean isEntityOfLevelForCoreEntity(Entity coreObject, StructuredType structuredType, int level) {
        boolean isSubEntityForCoreObject = false;
        int entityLevel = -1;
        StructuredType superType = structuredType;
        while (superType != null) {
            entityLevel += 1;
            isSubEntityForCoreObject = isSubEntityForCoreObject || superType.equals(coreObject);
            superType = superType.getSupertype();
        }
        return level == entityLevel && isSubEntityForCoreObject;
    }

    public List<Entity> getSubEntities(Entity coreObject) {
        return new SimpleCrossReferenceProvider().getInverseReferences(coreObject).stream()
                .filter(setting -> setting.getEStructuralFeature().equals(EnvironmentPackage.eINSTANCE.getStructuredType_Supertype()))
                .map(EStructuralFeature.Setting::getEObject)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> entity.eResource() != null)
                .toList();
    }

    public Entity createSubEntity(Entity entity, String name) {
        Entity subEntity = null;

        if (this.canCreateNewSubEntity(entity)) {
            subEntity = EntityFactory.eINSTANCE.createEntity();
            subEntity.setName(name);
            subEntity.setSupertype(entity);

            if (entity.eContainer() instanceof TypesDefinition typesDefinition) {
                typesDefinition.getTypes().add(subEntity);
            }

        } else {
            String message = "Cannot create sub-entity: maximum depth of " + NB_LEVEL + " levels reached";
            this.feedbackMessageService.addFeedbackMessage(new Message(message, MessageLevel.ERROR));
        }

        return subEntity;
    }

    public Entity deleteEntity(Entity entity) {
        this.editService.delete(entity);
        return entity;
    }

    public void deleteObject(Object object) {
        this.editService.delete(object);
    }

    public int getEntityLevel(Entity entity) {
        int level = 0;
        StructuredType supertype = entity.getSupertype();
        while (supertype != null) {
            level = level + 1;
            supertype = supertype.getSupertype();
        }

        return level;
    }

    public Annotation createComment(Entity entity, String title) {
        Annotation comment = EnvironmentFactory.eINSTANCE.createAnnotation();
        comment.setTitle(title);

        if (Objects.isNull(entity.getMetadatas())) {
            var metadatasContainer = EnvironmentFactory.eINSTANCE.createMetaDataContainer();
            entity.setMetadatas(metadatasContainer);
        }

        entity.getMetadatas().getMetadatas().add(comment);

        return comment;
    }

    public Attribute createAttribute(Entity entity, String name) {
        Attribute attribute = EnvironmentFactory.eINSTANCE.createAttribute();
        attribute.setName(name);
        attribute.setContainingType(entity);

        return attribute;
    }

    public Reference createReference(Entity entity, String name) {
        Reference reference = EnvironmentFactory.eINSTANCE.createReference();
        reference.setName(name);
        reference.setContainingType(entity);

        return reference;
    }

    public boolean isMainEntity(Object self, DiagramContext diagramContext) {
        if (!(self instanceof Entity entity)) {
            return false;
        }

        return Optional.ofNullable(diagramContext)
                .map(DiagramContext::diagram)
                .map(Diagram::getTargetObjectId)
                .map(targetObjectId -> targetObjectId.equals(this.identityService.getId(entity)))
                .orElse(false);
    }

    public boolean canUseDeleteFromModelTool(Object self, DiagramContext diagramContext) {
        var isMainEntity = this.isMainEntity(self, diagramContext);

        if (isMainEntity) {
            this.feedbackMessageService.addFeedbackMessage(new Message("This operation is not authorized on diagram main entity", MessageLevel.INFO));
        }

        return !isMainEntity;
    }

    public List<Entity> getRelationsSemanticCandidates(Entity entity) {
        return Stream.concat(
                Stream.of(entity),
                entity.getOwnedReferences().stream()
                        .map(Reference::getReferencedType)
                        .filter(Entity.class::isInstance)
                        .map(Entity.class::cast))
                .distinct()
                .toList();
    }

    public Entity getReferenceReferencedType(Reference reference) {
        if (reference.getReferencedType() instanceof Entity entity) {
            return entity;
        }
        return null;
    }

    public List<Reference> getOwnedReferences(Entity entity) {
        return entity.getOwnedReferences();
    }

    public Entity getReferenceContainingType(Reference reference) {
        if (reference.getContainingType() instanceof Entity entity) {
            return entity;
        }
        return null;
    }

    public List<Entity> getEntities(Entity entity) {
        return Optional.of(entity)
                .map(EObject::eContainer)
                .filter(Namespace.class::isInstance)
                .map(Namespace.class::cast).stream()
                .flatMap(namespace -> namespace.getTypes().stream())
                .filter(Entity.class::isInstance).map(Entity.class::cast)
                .toList();
    }

    public Object dropIntoDiagramFromExplorer(Object droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
                                              Map<NodeDescription, org.eclipse.sirius.components.diagrams.description.NodeDescription> convertedNodes) {
        if (Objects.isNull(selectedNode)) {
            var droppedElementId = this.identityService.getId(droppedElement);
            var diagramId = diagramContext.diagram().getId();

            var descriptionId = convertedNodes.entrySet()
                    .stream()
                    .filter(entry ->
                            entry.getKey()
                                    .getName()
                                    .equals(EntityUnsynchronizedNodeDescriptionProvider.ENTITY_UNSYNCHRONIZED_NODE_NAME)
                    )
                    .findFirst()
                    .map(entry -> entry.getValue().getId())
                    .orElse(null);

            if (droppedElementId != null && diagramId != null && descriptionId != null) {
                var viewCreationRequest = ViewCreationRequest.newViewCreationRequest()
                        .parentElementId(diagramId)
                        .targetObjectId(droppedElementId)
                        .descriptionId(descriptionId)
                        .containmentKind(NodeContainmentKind.CHILD_NODE)
                        .build();

                diagramContext.viewCreationRequests().add(viewCreationRequest);
            }
        }

        return droppedElement;
    }

    public List<Reference> getReferences(Entity entity) {
        return Optional.of(entity)
                .map(EObject::eContainer)
                .filter(Namespace.class::isInstance)
                .map(Namespace.class::cast).stream()
                .flatMap(namespace -> namespace.getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .flatMap(namespaceEntity -> namespaceEntity.getOwnedReferences().stream())
                .toList();
    }

    public List<Attribute> getEntityAttributes(Entity entity) {
        return entity.getOwnedAttributes();
    }

    public String getAttributeItemLabel(Attribute attribute) {
        var attributeType = attribute.getType() != null ? attribute.getType().getName() : "undefined";
        return attribute.getName() + " : " + attributeType;
    }

    public <T> Stream<T> objectsReferencingEntity(Entity coreObject, EStructuralFeature feature, Class<T> clazz) {
        return new SimpleCrossReferenceProvider().getInverseReferences(coreObject).stream()
                .filter(setting -> setting.getEStructuralFeature().equals(feature))
                .map(EStructuralFeature.Setting::getEObject)
                .filter(clazz::isInstance)
                .filter(e -> e.eResource() != null)
                .map(clazz::cast);
    }


    public <T> Optional<T> objectReferencingEntity(Entity coreObject, EStructuralFeature feature, Class<T> clazz) {
        return objectsReferencingEntity(coreObject, feature, clazz)
                .findFirst();
    }

    public boolean canCreateNewSubEntity(Entity entity) {
        return this.getEntityLevel(entity) < NB_LEVEL;
    }

}
