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

import fr.obeo.ontology.ontologymm.BusinessDomain;
import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.DataSource;
import fr.obeo.ontology.ontologymm.OntologyPackage;
import fr.obeo.ontology.ontologymm.OrganizationInformation;
import fr.obeo.ontology.services.representations.EntityJavaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.variables.CommonVariables;
import org.eclipse.sirius.components.forms.MultiSelectStyle;
import org.eclipse.sirius.components.forms.SelectStyle;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.forms.components.SelectComponent;
import org.eclipse.sirius.components.forms.description.AbstractControlDescription;
import org.eclipse.sirius.components.forms.description.GroupDescription;
import org.eclipse.sirius.components.forms.description.MultiSelectDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.forms.description.SelectDescription;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;
import org.eclipse.sirius.components.view.emf.compatibility.IPropertiesConfigurerService;
import org.eclipse.sirius.components.view.emf.form.converters.OptionIconURLsProvider;
import org.eclipse.sirius.components.view.emf.form.converters.OptionIdProvider;
import org.eclipse.sirius.components.view.emf.form.converters.TargetObjectIdProvider;
import org.eclipse.sirius.components.view.emf.form.converters.validation.DiagnosticKindProvider;
import org.eclipse.sirius.components.view.emf.form.converters.validation.DiagnosticMessageProvider;
import org.obeonetwork.dsl.entity.Entity;
import org.springframework.stereotype.Service;

/**
 * Brings some helper method to build widget descriptions.
 *
 * @author lfasani
 */
@Service
public class WidgetDescriptionCreationService {

    private final IPropertiesConfigurerService propertiesConfigurerService;

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IObjectSearchService objectSearchService;

    private final IFeedbackMessageService feedbackMessageService;

    private final EntityJavaService entityJavaService;

    public WidgetDescriptionCreationService(IPropertiesConfigurerService propertiesConfigurerService, IIdentityService identityService, ILabelService labelService,
            IObjectSearchService objectSearchService, IFeedbackMessageService feedbackMessageService, EntityJavaService entityJavaService) {
        this.propertiesConfigurerService = Objects.requireNonNull(propertiesConfigurerService);
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.entityJavaService = Objects.requireNonNull(entityJavaService);
    }

    public MultiSelectDescription createDataSourceMultiSelectWidgetDescription() {

        Function<VariableManager, List<?>> optionsProvider = variableManager -> {
            return  variableManager.get(VariableManager.SELF, EObject.class)
                    .map(EObject::eResource)
                    .stream()
                    .flatMap(resource -> resource.getContents().stream())
                    .filter(OrganizationInformation.class::isInstance)
                    .map(OrganizationInformation.class::cast)
                    .flatMap(oi -> oi.getDataSources().stream())
                    .toList();
        };
        Function<VariableManager, List<String>> valuesProvider = variableManager -> {
            return variableManager.get(VariableManager.SELF, Entity.class)
                    .stream()
                    .flatMap(entity -> entityJavaService.objectsReferencingEntity(entity, OntologyPackage.eINSTANCE.getDataSource_Entities(), DataSource.class))
                    .map(identityService::getId)
                    .toList();
        };

        BiFunction<VariableManager, List<String>, IStatus> newHandlerProvider = (variableManager, newValues) -> {
            return variableManager.get(VariableManager.SELF, Entity.class)
                    .map(entity -> {
                        entityJavaService.objectsReferencingEntity(entity, OntologyPackage.eINSTANCE.getDataSource_Entities(), DataSource.class)
                                .forEach(ds -> ds.getEntities().remove(entity));

                        variableManager.get(CommonVariables.EDITING_CONTEXT.name(), IEditingContext.class).stream()
                                .flatMap(iEditingContext -> newValues.stream().flatMap(newValue -> this.objectSearchService.getObject(iEditingContext, newValue).stream()))
                                .filter(DataSource.class::isInstance)
                                .map(DataSource.class::cast)
                                .forEach(ds -> ds.getEntities().add(entity));
                        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
                    })
                    .map(IStatus.class::cast)
                    .orElse(this.createErrorStatus("Something went wrong while setting the reference value."));
        };

        return this.createMultiSelectWidgetDescription("entity.dataSource", "Data Sources", valuesProvider, optionsProvider, newHandlerProvider);
    }

    public MultiSelectDescription createMultiSelectWidgetDescription(String widgetDescriptionId, String label, Function<VariableManager, List<String>> valuesProvider,
            Function<VariableManager, List<?>> optionsProvider, BiFunction<VariableManager, List<String>, IStatus> newValuesHandler) {

        return  MultiSelectDescription.newMultiSelectDescription(widgetDescriptionId)
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(new TargetObjectIdProvider(this.identityService))
                .labelProvider(variableManager -> label)
                .valuesProvider(valuesProvider)
                .optionsProvider(optionsProvider)
                .optionIdProvider(new OptionIdProvider(this.identityService))
                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(object -> this.labelService.getStyledLabel(object).toString())
                        .orElse(""))
                .optionIconURLProvider(new OptionIconURLsProvider(this.labelService))
                .newValuesHandler(newValuesHandler)
                .styleProvider(vm -> MultiSelectStyle.newMultiSelectStyle().showIcon(true).build())
                .diagnosticsProvider(this.propertiesConfigurerService.getDiagnosticsProvider(DiagramPackage.Literals.LINE_STYLE))
                .kindProvider(new DiagnosticKindProvider())
                .messageProvider(new DiagnosticMessageProvider())
                .build();
    }

    public SelectDescription createDataOwnerSelectWidgetDescription() {

        Function<VariableManager, List<?>> optionsProvider = variableManager -> {
            return  variableManager.get(VariableManager.SELF, EObject.class)
                    .map(EObject::eResource)
                    .stream()
                    .flatMap(resource -> resource.getContents().stream())
                    .filter(OrganizationInformation.class::isInstance)
                    .map(OrganizationInformation.class::cast)
                    .flatMap(oi -> oi.getDataOwners().stream())
                    .toList();
        };
        Function<VariableManager, String> valueProvider = variableManager -> {
            return variableManager.get(VariableManager.SELF, Entity.class)
                    .flatMap(entity -> entityJavaService.objectReferencingEntity(entity, OntologyPackage.eINSTANCE.getDataOwner_Entities(), DataOwner.class))
                    .map(identityService::getId)
                    .orElse(null);
        };

        BiFunction<VariableManager, String, IStatus> newHandlerProvider = (variableManager, newValue) -> {
            return variableManager.get(VariableManager.SELF, Entity.class)
                    .map(entity -> {
                        entityJavaService.objectReferencingEntity(entity, OntologyPackage.eINSTANCE.getDataOwner_Entities(), DataOwner.class)
                                .ifPresent(dataOwner -> dataOwner.getEntities().remove(entity));

                        variableManager.get(CommonVariables.EDITING_CONTEXT.name(), IEditingContext.class)
                                .flatMap(iEditingContext -> objectSearchService.getObject(iEditingContext, newValue))
                                .filter(DataOwner.class::isInstance)
                                .map(DataOwner.class::cast)
                                .ifPresent(dataOwner -> dataOwner.getEntities().add(entity));
                        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
                    })
                    .map(IStatus.class::cast)
                    .orElse(this.createErrorStatus("Something went wrong while setting the reference value."));
        };

        return createSelectWidgetDescription("entity.dataOwner", "Data Owner", false, optionsProvider, valueProvider, newHandlerProvider);
    }

    public SelectDescription createBusinessAreaSelectWidgetDescription() {

        Function<VariableManager, List<?>> optionsProvider = variableManager -> {
            return  variableManager.get(VariableManager.SELF, EObject.class)
                    .map(EObject::eResource)
                    .stream()
                    .flatMap(resource -> resource.getContents().stream())
                    .filter(OrganizationInformation.class::isInstance)
                    .map(OrganizationInformation.class::cast)
                    .flatMap(oi -> oi.getBusinessDomains().stream())
                    .toList();
                };
        Function<VariableManager, String> valueProvider = variableManager -> {
            return variableManager.get(VariableManager.SELF, Entity.class)
                    .flatMap(entity -> entityJavaService.objectReferencingEntity(entity, OntologyPackage.eINSTANCE.getBusinessDomain_Entities(), BusinessDomain.class))
                    .map(identityService::getId)
                    .orElse(null);
                };

        BiFunction<VariableManager, String, IStatus> newHandlerProvider = (variableManager, newValue) -> {
            return variableManager.get(VariableManager.SELF, Entity.class)
                    .map(entity -> {
                        entityJavaService.objectReferencingEntity(entity, OntologyPackage.eINSTANCE.getBusinessDomain_Entities(), BusinessDomain.class)
                                .ifPresent(bd -> bd.getEntities().remove(entity));

                        variableManager.get(CommonVariables.EDITING_CONTEXT.name(), IEditingContext.class)
                                .flatMap(iEditingContext -> objectSearchService.getObject(iEditingContext, newValue))
                                .filter(BusinessDomain.class::isInstance)
                                .map(BusinessDomain.class::cast)
                                .ifPresent(bd -> bd.getEntities().add(entity));
                        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
                    })
                    .map(IStatus.class::cast)
                    .orElse(this.createErrorStatus("Something went wrong while setting the reference value."));
        };

        return createSelectWidgetDescription("entity.businessDomain", "Functional Area", false, optionsProvider, valueProvider, newHandlerProvider);
    }

    public SelectDescription createSelectWidgetDescription(String id, String label, boolean isReadOnly, Function<VariableManager, List<?>> optionsProvider, Function<VariableManager, String> valueProvider,  BiFunction<VariableManager, String, IStatus> newHandlerProvider) {
        return SelectDescription.newSelectDescription(id)
                .isReadOnlyProvider(__ -> isReadOnly)
                .idProvider(variableManager -> id)
                .targetObjectIdProvider(this.propertiesConfigurerService.getSemanticTargetIdProvider())
                .labelProvider(variableManager -> label)
                .styleProvider(vm -> SelectStyle.newSelectStyle().showIcon(true).build())
                .valueProvider(valueProvider)
                .optionsProvider(optionsProvider)
                .optionIdProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(identityService::getId)
                        .orElse(""))
                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(object -> labelService.getStyledLabel(object).toString())
                        .orElse(""))
                .optionIconURLProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(this.labelService::getImagePaths)
                        .orElse(List.of()))
                .newValueHandler(newHandlerProvider)
                .diagnosticsProvider(this.propertiesConfigurerService.getDiagnosticsProvider(DiagramPackage.Literals.LINE_STYLE))
                .kindProvider(this.propertiesConfigurerService.getKindProvider())
                .messageProvider(this.propertiesConfigurerService.getMessageProvider())
                .build();
    }

    public SelectDescription createSelectWidgetDescription(String widgetDescriptionId, String label, boolean isReadOnly, EStructuralFeature feature,
            Function<VariableManager, List<?>> optionsProvider) {
        return SelectDescription.newSelectDescription(widgetDescriptionId)
                .isReadOnlyProvider(__ -> isReadOnly)
                .idProvider(variableManager -> widgetDescriptionId)
                .targetObjectIdProvider(this.propertiesConfigurerService.getSemanticTargetIdProvider())
                .labelProvider(variableManager -> label)
                .styleProvider(vm -> SelectStyle.newSelectStyle().showIcon(true).build())
                .valueProvider(variableManager -> this.getReferenceValue(variableManager, feature))
                .optionsProvider(optionsProvider)
                .optionIdProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(this.identityService::getId)
                        .orElse(""))
                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(object -> this.labelService.getStyledLabel(object).toString())
                        .orElse(""))
                .optionIconURLProvider(new OptionIconURLsProvider(this.labelService))
                .newValueHandler(this.getSelectNewValueHandler(feature))
                .diagnosticsProvider(this.propertiesConfigurerService.getDiagnosticsProvider(DiagramPackage.Literals.LINE_STYLE))
                .kindProvider(this.propertiesConfigurerService.getKindProvider())
                .messageProvider(this.propertiesConfigurerService.getMessageProvider())
                .build();
    }

    private BiFunction<VariableManager, String, IStatus> getSelectNewValueHandler(EStructuralFeature feature) {
        return (variableManager, newValue) -> {
            if (newValue.isEmpty()) {
                return variableManager.get(VariableManager.SELF, EObject.class)
                        .map(referenceOwner -> {
                            referenceOwner.eUnset(feature);
                            return (IStatus) new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
                        })
                        .orElseGet(() -> this.createErrorStatus("Something went wrong while setting the reference value."));
            }

            return variableManager.get(CommonVariables.EDITING_CONTEXT.name(), IEditingContext.class)
                    .flatMap(iEditingContext -> objectSearchService.getObject(iEditingContext, newValue))
                    .map(newObjectValue -> {
                        return variableManager.get(VariableManager.SELF, EObject.class)
                                .map(referenceOwner -> {
                                    referenceOwner.eSet(feature, newObjectValue);
                                    return (IStatus) new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
                                })
                                .orElseGet(() -> this.createErrorStatus("Something went wrong while setting the reference value."));
                    })
                    .orElseGet(() -> this.createErrorStatus("Something went wrong while setting the reference value."));
        };
    }

    private String getReferenceValue(VariableManager variableManager, Object feature) {
        String value = null;
        EStructuralFeature.Setting setting = this.resolveSetting(variableManager, feature);
        if (setting != null) {
            var rawValue = setting.get(true);
            if (!setting.getEStructuralFeature().isMany() && rawValue != null) {
                value = identityService.getId(rawValue);
            }
        }
        return value;
    }

    private EStructuralFeature.Setting resolveSetting(VariableManager variableManager, Object feature) {
        EObject referenceOwner = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (referenceOwner != null && feature instanceof EReference reference) {
            return ((InternalEObject) referenceOwner).eSetting(reference);
        } else {
            return null;
        }
    }

    private IStatus createErrorStatus(String message) {
        List<Message> errorMessages = new ArrayList<>();
        errorMessages.add(new Message(message, MessageLevel.ERROR));
        errorMessages.addAll(this.feedbackMessageService.getFeedbackMessages());
        return new Failure(errorMessages);
    }

    public GroupDescription createSimpleGroupDescription(String id, String label, List<AbstractControlDescription> controls) {
        return GroupDescription.newGroupDescription(id)
                .idProvider((variableManager) -> id)
                .labelProvider((variableManager) -> label)
                .semanticElementsProvider(this.propertiesConfigurerService.getSemanticElementsProvider())
                .controlDescriptions(controls)
                .build();
    }

    public PageDescription createSimplePageDescription(String id, List<GroupDescription> groupDescriptions, Predicate<VariableManager> canCreatePredicate) {
        return PageDescription.newPageDescription(id)
                .idProvider((variableManager) -> id)
                .labelProvider((variableManager) -> "Properties")
                .semanticElementsProvider(this.propertiesConfigurerService.getSemanticElementsProvider()).canCreatePredicate(canCreatePredicate)
                .groupDescriptions(groupDescriptions).build();
    }
}
