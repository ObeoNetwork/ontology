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

import fr.obeo.ontology.services.project.OntologyEditingContextPredicate;
import fr.obeo.ontology.services.representations.builders.ViewDiagramDescriptionBuilder;
import fr.obeo.ontology.services.representations.builders.ViewExplorerTreeDescriptionBuilder;
import fr.obeo.ontology.services.representations.builders.ViewOntologyPaletteBuilder;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.emf.tree.ITreeIdProvider;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.springframework.stereotype.Service;

/**
 * Used to provide the view representation descriptions for ontolgy projects.
 *
 * @author lfasani
 */
@Service
public class OntologyViewRepresentationDescriptionProvider implements IEditingContextProcessor {

    private static final String ONTOLOGY_VIEW_ID = "Ontology View";

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    private final ViewBuilders viewBuilderHelper = new ViewBuilders();

    private final View view;

    private final ITreeIdProvider treeIdProvider;

    private TreeDescription viewDescription;

    public OntologyViewRepresentationDescriptionProvider(OntologyEditingContextPredicate ontologyEditingContextPredicate, ITreeIdProvider treeIdProvider) {
        this.ontologyEditingContextPredicate = Objects.requireNonNull(ontologyEditingContextPredicate);
        this.treeIdProvider = Objects.requireNonNull(treeIdProvider);
        this.view = this.createView();
    }

    @Override
    public void preProcess(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext siriusWebEditingContext && this.ontologyEditingContextPredicate.test(editingContext)) {
            siriusWebEditingContext.getViews().add(this.view);
        }
    }

    private View createView() {
        var domainTextStylePalette = new ViewOntologyPaletteBuilder().createTextStylePalette();
        var colorPalette = new ViewOntologyPaletteBuilder().createColorPalettes();

        var domainView = new ViewBuilders().newView()
                .textStylePalettes(domainTextStylePalette)
                .colorPalettes(colorPalette.toArray(new ColorPalette[0]))
                .build();

        new ViewDiagramDescriptionBuilder(domainView).addRepresentationDescription();
        new ViewExplorerTreeDescriptionBuilder(domainView).addRepresentationDescription();

        domainView.eAllContents().forEachRemaining(eObject -> {
            var id = UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes());
            eObject.eAdapters().add(new IDAdapter(id));
        });

        String resourcePath = UUID.nameUUIDFromBytes(ONTOLOGY_VIEW_ID.getBytes()).toString();
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourcePath);
        resource.eAdapters().add(new ResourceMetadataAdapter(ONTOLOGY_VIEW_ID));
        resource.getContents().add(domainView);

        return domainView;
    }

    public String getRepresentationDescriptionId() {
        return this.treeIdProvider.getId(this.viewDescription);
    }

}
