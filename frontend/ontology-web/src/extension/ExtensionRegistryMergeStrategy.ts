import { ComponentExtension, DataExtension } from '@eclipse-sirius/sirius-components-core';
import { treeItemContextMenuEntryExtensionPoint } from '@eclipse-sirius/sirius-components-trees';
import {
  DefaultExtensionRegistryMergeStrategy,
  navigationBarIconExtensionPoint,
} from '@eclipse-sirius/sirius-web-application';

export class ExtensionRegistryMergeStrategy extends DefaultExtensionRegistryMergeStrategy {
  public override mergeComponentExtensions(
    identifier: string,
    existingValues: ComponentExtension<any>[],
    newValues: ComponentExtension<any>[]
  ): ComponentExtension<any>[] {
    if (identifier === navigationBarIconExtensionPoint.identifier) {
      return [...existingValues, ...newValues].filter((extension) => extension.identifier.startsWith('ontology'));
    }
    if (identifier === treeItemContextMenuEntryExtensionPoint.identifier) {
      return [...existingValues, ...newValues].filter((extension) => extension.identifier.startsWith('ontology'));
    }
    console.log(existingValues);
    console.log(newValues);

    return [...existingValues, ...newValues];
  }

  public override mergeDataExtensions(
    identifier: string,
    existingValue: DataExtension<any>,
    newValue: DataExtension<any>
  ): DataExtension<any> {
    if (identifier === 'projectSettings#tabContribution') {
      return this.mergeTabContributions(existingValue, newValue);
    }
    if (identifier === 'apolloClient#apolloClientOptionsConfigurers') {
      return this.mergeApolloClientContributions(existingValue, newValue);
    }
    if (identifier === 'navigationBarMenu#helpURL') {
      return this.mergeNavigationBarMenuHelpURL(existingValue, newValue);
    }
    return newValue;
  }

  private mergeNavigationBarMenuHelpURL(
    apolloClientOptionsConfigurers: DataExtension<any>,
    _otherApolloClientOptionsConfigurers: DataExtension<any>
  ): DataExtension<any> {
    return {
      identifier: 'ontology_navigationBarMenu#helpURL',
      data: apolloClientOptionsConfigurers.data,
    };
  }

  private mergeTabContributions(
    existingTabContributions: DataExtension<any>,
    newOcpTabContributions: DataExtension<any>
  ): DataExtension<any> {
    return {
      identifier: 'ontology_projectSettings#tabContribution',
      data: [...existingTabContributions.data, ...newOcpTabContributions.data],
    };
  }

  private mergeApolloClientContributions(
    existingApolloClientContributions: DataExtension<any>,
    newApolloClientContributions: DataExtension<any>
  ): DataExtension<any> {
    return {
      identifier: 'ontology_apolloClient#apolloClientOptionsConfigurers',
      data: [...existingApolloClientContributions.data, ...newApolloClientContributions.data],
    };
  }
}
