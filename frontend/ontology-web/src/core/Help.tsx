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
import IconButton from '@mui/material/IconButton';
import Link from '@mui/material/Link';
import { emphasize } from '@mui/material/styles';
import { makeStyles } from 'tss-react/mui';
import HelpIcon from '@mui/icons-material/Help';
import { httpOrigin } from '../core/URL';

const useHelpStyle = makeStyles()((theme) => ({
  onDarkBackground: {
    '&:hover': {
      backgroundColor: emphasize(theme.palette.secondary.main, 0.08),
    },
  },
}));

export const Help = () => {
  const { classes } = useHelpStyle();
  return (
    <Link
      href={`${httpOrigin}/doc/user.html`}
      rel="noopener noreferrer"
      target="_blank"
      color="inherit"
      data-testid="help-link">
      <IconButton className={classes.onDarkBackground} color="inherit">
        <HelpIcon />
      </IconButton>
    </Link>
  );
};
