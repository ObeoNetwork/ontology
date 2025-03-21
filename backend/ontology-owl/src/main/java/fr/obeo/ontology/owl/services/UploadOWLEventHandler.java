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

import fr.obeo.ontology.owl.controllers.UploadOWLFileSuccessPayload;
import fr.obeo.ontology.owl.controllers.UploadOWLInput;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventHandler;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.document.dto.DocumentDTO;
import org.eclipse.sirius.web.application.document.dto.UploadDocumentInput;
import org.eclipse.sirius.web.application.document.services.api.IUploadDocumentReportProvider;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerDescriptionProvider;
import org.eclipse.sirius.web.domain.services.Failure;
import org.eclipse.sirius.web.domain.services.Success;
import org.eclipse.sirius.web.domain.services.api.IMessageService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Event handler used to create a new document from an OWL file upload.
 *
 * @author fbarbin
 */
@Service
public class UploadOWLEventHandler implements IEditingContextEventHandler {


    private final List<IUploadDocumentReportProvider> uploadDocumentReportProviders;

    private final IMessageService messageService;

    private final UploadOWLLoader uploadOWLLoader;

    private final Counter counter;


    public UploadOWLEventHandler(List<IUploadDocumentReportProvider> uploadDocumentReportProviders, IMessageService messageService,
            UploadOWLLoader uploadOWLLoader, MeterRegistry meterRegistry) {
        this.uploadDocumentReportProviders = Objects.requireNonNull(uploadDocumentReportProviders);
        this.messageService = Objects.requireNonNull(messageService);
        this.uploadOWLLoader = Objects.requireNonNull(uploadOWLLoader);
        counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IInput input) {
        return input instanceof UploadOWLInput;
    }

    @Override
    public void handle(Sinks.One<IPayload> payloadSink, Sinks.Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, IInput input) {
        counter.increment();

        IPayload payload = new ErrorPayload(input.id(), messageService.unexpectedError());
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);

        if (input instanceof UploadOWLInput uploaduploadOWLInput && editingContext instanceof IEMFEditingContext emfEditingContext) {
            var resourceSet = emfEditingContext.getDomain().getResourceSet();

            var result = uploadOWLLoader.load(resourceSet, emfEditingContext, uploaduploadOWLInput.file());
            if (result instanceof Success<Resource> success) {
                var newResource = success.data();

                var optionalId = new UUIDParser().parse(newResource.getURI().path().substring(1));

                var optionalName = newResource.eAdapters().stream()
                        .filter(ResourceMetadataAdapter.class::isInstance)
                        .map(ResourceMetadataAdapter.class::cast)
                        .findFirst()
                        .map(ResourceMetadataAdapter::getName);

                if (optionalId.isPresent() && optionalName.isPresent()) {
                    var id = optionalId.get();
                    var name = optionalName.get();

                    String report = getReport(newResource);
                    payload = new UploadOWLFileSuccessPayload(input.id(), new DocumentDTO(id, name, ExplorerDescriptionProvider.DOCUMENT_KIND), report);
                    var newInput = new UploadDocumentInput(input.id(), ((UploadOWLInput) input).editingContextId(), ((UploadOWLInput) input).file());
                    changeDescription = new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), newInput);
                }

            } else if (result instanceof Failure<Resource> failure) {
                payload = new ErrorPayload(input.id(), failure.message());
            }
        }

        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }


    private String getReport(Resource resource) {
        return uploadDocumentReportProviders.stream()
                .filter(provider -> provider.canHandle(resource))
                .map(provider -> provider.createReport(resource))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
