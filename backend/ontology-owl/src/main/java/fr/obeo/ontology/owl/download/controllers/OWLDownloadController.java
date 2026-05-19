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
package fr.obeo.ontology.owl.download.controllers;

import fr.obeo.ontology.owl.download.OntologyToOWLModelConverter;
import fr.obeo.ontology.service.validation.OntologyModelValidation;
import fr.obeo.ontology.service.validation.OntologyModelValidation.Severity;
import fr.obeo.ontology.service.validation.OntologyModelValidation.ValidationStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.web.application.document.services.api.IDocumentDownloadResourceSearchService;
import org.obeonetwork.dsl.entity.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The entry point of the HTTP API to download owl.
 *
 * <pre>
 * PROTOCOL://DOMAIN.TLD(:PORT)/API_BASE_PATH/editingcontexts/EDITING_CONTEXT_ID/owl/DOCUMENT_ID
 * </pre>
 *
 * @author fbarbin
 */
@Controller
@RequestMapping("/api/editingcontexts/{editingContextId}/owl")
public class OWLDownloadController {

    // Allow to have a more readable content
    // see https://jena.apache.org/documentation/io/rdfxml_howto.html
    public static final String RDF_XML = "RDF/XML-ABBREV";
    private final Logger logger = LoggerFactory.getLogger(OWLDownloadController.class);

    private final IEditingContextSearchService editingContextSearchService;

    private final List<IDocumentDownloadResourceSearchService> documentDownloadResourceSearchServices;

    private final OntologyToOWLModelConverter owlOntologyConverter;

    private final OntologyModelValidation ontologyModelValidation;

    public OWLDownloadController(IEditingContextSearchService editingContextSearchService, OntologyToOWLModelConverter owlOntologyConverter,
            List<IDocumentDownloadResourceSearchService> documentDownloadResourceSearchServices, OntologyModelValidation ontologyModelValidation) {
        this.editingContextSearchService = Objects.requireNonNull(editingContextSearchService);
        this.owlOntologyConverter = Objects.requireNonNull(owlOntologyConverter);
        this.documentDownloadResourceSearchServices = Objects.requireNonNull(documentDownloadResourceSearchServices);
        this.ontologyModelValidation = Objects.requireNonNull(ontologyModelValidation);
    }

    @ResponseBody
    @GetMapping(path = "/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String editingContextId, @PathVariable String documentId, @RequestHeader HttpHeaders requestHeaders) {
        var optionalResource =
                documentDownloadResourceSearchServices.stream()
                        .map(documentDownloadResourceSearchService -> documentDownloadResourceSearchService.findResource(editingContextId, documentId))
                        .filter(Optional::isPresent)
                        .findFirst()
                        .orElseGet(() -> getResource(editingContextId, documentId));

        if (optionalResource.isPresent()) {
            var resource = optionalResource.get();
            Optional<Root> optionalRoot = resource.getContents().stream().filter(Root.class::isInstance).map(Root.class::cast).findFirst();
            if (optionalRoot.isPresent()) {
                List<ValidationStatus> statuses = this.ontologyModelValidation.validate(optionalRoot.get());
                boolean hasError = statuses.stream().anyMatch(status -> Severity.ERROR.equals(status.severity()));
                if (hasError) {
                    return this.toValidationTextResponse(statuses);
                }

                Model model = owlOntologyConverter.convertToOWLModel(optionalRoot.get());
                Optional<byte[]> optionalBytes = Optional.empty();
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    model.write(outputStream, RDF_XML);
                    optionalBytes = Optional.of(outputStream.toByteArray());
                } catch (IOException exception) {
                    logger.warn(exception.getMessage(), exception);
                }
                if (optionalBytes.isPresent()) {
                    var content = optionalBytes.get();

                    var name = resource.eAdapters().stream()
                            .filter(ResourceMetadataAdapter.class::isInstance)
                            .map(ResourceMetadataAdapter.class::cast)
                            .findFirst()
                            .map(ResourceMetadataAdapter::getName)
                            .map(this::withOwlExtension)
                            .orElse("resource.owl");
                    ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                            .filename(name)
                            .build();

                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.setContentDisposition(contentDisposition);
                    responseHeaders.setContentType(MediaType.APPLICATION_XML);
                    responseHeaders.setContentLength(content.length);

                    InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(content));
                    return new ResponseEntity<>(inputStreamResource, responseHeaders, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    private String withOwlExtension(String name) {
        if (name == null || name.isBlank()) {
            return "resource.owl";
        }
        if (name.length() >= 4 && name.regionMatches(true, name.length() - 4, ".owl", 0, 4)) {
            return name;
        }
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex > 0) {
            return name.substring(0, extensionIndex) + ".owl";
        }
        return name + ".owl";
    }

    private ResponseEntity<Resource> toValidationTextResponse(List<ValidationStatus> statuses) {
        byte[] content = statuses.stream()
                .map(status -> status.severity().name() + " - " + status.message())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("")
                .getBytes(StandardCharsets.UTF_8);

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("ontology-validation-report.txt")
                .build();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentDisposition(contentDisposition);
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);
        responseHeaders.setContentLength(content.length);

        InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(content));
        return new ResponseEntity<>(inputStreamResource, responseHeaders, HttpStatus.OK);
    }

    private Optional<org.eclipse.emf.ecore.resource.Resource> getResource(String editingContextId, String documentId) {
        return editingContextSearchService.findById(editingContextId)
                .filter(IEMFEditingContext.class::isInstance)
                .map(IEMFEditingContext.class::cast)
                .flatMap(editingContext -> {
                    var uri = new JSONResourceFactory().createResourceURI(documentId);
                    return editingContext.getDomain().getResourceSet().getResources().stream()
                            .filter(resource -> resource.getURI().equals(uri))
                            .findFirst();
                });
    }

}
