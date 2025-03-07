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
package fr.obeo.ontology.services.project;

import java.util.List;

import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateProvider;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplate;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplateNature;
import org.springframework.stereotype.Service;

/**
 * Provides Ontology project templates.
 *
 * @author lfasani
 */
@Service
public class OntologyProjectTemplateProvider implements IProjectTemplateProvider {

    public static final String ONTOLOGY_EXAMPLE_TEMPLATE_ID = "ontology-template";

    public static final String ONTOLOGY_NATURE = "siriusWeb://nature?kind=ontology";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        var ontologyTemplate = new ProjectTemplate(ONTOLOGY_EXAMPLE_TEMPLATE_ID, "Ontology", "/project-templates/ontology-project-template.svg", List.of(new ProjectTemplateNature(ONTOLOGY_NATURE)));
        return List.of(ontologyTemplate);
    }
}
