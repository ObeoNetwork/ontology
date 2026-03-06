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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each operation of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see fr.obeo.ontology.ontologymm.OntologyFactory
 * @model kind="package"
 * @generated
 */
public interface OntologyPackage extends EPackage {
    /**
     * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String eNAME = "ontologymm";

    /**
     * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String eNS_URI = "https://www.eclipse.org/ontology";

    /**
     * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String eNS_PREFIX = "ontology";

    /**
     * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    OntologyPackage eINSTANCE = fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl.init();

    /**
     * The meta object id for the '{@link fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl <em>Organization
     * Information</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl
     * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getOrganizationInformation()
     * @generated
     */
    int ORGANIZATION_INFORMATION = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int ORGANIZATION_INFORMATION__NAME = 0;

    /**
     * The feature id for the '<em><b>Business Domains</b></em>' containment reference list. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int ORGANIZATION_INFORMATION__BUSINESS_DOMAINS = 1;

    /**
     * The feature id for the '<em><b>Data Owners</b></em>' containment reference list. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int ORGANIZATION_INFORMATION__DATA_OWNERS = 2;

    /**
     * The feature id for the '<em><b>Data Sources</b></em>' containment reference list. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int ORGANIZATION_INFORMATION__DATA_SOURCES = 3;

    /**
     * The number of structural features of the '<em>Organization Information</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int ORGANIZATION_INFORMATION_FEATURE_COUNT = 4;

    /**
     * The number of operations of the '<em>Organization Information</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int ORGANIZATION_INFORMATION_OPERATION_COUNT = 0;

    /**
     * The meta object id for the '{@link fr.obeo.ontology.ontologymm.impl.BusinessDomainImpl <em>Business Domain</em>}'
     * class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see fr.obeo.ontology.ontologymm.impl.BusinessDomainImpl
     * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getBusinessDomain()
     * @generated
     */
    int BUSINESS_DOMAIN = 1;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int BUSINESS_DOMAIN__NAME = 0;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int BUSINESS_DOMAIN__DESCRIPTION = 1;

    /**
     * The feature id for the '<em><b>Entities</b></em>' reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int BUSINESS_DOMAIN__ENTITIES = 2;

    /**
     * The number of structural features of the '<em>Business Domain</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int BUSINESS_DOMAIN_FEATURE_COUNT = 3;

    /**
     * The number of operations of the '<em>Business Domain</em>' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int BUSINESS_DOMAIN_OPERATION_COUNT = 0;

    /**
     * The meta object id for the '{@link fr.obeo.ontology.ontologymm.impl.DataSourceImpl <em>Data Source</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see fr.obeo.ontology.ontologymm.impl.DataSourceImpl
     * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getDataSource()
     * @generated
     */
    int DATA_SOURCE = 2;

    /**
     * The feature id for the '<em><b>Code</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_SOURCE__CODE = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_SOURCE__NAME = 1;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_SOURCE__DESCRIPTION = 2;

    /**
     * The feature id for the '<em><b>Entities</b></em>' reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_SOURCE__ENTITIES = 3;

    /**
     * The number of structural features of the '<em>Data Source</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int DATA_SOURCE_FEATURE_COUNT = 4;

    /**
     * The number of operations of the '<em>Data Source</em>' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_SOURCE_OPERATION_COUNT = 0;

    /**
     * The meta object id for the '{@link fr.obeo.ontology.ontologymm.impl.DataOwnerImpl <em>Data Owner</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see fr.obeo.ontology.ontologymm.impl.DataOwnerImpl
     * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getDataOwner()
     * @generated
     */
    int DATA_OWNER = 3;

    /**
     * The feature id for the '<em><b>Code</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_OWNER__CODE = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_OWNER__NAME = 1;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_OWNER__DESCRIPTION = 2;

    /**
     * The feature id for the '<em><b>Entities</b></em>' reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_OWNER__ENTITIES = 3;

    /**
     * The number of structural features of the '<em>Data Owner</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int DATA_OWNER_FEATURE_COUNT = 4;

    /**
     * The number of operations of the '<em>Data Owner</em>' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int DATA_OWNER_OPERATION_COUNT = 0;

    /**
     * Returns the meta object for class '{@link fr.obeo.ontology.ontologymm.OrganizationInformation <em>Organization
     * Information</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Organization Information</em>'.
     * @see fr.obeo.ontology.ontologymm.OrganizationInformation
     * @generated
     */
    EClass getOrganizationInformation();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getName
     * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see fr.obeo.ontology.ontologymm.OrganizationInformation#getName()
     * @see #getOrganizationInformation()
     * @generated
     */
    EAttribute getOrganizationInformation_Name();

    /**
     * Returns the meta object for the containment reference list
     * '{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getBusinessDomains <em>Business Domains</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Business Domains</em>'.
     * @see fr.obeo.ontology.ontologymm.OrganizationInformation#getBusinessDomains()
     * @see #getOrganizationInformation()
     * @generated
     */
    EReference getOrganizationInformation_BusinessDomains();

    /**
     * Returns the meta object for the containment reference list
     * '{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getDataOwners <em>Data Owners</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Data Owners</em>'.
     * @see fr.obeo.ontology.ontologymm.OrganizationInformation#getDataOwners()
     * @see #getOrganizationInformation()
     * @generated
     */
    EReference getOrganizationInformation_DataOwners();

    /**
     * Returns the meta object for the containment reference list
     * '{@link fr.obeo.ontology.ontologymm.OrganizationInformation#getDataSources <em>Data Sources</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Data Sources</em>'.
     * @see fr.obeo.ontology.ontologymm.OrganizationInformation#getDataSources()
     * @see #getOrganizationInformation()
     * @generated
     */
    EReference getOrganizationInformation_DataSources();

    /**
     * Returns the meta object for class '{@link fr.obeo.ontology.ontologymm.BusinessDomain <em>Business Domain</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Business Domain</em>'.
     * @see fr.obeo.ontology.ontologymm.BusinessDomain
     * @generated
     */
    EClass getBusinessDomain();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.BusinessDomain#getName
     * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see fr.obeo.ontology.ontologymm.BusinessDomain#getName()
     * @see #getBusinessDomain()
     * @generated
     */
    EAttribute getBusinessDomain_Name();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.BusinessDomain#getDescription
     * <em>Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Description</em>'.
     * @see fr.obeo.ontology.ontologymm.BusinessDomain#getDescription()
     * @see #getBusinessDomain()
     * @generated
     */
    EAttribute getBusinessDomain_Description();

    /**
     * Returns the meta object for the reference list '{@link fr.obeo.ontology.ontologymm.BusinessDomain#getEntities
     * <em>Entities</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the reference list '<em>Entities</em>'.
     * @see fr.obeo.ontology.ontologymm.BusinessDomain#getEntities()
     * @see #getBusinessDomain()
     * @generated
     */
    EReference getBusinessDomain_Entities();

    /**
     * Returns the meta object for class '{@link fr.obeo.ontology.ontologymm.DataSource <em>Data Source</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Data Source</em>'.
     * @see fr.obeo.ontology.ontologymm.DataSource
     * @generated
     */
    EClass getDataSource();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.DataSource#getCode <em>Code</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Code</em>'.
     * @see fr.obeo.ontology.ontologymm.DataSource#getCode()
     * @see #getDataSource()
     * @generated
     */
    EAttribute getDataSource_Code();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.DataSource#getName <em>Name</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see fr.obeo.ontology.ontologymm.DataSource#getName()
     * @see #getDataSource()
     * @generated
     */
    EAttribute getDataSource_Name();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.DataSource#getDescription
     * <em>Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Description</em>'.
     * @see fr.obeo.ontology.ontologymm.DataSource#getDescription()
     * @see #getDataSource()
     * @generated
     */
    EAttribute getDataSource_Description();

    /**
     * Returns the meta object for the reference list '{@link fr.obeo.ontology.ontologymm.DataSource#getEntities
     * <em>Entities</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the reference list '<em>Entities</em>'.
     * @see fr.obeo.ontology.ontologymm.DataSource#getEntities()
     * @see #getDataSource()
     * @generated
     */
    EReference getDataSource_Entities();

    /**
     * Returns the meta object for class '{@link fr.obeo.ontology.ontologymm.DataOwner <em>Data Owner</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Data Owner</em>'.
     * @see fr.obeo.ontology.ontologymm.DataOwner
     * @generated
     */
    EClass getDataOwner();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.DataOwner#getCode <em>Code</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Code</em>'.
     * @see fr.obeo.ontology.ontologymm.DataOwner#getCode()
     * @see #getDataOwner()
     * @generated
     */
    EAttribute getDataOwner_Code();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.DataOwner#getName <em>Name</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see fr.obeo.ontology.ontologymm.DataOwner#getName()
     * @see #getDataOwner()
     * @generated
     */
    EAttribute getDataOwner_Name();

    /**
     * Returns the meta object for the attribute '{@link fr.obeo.ontology.ontologymm.DataOwner#getDescription
     * <em>Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Description</em>'.
     * @see fr.obeo.ontology.ontologymm.DataOwner#getDescription()
     * @see #getDataOwner()
     * @generated
     */
    EAttribute getDataOwner_Description();

    /**
     * Returns the meta object for the reference list '{@link fr.obeo.ontology.ontologymm.DataOwner#getEntities
     * <em>Entities</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the reference list '<em>Entities</em>'.
     * @see fr.obeo.ontology.ontologymm.DataOwner#getEntities()
     * @see #getDataOwner()
     * @generated
     */
    EReference getDataOwner_Entities();

    /**
     * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the factory that creates the instances of the model.
     * @generated
     */
    OntologyFactory getOntologyFactory();

    /**
     * <!-- begin-user-doc --> Defines literals for the meta objects that represent
     * <ul>
     * <li>each class,</li>
     * <li>each feature of each class,</li>
     * <li>each operation of each class,</li>
     * <li>each enum,</li>
     * <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    interface Literals {
        /**
         * The meta object literal for the '{@link fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl
         * <em>Organization Information</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl
         * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getOrganizationInformation()
         * @generated
         */
        EClass ORGANIZATION_INFORMATION = eINSTANCE.getOrganizationInformation();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute ORGANIZATION_INFORMATION__NAME = eINSTANCE.getOrganizationInformation_Name();

        /**
         * The meta object literal for the '<em><b>Business Domains</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference ORGANIZATION_INFORMATION__BUSINESS_DOMAINS = eINSTANCE.getOrganizationInformation_BusinessDomains();

        /**
         * The meta object literal for the '<em><b>Data Owners</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference ORGANIZATION_INFORMATION__DATA_OWNERS = eINSTANCE.getOrganizationInformation_DataOwners();

        /**
         * The meta object literal for the '<em><b>Data Sources</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference ORGANIZATION_INFORMATION__DATA_SOURCES = eINSTANCE.getOrganizationInformation_DataSources();

        /**
         * The meta object literal for the '{@link fr.obeo.ontology.ontologymm.impl.BusinessDomainImpl <em>Business
         * Domain</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see fr.obeo.ontology.ontologymm.impl.BusinessDomainImpl
         * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getBusinessDomain()
         * @generated
         */
        EClass BUSINESS_DOMAIN = eINSTANCE.getBusinessDomain();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute BUSINESS_DOMAIN__NAME = eINSTANCE.getBusinessDomain_Name();

        /**
         * The meta object literal for the '<em><b>Description</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute BUSINESS_DOMAIN__DESCRIPTION = eINSTANCE.getBusinessDomain_Description();

        /**
         * The meta object literal for the '<em><b>Entities</b></em>' reference list feature. <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference BUSINESS_DOMAIN__ENTITIES = eINSTANCE.getBusinessDomain_Entities();

        /**
         * The meta object literal for the '{@link fr.obeo.ontology.ontologymm.impl.DataSourceImpl <em>Data
         * Source</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see fr.obeo.ontology.ontologymm.impl.DataSourceImpl
         * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getDataSource()
         * @generated
         */
        EClass DATA_SOURCE = eINSTANCE.getDataSource();

        /**
         * The meta object literal for the '<em><b>Code</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute DATA_SOURCE__CODE = eINSTANCE.getDataSource_Code();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute DATA_SOURCE__NAME = eINSTANCE.getDataSource_Name();

        /**
         * The meta object literal for the '<em><b>Description</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute DATA_SOURCE__DESCRIPTION = eINSTANCE.getDataSource_Description();

        /**
         * The meta object literal for the '<em><b>Entities</b></em>' reference list feature. <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference DATA_SOURCE__ENTITIES = eINSTANCE.getDataSource_Entities();

        /**
         * The meta object literal for the '{@link fr.obeo.ontology.ontologymm.impl.DataOwnerImpl <em>Data Owner</em>}'
         * class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see fr.obeo.ontology.ontologymm.impl.DataOwnerImpl
         * @see fr.obeo.ontology.ontologymm.impl.OntologyPackageImpl#getDataOwner()
         * @generated
         */
        EClass DATA_OWNER = eINSTANCE.getDataOwner();

        /**
         * The meta object literal for the '<em><b>Code</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute DATA_OWNER__CODE = eINSTANCE.getDataOwner_Code();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute DATA_OWNER__NAME = eINSTANCE.getDataOwner_Name();

        /**
         * The meta object literal for the '<em><b>Description</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EAttribute DATA_OWNER__DESCRIPTION = eINSTANCE.getDataOwner_Description();

        /**
         * The meta object literal for the '<em><b>Entities</b></em>' reference list feature. <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference DATA_OWNER__ENTITIES = eINSTANCE.getDataOwner_Entities();

    }

} // OntologyPackage
