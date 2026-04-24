/*******************************************************************************
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
 *******************************************************************************/
package fr.obeo.ontology.xls.upload;

import fr.obeo.ontology.ontologymm.BusinessDomain;
import fr.obeo.ontology.ontologymm.DataOwner;
import fr.obeo.ontology.ontologymm.DataSource;
import fr.obeo.ontology.ontologymm.OntologyFactory;
import fr.obeo.ontology.ontologymm.OrganizationInformation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.obeonetwork.dsl.entity.Entity;
import org.obeonetwork.dsl.entity.Root;
import org.obeonetwork.dsl.environment.Annotation;
import org.obeonetwork.dsl.environment.Attribute;
import org.obeonetwork.dsl.environment.DataType;
import org.obeonetwork.dsl.environment.EnvironmentFactory;
import org.obeonetwork.dsl.environment.MetaDataContainer;
import org.obeonetwork.dsl.environment.MultiplicityKind;
import org.obeonetwork.dsl.environment.Namespace;
import org.obeonetwork.dsl.environment.PrimitiveType;
import org.obeonetwork.dsl.environment.PrimitiveTypeKind;
import org.obeonetwork.dsl.environment.Reference;

public class EntityBuilder {
    public static final String BOOLEAN_TYPE = "Boolean";

    public static final String INT_TYPE = "Int";

    public static final String DOUBLE_TYPE = "Double";

    public static final String STRING_TYPE = "String";

    private final Map<Integer, Entity> currentLevelToEntity = new LinkedHashMap<>();

    private final Namespace namespace;

    private final Root root;

    private final OrganizationInformation organizationInformation;

    private Set<String> businessDomainNames = new LinkedHashSet<>();

    private Map<Integer, String> indexToDataOwnerName = new LinkedHashMap<>();

    private Map<Integer, String> indexToDataSourceName = new LinkedHashMap<>();

    private final Map<String, Entity> nameToEntity = new LinkedHashMap<>();

    private final Map<String, DataType> dataTypeNameToDataType = new LinkedHashMap<>();

    private final Map<String, MultiplicityKind> nameToMultiplicityKind = Map.of("1", MultiplicityKind.ONE_LITERAL, "0_N", MultiplicityKind.ZERO_STAR_LITERAL, "1_N", MultiplicityKind.ONE_STAR_LITERAL,
            "0_1",
            MultiplicityKind.ZERO_ONE_LITERAL);

    private record EntityRecord(String name, int level, List<String> attributeNames, String comment, String businessArea, List<Integer> dataSourceIndexes, List<Integer> dataOwnerIndexes) {
    }

    private final List<EntityRecord> entityRecordList = new ArrayList<>();

    public EntityBuilder() {
        this.root = initializeRoot();
        this.namespace = this.root.getOwnedNamespaces().get(0);
        this.organizationInformation = OntologyFactory.eINSTANCE.createOrganizationInformation();
    }

    private Root initializeRoot() {
        Root root = org.obeonetwork.dsl.entity.EntityFactory.eINSTANCE.createRoot();
        root.setName("Root");

        Namespace namespace = EnvironmentFactory.eINSTANCE.createNamespace();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm")
                .withZone(ZoneId.systemDefault());

        Instant instant = Instant.now();
        String formattedInstant = formatter.format(instant);
        namespace.setName("Entities imported from xls" + formattedInstant);
        root.getOwnedNamespaces().add(namespace);

        // Primitive types creation
        namespace.getTypes().addAll(this.createPrimitiveTypes());

        return root;
    }

    private List<PrimitiveType> createPrimitiveTypes() {
        PrimitiveType booleanType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        booleanType.setName(BOOLEAN_TYPE);
        booleanType.setKind(PrimitiveTypeKind.OTHER);
        dataTypeNameToDataType.put(BOOLEAN_TYPE.toLowerCase(), booleanType);

        PrimitiveType integerType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        integerType.setName(INT_TYPE);
        integerType.setKind(PrimitiveTypeKind.NUMBER);
        dataTypeNameToDataType.put(INT_TYPE.toLowerCase(), integerType);

        PrimitiveType doubleType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        doubleType.setName(DOUBLE_TYPE);
        doubleType.setKind(PrimitiveTypeKind.NUMBER);
        dataTypeNameToDataType.put(DOUBLE_TYPE.toLowerCase(), doubleType);

        PrimitiveType stringType = EnvironmentFactory.eINSTANCE.createPrimitiveType();
        stringType.setName(STRING_TYPE);
        stringType.setKind(PrimitiveTypeKind.TEXT);
        dataTypeNameToDataType.put(STRING_TYPE.toLowerCase(), stringType);

        return List.of(booleanType, integerType, doubleType, stringType);
    }

    public void addEntity(String name, int level, List<String> attributeNames, String comment, String businessArea, List<Integer> dataSourceIndexes, List<Integer> dataOwnerIndexes) {
        entityRecordList.add(new EntityRecord(name, level, attributeNames, businessArea, businessArea, dataSourceIndexes, dataOwnerIndexes));

        Entity entity = org.obeonetwork.dsl.entity.EntityFactory.eINSTANCE.createEntity();
        this.currentLevelToEntity.put(level, entity);
        entity.setName(name);

        if (level > 0) {
            entity.setSupertype(this.currentLevelToEntity.get(level - 1));
        }
        this.namespace.getTypes().add(entity);

        EList<Attribute> attributes = entity.getOwnedAttributes();
        attributeNames.stream()
                .map(s -> s.replace(" ", ""))
                .filter(s -> !s.isBlank())
                .forEach(attributeString -> {
                    List<String> attributeStrings = Arrays.stream(attributeString.split("\\|")).map(String::trim).toList();
                    Attribute attribute = EnvironmentFactory.eINSTANCE.createAttribute();
                    attribute.setName(attributeStrings.get(0));
                    if (attributeStrings.size() > 1)
                        attribute.setDescription(attributeStrings.get(1));
                    if (attributeStrings.size() > 2)
                        attribute.setType(dataTypeNameToDataType.get(attributeStrings.get(2)));
                    if (attributeStrings.size() > 3)
                        attribute.setMultiplicity(nameToMultiplicityKind.get(attributeStrings.get(3)));
                    attributes.add(attribute);
                });

        if (!comment.isBlank()) {
            MetaDataContainer metaDataContainer = EnvironmentFactory.eINSTANCE.createMetaDataContainer();
            Annotation annotation = EnvironmentFactory.eINSTANCE.createAnnotation();
            annotation.setTitle("SID");
            annotation.setBody(comment);
            metaDataContainer.getMetadatas().add(annotation);
            entity.setMetadatas(metaDataContainer);
        }

        nameToEntity.put(name, entity);
    }

    /**
     * @return al list of resource roots.
     */
    public List<EObject> build() {
        createOrganizationInformation();
        createOrganizationInformationForEntities();
        return List.of(root, organizationInformation);
    }

    private void createOrganizationInformationForEntities() {
        entityRecordList.forEach(entityRecord -> {
            Entity entity = nameToEntity.get(entityRecord.name);
            entityRecord.dataOwnerIndexes.stream()
                    .forEach(index -> organizationInformation.getDataOwners().get(index).getEntities().add(entity));
            entityRecord.dataSourceIndexes.stream()
                    .forEach(index -> organizationInformation.getDataSources().get(index).getEntities().add(entity));
            organizationInformation.getBusinessDomains().stream()
                    .filter(businessDomain -> businessDomain.getName().equals(entityRecord.businessArea))
                    .findFirst()
                    .ifPresent(businessDomain -> businessDomain.getEntities().add(entity));
        });
    }

    private void createOrganizationInformation() {
        organizationInformation.getBusinessDomains().addAll(businessDomainNames.stream()
                .map(businessDomainName -> {
                    BusinessDomain businessDomain = OntologyFactory.eINSTANCE.createBusinessDomain();
                    businessDomain.setName(businessDomainName);
                    return businessDomain;
                })
                .toList());

        organizationInformation.getDataSources().addAll(indexToDataSourceName.values().stream()
                .map(dataSourceName -> {
                    DataSource dataSource = OntologyFactory.eINSTANCE.createDataSource();
                    dataSource.setCode(dataSourceName);
                    return dataSource;
                })
                .toList());

        organizationInformation.getDataOwners().addAll(indexToDataOwnerName.values().stream()
                .map(dataOwnerName -> {
                    DataOwner dataOwner = OntologyFactory.eINSTANCE.createDataOwner();
                    dataOwner.setCode(dataOwnerName);
                    return dataOwner;
                })
                .toList());
    }

    public void addRelation(String owningEntityName, String typeEntityName, String referenceName, String referenceDescription) {
        Entity owningEntity = nameToEntity.get(owningEntityName);
        Entity typeEntity = nameToEntity.get(typeEntityName);
        if (owningEntity != null && typeEntity != null) {
            Reference reference = EnvironmentFactory.eINSTANCE.createReference();
            reference.setName(referenceName);
            reference.setDescription(referenceDescription);
            reference.setReferencedType(typeEntity);
            owningEntity.getOwnedReferences().add(reference);
        }
    }

    public void addOrganizationInformation(Set<String> businessDomainNameSet, Map<Integer, String> dataOwners, Map<Integer, String> dataSources) {
        this.businessDomainNames = businessDomainNameSet;
        this.indexToDataOwnerName = dataOwners;
        this.indexToDataSourceName = dataSources;
    }
}
