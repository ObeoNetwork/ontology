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
import { Selection, useSelection } from '@eclipse-sirius/sirius-components-core';
import { TreeItemContextMenuComponentProps } from '@eclipse-sirius/sirius-components-trees';
import { NewObjectModal, NewRepresentationModal } from '@eclipse-sirius/sirius-web-application';
import AddIcon from '@mui/icons-material/Add';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import MenuItem from '@mui/material/MenuItem';
import { Fragment, forwardRef, useState } from 'react';
import { UploadOWLFileModal } from '../upload/upload-owl/UploadOWLFileModal';

type Modal = 'CreateNewObject' | 'CreateNewRepresentation' | 'ImportOWL';

export const ObjectTreeItemContextMenuContribution = forwardRef(
  (
    { editingContextId, treeId, item, readOnly, expandItem, onClose }: TreeItemContextMenuComponentProps,
    ref: React.ForwardedRef<HTMLLIElement>
  ) => {
    const [modal, setModal] = useState<Modal>(null);
    const { setSelection } = useSelection();

    console.log(item.kind);
    const displayNewObject = !item.kind.startsWith('siriusWeb://document');
    const displayNewRepresentation = !item.kind.startsWith('siriusWeb://document');
    const displayImportOWL = item.kind.startsWith('siriusWeb://document');

    const onObjectCreated = (selection: Selection) => {
      setSelection(selection);
      expandItem();
      onClose();
    };

    let modalElement = null;
    if (modal === 'CreateNewObject') {
      modalElement = (
        <NewObjectModal
          editingContextId={editingContextId}
          item={item}
          onObjectCreated={onObjectCreated}
          onClose={onClose}
        />
      );
    } else if (modal === 'CreateNewRepresentation') {
      modalElement = (
        <NewRepresentationModal
          editingContextId={editingContextId}
          item={item}
          onRepresentationCreated={onObjectCreated}
          onClose={onClose}
        />
      );
    } else if (modal === 'ImportOWL') {
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
            <ListItemText primary="Upload OWL file" />
          </MenuItem>
        )}
        {displayNewObject && (
          <MenuItem
            key="new-object"
            onClick={() => setModal('CreateNewObject')}
            data-testid="new-object"
            disabled={readOnly}
            ref={ref}
            aria-disabled>
            <ListItemIcon>
              <AddIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText primary="New object" />
          </MenuItem>
        )}
        {displayNewRepresentation && (
          <MenuItem
            key="new-representation"
            onClick={() => setModal('CreateNewRepresentation')}
            data-testid="new-representation"
            disabled={readOnly}
            aria-disabled>
            <ListItemIcon>
              <AddIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText primary="New representation" />
          </MenuItem>
        )}
        {modalElement}
      </Fragment>
    );
  }
);
