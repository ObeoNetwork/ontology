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
package fr.obeo.ontology.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Entry point of the ontology Web server.
 *
 * @author lfasani
 */
@SpringBootApplication
//@ComponentScan(basePackages = { "fr.obeo.ontology", "fr.obeo.ocp.license.application.validator" })
@ComponentScan(basePackages = { "fr.obeo.ontology" })
public class OntologyWeb {
    /**
     * The entry point of the server.
     *
     * @param args
     *         The command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OntologyWeb.class, args);
    }
}
