package fr.obeo.ontology.services.representations.modelexplorer;

import fr.obeo.ontology.services.project.OntologyEditingContextPredicate;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.sirius.components.core.api.IDefaultEditService;
import org.eclipse.sirius.components.core.api.IEditServiceDelegate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.obeonetwork.dsl.entity.EntityPackage;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A Specific Ontology Edit Service to filter children creation on Namespace container.
 * @author fbarbin
 */
@Service
public class OntologyEditServiceDelegate implements IEditServiceDelegate {

    private final List<EClass> namespaceContainerChildrenList = List.of(EntityPackage.Literals.ENTITY, EnvironmentPackage.Literals.NAMESPACE,
            EnvironmentPackage.Literals.ENUMERATION, EnvironmentPackage.Literals.PRIMITIVE_TYPE, EnvironmentPackage.Literals.ATTRIBUTE,
            EnvironmentPackage.Literals.META_DATA_CONTAINER, EnvironmentPackage.Literals.REFERENCE, EnvironmentPackage.Literals.ANNOTATION);

    private final List<EPackage> authorizedPackage = List.of(EnvironmentPackage.eINSTANCE, EntityPackage.eINSTANCE);

    private final IDefaultEditService defaultEditService;

    private final OntologyEditingContextPredicate ontologyEditingContextPredicate;

    private final ComposedAdapterFactory composedAdapterFactory;

    public OntologyEditServiceDelegate(IDefaultEditService defaultEditService, OntologyEditingContextPredicate ontologyEditingContextPredicate, ComposedAdapterFactory composedAdapterFactory) {
        this.defaultEditService = Objects.requireNonNull(defaultEditService);
        this.ontologyEditingContextPredicate = Objects.requireNonNull(ontologyEditingContextPredicate);
        this.composedAdapterFactory = Objects.requireNonNull(composedAdapterFactory);
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof EObject eObject && authorizedPackage.contains(eObject.eClass().getEPackage());
    }

    @Override
    public boolean canHandle(IEditingContext editingContext) {
        return this.ontologyEditingContextPredicate.test(editingContext);
    }

    @Override
    public List<ChildCreationDescription> getRootCreationDescriptions(IEditingContext editingContext, String domainId, boolean suggested, String referenceKind) {
        return this.defaultEditService.getRootCreationDescriptions(editingContext, domainId, suggested, referenceKind);
    }

    @Override
    public List<ChildCreationDescription> getChildCreationDescriptions(IEditingContext editingContext, String kind, String referenceKind) {
        List<ChildCreationDescription> childCreationDescriptions = this.defaultEditService.getChildCreationDescriptions(editingContext, kind, referenceKind);
        return childCreationDescriptions.stream()
                .filter(this::filterNamespaceContainerChildren)
                .toList();
    }

    private String computeLabel(EClass eClass, AdapterFactoryEditingDomain adapterFactoryEditingDomain) {
        EObject eObject = EcoreUtil.create(eClass);
        Adapter adapter = adapterFactoryEditingDomain.getAdapterFactory().adapt(eObject, IItemLabelProvider.class);
        if (adapter instanceof ResourceLocator resourceLocator) {
            return resourceLocator.getString("_UI_" + eClass.getName() + "_type");
        }
        return eClass.getName();
    }

    private boolean filterNamespaceContainerChildren(ChildCreationDescription childCreationDescription) {
        AdapterFactoryEditingDomain adapterFactoryEditingDomain = new AdapterFactoryEditingDomain(this.composedAdapterFactory, new BasicCommandStack());
        List<String> eClassifierLabels = namespaceContainerChildrenList.stream().map(eClass -> computeLabel(eClass, adapterFactoryEditingDomain)).toList();
        return eClassifierLabels.contains(childCreationDescription.getLabel());
    }
    @Override
    public Optional<Object> createChild(IEditingContext editingContext, Object object, String childCreationDescriptionId) {
        return this.defaultEditService.createChild(editingContext, object, childCreationDescriptionId);
    }

    @Override
    public Optional<Object> createRootObject(IEditingContext editingContext, UUID documentId, String domainId, String rootObjectCreationDescriptionId) {
        return this.defaultEditService.createRootObject(editingContext, documentId, domainId, rootObjectCreationDescriptionId);
    }

    @Override
    public void delete(Object object) {
        this.defaultEditService.delete(object);
    }

    @Override
    public void editLabel(Object object, String labelField, String newValue) {
        this.defaultEditService.editLabel(object, labelField, newValue);
    }
}
