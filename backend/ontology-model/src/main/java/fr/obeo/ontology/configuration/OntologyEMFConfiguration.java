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
package fr.obeo.ontology.configuration;

import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.obeonetwork.dsl.entity.provider.EntityItemProviderAdapterFactory;
import org.obeonetwork.dsl.environment.provider.EnvironmentItemProviderAdapterFactory;
import org.obeonetwork.dsl.technicalid.provider.TechnicalIDItemProviderAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the EMF support for Entity MM.
 *
 * @author jmallet
 */
@Configuration
public class OntologyEMFConfiguration {

    @Bean
    public ComposedAdapterFactory.Descriptor environmentAdapterFactory() {
        return EnvironmentItemProviderAdapterFactory::new;
    }

    @Bean
    public ComposedAdapterFactory.Descriptor entityAdapterFactory() {
        return EntityItemProviderAdapterFactory::new;
    }

    @Bean
    public ComposedAdapterFactory.Descriptor technicalidAdapterFactory() {
        return TechnicalIDItemProviderAdapterFactory::new;
    }
}
