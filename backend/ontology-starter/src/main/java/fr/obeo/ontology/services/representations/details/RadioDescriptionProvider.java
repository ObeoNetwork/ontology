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
package fr.obeo.ontology.services.representations.details;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.emf.forms.api.IPropertiesValidationProvider;
import org.eclipse.sirius.components.forms.components.SelectComponent;
import org.eclipse.sirius.components.forms.description.RadioDescription;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service providing a generic radio description.
 *
 * @author fbarbin
 */
@Service
public class RadioDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(RadioDescriptionProvider.class);

    private final List<ComposedAdapterFactory.Descriptor> composedAdapterFactoryDescriptors;

    private final IPropertiesValidationProvider propertiesValidationProvider;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    public RadioDescriptionProvider(List<ComposedAdapterFactory.Descriptor> composedAdapterFactoryDescriptors,
            IPropertiesValidationProvider propertiesValidationProvider, IIdentityService identityService) {
        this.composedAdapterFactoryDescriptors = composedAdapterFactoryDescriptors;
        this.propertiesValidationProvider = Objects.requireNonNull(propertiesValidationProvider);
        this.semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(identityService::getId)
                .orElse(null);
    }

    public RadioDescription getRadioDescription(EStructuralFeature feature, String id, String title, Optional<Function<VariableManager, List<?>>> optionsIdProviderOpt) {
        return RadioDescription.newRadioDescription(id)
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .idProvider(variableManager -> id)
                .labelProvider(variableManager -> title)
                .optionsProvider(optionsIdProviderOpt.orElseGet(() -> this.getOptionsProvider(feature)))
                .optionSelectedProvider(this.getOptionSelectedProvider(feature))
                .optionIdProvider(this.getOptionIdProvider())
                .optionLabelProvider(this.getOptionLabelProvider(feature))
                .newValueHandler(this.getNewValueHandler(feature))
                .diagnosticsProvider(this.propertiesValidationProvider.getDiagnosticsProvider())
                .kindProvider(this.propertiesValidationProvider.getKindProvider())
                .messageProvider(this.propertiesValidationProvider.getMessageProvider())
                .isReadOnlyProvider(variableManager -> false)
                .build();
    }

    private Function<VariableManager, List<?>> getOptionsProvider(EStructuralFeature feature) {
        return variableManager -> {
            if (feature instanceof EAttribute) {
                EDataType eEnum = ((EAttribute) feature).getEAttributeType();
                if (eEnum instanceof EEnum) {
                    return ((EEnum) eEnum).getELiterals().stream()
                            .map(EEnumLiteral::getInstance)
                            .toList();
                }
            }
            return new ArrayList<>();
        };
    }

    private Function<VariableManager, String> getOptionIdProvider() {
        return variableManager -> {
            Object literal = variableManager.getVariables().get(SelectComponent.CANDIDATE_VARIABLE);
            if (literal instanceof Enumerator) {
                return Integer.valueOf(((Enumerator) literal).getValue()).toString();
            }
            return "";
        };
    }

    private Function<VariableManager, String> getOptionLabelProvider(EStructuralFeature feature) {
        return variableManager -> {

            Map<String, Object> variables = variableManager.getVariables();
            Object object = variables.get("self");
            Object literal = variables.get("candidate");
            String result = "";
            if (object instanceof EObject eObject) {
                List<AdapterFactory> adapterFactories = this.composedAdapterFactoryDescriptors.stream()
                        .map(ComposedAdapterFactory.Descriptor::createAdapterFactory)
                        .toList();
                var composedAdapterFactory = new ComposedAdapterFactory(adapterFactories);
                Adapter adapter = composedAdapterFactory.adapt(eObject, IItemPropertySource.class);
                composedAdapterFactory.dispose();

                if (adapter instanceof IItemPropertySource itemPropertySource) {
                    IItemPropertyDescriptor descriptor = itemPropertySource.getPropertyDescriptor(eObject, feature);
                    if (descriptor != null) {
                        result = descriptor.getLabelProvider(eObject).getText(literal);
                    }
                }

            }

            if (result.isEmpty() && literal instanceof Enumerator enumerator) {
                result = enumerator.getLiteral();
            }

            return result;
        };
    }

    private Function<VariableManager, Boolean> getOptionSelectedProvider(EStructuralFeature feature) {
        return variableManager -> {
            var optionalEnumerator = variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Enumerator.class);
            if (optionalEnumerator.isPresent()) {
                Enumerator enumerator = optionalEnumerator.get();
                String optionLitteralId = Integer.valueOf(enumerator.getValue()).toString();

                var optionalEObject = variableManager.get(VariableManager.SELF, EObject.class);
                if (optionalEObject.isPresent()) {
                    EObject eObject = optionalEObject.get();

                    Object value = eObject.eGet(feature);
                    if (value instanceof Enumerator) {
                        String selectedLitteralId = Integer.valueOf(((Enumerator) value).getValue()).toString();
                        return optionLitteralId.equals(selectedLitteralId);
                    }
                }
            }
            return false;
        };
    }

    private BiFunction<VariableManager, String, IStatus> getNewValueHandler(EStructuralFeature feature) {
        return (variableManager, newValue) -> {
            var optionalEObject = variableManager.get(VariableManager.SELF, EObject.class);

            try {
                int id = Integer.valueOf(newValue).intValue();
                if (optionalEObject.isPresent()) {
                    EObject eObject = optionalEObject.get();
                    EClassifier eType = feature.getEType();
                    if (eType instanceof EEnum eEnum) {
                        EEnumLiteral literal = eEnum.getEEnumLiteral(id);
                        if (literal != null) {
                            Object value = EcoreUtil.createFromString(eEnum, literal.getLiteral());
                            eObject.eSet(feature, value);
                        }
                    }
                }
            } catch (NumberFormatException exception) {
                this.logger.warn(exception.getMessage(), exception);
                return new Failure("");
            }
            return new Success();
        };
    }
}
