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
import { ServerContext, ServerContextValue } from '@eclipse-sirius/sirius-components-core';
import { TreeItemContextMenuComponentProps } from '@eclipse-sirius/sirius-components-trees';
import AddIcon from '@mui/icons-material/Add';
import GetAppIcon from '@mui/icons-material/GetApp';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import MenuItem from '@mui/material/MenuItem';
import { Fragment, forwardRef, useContext, useState } from 'react';
import { UploadOWLFileModal } from '../upload/upload-owl/UploadOWLFileModal';

type Modal = 'ImportOWL';

export const DocumentTreeItemContextMenuContribution = forwardRef(
  (
    { editingContextId, item, readOnly, onClose }: TreeItemContextMenuComponentProps,
    ref: React.ForwardedRef<HTMLLIElement>
  ) => {
    const [modal, setModal] = useState<Modal>(null);
    const { httpOrigin } = useContext<ServerContextValue>(ServerContext);

    const displayImportOWL = item.kind.startsWith('siriusWeb://document');
    const displayDownloadOWL = item.kind.startsWith('siriusWeb://document');

    let modalElement = null;
    if (modal === 'ImportOWL') {
      modalElement = <UploadOWLFileModal editingContextId={editingContextId} onClose={onClose} />;
    }

    return (
      <Fragment key="object-tree-item-context-menu-contribution">
        {displayImportOWL && (
          <MenuItem
            key="import-owl"
            onClick={() => setModal('ImportOWL')}
            data-testid="import-owl"
            disabled={readOnly}
            ref={ref}
            aria-disabled>
            <ListItemIcon>
              <AddIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText primary="Upload OWL" />
          </MenuItem>
        )}
        {displayDownloadOWL && (
          <MenuItem
            key="download-owl"
            divider
            onClick={onClose}
            component="a"
            href={`${httpOrigin}/api/editingcontexts/${editingContextId}/owl/${item.id}`}
            type="application/octet-stream"
            data-testid="download-owl"
            aria-disabled>
            <ListItemIcon>
              <GetAppIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText primary="Download OWL" aria-disabled />
          </MenuItem>
        )}
        {modalElement}
      </Fragment>
    );
  }
);
