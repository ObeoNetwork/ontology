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

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.interpreter.SimpleCrossReferenceProvider;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.web.services.FeedbackMessageService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.environment.Namespace;
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

    private final FeedbackMessageService feedbackMessageService;

    public EntityJavaService(IEditService editService, FeedbackMessageService feedbackMessageService) {
        this.editService = editService;
        this.feedbackMessageService = feedbackMessageService;
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
            var message = "The operation is not authorized because the move operation includes entities of level " + this.NB_LEVEL;
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
                .toList();
    }

    public Entity createSubEntity(Entity entity, String name) {
        Entity subEntity = EntityFactory.eINSTANCE.createEntity();
        subEntity.setName(name);
        subEntity.setSupertype(entity);

        if (entity.eContainer() instanceof TypesDefinition typesDefinition) {
            typesDefinition.getTypes().add(subEntity);
        }
        return subEntity;
    }

    public Entity deleteEntity(Entity entity) {
        StructuredType supertype = entity.getSupertype();
        this.getSubEntities(entity).forEach(subEntity -> subEntity.setSupertype(supertype));
        this.editService.delete(entity);
        return entity;
    }

    public void deleteObjet(Object object) {
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

//    public List<Entity> getEntitiesHierarchy(Entity entity) {
//        ArrayList<Entity> entities = new ArrayList<>();
//        entities.add(entity);
//        Namespace level0 = (Namespace) entity.eContainer();
//        Namespace entityNamespace = level0.getOwnedNamespaces().stream().filter(n -> n.getName() != null && n.getName().equals(entity.getName())).findFirst().get();
//        if (entityNamespace != null) {
//            for (Namespace namespace : entityNamespace.getOwnedNamespaces()) {
//                entities.addAll(this.getEntities(namespace));
//            }
//        }
//        return entities;
//    }

//    public Entity getEntitySuperType(Entity entity) {
//        StructuredType superType = entity.getSupertype();
//        if (superType instanceof Entity) {
//            return (Entity) superType;
//        }
//        return null;
//    }
//
//    public boolean hideEntityBorder(Entity entity) {
//        String name = entity.getName();
//        return entity.getSupertype() == null;
//    }
//
//    public boolean hideEntityBorderLevel0(Entity entity, IDiagramContext diagramContext, IEditingContext editingContext) {
//        Entity rootEntity = null;
//        Diagram diagram = diagramContext.getDiagram();
//        String targetObjectId = diagram.getTargetObjectId();
//        Optional<Object> object = this.identityService.getObject(editingContext, targetObjectId);
//        if (object.isPresent()) {
//            rootEntity = (Entity) object.get();
//        }
//        ;
//        return entity.getSupertype() == null && !entity.equals(rootEntity);
//    }

//    public Entity createEntity(Namespace namespace) {
//        Entity newEntity = EntityFactory.eINSTANCE.createEntity();
//        newEntity.setName("NewEntity");
//        namespace.getTypes().add(newEntity);
//        return newEntity;
//    }

//    public List<Entity> getEntityAndSiblings(Entity entity) {
//        ArrayList<Entity> entities = new ArrayList<>();
//        Namespace level0 = (Namespace) entity.eContainer();
//        entities.addAll(level0.getTypes().stream().filter(Entity.class::isInstance).map(Entity.class::cast).collect(Collectors.toList()));
//        return entities;
//    }
//
//    public EList<Reference> getReferences(Entity entity) {
//        return entity.getOwnedReferences();
//    }
//
//    public Entity getReferencedType(EObject reference) {
//        return (Entity) ((Reference) reference).getReferencedType();
//
//    }
//
//    public Entity getSource(EObject reference) {
//        return (Entity) reference.eContainer();
//
//    }
//
//    public Reference createUnknownReference(Entity entitySource, Entity entityTarget) {
//        Reference newReference = EnvironmentFactory.eINSTANCE.createReference();
//        entitySource.getOwnedReferences().add(newReference);
//        newReference.setReferencedType(entityTarget);
//        return newReference;
//    }

}
