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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Service;

/**
 * A service to convert an XLS Workbook into an Ontology Root.
 *
 * @author lfasani
 */
@Service
public class XLSToOntologyModelConverter {

    public List<EObject> convertToOntology(Workbook workbook) {
        EntityBuilder entityBuilder = new EntityBuilder();

        Sheet sheet = workbook.getSheet(" Liste des objets & attributs");

        String previousCoreEntity = null;
        for (int iRow = 2; iRow <= 500; iRow++) {
            Row row = sheet.getRow(iRow);
            if (row == null) {
                break;
            }
            String coreEntityName = row.getCell(0).getStringCellValue();
            if (coreEntityName.isBlank()) {
                break;
            }
            String name = row.getCell(1).getStringCellValue();
            int level = (int) row.getCell(2).getNumericCellValue();
            List<String> attributes = Optional.ofNullable(row.getCell(3))
                    .map(Cell::getStringCellValue)
                    .map(s -> Arrays.asList(s.split("\n")))
                    .orElseGet(List::of);
            String comment = Optional.ofNullable(row.getCell(4))
                    .map(Cell::getStringCellValue).orElse("");
            String businessArea = Optional.ofNullable(row.getCell(5))
                    .map(Cell::getStringCellValue).orElse("");

            int numberOfDataSource = getNumbersOfCells(sheet, 6);
            List<Integer> dataSourceIndexes = new ArrayList<>();
            IntStream.range(6, numberOfDataSource + 6)
                    .forEach(i -> {
                        Optional.ofNullable(row.getCell(i))
                                .map(Cell::getStringCellValue)
                                .filter(s -> s.equals("OUI"))
                                .ifPresent(s -> dataSourceIndexes.add(i - 6));
                    });

            List<Integer> dataOwnerIndexes = new ArrayList<>();
            int dataOwnerStartIndex = 6 + numberOfDataSource;
            int numberOfDataOwner = getNumbersOfCells(sheet, dataOwnerStartIndex);
            IntStream.range(dataOwnerStartIndex, dataOwnerStartIndex + numberOfDataOwner)
                    .forEach(i -> {
                        Optional.ofNullable(row.getCell(i))
                                .map(Cell::getStringCellValue)
                                .filter(s -> s.equals("OUI"))
                                .ifPresent(s -> dataOwnerIndexes.add(i - dataOwnerStartIndex));
                    });

            if (!coreEntityName.equals(previousCoreEntity)) {
                entityBuilder.addEntity(coreEntityName, 0, List.of(), "", "", List.of(), List.of());
                previousCoreEntity = coreEntityName;
            }
            entityBuilder.addEntity(name, level, attributes, comment, businessArea, dataSourceIndexes, dataOwnerIndexes);
        }

        this.createReferences(workbook, entityBuilder);

        this.createOrganizationInformation(workbook, entityBuilder);

        return entityBuilder.build();
    }

    private void createOrganizationInformation(Workbook workbook, EntityBuilder entityBuilder) {
        Sheet sheet = workbook.getSheet(" Liste des objets & attributs");
        final int businessDomainColumnIndex = 5;
        Set<String> businessDomainNameSet = new LinkedHashSet<>();
        for (int i = 2; true; i++) {
            Optional<String> name = Optional.ofNullable(sheet.getRow(i))
                    .map(row -> row.getCell(businessDomainColumnIndex))
                    .map(Cell::getStringCellValue)
                    .filter(str -> !str.isBlank());
            if (name.isPresent()) {
                businessDomainNameSet.add(name.get());
            } else break;
        }

        // Add data sources
        final int dataSourceColumnStartIndex = businessDomainColumnIndex + 1;
        int numberOfDataSource = getNumbersOfCells(sheet, dataSourceColumnStartIndex);

        Map<Integer, String> dataSourceIndexToName = new LinkedHashMap<>();
        Row dataSourcesAndOwnersRow = sheet.getRow(1);
        for (int i = dataSourceColumnStartIndex; i < dataSourceColumnStartIndex + numberOfDataSource; i++) {
            Optional<String> name = Optional.ofNullable(dataSourcesAndOwnersRow.getCell(i))
                    .map(Cell::getStringCellValue)
                    .filter(str -> !str.isBlank());
            if (name.isPresent()) {
                dataSourceIndexToName.put(i, name.get());
            }
        }

        final int dataOwnerColumnStartIndex = dataSourceColumnStartIndex + numberOfDataSource;
        int numberOfDataOwner = getNumbersOfCells(sheet, dataOwnerColumnStartIndex);

        Map<Integer, String> dataOwnerIndexToName = new LinkedHashMap<>();
        for (int i = dataOwnerColumnStartIndex; i < dataOwnerColumnStartIndex + numberOfDataOwner; i++) {
            Optional<String> name = Optional.ofNullable(dataSourcesAndOwnersRow.getCell(i))
                    .map(Cell::getStringCellValue)
                    .filter(str -> !str.isBlank());
            if (name.isPresent()) {
                dataOwnerIndexToName.put(i, name.get());
            }
        }

        entityBuilder.addOrganizationInformation(businessDomainNameSet, dataOwnerIndexToName, dataSourceIndexToName);
    }

    private int getNumbersOfCells(Sheet sheet, int mergeCellIndex) {
        return sheet.getMergedRegions().stream()
                .filter(cellAddress -> cellAddress.isInRange(sheet.getRow(0).getCell(mergeCellIndex)))
                .findFirst()
                .map(CellRangeAddressBase::getNumberOfCells)
                .orElse(0);
    }

    private void createReferences(Workbook workbook, EntityBuilder entityFactory) {
        Map<String, String> referenceDescriptions = getReferenceDescriptions(workbook);

        Sheet relationsBetweenObjectsSheet = workbook.getSheet("Lien entre objets");
        Row coreEntitiesRow = relationsBetweenObjectsSheet.getRow(2);
        int maxColumn = 2;
        while (true) {
            if (Optional.ofNullable(coreEntitiesRow.getCell(maxColumn))
                    .map(Cell::getStringCellValue).isPresent()) {
                maxColumn++;
            } else break;
        }

        for (int iRow = 3; true; iRow++) {
            Row currentRow = relationsBetweenObjectsSheet.getRow(iRow);
            if (currentRow == null) break;
            Optional<String> verticalCoreEntityNameOpt = Optional.ofNullable(currentRow.getCell(2))
                    .map(Cell::getStringCellValue).filter(str -> !str.isBlank());
            if (verticalCoreEntityNameOpt.isPresent()) {
                for (int column = 3; column <= maxColumn; column++) {
                    Optional<String> horizontalCoreEntityNameOpt = Optional.ofNullable(coreEntitiesRow.getCell(column))
                            .map(Cell::getStringCellValue).filter(str -> !str.isBlank());
                    if (horizontalCoreEntityNameOpt.isPresent()) {
                        Optional.ofNullable(currentRow.getCell(column))
                                .map(Cell::getStringCellValue)
                                .filter(str -> !str.isBlank())
                                .stream()
                                .flatMap(s -> Stream.of(s.split("\n")))
                                .forEach(relationName -> entityFactory.addRelation(horizontalCoreEntityNameOpt.get(), verticalCoreEntityNameOpt.get(), relationName,
                                        referenceDescriptions.get(relationName)));
                    } else break;
                }
            } else break;
        }
    }

    Map<String, String> getReferenceDescriptions(Workbook workbook) {
        Map<String, String> referenceNameToDescription = new LinkedHashMap<>();
        Sheet relationsDescriptionSheet = workbook.getSheet("Typologie de lien");

        for (int iRow = 1; true; iRow++) {
            Row currentRow = relationsDescriptionSheet.getRow(iRow);
            Optional<String> referenceNameOpt = Optional.ofNullable(currentRow)
                    .map(row -> row.getCell(0))
                    .map(Cell::getStringCellValue)
                    .filter(str -> !str.isBlank());
            if (referenceNameOpt.isPresent()) {
                Optional.ofNullable(currentRow.getCell(1))
                        .map(Cell::getStringCellValue)
                        .filter(str -> !str.isBlank())
                        .ifPresent(description -> referenceNameToDescription.put(referenceNameOpt.get(), description));

            } else break;
        }

        return referenceNameToDescription;
    }
}
