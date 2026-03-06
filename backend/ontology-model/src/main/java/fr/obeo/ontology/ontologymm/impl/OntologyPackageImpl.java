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

import fr.obeo.ontology.ontologymm.BusinessDomain;
import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.DataSource;
import fr.obeo.ontology.ontologymm.OntologyFactory;
import fr.obeo.ontology.ontologymm.OntologyPackage;
import fr.obeo.ontology.ontologymm.OrganizationInformation;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.obeonetwork.dsl.entity.EntityPackage;

import org.obeonetwork.dsl.environment.EnvironmentPackage;

import org.obeonetwork.dsl.technicalid.TechnicalIDPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class OntologyPackageImpl extends EPackageImpl implements OntologyPackage {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass organizationInformationEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass businessDomainEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass dataSourceEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass dataOwnerEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
     * EPackage.Registry} by the package package URI value.
     * <p>
     * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
     * performs initialization of the package, or returns the registered package, if one already exists. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see fr.obeo.ontology.ontologymm.OntologyPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private OntologyPackageImpl() {
        super(eNS_URI, OntologyFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     *
     * <p>
     * This method is used to initialize {@link OntologyPackage#eINSTANCE} when that field is accessed. Clients should
     * not invoke it directly. Instead, they should simply access that field to obtain the package. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static OntologyPackage init() {
        if (isInited)
            return (OntologyPackage) EPackage.Registry.INSTANCE.getEPackage(OntologyPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredOntologyPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        OntologyPackageImpl theOntologyPackage = registeredOntologyPackage instanceof OntologyPackageImpl ? (OntologyPackageImpl) registeredOntologyPackage : new OntologyPackageImpl();

        isInited = true;

        // Initialize simple dependencies
        EntityPackage.eINSTANCE.eClass();
        EnvironmentPackage.eINSTANCE.eClass();
        TechnicalIDPackage.eINSTANCE.eClass();

        // Create package meta-data objects
        theOntologyPackage.createPackageContents();

        // Initialize created meta-data
        theOntologyPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theOntologyPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(OntologyPackage.eNS_URI, theOntologyPackage);
        return theOntologyPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getOrganizationInformation() {
        return organizationInformationEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getOrganizationInformation_Name() {
        return (EAttribute) organizationInformationEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getOrganizationInformation_BusinessDomains() {
        return (EReference) organizationInformationEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getOrganizationInformation_DataOwners() {
        return (EReference) organizationInformationEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getOrganizationInformation_DataSources() {
        return (EReference) organizationInformationEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getBusinessDomain() {
        return businessDomainEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getBusinessDomain_Name() {
        return (EAttribute) businessDomainEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getBusinessDomain_Description() {
        return (EAttribute) businessDomainEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getBusinessDomain_Entities() {
        return (EReference) businessDomainEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getDataSource() {
        return dataSourceEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getDataSource_Code() {
        return (EAttribute) dataSourceEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getDataSource_Name() {
        return (EAttribute) dataSourceEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getDataSource_Description() {
        return (EAttribute) dataSourceEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getDataSource_Entities() {
        return (EReference) dataSourceEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getDataOwner() {
        return dataOwnerEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getDataOwner_Code() {
        return (EAttribute) dataOwnerEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getDataOwner_Name() {
        return (EAttribute) dataOwnerEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getDataOwner_Description() {
        return (EAttribute) dataOwnerEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getDataOwner_Entities() {
        return (EReference) dataOwnerEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public OntologyFactory getOntologyFactory() {
        return (OntologyFactory) getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package. This method is guarded to have no affect on any invocation but
     * its first. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void createPackageContents() {
        if (isCreated)
            return;
        isCreated = true;

        // Create classes and their features
        organizationInformationEClass = createEClass(ORGANIZATION_INFORMATION);
        createEAttribute(organizationInformationEClass, ORGANIZATION_INFORMATION__NAME);
        createEReference(organizationInformationEClass, ORGANIZATION_INFORMATION__BUSINESS_DOMAINS);
        createEReference(organizationInformationEClass, ORGANIZATION_INFORMATION__DATA_OWNERS);
        createEReference(organizationInformationEClass, ORGANIZATION_INFORMATION__DATA_SOURCES);

        businessDomainEClass = createEClass(BUSINESS_DOMAIN);
        createEAttribute(businessDomainEClass, BUSINESS_DOMAIN__NAME);
        createEAttribute(businessDomainEClass, BUSINESS_DOMAIN__DESCRIPTION);
        createEReference(businessDomainEClass, BUSINESS_DOMAIN__ENTITIES);

        dataSourceEClass = createEClass(DATA_SOURCE);
        createEAttribute(dataSourceEClass, DATA_SOURCE__CODE);
        createEAttribute(dataSourceEClass, DATA_SOURCE__NAME);
        createEAttribute(dataSourceEClass, DATA_SOURCE__DESCRIPTION);
        createEReference(dataSourceEClass, DATA_SOURCE__ENTITIES);

        dataOwnerEClass = createEClass(DATA_OWNER);
        createEAttribute(dataOwnerEClass, DATA_OWNER__CODE);
        createEAttribute(dataOwnerEClass, DATA_OWNER__NAME);
        createEAttribute(dataOwnerEClass, DATA_OWNER__DESCRIPTION);
        createEReference(dataOwnerEClass, DATA_OWNER__ENTITIES);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model. This method is guarded to have no affect on any
     * invocation but its first. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized)
            return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        EntityPackage theEntityPackage = (EntityPackage) EPackage.Registry.INSTANCE.getEPackage(EntityPackage.eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes

        // Initialize classes, features, and operations; add parameters
        initEClass(organizationInformationEClass, OrganizationInformation.class, "OrganizationInformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getOrganizationInformation_Name(), ecorePackage.getEString(), "name", null, 0, 1, OrganizationInformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE,
                !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOrganizationInformation_BusinessDomains(), this.getBusinessDomain(), null, "businessDomains", null, 0, -1, OrganizationInformation.class, !IS_TRANSIENT, !IS_VOLATILE,
                IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOrganizationInformation_DataOwners(), this.getDataOwner(), null, "dataOwners", null, 0, -1, OrganizationInformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
                IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOrganizationInformation_DataSources(), this.getDataSource(), null, "dataSources", null, 0, -1, OrganizationInformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
                IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(businessDomainEClass, BusinessDomain.class, "BusinessDomain", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getBusinessDomain_Name(), ecorePackage.getEString(), "name", null, 0, 1, BusinessDomain.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getBusinessDomain_Description(), ecorePackage.getEString(), "description", null, 0, 1, BusinessDomain.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getBusinessDomain_Entities(), theEntityPackage.getEntity(), null, "entities", null, 0, -1, BusinessDomain.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE,
                IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(dataSourceEClass, DataSource.class, "DataSource", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDataSource_Code(), ecorePackage.getEString(), "code", null, 0, 1, DataSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDataSource_Name(), ecorePackage.getEString(), "name", null, 0, 1, DataSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDataSource_Description(), ecorePackage.getEString(), "description", null, 0, 1, DataSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDataSource_Entities(), theEntityPackage.getEntity(), null, "entities", null, 0, -1, DataSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE,
                IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(dataOwnerEClass, DataOwner.class, "DataOwner", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDataOwner_Code(), ecorePackage.getEString(), "code", null, 0, 1, DataOwner.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getDataOwner_Name(), ecorePackage.getEString(), "name", null, 0, 1, DataOwner.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getDataOwner_Description(), ecorePackage.getEString(), "description", null, 0, 1, DataOwner.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEReference(getDataOwner_Entities(), theEntityPackage.getEntity(), null, "entities", null, 0, -1, DataOwner.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE,
                IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        createResource(eNS_URI);
    }

} // OntologyPackageImpl
