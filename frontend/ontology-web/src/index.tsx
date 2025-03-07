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
import { loadDevMessages, loadErrorMessages } from '@apollo/client/dev';
import {
  SiriusWebApplication,
  footerExtensionPoint,
  navigationBarIconExtensionPoint,
  navigationBarMenuHelpURLExtensionPoint,
} from '@eclipse-sirius/sirius-web-application';
import { createRoot } from 'react-dom/client';
import { OntologyNavigationBarIcon } from './core/OntologyNavigationBarIcon';
import { httpOrigin, wsOrigin } from './core/URL';
import { Footer } from './footer/Footer';

import { ExtensionRegistry } from '@eclipse-sirius/sirius-components-core';
import { ExtensionRegistryMergeStrategy } from './extension/ExtensionRegistryMergeStrategy';

import { treeItemContextMenuEntryExtensionPoint } from '@eclipse-sirius/sirius-components-trees';
import { ObjectTreeItemContextMenuContribution } from './explorer/ObjectTreeItemContextMenuContribution';
import './fonts.css';
import './portals.css';
import './ReactFlow.css';
import './reset.css';
import './variables.css';

if (process.env.NODE_ENV !== 'production') {
  loadDevMessages();
  loadErrorMessages();
}

const extensionRegistry = new ExtensionRegistry();

// Help component contribution
// obeoCloudPlatformOptions.extensionRegistry.addComponent(navigationBarMenuIconExtensionPoint, {
//   identifier: `ontology_${navigationBarMenuIconExtensionPoint.identifier}`,
//   Component: () => <OntologyIcon />,
// });
extensionRegistry.putData(navigationBarMenuHelpURLExtensionPoint, {
  identifier: `ontology_${navigationBarMenuHelpURLExtensionPoint.identifier}`,
  data: `${httpOrigin}/doc/user.html`,
});

// Footer contribution
extensionRegistry.addComponent(footerExtensionPoint, {
  identifier: `ontology_${footerExtensionPoint.identifier}`,
  Component: Footer,
});

// Main icon contribution
extensionRegistry.addComponent(navigationBarIconExtensionPoint, {
  identifier: `ontology_${navigationBarIconExtensionPoint.identifier}`,
  Component: OntologyNavigationBarIcon,
});

/*******************************************************************************
 *
 * Tree item context menu
 *
 * Used to register new components in the tree item context menu
 *
 *******************************************************************************/
extensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: `ontology_${treeItemContextMenuEntryExtensionPoint.identifier}_object`,
  Component: ObjectTreeItemContextMenuContribution,
});

const extensionRegistryMergeStrategy = new ExtensionRegistryMergeStrategy();

const container = document.getElementById('root');
const root = createRoot(container!);
root.render(
  <SiriusWebApplication
    httpOrigin={httpOrigin}
    wsOrigin={wsOrigin}
    extensionRegistryMergeStrategy={extensionRegistryMergeStrategy}
    extensionRegistry={extensionRegistry}
  />
);
