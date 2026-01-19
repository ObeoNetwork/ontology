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
package fr.obeo.ontology.services.project;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.Namespace;

/**
 * Builder for samples of Ontology.
 *
 * @author lfasani
 */
public class OntologySampleBuilder {
    public List<EObject> getEmptySampleContent() {

        Root root = EntityFactory.eINSTANCE.createRoot();
        root.setName("Root");

        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        namespace.setName("Core entities");
        root.getOwnedNamespaces().add(namespace);

        Entity coreEntity = EntityFactory.eINSTANCE.createEntity();
        coreEntity.setName("Core Entity");
        Entity entityLevel1 = EntityFactory.eINSTANCE.createEntity();
        entityLevel1.setName("Entity Level 1");
        entityLevel1.setSupertype(coreEntity);
        namespace.getTypes().addAll(List.of(coreEntity, entityLevel1));

        return List.of(root);
    }
}
