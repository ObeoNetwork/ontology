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
import fr.obeo.ontology.ontologymm.OntologyPackage;
import fr.obeo.ontology.ontologymm.OrganizationInformation;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Organization Information</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl#getName <em>Name</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl#getBusinessDomains <em>Business
 * Domains</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl#getDataOwners <em>Data Owners</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.OrganizationInformationImpl#getDataSources <em>Data Sources</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OrganizationInformationImpl extends MinimalEObjectImpl.Container implements OrganizationInformation {
    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getBusinessDomains() <em>Business Domains</em>}' containment reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getBusinessDomains()
     * @generated
     * @ordered
     */
    protected EList<BusinessDomain> businessDomains;

    /**
     * The cached value of the '{@link #getDataOwners() <em>Data Owners</em>}' containment reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDataOwners()
     * @generated
     * @ordered
     */
    protected EList<DataOwner> dataOwners;

    /**
     * The cached value of the '{@link #getDataSources() <em>Data Sources</em>}' containment reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDataSources()
     * @generated
     * @ordered
     */
    protected EList<DataSource> dataSources;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected OrganizationInformationImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return OntologyPackage.Literals.ORGANIZATION_INFORMATION;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OntologyPackage.ORGANIZATION_INFORMATION__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<BusinessDomain> getBusinessDomains() {
        if (businessDomains == null) {
            businessDomains = new EObjectContainmentEList<BusinessDomain>(BusinessDomain.class, this, OntologyPackage.ORGANIZATION_INFORMATION__BUSINESS_DOMAINS);
        }
        return businessDomains;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<DataOwner> getDataOwners() {
        if (dataOwners == null) {
            dataOwners = new EObjectContainmentEList<DataOwner>(DataOwner.class, this, OntologyPackage.ORGANIZATION_INFORMATION__DATA_OWNERS);
        }
        return dataOwners;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<DataSource> getDataSources() {
        if (dataSources == null) {
            dataSources = new EObjectContainmentEList<DataSource>(DataSource.class, this, OntologyPackage.ORGANIZATION_INFORMATION__DATA_SOURCES);
        }
        return dataSources;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case OntologyPackage.ORGANIZATION_INFORMATION__BUSINESS_DOMAINS:
                return ((InternalEList<?>) getBusinessDomains()).basicRemove(otherEnd, msgs);
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_OWNERS:
                return ((InternalEList<?>) getDataOwners()).basicRemove(otherEnd, msgs);
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_SOURCES:
                return ((InternalEList<?>) getDataSources()).basicRemove(otherEnd, msgs);
            default:
                return super.eInverseRemove(otherEnd, featureID, msgs);
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case OntologyPackage.ORGANIZATION_INFORMATION__NAME:
                return getName();
            case OntologyPackage.ORGANIZATION_INFORMATION__BUSINESS_DOMAINS:
                return getBusinessDomains();
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_OWNERS:
                return getDataOwners();
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_SOURCES:
                return getDataSources();
            default:
                return super.eGet(featureID, resolve, coreType);
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case OntologyPackage.ORGANIZATION_INFORMATION__NAME:
                setName((String) newValue);
                return;
            case OntologyPackage.ORGANIZATION_INFORMATION__BUSINESS_DOMAINS:
                getBusinessDomains().clear();
                getBusinessDomains().addAll((Collection<? extends BusinessDomain>) newValue);
                return;
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_OWNERS:
                getDataOwners().clear();
                getDataOwners().addAll((Collection<? extends DataOwner>) newValue);
                return;
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_SOURCES:
                getDataSources().clear();
                getDataSources().addAll((Collection<? extends DataSource>) newValue);
                return;
            default:
                super.eSet(featureID, newValue);
                return;
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case OntologyPackage.ORGANIZATION_INFORMATION__NAME:
                setName(NAME_EDEFAULT);
                return;
            case OntologyPackage.ORGANIZATION_INFORMATION__BUSINESS_DOMAINS:
                getBusinessDomains().clear();
                return;
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_OWNERS:
                getDataOwners().clear();
                return;
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_SOURCES:
                getDataSources().clear();
                return;
            default:
                super.eUnset(featureID);
                return;
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case OntologyPackage.ORGANIZATION_INFORMATION__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case OntologyPackage.ORGANIZATION_INFORMATION__BUSINESS_DOMAINS:
                return businessDomains != null && !businessDomains.isEmpty();
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_OWNERS:
                return dataOwners != null && !dataOwners.isEmpty();
            case OntologyPackage.ORGANIZATION_INFORMATION__DATA_SOURCES:
                return dataSources != null && !dataSources.isEmpty();
            default:
                return super.eIsSet(featureID);
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy())
            return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (name: ");
        result.append(name);
        result.append(')');
        return result.toString();
    }

} // OrganizationInformationImpl
