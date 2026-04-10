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
package fr.obeo.ontology.owl.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.web.domain.services.api.IMessageService;
import org.obeonetwork.dsl.entity.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A Service responsible to load Entity OWL files.
 * @author fbarbin
 */
@Service
public class OWLUploadLoader {
    // Allow to have a more readable content
    // see https://jena.apache.org/documentation/io/rdfxml_howto.html
    public static final String RDF_XML = "RDF/XML-ABBREV";

    private final IMessageService messageService;

    private final OWLToOntologyModelConverter owlToOntologyModelConverter;

    private final Logger logger = LoggerFactory.getLogger(OWLUploadLoader.class);

    public OWLUploadLoader(IMessageService messageService, OWLToOntologyModelConverter owlToOntologyModelConverter) {
        this.messageService = Objects.requireNonNull(messageService);
        this.owlToOntologyModelConverter = Objects.requireNonNull(owlToOntologyModelConverter);
    }

    public Optional<Resource> load(ResourceSet resourceSet, InputStream inputStream, URI resourceURI) {
        Model loadedModel = ModelFactory.createDefaultModel();
        try (inputStream) {
            loadedModel.read(inputStream, null, RDF_XML);
            return this.createResourceFromModel(resourceSet, resourceURI, loadedModel);
        } catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Optional<Resource> createResourceFromModel(ResourceSet resourceSet, URI resourceURI, Model loadedModel) {
        var resource = new JSONResourceFactory().createResource(resourceURI);
        resourceSet.getResources().add(resource);
        Root root = this.owlToOntologyModelConverter.convertToOntology(loadedModel);

        resource.getContents().add(root);
        return Optional.of(resource);
    }



}
