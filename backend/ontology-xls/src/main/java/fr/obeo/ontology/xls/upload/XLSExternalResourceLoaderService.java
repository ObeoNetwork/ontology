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
package fr.obeo.ontology.xls.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.web.application.document.services.LoadingReport;
import org.eclipse.sirius.web.application.document.services.api.ExternalResourceLoadingResult;
import org.eclipse.sirius.web.application.document.services.api.IExternalResourceLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A service providing capabilities to load xls files.
 *
 * @author lfasani
 */
@Service
public class XLSExternalResourceLoaderService implements IExternalResourceLoaderService {

    private final Logger logger = LoggerFactory.getLogger(XLSExternalResourceLoaderService.class);

    private final XLSToOntologyModelConverter xlsToOntologyModelConverter;

    public XLSExternalResourceLoaderService(XLSToOntologyModelConverter xlsToOntologyModelConverter) {
        this.xlsToOntologyModelConverter = xlsToOntologyModelConverter;
    }
    @Override
    public boolean canHandle(InputStream inputStream, URI resourceURI, ResourceSet resourceSet) {
        boolean canHandle = false;

        try (inputStream) {
            Workbook wb = WorkbookFactory.create(inputStream);
            canHandle = wb != null;
        } catch (IOException | EncryptedDocumentException e) {
        }
        return canHandle;
    }

    @Override
    public Optional<ExternalResourceLoadingResult> getResource(InputStream inputStream, URI resourceURI, ResourceSet resourceSet, boolean applyMigrationParticipants) {
        return  this.load(resourceSet, inputStream, resourceURI)
                .map(resource -> new ExternalResourceLoadingResult(resource, new LoadingReport(List.of())));
    }

    public Optional<Resource> load(ResourceSet resourceSet, InputStream inputStream, URI resourceURI) {
        try (inputStream) {
            Workbook wb = WorkbookFactory.create(inputStream);
            return Optional.of(this.createResourceFromModel(resourceSet, resourceURI, wb));
        } catch (IOException | EncryptedDocumentException e) {
            this.logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Resource createResourceFromModel(ResourceSet resourceSet, URI resourceURI, Workbook workbook) {
        var resource = new JSONResourceFactory().createResource(resourceURI);
        resourceSet.getResources().add(resource);

        List<EObject> roots = this.xlsToOntologyModelConverter.convertToOntology(workbook);

        resource.getContents().addAll(roots);
        return resource;
    }
}
