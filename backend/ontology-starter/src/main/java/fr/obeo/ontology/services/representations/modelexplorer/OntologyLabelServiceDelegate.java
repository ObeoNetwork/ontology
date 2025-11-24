package fr.obeo.ontology.services.representations.modelexplorer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IDefaultLabelService;
import org.eclipse.sirius.components.core.api.ILabelServiceDelegate;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.springframework.stereotype.Service;

/**
 * ILabelServiceDelegate to override {@link org.obeonetwork.dsl.environment.MetaDataContainer} and {@link org.obeonetwork.dsl.environment.Annotation} labels.
 *
 * @author fbarbin
 */
@Service
public class OntologyLabelServiceDelegate implements ILabelServiceDelegate {

    public static final String COMMENTS = "Comments";

    private final IDefaultLabelService defaultLabelService;

    public OntologyLabelServiceDelegate(IDefaultLabelService defaultLabelService) {
        this.defaultLabelService = Objects.requireNonNull(defaultLabelService);
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof MetaDataContainer || object instanceof Annotation;
    }

    @Override
    public StyledString getStyledLabel(Object object) {
        StyledString styledString = StyledString.of("");
        if (object instanceof MetaDataContainer metaDataContainer) {
            styledString = StyledString.of(COMMENTS);
        } else if (object instanceof Annotation annotation) {
            String title = Optional.ofNullable(annotation.getTitle()).map(Object::toString).orElse("");
            styledString = StyledString.of(title);
        } else {
            styledString = this.defaultLabelService.getStyledLabel(object);
        }
        return styledString;
    }

    @Override
    public List<String> getImagePaths(Object object) {
        return this.defaultLabelService.getImagePaths(object);
    }
}
