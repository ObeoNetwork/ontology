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
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.graphql.api.UploadFile;
import org.eclipse.sirius.web.domain.services.Failure;
import org.eclipse.sirius.web.domain.services.IResult;
import org.eclipse.sirius.web.domain.services.Success;
import org.eclipse.sirius.web.domain.services.api.IMessageService;
import org.obeonetwork.dsl.entity.EntityFactory;
import org.obeonetwork.dsl.entity.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A Service responsible to load Entity OWL files.
 * @author fbarbin
 */
@Service
public class UploadOWLLoader {

    private final IMessageService messageService;

    private final OWLOntologyConverter owlOntologyConverter;

    private final Logger logger = LoggerFactory.getLogger(UploadOWLLoader.class);

    public UploadOWLLoader(IMessageService messageService, OWLOntologyConverter owlOntologyConverter) {
        this.messageService = Objects.requireNonNull(messageService);
        this.owlOntologyConverter = Objects.requireNonNull(owlOntologyConverter);
    }

    public Optional<Resource> load(ResourceSet resourceSet, InputStream inputStream, URI resourceURI) {
        Model loadedModel = ModelFactory.createDefaultModel();
        try (inputStream) {
            loadedModel.read(inputStream, null, "RDF/XML");
            return this.createResourceFromModel(resourceSet, resourceURI, loadedModel);
        } catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Optional<Resource> createResourceFromModel(ResourceSet resourceSet, URI resourceURI, Model loadedModel) {
        var resource = new JSONResourceFactory().createResource(resourceURI);
        resourceSet.getResources().add(resource);
        Root root = this.owlOntologyConverter.convertToOntology(loadedModel);

        resource.getContents().add(root);
        return Optional.of(resource);
    }



}
