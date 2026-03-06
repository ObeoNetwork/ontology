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
package fr.obeo.ontology.ontologymm.impl;

import fr.obeo.ontology.ontologymm.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class OntologyFactoryImpl extends EFactoryImpl implements OntologyFactory {
    /**
     * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static OntologyFactory init() {
        try {
            OntologyFactory theOntologyFactory = (OntologyFactory) EPackage.Registry.INSTANCE.getEFactory(OntologyPackage.eNS_URI);
            if (theOntologyFactory != null) {
                return theOntologyFactory;
            }
        } catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new OntologyFactoryImpl();
    }

    /**
     * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public OntologyFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case OntologyPackage.ORGANIZATION_INFORMATION:
                return createOrganizationInformation();
            case OntologyPackage.BUSINESS_DOMAIN:
                return createBusinessDomain();
            case OntologyPackage.DATA_SOURCE:
                return createDataSource();
            case OntologyPackage.DATA_OWNER:
                return createDataOwner();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public OrganizationInformation createOrganizationInformation() {
        OrganizationInformationImpl organizationInformation = new OrganizationInformationImpl();
        return organizationInformation;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public BusinessDomain createBusinessDomain() {
        BusinessDomainImpl businessDomain = new BusinessDomainImpl();
        return businessDomain;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public DataSource createDataSource() {
        DataSourceImpl dataSource = new DataSourceImpl();
        return dataSource;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public DataOwner createDataOwner() {
        DataOwnerImpl dataOwner = new DataOwnerImpl();
        return dataOwner;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public OntologyPackage getOntologyPackage() {
        return (OntologyPackage) getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @deprecated
     * @generated
     */
    @Deprecated
    public static OntologyPackage getPackage() {
        return OntologyPackage.eINSTANCE;
    }

} // OntologyFactoryImpl
