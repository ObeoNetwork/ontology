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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Organization Information</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getName <em>Name</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getBusinessDomains <em>Business Domains</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getDataOwners <em>Data Owners</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getDataSources <em>Data Sources</em>}</li>
 * </ul>
 *
 * @see fr.obeo.ontology.ontologymm.OntologyPackage#getOrganizationInformation()
 * @model
 * @generated
 */
public interface OrganizationInformation extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getOrganizationInformation_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getName <em>Name</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Business Domains</b></em>' containment reference list. The list contents are of
     * type {@link fr.obeo.ontology.ontologymm.BusinessDomain}. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Business Domains</em>' containment reference list.
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getOrganizationInformation_BusinessDomains()
     * @model containment="true"
     * @generated
     */
    EList<BusinessDomain> getBusinessDomains();

    /**
     * Returns the value of the '<em><b>Data Owners</b></em>' containment reference list. The list contents are of type
     * {@link fr.obeo.ontology.ontologymm.DataOwner}. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Data Owners</em>' containment reference list.
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getOrganizationInformation_DataOwners()
     * @model containment="true"
     * @generated
     */
    EList<DataOwner> getDataOwners();

    /**
     * Returns the value of the '<em><b>Data Sources</b></em>' containment reference list. The list contents are of type
     * {@link fr.obeo.ontology.ontologymm.DataSource}. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Data Sources</em>' containment reference list.
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getOrganizationInformation_DataSources()
     * @model containment="true"
     * @generated
     */
    EList<DataSource> getDataSources();

} // OrganizationInformation
