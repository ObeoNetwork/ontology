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
package fr.obeo.ontology.services.representations;

import java.util.List;

import org.eclipse.sirius.components.core.api.IImagePathService;
import org.springframework.stereotype.Service;

/**
 * Used to support custom images.
 *
 * @author lfasani
 */
@Service
public class OntologyImageService implements IImagePathService {
    @Override
    public List<String> getPaths() {
        return List.of("/customImages");
    }

//    public List<ParametricSVGImage> getImages() {
//        return List.of(new ParametricSVGImage(UUID.nameUUIDFromBytes("/customImages/blueRing.svg".getBytes()), "Blue Ring", "/customImages/blueRing.svg"));
//    }

}
