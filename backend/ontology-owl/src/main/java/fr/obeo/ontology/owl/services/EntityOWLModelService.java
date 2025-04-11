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
package fr.obeo.ontology.owl.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

/**
 * A Service providing the Entity OWL model.
 *
 * @author fbarbin
 */
@Service
public class EntityOWLModelService {

    public static final String BASE_URI = "http://obeo.fr/ontology#";

    public static final String ENTITY_DESCRIPTION_URI = BASE_URI + "description";

    public static final String ENTITY_NAME_URI = BASE_URI + "name";

    private static final String ENTITY_RESOURCE_URI = BASE_URI + "Entity";

    private static final String NAMESPACE_RESOURCE_URI = BASE_URI + "Namespace";

    public EntityOWLModelService() {

    }

    public Resource getEntityClass(Model model) {
        return model.getResource(ENTITY_RESOURCE_URI);
    }

    public Resource getNamespaceClass(Model model) {
        return model.getResource(NAMESPACE_RESOURCE_URI);
    }

    public Model createBaseModel() {
        Model model = ModelFactory.createDefaultModel();

        model.setNsPrefix("ex", BASE_URI);

        model.createResource(ENTITY_RESOURCE_URI).addProperty(RDF.type, OWL.Class);
        model.createResource(NAMESPACE_RESOURCE_URI).addProperty(RDF.type, OWL.Class);

        // Création de propriétés OWL
        model.createProperty(ENTITY_NAME_URI);
        model.createProperty(ENTITY_DESCRIPTION_URI);
        return model;
    }

    public Property getNameProperty(Model model) {
        return model.getProperty(ENTITY_NAME_URI);
    }

    public Property getDescriptionProperty(Model model) {
        return model.getProperty(ENTITY_DESCRIPTION_URI);
    }
}
