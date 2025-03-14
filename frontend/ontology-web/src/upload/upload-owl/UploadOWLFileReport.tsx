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

import GetAppIcon from '@mui/icons-material/GetApp';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { Theme } from '@mui/material/styles';
import { useState } from 'react';
import { makeStyles } from 'tss-react/mui';
import { UploadOWLFileReportProps, UploadOWLFileReportState } from './UploadOWLFileReport.types';

const useUploadOWLFileReportStyles = makeStyles()((theme: Theme) => ({
  report: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing(2),
  },
  message: {
    color: theme.palette.text.secondary,
  },
}));

export const UploadOWLFileReport = ({ uploadedDocument }: UploadOWLFileReportProps) => {
  const [state, setState] = useState<UploadOWLFileReportState>({
    downloaded: false,
  });

  const onDownloadReport = () => {
    if (uploadedDocument) {
      const { report } = uploadedDocument;

      const fileName: string = 'upload-document-report.txt';
      const blob: Blob = new Blob([report], { type: 'text/plain' });
      const hyperlink: HTMLAnchorElement = document.createElement('a');
      hyperlink.setAttribute('download', fileName);
      hyperlink.setAttribute('href', window.URL.createObjectURL(blob));
      hyperlink.click();

      setState((prevState) => ({ ...prevState, downloaded: true }));
    }
  };

  const { classes } = useUploadOWLFileReportStyles();

  if (!uploadedDocument) {
    return null;
  }

  return (
    <div className={classes.report}>
      <Typography variant="body1">The document has been successfully uploaded</Typography>

      {uploadedDocument?.report ? (
        <Button
          variant="outlined"
          size="small"
          disabled={state.downloaded}
          color="primary"
          type="button"
          form="upload-form-id"
          startIcon={<GetAppIcon />}
          data-testid="upload-document-download-report"
          onClick={() => onDownloadReport()}>
          Download report
        </Button>
      ) : null}
    </div>
  );
};
