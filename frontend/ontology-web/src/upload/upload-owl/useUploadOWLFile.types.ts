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
export interface UseUploadOWLFileValue {
  uploadOWLFile: (editingContextId: string, file: File) => void;
  loading: boolean;
  uploadedDocument: GQLUploadOWLFileSuccessPayload | null;
}

export interface UseUploadOWLFileState {
  loading: boolean;
  uploadedDocument: GQLUploadOWLFileSuccessPayload | null;
}

export interface GQLUploadOWLFileMutationVariables {
  input: GQLUploadOWLFileInput;
}

export interface GQLUploadOWLFileInput {
  id: string;
  editingContextId: string;
  file: File | null;
}

export interface GQLUploadOWLFileMutationData {
  uploadOWLFile: GQLUploadOWLFilePayload;
}

export interface GQLUploadOWLFilePayload {
  __typename: string;
}

export interface GQLErrorPayload extends GQLUploadOWLFilePayload {
  message: string;
}

export interface GQLUploadOWLFileSuccessPayload extends GQLUploadOWLFilePayload {
  report: string;
}
