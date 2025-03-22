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
package fr.obeo.ontology.application.tables;

import fr.obeo.ontology.services.representations.builders.ViewEntityTableDescriptionBuilder;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilder;
import org.eclipse.sirius.components.view.emf.table.ITableIdProvider;
import org.eclipse.sirius.components.view.table.TableDescription;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.springframework.stereotype.Service;

/**
 * Used to provide a view based diagram description to test Ontology table.
 *
 * @author lfasani
 */
@Service
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class OntologyTableDescriptionProvider implements IEditingContextProcessor {

    private final ITableIdProvider tableIdProvider;

    private final View view;

    private TableDescription tableDescription;

    public OntologyTableDescriptionProvider(ITableIdProvider tableIdProvider) {
        this.tableIdProvider = Objects.requireNonNull(tableIdProvider);
        this.view = this.createView();
    }

    private View createView() {
        View view = new ViewBuilder().build();
        new ViewEntityTableDescriptionBuilder(view).addRepresentationDescription();
        this.tableDescription = view.getDescriptions().stream()
                .filter(representationDescription -> representationDescription.getName().equals(ViewEntityTableDescriptionBuilder.ENTITIES_TABLE_NAME))
                .map(TableDescription.class::cast)
                .findFirst()
                .get();

        view.eAllContents().forEachRemaining(eObject -> {
            eObject.eAdapters().add(new IDAdapter(UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes())));
        });

        String resourcePath = UUID.nameUUIDFromBytes("OntologyTableDescription".getBytes()).toString();
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourcePath);
        resource.eAdapters().add(new ResourceMetadataAdapter("OntologyTableDescription"));
        resource.getContents().add(view);

        return view;
    }

    @Override
    public void preProcess(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext siriusWebEditingContext) {
            siriusWebEditingContext.getViews().add(this.view);
        }
    }

    public String getRepresentationDescriptionId() {
        return this.tableIdProvider.getId(this.tableDescription);
    }
}
