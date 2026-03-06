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

import org.obeonetwork.dsl.entity.Entity;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Business Domain</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link fr.obeo.ontology.ontologymm.BusinessDomain#getName <em>Name</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.BusinessDomain#getDescription <em>Description</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.BusinessDomain#getEntities <em>Entities</em>}</li>
 * </ul>
 *
 * @see fr.obeo.ontology.ontologymm.OntologyPackage#getBusinessDomain()
 * @model
 * @generated
 */
public interface BusinessDomain extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getBusinessDomain_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link fr.obeo.ontology.ontologymm.BusinessDomain#getName <em>Name</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Description</em>' attribute.
     * @see #setDescription(String)
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getBusinessDomain_Description()
     * @model
     * @generated
     */
    String getDescription();

    /**
     * Sets the value of the '{@link fr.obeo.ontology.ontologymm.BusinessDomain#getDescription <em>Description</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Description</em>' attribute.
     * @see #getDescription()
     * @generated
     */
    void setDescription(String value);

    /**
     * Returns the value of the '<em><b>Entities</b></em>' reference list. The list contents are of type
     * {@link org.obeonetwork.dsl.entity.Entity}. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Entities</em>' reference list.
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#getBusinessDomain_Entities()
     * @model
     * @generated
     */
    EList<Entity> getEntities();

} // BusinessDomain
