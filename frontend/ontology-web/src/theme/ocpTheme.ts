/***********************************************************************************************
 * Copyright (c) 2024 Obeo. All Rights Reserved.
 * This software and the attached documentation are the exclusive ownership
 * of its authors and was conceded to the profit of Obeo S.A.S.
 * This software and the attached documentation are protected under the rights
 * of intellectual ownership, including the section "Titre II  Droits des auteurs (Articles L121-1 L123-12)"
 * By installing this software, you acknowledge being aware of these rights and
 * accept them, and as a consequence you must:
 * - be in possession of a valid license of use conceded by Obeo only.
 * - agree that you have read, understood, and will comply with the license terms and conditions.
 * - agree not to do anything that could conflict with intellectual ownership owned by Obeo or its beneficiaries
 * or the authors of this software.
 *
 * Should you not agree with these terms, you must stop to use this software and give it back to its legitimate owner.
 ***********************************************************************************************/

import { theme } from '@eclipse-sirius/sirius-components-core';
import { Theme, createTheme } from '@mui/material/styles';

export const baseTheme: Theme = createTheme({
  ...theme,
  palette: {
    mode: 'light',
    primary: {
      main: '#007788',
      dark: '#00535F',
      light: '#33929F',
    },
    secondary: {
      main: '#002b3c',
      dark: '#001E2A',
      light: '#335563',
    },
    text: {
      primary: '#002b3c',
      disabled: '#B3BFC5',
    },
    info: {
      main: '#2196F3',
      dark: '#1D7DCC',
      light: '#24A7FF',
    },
    divider: '#B3BFC5',
    navigation: {
      leftBackground: '#00778814',
      rightBackground: '#002B3C14',
    },
    navigationBar: {
      border: '#007788',
      background: '#002B3C',
    },
    selected: '#007788',
    action: {
      hover: '#00778826',
      selected: '#00778842',
    },
  },
  components: {
    MuiSnackbarContent: {
      styleOverrides: {
        root: {
          backgroundColor: '#009DB5',
        },
      },
    },
  },
});

export const ocpTheme: Theme = createTheme(
  {
    components: {
      MuiAvatar: {
        styleOverrides: {
          colorDefault: {
            backgroundColor: baseTheme.palette.primary.main,
          },
        },
      },
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            backgroundColor: baseTheme.palette.common.black,
          },
        },
      },
      MuiLink: {
        defaultProps: {
          underline: 'hover',
        },
      },
    },
  },
  baseTheme
);
