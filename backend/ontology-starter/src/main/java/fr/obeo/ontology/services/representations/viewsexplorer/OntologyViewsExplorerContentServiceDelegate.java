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
package fr.obeo.ontology.services.representations.viewsexplorer;

import fr.obeo.ontology.services.project.OntologyEditingContextPredicate;
import fr.obeo.ontology.services.representations.providers.ViewDiagramDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewEntityTableDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewRelationsDiagramDescriptionProvider;
import fr.obeo.ontology.services.representations.providers.ViewRelationsOverviewDiagramDescriptionProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.tables.Table;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.RepresentationDescriptionType;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.RepresentationKind;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.api.IDefaultViewsExplorerContentService;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.api.IViewsExplorerContentServiceDelegate;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.environment.StructuredType;
import org.springframework.stereotype.Service;

/**
 * Orders ontology views and computes display labels without renaming representations.
 *
 * @author lfasani
 */
@Service
public class OntologyViewsExplorerContentServiceDelegate implements IViewsExplorerContentServiceDelegate {

    private final List<String> descriptionOrder = List.of(ViewEntityTableDescriptionProvider.ENTITIES_TABLE_NAME,
            ViewDiagramDescriptionProvider.ONTOLOGY_DIAGRAM_NAME, ViewRelationsOverviewDiagramDescriptionProvider.RELATIONS_OVERVIEW_DIAGRAM_NAME,
            ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME);

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    private final IDefaultViewsExplorerContentService defaultContentService;

    private final IObjectSearchService objectSearchService;

    public OntologyViewsExplorerContentServiceDelegate(OntologyEditingContextPredicate ontologyEditingContextPredicate,
            IDefaultViewsExplorerContentService defaultContentService, IObjectSearchService objectSearchService) {
        this.ontologyEditingContextPredicate = Objects.requireNonNull(ontologyEditingContextPredicate);
        this.defaultContentService = Objects.requireNonNull(defaultContentService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext) {
        return this.ontologyEditingContextPredicate.test(editingContext);
    }

    @Override
    public List<RepresentationKind> getContents(IEditingContext editingContext, List<RepresentationMetadata> representationMetadata,
            Map<String, IRepresentationDescription> representationDescriptions) {
        return this.defaultContentService.getContents(editingContext, representationMetadata, representationDescriptions).stream()
                .map(kind -> new RepresentationKind(kind.id(), kind.name(), this.getDescriptionTypes(editingContext, kind)))
                .sorted(Comparator.comparingInt(this::getKindPriority).thenComparing(RepresentationKind::name))
                .toList();
    }

    private List<RepresentationDescriptionType> getDescriptionTypes(IEditingContext editingContext, RepresentationKind kind) {
        return kind.representationDescriptionTypes().stream()
                .map(type -> new RepresentationDescriptionType(type.id(), type.description(), this.getMetadata(editingContext, type)))
                .sorted(Comparator.comparingInt(this::getDescriptionPriority)
                        .thenComparing(type -> type.description().getLabel(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RepresentationDescriptionType::id))
                .toList();
    }

    private List<RepresentationMetadata> getMetadata(IEditingContext editingContext, RepresentationDescriptionType type) {
        boolean relations = ViewRelationsDiagramDescriptionProvider.RELATIONS_DIAGRAM_NAME.equals(type.description().getLabel());
        return type.representationsMetadata().stream()
                .map(metadata -> relations ? this.getDisplayMetadata(editingContext, metadata) : metadata)
                .sorted(Comparator.comparing(RepresentationMetadata::getLabel, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RepresentationMetadata::getRepresentationMetadataId))
                .toList();
    }

    private RepresentationMetadata getDisplayMetadata(IEditingContext editingContext, RepresentationMetadata metadata) {
        return this.objectSearchService.getObject(editingContext, metadata.getTargetObjectId())
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .<RepresentationMetadata>map(entity -> new OntologyViewsExplorerRepresentationMetadata(metadata, this.getFullName(entity)))
                .orElse(metadata);
    }

    private int getDescriptionPriority(RepresentationDescriptionType type) {
        int index = this.descriptionOrder.indexOf(type.description().getLabel());
        return index >= 0 ? index : this.descriptionOrder.size();
    }

    private int getKindPriority(RepresentationKind kind) {
        int priority = 2;
        if (Table.KIND.equals(kind.id())) {
            priority = 0;
        } else if (Diagram.KIND.equals(kind.id())) {
            priority = 1;
        }
        return priority;
    }

    /**
     * Computes the name base on an entity's inheritance path, including protection against cyclic supertype links.
     */
    private String getFullName(Entity entity) {
        List<String> names = new ArrayList<>();
        Set<StructuredType> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        StructuredType current = Objects.requireNonNull(entity);
        while (current != null && visited.add(current)) {
            names.add(Objects.toString(current.getName(), ""));
            current = current.getSupertype();
        }
        Collections.reverse(names);
        return String.join(" > ", names);
    }
}
