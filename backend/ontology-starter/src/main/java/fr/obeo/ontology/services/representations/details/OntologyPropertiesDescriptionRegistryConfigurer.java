package fr.obeo.ontology.services.representations.details;

import fr.obeo.ontology.ontologymm.BusinessDomain;
import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.DataSource;
import fr.obeo.ontology.ontologymm.OntologyPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.components.forms.description.AbstractControlDescription;
import org.eclipse.sirius.components.forms.description.GroupDescription;
import org.eclipse.sirius.components.forms.description.MultiSelectDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.forms.description.RadioDescription;
import org.eclipse.sirius.components.forms.description.SelectDescription;
import org.eclipse.sirius.components.forms.description.TextfieldDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.emf.compatibility.IPropertiesConfigurerService;
import org.eclipse.sirius.components.view.emf.compatibility.IPropertiesWidgetCreationService;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.EntityPackage;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.NamespacesContainer;
import org.obeonetwork.dsl.environment.ObeoDSMObject;
import org.obeonetwork.dsl.environment.Property;
import org.obeonetwork.dsl.environment.Reference;
import org.obeonetwork.dsl.environment.TypesDefinition;
import org.springframework.stereotype.Service;

/**
 * Specific Ontology implementation to provide a specific properties view for {@link Entity} and {@link Namespace}.
 *
 * @author fbarbin
 */
@Service
public class OntologyPropertiesDescriptionRegistryConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    public static final String NAME = "Name";

    public static final String DESCRIPTION = "Description";

    private final IPropertiesWidgetCreationService propertiesWidgetCreationService;

    private final List<ComposedAdapterFactory.Descriptor> composedAdapterFactoryDescriptors;

    private final RadioDescriptionProvider radioDescriptionProvider;

    private final WidgetDescriptionCreationService widgetDescriptionCreationService;

    public OntologyPropertiesDescriptionRegistryConfigurer(IPropertiesConfigurerService propertiesConfigurerService,
            IPropertiesWidgetCreationService propertiesWidgetCreationService, List<ComposedAdapterFactory.Descriptor> composedAdapterFactoryDescriptors,
            RadioDescriptionProvider radioDescriptionProvider, WidgetDescriptionCreationService widgetDescriptionCreationService) {
        this.propertiesWidgetCreationService = Objects.requireNonNull(propertiesWidgetCreationService);
        this.composedAdapterFactoryDescriptors = Objects.requireNonNull(composedAdapterFactoryDescriptors);
        this.radioDescriptionProvider = Objects.requireNonNull(radioDescriptionProvider);
        this.widgetDescriptionCreationService = Objects.requireNonNull(widgetDescriptionCreationService);
    }

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        registry.add(this.createEntityPageDescription());
        registry.add(this.createNamespacePageDescription());
        registry.add(this.createRootPageDescription());
        registry.add(this.createAttributePageDescription());
        registry.add(this.createReferencePageDescription());
        registry.add(this.createBusinessDomainPageDescription());
        registry.add(this.createDataSourcePageDescription());
        registry.add(this.createDataOwnerPageDescription());
    }

    private PageDescription createEntityPageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("entityProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createEntityControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, Object.class).filter(Entity.class::isInstance).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createNamespacePageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("namespaceProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createNamespaceControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, Object.class).filter(Namespace.class::isInstance).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createRootPageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("rootProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createRootControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, Object.class).filter(Root.class::isInstance).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createAttributePageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("attributeProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createAttributeControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, Object.class).filter(Attribute.class::isInstance).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createReferencePageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("referenceProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createReferenceControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, Object.class).filter(Reference.class::isInstance).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createBusinessDomainPageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("businessDomainProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createBusinessDomainControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, BusinessDomain.class).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createDataOwnerPageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("dataOwnerProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createDataOwnerControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, DataOwner.class).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private PageDescription createDataSourcePageDescription() {
        String formDescriptionId = UUID.nameUUIDFromBytes("dataSourceProperties".getBytes()).toString();

        List<AbstractControlDescription> controls = this.createDataSourceControls();

        Predicate<VariableManager> canCreatePagePredicate = variableManager -> variableManager.get(VariableManager.SELF, DataSource.class).isPresent();
        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);
        return this.propertiesWidgetCreationService.createSimplePageDescription(formDescriptionId, groupDescription, canCreatePagePredicate);
    }

    private List<AbstractControlDescription> createNamespaceControls() {
        TextfieldDescription nameDescription = this.propertiesWidgetCreationService.createTextField("namespace.name", NAME, namespace -> ((Namespace) namespace).getName(), (namespace, newName) -> {
            ((Namespace) namespace).setName(newName);
        }, EnvironmentPackage.Literals.NAMESPACE__NAME);
        TextfieldDescription descriptionDescription = this.getDescriptionTextfieldDescription();
        return List.of(nameDescription, descriptionDescription);
    }

    private List<AbstractControlDescription> createRootControls() {
        TextfieldDescription nameDescription = this.propertiesWidgetCreationService.createTextField("root.name", NAME, root -> ((Root) root).getName(), (root, newName) -> {
            ((Root) root).setName(newName);
        }, EntityPackage.Literals.ROOT__NAME);
        TextfieldDescription descriptionDescription = this.getDescriptionTextfieldDescription();
        return List.of(nameDescription, descriptionDescription);
    }

    private List<AbstractControlDescription> createEntityControls() {
        TextfieldDescription nameDescription = this.propertiesWidgetCreationService.createTextField("entity.name", NAME, entity -> ((Entity) entity).getName(), (entity, newName) -> {
            ((Entity) entity).setName(newName);
        }, EnvironmentPackage.Literals.TYPE__NAME);
        TextfieldDescription descriptionDescription = this.getDescriptionTextfieldDescription();

        SelectDescription superTypeDescription = this.widgetDescriptionCreationService.createSelectWidgetDescription("entity.superType", "Super Type", true,
                EnvironmentPackage.Literals.STRUCTURED_TYPE__SUPERTYPE, variableManager -> this.getChoiceOfValue(variableManager, EnvironmentPackage.Literals.STRUCTURED_TYPE__SUPERTYPE));
        SelectDescription businessDomainDescription = this.widgetDescriptionCreationService.createBusinessAreaSelectWidgetDescription();
        SelectDescription dataOwnerSelectWidgetDescription = this.widgetDescriptionCreationService.createDataOwnerSelectWidgetDescription();
        MultiSelectDescription dataSourceSelectWidgetDescription = this.widgetDescriptionCreationService.createDataSourceMultiSelectWidgetDescription();

        return List.of(nameDescription, descriptionDescription, superTypeDescription, businessDomainDescription, dataOwnerSelectWidgetDescription, dataSourceSelectWidgetDescription);
    }

    private List<AbstractControlDescription> createBusinessDomainControls() {
        TextfieldDescription nameDescription = this.propertiesWidgetCreationService.createTextField("businessDomain.name", NAME,
                bd -> ((BusinessDomain) bd).getName(),
                (bd, newName) -> {
                    ((BusinessDomain) bd).setName(newName);
                }, OntologyPackage.Literals.BUSINESS_DOMAIN__NAME);
        TextfieldDescription descriptionDescription = this.propertiesWidgetCreationService.createTextField("businessDomain.description", DESCRIPTION,
                bd -> ((BusinessDomain) bd).getDescription(),
                (bd, newName) -> {
                    ((BusinessDomain) bd).setDescription(newName);
                }, OntologyPackage.Literals.BUSINESS_DOMAIN__DESCRIPTION);

        return List.of(nameDescription, descriptionDescription);
    }

    private List<AbstractControlDescription> createDataOwnerControls() {
        TextfieldDescription codeDescription = this.propertiesWidgetCreationService.createTextField("dataOwner.code", "Code",
                bd -> ((DataOwner) bd).getCode(),
                (bd, newName) -> {
                    ((DataOwner) bd).setName(newName);
                }, OntologyPackage.Literals.DATA_OWNER__CODE);
        TextfieldDescription nameDescription = this.propertiesWidgetCreationService.createTextField("dataOwner.name", NAME,
                bd -> ((DataOwner) bd).getName(),
                (bd, newName) -> {
                    ((DataOwner) bd).setName(newName);
                }, OntologyPackage.Literals.DATA_OWNER__NAME);
        TextfieldDescription descriptionDescription = this.propertiesWidgetCreationService.createTextField("dataOwner.description", DESCRIPTION,
                bd -> ((DataOwner) bd).getDescription(),
                (bd, newName) -> {
                    ((DataOwner) bd).setDescription(newName);
                }, OntologyPackage.Literals.DATA_OWNER__DESCRIPTION);

        return List.of(codeDescription, nameDescription, descriptionDescription);
    }

    private List<AbstractControlDescription> createDataSourceControls() {
        TextfieldDescription codeDescription = this.propertiesWidgetCreationService.createTextField("dataSource.code", "Code",
                bd -> ((DataSource) bd).getCode(),
                (bd, newName) -> {
                    ((DataSource) bd).setName(newName);
                }, OntologyPackage.Literals.DATA_SOURCE__CODE);
        TextfieldDescription nameDescription = this.propertiesWidgetCreationService.createTextField("dataSource.name", NAME,
                bd -> ((DataSource) bd).getName(),
                (bd, newName) -> {
                    ((DataSource) bd).setName(newName);
                }, OntologyPackage.Literals.DATA_SOURCE__NAME);
        TextfieldDescription descriptionDescription = this.propertiesWidgetCreationService.createTextField("dataSource.description", DESCRIPTION,
                bd -> ((DataSource) bd).getDescription(),
                (bd, newName) -> {
                    ((DataSource) bd).setDescription(newName);
                }, OntologyPackage.Literals.DATA_SOURCE__DESCRIPTION);

        return List.of(codeDescription, nameDescription, descriptionDescription);
    }

    private List<AbstractControlDescription> createAttributeControls() {
        TextfieldDescription nameDescription = this.getPropertyNameTextfieldDescription();
        TextfieldDescription descriptionDescription = this.getDescriptionTextfieldDescription();
        RadioDescription multiplicityRadioDescription = this.getMultiplicityRadioDescription();

        EReference attributeType = EnvironmentPackage.Literals.ATTRIBUTE__TYPE;
        ReferenceWidgetDescription typeDescription = this.propertiesWidgetCreationService.createReferenceWidget("attribute.type", "Type",
                attributeType, variableManager -> this.getChoiceOfValue(variableManager, attributeType));

        return List.of(nameDescription, descriptionDescription, typeDescription, multiplicityRadioDescription);
    }

    private RadioDescription getMultiplicityRadioDescription() {
        return this.radioDescriptionProvider.getRadioDescription(EnvironmentPackage.Literals.PROPERTY__MULTIPLICITY, "property.multiplicity", "Multiplicity");
    }

    private List<AbstractControlDescription> createReferenceControls() {
        TextfieldDescription nameDescription = this.getPropertyNameTextfieldDescription();
        TextfieldDescription descriptionDescription = this.getDescriptionTextfieldDescription();
        RadioDescription multiplicityRadioDescription = this.getMultiplicityRadioDescription();

        EReference attributeType = EnvironmentPackage.Literals.REFERENCE__REFERENCED_TYPE;
        ReferenceWidgetDescription typeDescription = this.propertiesWidgetCreationService.createReferenceWidget("reference.referencedType", "Referenced Type",
                attributeType, variableManager -> this.getChoiceOfValue(variableManager, attributeType));

        return List.of(nameDescription, descriptionDescription, typeDescription, multiplicityRadioDescription);
    }

    private TextfieldDescription getDescriptionTextfieldDescription() {
        return this.propertiesWidgetCreationService.createTextField("entity.description", "Description", dsmObject -> ((ObeoDSMObject) dsmObject).getDescription(), (dsmObject, newDescription) -> {
            ((ObeoDSMObject) dsmObject).setDescription(newDescription);
        }, EnvironmentPackage.Literals.OBEO_DSM_OBJECT__DESCRIPTION);
    }

    private TextfieldDescription getPropertyNameTextfieldDescription() {
        return this.propertiesWidgetCreationService.createTextField("property.name", NAME, property -> ((Property) property).getName(), (property, newName) -> {
            ((Property) property).setName(newName);
        }, EnvironmentPackage.Literals.PROPERTY__NAME);
    }

    private List<?> getChoiceOfValue(VariableManager variableManager, EStructuralFeature feature) {
        var optionalEObject = variableManager.get(VariableManager.SELF, EObject.class);
        if (optionalEObject.isPresent()) {
            EObject eObject = optionalEObject.get();

            List<AdapterFactory> adapterFactories = this.composedAdapterFactoryDescriptors.stream()
                    .map(ComposedAdapterFactory.Descriptor::createAdapterFactory)
                    .toList();
            var composedAdapterFactory = new ComposedAdapterFactory(adapterFactories);
            Object adapter = composedAdapterFactory.adapt(eObject, IItemPropertySource.class);
            composedAdapterFactory.dispose();

            if (adapter instanceof IItemPropertySource itemPropertySource) {
                IItemPropertyDescriptor descriptor = itemPropertySource.getPropertyDescriptor(eObject, feature);
                if (descriptor != null) {
                    return descriptor.getChoiceOfValues(eObject).stream()
                            .filter(Objects::nonNull)
                            .toList();
                }
            }
        }

        return new ArrayList<>();
    }

    private List<TypesDefinition> getNestedTypeDefinitions(TypesDefinition typesDefinition) {
        List<TypesDefinition> typesDefinitions = new ArrayList<>();
        typesDefinitions.add(typesDefinition);
        if (typesDefinition instanceof NamespacesContainer namespacesContainer) {
            List<TypesDefinition> nestedTypes = namespacesContainer.getOwnedNamespaces().stream()
                    .map(this::getNestedTypeDefinitions)
                    .flatMap(List::stream)
                    .toList();
            typesDefinitions.addAll(nestedTypes);
        }
        return typesDefinitions;
    }

}
