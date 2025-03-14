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

import { gql } from '@apollo/client';
import { ServerContext, ServerContextValue, useMultiToast } from '@eclipse-sirius/sirius-components-core';
import { useContext, useState } from 'react';
import { sendFile } from '../file-upload/sendFile';
import {
  GQLErrorPayload,
  GQLUploadOWLFileMutationVariables,
  GQLUploadOWLFilePayload,
  GQLUploadOWLFileSuccessPayload,
  UseUploadOWLFileState,
  UseUploadOWLFileValue,
} from './useUploadOWLFile.types';

const uploadOWLFileMutationFile = gql`
  mutation uploadOWLFile($input: UploadOWLFileInput!) {
    uploadOWLFile(input: $input) {
      __typename
      ... on ErrorPayload {
        message
      }
      ... on UploadOWLFileSuccessPayload {
        report
      }
    }
  }
`;

const isUploadOWLFileSuccessPayload = (payload: GQLUploadOWLFilePayload): payload is GQLUploadOWLFileSuccessPayload =>
  payload.__typename === 'UploadOWLFileSuccessPayload';
const isErrorPayload = (payload: GQLUploadOWLFilePayload): payload is GQLErrorPayload =>
  payload.__typename === 'ErrorPayload';

export const useUploadOWLFile = (): UseUploadOWLFileValue => {
  const [state, setState] = useState<UseUploadOWLFileState>({
    loading: false,
    uploadedDocument: null,
  });
  const { httpOrigin } = useContext<ServerContextValue>(ServerContext);
  const { addErrorMessage } = useMultiToast();

  const uploadOWLFile = (editingContextId: string, file: File) => {
    setState((prevState) => ({ ...prevState, loading: true }));

    const variables: GQLUploadOWLFileMutationVariables = {
      input: {
        id: crypto.randomUUID(),
        editingContextId,
        file: null, // the file will be send as a part of the multipart POST query.
      },
    };

    try {
      sendFile(httpOrigin, uploadOWLFileMutationFile.loc?.source.body ?? '', variables, file).then((result) => {
        const { data, error } = result;
        if (error) {
          addErrorMessage('An unexpected error has occurred, the file uploaded may be too large');
        }
        if (data) {
          const { uploadOWLFile } = data;
          if (isErrorPayload(uploadOWLFile)) {
            const { message } = uploadOWLFile;
            addErrorMessage(message);
            setState((prevState) => ({ ...prevState, loading: false, uploadedDocument: null }));
          } else if (isUploadOWLFileSuccessPayload(uploadOWLFile)) {
            setState((prevState) => ({ ...prevState, loading: false, uploadedDocument: uploadOWLFile }));
          }
        }
      });
    } catch (exception) {
      // Handle other errors like max file size error send by the backend...
      addErrorMessage('An unexpected error has occurred, the file uploaded may be too large');
      setState((prevState) => ({ ...prevState, loading: false, uploadedDocument: null }));
    }
  };

  const { loading, uploadedDocument } = state;
  return {
    uploadOWLFile,
    loading,
    uploadedDocument,
  };
};
