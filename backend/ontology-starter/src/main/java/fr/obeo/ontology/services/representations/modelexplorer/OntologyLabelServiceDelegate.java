package fr.obeo.ontology.services.representations.modelexplorer;

import fr.obeo.ontology.ontologymm.OntologyPackage;
import fr.obeo.ontology.ontologymm.provider.OntologyItemProviderAdapterFactory;
import fr.obeo.ontology.services.project.OntologySampleBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IItemStyledLabelProvider;
import org.eclipse.sirius.components.core.api.IDefaultLabelService;
import org.eclipse.sirius.components.core.api.ILabelServiceDelegate;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.emf.services.api.IStyledStringConverter;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.obeonetwork.dsl.environment.PrimitiveType;
import org.obeonetwork.dsl.environment.Reference;
import org.springframework.stereotype.Service;

/**
 * ILabelServiceDelegate to override {@link org.obeonetwork.dsl.environment.MetaDataContainer} and {@link org.obeonetwork.dsl.environment.Annotation} labels.
 *
 * @author fbarbin
 */
@Service
public class OntologyLabelServiceDelegate implements ILabelServiceDelegate {

    public static final String COMMENTS = "Comments";

    public static final String UNNAMED = "(unnamed)";

    private static final String CUSTOM_ICONS_FULL_PATH = "customImages/%s.svg";

    private final IDefaultLabelService defaultLabelService;

    private final IStyledStringConverter styledStringConverter;

    public OntologyLabelServiceDelegate(IDefaultLabelService defaultLabelService, IStyledStringConverter styledStringConverter) {
        this.defaultLabelService = Objects.requireNonNull(defaultLabelService);
        this.styledStringConverter = styledStringConverter;
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof MetaDataContainer || object instanceof Annotation || object instanceof Attribute || object instanceof Reference
                || object instanceof EObject eObject && eObject.eClass().getEPackage().getNsURI().equals(OntologyPackage.eNS_URI) || object instanceof PrimitiveType;
    }

    @Override
    public StyledString getStyledLabel(Object object) {
        StyledString styledString = StyledString.of("");
        if (object instanceof MetaDataContainer metaDataContainer) {
            styledString = StyledString.of(COMMENTS);
        } else if (object instanceof Annotation annotation) {
            String title = Optional.ofNullable(annotation.getTitle()).map(Object::toString).filter(str -> !str.isBlank()).orElse(UNNAMED);
            styledString = StyledString.of(title);
        } else if (object instanceof Attribute attribute) {
            String title = Optional.ofNullable(attribute.getName()).map(Object::toString).filter(str -> !str.isBlank()).orElse(UNNAMED);
            styledString = StyledString.of(title);
        } else if (object instanceof Reference reference) {
            String title = Optional.ofNullable(reference.getName()).map(Object::toString).filter(str -> !str.isBlank()).orElse(UNNAMED);
            styledString = StyledString.of(title);
        } else if (object instanceof PrimitiveType primitiveType) {
                String title = Optional.ofNullable(primitiveType.getName()).map(Object::toString).filter(str -> !str.isBlank()).orElse(UNNAMED);
                styledString = StyledString.of(title);
        } else {
            // defaultLabelService does not fallback on xxxItemProviders so we need to call it explicitly
            var adapter = new OntologyItemProviderAdapterFactory().adapt(object, IItemStyledLabelProvider.class);
            if (adapter instanceof IItemStyledLabelProvider itemStyledLabelProvider) {
                var rawStyledString = itemStyledLabelProvider.getStyledText(object);
                if (rawStyledString instanceof org.eclipse.emf.edit.provider.StyledString emfStyledString) {
                    styledString = this.styledStringConverter.convert(emfStyledString);
                }
            }
        }
        return styledString;
    }


    @Override
    public List<String> getImagePaths(Object object) {
        List<String> imagePaths = List.of();
        if (object instanceof Attribute || object instanceof PrimitiveType) {
            PrimitiveType primitiveType = this.extractPrimitiveType(object);
            if (Objects.nonNull(primitiveType)) {
                imagePaths = this.getPrimitiveTypeImagePath(primitiveType);
            }
        }

        if (!imagePaths.isEmpty()) {
            return imagePaths;
        }
        return this.defaultLabelService.getImagePaths(object);
    }

    private PrimitiveType extractPrimitiveType(Object object) {
        if (object instanceof PrimitiveType primitiveType) {
            return primitiveType;
        }
        if (object instanceof Attribute attribute
                && attribute.getType() instanceof PrimitiveType primitiveType) {
            return primitiveType;
        }
        return null;
    }

    private List<String> getPrimitiveTypeImagePath(PrimitiveType primitiveType) {
        String imageName = switch (primitiveType.getKind()) {
            case TEXT -> "string";
            case NUMBER -> {
                if (OntologySampleBuilder.INT_TYPE.equals(primitiveType.getName())) {
                    yield "integer";
                }
                if (OntologySampleBuilder.DOUBLE_TYPE.equals(primitiveType.getName())) {
                    yield "double";
                }
                yield "";
            }
            case OTHER -> {
                if (OntologySampleBuilder.BOOLEAN_TYPE.equals(primitiveType.getName())) {
                    yield "bool";
                }
                yield "";
            }
        };

        if (imageName.isBlank()) {
            return List.of();
        }
        return List.of(String.format(CUSTOM_ICONS_FULL_PATH, imageName));
    }
}
