package fr.obeo.ontology.services.representations.details;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

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
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.forms.description.RadioDescription;
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

    private final IPropertiesWidgetCreationService propertiesWidgetCreationService;

    private final ComposedAdapterFactory composedAdapterFactory;

    private final RadioDescriptionProvider radioDescriptionProvider;

    public OntologyPropertiesDescriptionRegistryConfigurer(IPropertiesConfigurerService propertiesConfigurerService, ComposedAdapterFactory composedAdapterFactory,
            IPropertiesWidgetCreationService propertiesWidgetCreationService, RadioDescriptionProvider radioDescriptionProvider) {
        this.propertiesWidgetCreationService = Objects.requireNonNull(propertiesWidgetCreationService);
        this.composedAdapterFactory = Objects.requireNonNull(composedAdapterFactory);
        this.radioDescriptionProvider = Objects.requireNonNull(radioDescriptionProvider);
    }

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        registry.add(this.createEntityPageDescription());
        registry.add(this.createNamespacePageDescription());
        registry.add(this.createRootPageDescription());
        registry.add(this.createAttributePageDescription());
        registry.add(this.createReferencePageDescription());
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

        ReferenceWidgetDescription superTypeDescription = this.propertiesWidgetCreationService.createReferenceWidget("entity.superType", "Super Type",
                EnvironmentPackage.Literals.STRUCTURED_TYPE__SUPERTYPE, variableManager -> this.getChoiceOfValue(variableManager, EnvironmentPackage.Literals.STRUCTURED_TYPE__SUPERTYPE));
        return List.of(nameDescription, descriptionDescription, superTypeDescription);
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
            Object adapter = this.composedAdapterFactory.adapt(eObject, IItemPropertySource.class);
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
