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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IEditService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.StructuredType;
import org.obeonetwork.dsl.environment.TypesDefinition;

/**
 * Java Service for the Entity view.
 *
 * @author jmallet
 */
public class EntityJavaService {

    private final IIdentityService identityService;

    private final IObjectService objectService;

    private final IEditService editService;

    public EntityJavaService(IIdentityService identityService, IObjectService objectService, IEditService editService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.objectService = Objects.requireNonNull(objectService);
        this.editService = editService;
    }

    public boolean canCreateEntityDiagram(Entity entity) {
        // TODO : condition de création
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
            List<Entity> orderedEntities = this.orderEntities(coreObject, level, entities);
            entities = orderedEntities;
        }
        return entities;
    }

    private List<Entity> orderEntities(Entity coreObject, int level, List<Entity> entities) {
        List<Entity> entitiesOfLowerLevel = getEntitiesOfLevel(coreObject, level - 1);
        return entities.stream()
                .sorted((e1, e2) -> Integer.valueOf(entitiesOfLowerLevel.indexOf(e2.getSupertype())).compareTo(Integer.valueOf(entitiesOfLowerLevel.indexOf(e1.getSupertype()))))
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
        return Optional.ofNullable(coreObject.eContainer())
                .filter(Namespace.class::isInstance)
                .stream()
                .flatMap(namespace -> ((Namespace) namespace).getTypes().stream())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(entity -> coreObject.equals(entity.getSupertype()))
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
        this.getSubEntities(entity).stream()
                .forEach(subEntity -> subEntity.setSupertype(supertype));
        editService.delete(entity);
        return entity;
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
//        Optional<Object> object = this.objectService.getObject(editingContext, targetObjectId);
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
