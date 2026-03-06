/**
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 */
package fr.obeo.ontology.ontologymm;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * 
 * @see fr.obeo.ontology.ontologymm.OntologyPackage
 * @generated
 */
public interface OntologyFactory extends EFactory {
    /**
     * The singleton instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    OntologyFactory eINSTANCE = fr.obeo.ontology.ontologymm.impl.OntologyFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Organization Information</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Organization Information</em>'.
     * @generated
     */
    OrganizationInformation createOrganizationInformation();

    /**
     * Returns a new object of class '<em>Business Domain</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Business Domain</em>'.
     * @generated
     */
    BusinessDomain createBusinessDomain();

    /**
     * Returns a new object of class '<em>Data Source</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Data Source</em>'.
     * @generated
     */
    DataSource createDataSource();

    /**
     * Returns a new object of class '<em>Data Owner</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Data Owner</em>'.
     * @generated
     */
    DataOwner createDataOwner();

    /**
     * Returns the package supported by this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the package supported by this factory.
     * @generated
     */
    OntologyPackage getOntologyPackage();

} // OntologyFactory
