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
package fr.obeo.ontology.license.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.obeo.ocp.domain.boundedcontexts.license.License;
import fr.obeo.ocp.domain.boundedcontexts.license.Privilege;
import fr.obeo.ocp.domain.boundedcontexts.license.Token;
import fr.obeo.ocp.domain.boundedcontexts.license.TokenPrivilege;
import fr.obeo.ocp.domain.boundedcontexts.license.repositories.ILicenseRepository;
import fr.obeo.ocp.domain.boundedcontexts.license.repositories.IPrivilegeRepository;
import fr.obeo.ocp.domain.boundedcontexts.license.repositories.ITokenRepository;
import fr.obeo.ocp.domain.boundedcontexts.license.services.api.ILicenseRegistrationService;
import fr.obeo.ocp.domain.services.api.IOCPMessageService;
import fr.obeo.ontology.license.data.LicenseData;
import fr.obeo.ontology.license.data.PrivilegeData;
import fr.obeo.ontology.license.data.TokenData;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.domain.services.Failure;
import org.eclipse.sirius.web.domain.services.IResult;
import org.eclipse.sirius.web.domain.services.Success;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Used to perform some temporary test of the license support.
 *
 * @author sbegaudeau
 */
@Service
public class LicenseRegistrationService implements ILicenseRegistrationService {

    private final ILicenseRepository licenseRepository;

    private final ITokenRepository tokenRepository;

    private final IPrivilegeRepository privilegeRepository;

    private final ObjectMapper objectMapper;

    private final IOCPMessageService messageService;

    private final Logger logger = LoggerFactory.getLogger(LicenseRegistrationService.class);

    public LicenseRegistrationService(ILicenseRepository licenseRepository, ITokenRepository tokenRepository, IPrivilegeRepository privilegeRepository, ObjectMapper objectMapper,
            IOCPMessageService messageService) {
        this.licenseRepository = Objects.requireNonNull(licenseRepository);
        this.tokenRepository = Objects.requireNonNull(tokenRepository);
        this.privilegeRepository = Objects.requireNonNull(privilegeRepository);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.messageService = Objects.requireNonNull(messageService);
    }

    @Override
    public IResult<License> registerLicense(ICause cause, String title, String description, String content) {
        IResult<License> result = null;

        var optionalLicenseData = this.getLicenseData(content);
        if (title.isBlank()) {
            result = new Failure<>(this.messageService.cannotBeBlank());
        } else if (content.isBlank()) {
            result = new Failure<>(this.messageService.cannotBeBlank());
        } else if (optionalLicenseData.isEmpty()) {
            result = new Failure<>(this.messageService.cannotBeBlank());
        } else {
            var licenseData = optionalLicenseData.get();

            var privilegeToCreate = licenseData.tokens().stream().map(TokenData::privileges).flatMap(Collection::stream).distinct()
                    .filter(privilegeData -> !this.privilegeRepository.existsById(privilegeData.id())).map(privilegeData -> this.toPrivilege(cause, privilegeData)).flatMap(Optional::stream).toList();
            this.privilegeRepository.saveAll(privilegeToCreate);

            var license = License.newLicense().title(title).description(description).content(content).build(cause);
            this.licenseRepository.save(license);

            var tokens = licenseData.tokens().stream().map(tokenData -> this.toToken(cause, license.getId(), tokenData)).flatMap(Optional::stream).toList();
            this.tokenRepository.saveAll(tokens);

            result = new Success<>(license);
        }

        return result;
    }

    private Optional<LicenseData> getLicenseData(String content) {
        Optional<LicenseData> optionalLicenseData = Optional.empty();

        try {
            var licenseData = this.objectMapper.readValue(content, LicenseData.class);
            optionalLicenseData = Optional.of(licenseData);
        } catch (JsonProcessingException exception) {
            this.logger.warn(exception.getMessage());
        }

        return optionalLicenseData;
    }

    private Optional<Token> toToken(ICause cause, UUID licenseId, TokenData tokenData) {
        var privileges = tokenData.privileges().stream().map(this::toTokenPrivilege).collect(Collectors.toSet());

        var token = Token.newToken(tokenData.id()).license(AggregateReference.to(licenseId)).title(tokenData.title()).description(tokenData.description()).totalTokenCount(tokenData.totalTokenCount())
                .privileges(privileges).assignments(Set.of()).startDate(tokenData.startDate()).endDate(tokenData.endDate()).build(cause);

        return Optional.of(token);
    }

    private Optional<Privilege> toPrivilege(ICause cause, PrivilegeData privilegeData) {
        var privilege = Privilege.newPrivilege(privilegeData.id()).name(privilegeData.name()).description(privilegeData.description()).build(cause);
        return Optional.of(privilege);
    }

    private TokenPrivilege toTokenPrivilege(PrivilegeData privilegeData) {
        return TokenPrivilege.newTokenPrivilege().privilege(AggregateReference.to(privilegeData.id())).build();
    }
}
