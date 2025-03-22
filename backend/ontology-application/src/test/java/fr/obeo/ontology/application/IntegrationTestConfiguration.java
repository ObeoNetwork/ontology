/*******************************************************************************
 * Copyright (c) 2024 Obeo.
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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the application used during the integration tests.
 *
 * @author sbegaudeau
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "org.eclipse.sirius.web", "fr.obeo.ontology" })
public class IntegrationTestConfiguration {
    @Bean
    public EPackage ecorePackage() {
        return EcorePackage.eINSTANCE;
    }
}

//@Configuration
//@ComponentScan({ "fr.obeo.ocp", "org.eclipse.sirius.web.tests", "fr.obeo.ontology" })
//public class IntegrationTestConfiguration {
//
//    public static final String JDOE_USERNAME = "jdoe";
//
//    @Bean
//    public EPackage ecorePackage() {
//        return EcorePackage.eINSTANCE;
//    }
//
//    @Bean
//    public OAuth2AccessTokenResponse accessToken() {
//        URI authorizationURI = UriComponentsBuilder.fromUriString(AbstractIntegrationTests.KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/OCP/protocol/openid-connect/token").build().encode().toUri();
//        WebClient webclient = WebClient.builder().build();
//        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//        formData.put("grant_type", Collections.singletonList("password"));
//        formData.put("client_id", Collections.singletonList("OCP-api"));
//        formData.put("client_secret", Collections.singletonList("dV2grdu3JKD2L0imBx42nZEtmA5egUp8"));
//        formData.put("username", Collections.singletonList(JDOE_USERNAME));
//        formData.put("password", Collections.singletonList("odweb"));
//        return webclient.post()
//                .uri(authorizationURI)
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .body(BodyInserters.fromFormData(formData))
//                .exchangeToMono(clientResponse -> clientResponse.body(OAuth2BodyExtractors.oauth2AccessTokenResponse()))
//                .block();
//    }
//
//}
