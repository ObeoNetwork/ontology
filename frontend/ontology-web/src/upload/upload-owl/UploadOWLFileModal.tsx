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
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import FormGroup from '@mui/material/FormGroup';
import { Theme } from '@mui/material/styles';
import { useState } from 'react';
import { makeStyles } from 'tss-react/mui';
import { FileUpload } from '../file-upload/FileUpload';
import { UploadOWLFileModalProps, UploadOWLFileModalState } from './UploadOWLFileModal.types';
import { UploadOWLFileReport } from './UploadOWLFileReport';
import { useUploadOWLFile } from './useUploadOWLFile';

const useFormStyles = makeStyles()((theme: Theme) => ({
  form: {
    display: 'flex',
    flexDirection: 'column',
    paddingTop: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    '& > *': {
      marginBottom: theme.spacing(2),
    },
  },
}));

export const UploadOWLFileModal = ({ editingContextId, onClose }: UploadOWLFileModalProps) => {
  const [state, setState] = useState<UploadOWLFileModalState>({
    file: null,
  });
  const { classes: styles } = useFormStyles();

  const { uploadOWLFile, loading, uploadedDocument } = useUploadOWLFile();

  const onFileSelected = (file: File) => setState((prevState) => ({ ...prevState, file }));

  const performDocumentUpload: React.FormEventHandler<HTMLFormElement> = (event) => {
    event.preventDefault();
    uploadOWLFile(editingContextId, state.file);
  };

  return (
    <Dialog open={true} onClose={onClose} aria-labelledby="dialog-title" fullWidth>
      <DialogTitle id="dialog-title">Upload new ontology model</DialogTitle>
      <DialogContent>
        <form
          id="upload-form-id"
          onSubmit={performDocumentUpload}
          encType="multipart/form-data"
          className={styles.form}>
          <FormGroup>
            <FileUpload onFileSelected={onFileSelected} fileExtensions=".xml" data-testid="file" />
          </FormGroup>
        </form>
        <UploadOWLFileReport uploadedDocument={uploadedDocument} />
      </DialogContent>
      <DialogActions>
        <Button
          variant="contained"
          disabled={!state.file || loading || !!uploadedDocument}
          color="primary"
          type="submit"
          form="upload-form-id"
          data-testid="upload-document-submit">
          Upload
        </Button>
        <Button
          variant={uploadedDocument === null ? 'outlined' : 'contained'}
          color="primary"
          type="button"
          form="upload-form-id"
          data-testid="upload-document-close"
          onClick={() => onClose()}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};
