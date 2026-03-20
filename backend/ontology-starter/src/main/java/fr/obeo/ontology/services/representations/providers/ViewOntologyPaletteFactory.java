/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package fr.obeo.ontology.services.representations.providers;

import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.TextStyleDescription;
import org.eclipse.sirius.components.view.TextStylePalette;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.builder.providers.DefaultColorPalettesProvider;

import java.util.ArrayList;
import java.util.List;

public class ViewOntologyPaletteFactory {

    static final String BLUE_ITALIC_TEXT_STYLE_NAME = "blue italic";

    static final String BLUE_BOLD_TEXT_STYLE_NAME = "blue bold";

    static final String NORMAL_TEXT_STYLE_NAME = "normal";

    private static final String AQL_TRUE = "aql:true";

    public static final String BLUE_GREY = "blue grey";

    public static final String COLOR_TRANSPARENT = "color_transparent";

    public List<ColorPalette> createColorPalettes() {
        var palettes = new ArrayList<>(new DefaultColorPalettesProvider().getDefaultColorPalettes());

        palettes.add(
                new ViewBuilders().newColorPalette()
                .name("Ontology Color Palette")
                .colors(
                        this.fixedColor(BLUE_GREY, "#E6F1FA"),
                        this.fixedColor(COLOR_TRANSPARENT, "transparent"))
                .build()
        );

        return palettes;
    }

    public TextStylePalette createTextStylePalette() {
        return new ViewBuilders()
                .newTextStylePalette()
                .name("Ontology Text Style Palette")
                .styles(this.getBlueBoldStyle(), this.getBlueItalicStyle(), this.getNormalStyle())
                .build();
    }

    private TextStyleDescription getBlueItalicStyle() {
        return new ViewBuilders().newTextStyleDescription()
                .name(BLUE_ITALIC_TEXT_STYLE_NAME)
                .foregroundColorExpression("aql:'#6584e2'")
                .isItalicExpression(AQL_TRUE)
                .build();
    }

    private TextStyleDescription getBlueBoldStyle() {
        return new ViewBuilders().newTextStyleDescription()
                .name(BLUE_BOLD_TEXT_STYLE_NAME)
                .foregroundColorExpression("aql:'#45B8EA'")
                .isBoldExpression(AQL_TRUE)
                .build();
    }

    private TextStyleDescription getNormalStyle() {
        return new ViewBuilders().newTextStyleDescription()
                .name(NORMAL_TEXT_STYLE_NAME)
                .build();
    }

    private FixedColor fixedColor(String name, String value) {
        return new ViewBuilders().newFixedColor()
                .name(name)
                .value(value)
                .build();
    }
}
