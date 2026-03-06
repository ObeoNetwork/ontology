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

import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.OntologyPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.obeonetwork.dsl.entity.Entity;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Data Owner</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.DataOwnerImpl#getCode <em>Code</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.DataOwnerImpl#getName <em>Name</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.DataOwnerImpl#getDescription <em>Description</em>}</li>
 * <li>{@link fr.obeo.ontology.ontologymm.impl.DataOwnerImpl#getEntities <em>Entities</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataOwnerImpl extends MinimalEObjectImpl.Container implements DataOwner {
    /**
     * The default value of the '{@link #getCode() <em>Code</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getCode()
     * @generated
     * @ordered
     */
    protected static final String CODE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCode() <em>Code</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getCode()
     * @generated
     * @ordered
     */
    protected String code = CODE_EDEFAULT;

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
     * The default value of the '{@link #getDescription() <em>Description</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected static final String DESCRIPTION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected String description = DESCRIPTION_EDEFAULT;

    /**
     * The cached value of the '{@link #getEntities() <em>Entities</em>}' reference list. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getEntities()
     * @generated
     * @ordered
     */
    protected EList<Entity> entities;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected DataOwnerImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return OntologyPackage.Literals.DATA_OWNER;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setCode(String newCode) {
        String oldCode = code;
        code = newCode;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OntologyPackage.DATA_OWNER__CODE, oldCode, code));
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
            eNotify(new ENotificationImpl(this, Notification.SET, OntologyPackage.DATA_OWNER__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setDescription(String newDescription) {
        String oldDescription = description;
        description = newDescription;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, OntologyPackage.DATA_OWNER__DESCRIPTION, oldDescription, description));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<Entity> getEntities() {
        if (entities == null) {
            entities = new EObjectResolvingEList<Entity>(Entity.class, this, OntologyPackage.DATA_OWNER__ENTITIES);
        }
        return entities;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case OntologyPackage.DATA_OWNER__CODE:
                return getCode();
            case OntologyPackage.DATA_OWNER__NAME:
                return getName();
            case OntologyPackage.DATA_OWNER__DESCRIPTION:
                return getDescription();
            case OntologyPackage.DATA_OWNER__ENTITIES:
                return getEntities();
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
            case OntologyPackage.DATA_OWNER__CODE:
                setCode((String) newValue);
                return;
            case OntologyPackage.DATA_OWNER__NAME:
                setName((String) newValue);
                return;
            case OntologyPackage.DATA_OWNER__DESCRIPTION:
                setDescription((String) newValue);
                return;
            case OntologyPackage.DATA_OWNER__ENTITIES:
                getEntities().clear();
                getEntities().addAll((Collection<? extends Entity>) newValue);
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
            case OntologyPackage.DATA_OWNER__CODE:
                setCode(CODE_EDEFAULT);
                return;
            case OntologyPackage.DATA_OWNER__NAME:
                setName(NAME_EDEFAULT);
                return;
            case OntologyPackage.DATA_OWNER__DESCRIPTION:
                setDescription(DESCRIPTION_EDEFAULT);
                return;
            case OntologyPackage.DATA_OWNER__ENTITIES:
                getEntities().clear();
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
            case OntologyPackage.DATA_OWNER__CODE:
                return CODE_EDEFAULT == null ? code != null : !CODE_EDEFAULT.equals(code);
            case OntologyPackage.DATA_OWNER__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case OntologyPackage.DATA_OWNER__DESCRIPTION:
                return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
            case OntologyPackage.DATA_OWNER__ENTITIES:
                return entities != null && !entities.isEmpty();
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
        result.append(" (code: ");
        result.append(code);
        result.append(", name: ");
        result.append(name);
        result.append(", description: ");
        result.append(description);
        result.append(')');
        return result.toString();
    }

} // DataOwnerImpl
