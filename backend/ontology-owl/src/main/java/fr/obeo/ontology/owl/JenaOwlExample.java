package fr.obeo.ontology.owl;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class JenaOwlExample {
    public static void main(String[] args) {
        // Création d'un modèle Jena en mémoire
        Model model = ModelFactory.createDefaultModel();

        // Définition de l'espace de noms
        String baseURI = "http://example.org/ontology#";
        model.setNsPrefix("ex", baseURI);

        // Définition de l'espace de noms OWL
        model.setNsPrefix("owl", OWL.NS);

        // Création de classes OWL
        Resource personClass = model.createResource(baseURI + "Person")
                .addProperty(RDF.type, OWL.Class);
        Resource companyClass = model.createResource(baseURI + "Company")
                .addProperty(RDF.type, OWL.Class);
        Resource employeeClass = model.createResource(baseURI + "Employee")
                .addProperty(RDF.type, OWL.Class)
                .addProperty(RDFS.subClassOf, personClass); // Employee est une sous-classe de Person

        Resource managerClass = model.createResource(baseURI + "Manager")
                .addProperty(RDF.type, OWL.Class)
                .addProperty(RDFS.subClassOf, employeeClass); // Manager est une sous-classe d'Employee

        // Ajout des propriétés
        Property hasName = model.createProperty(baseURI + "hasName");
        Property age = model.createProperty(baseURI + "age");
        Property worksFor = model.createProperty(baseURI + "worksFor");
        Property hasSalary = model.createProperty(baseURI + "hasSalary");

        // Définition des domaines et des plages pour les propriétés
        hasName.addProperty(RDFS.domain, personClass).addProperty(RDFS.range, RDFS.Literal);
        age.addProperty(RDFS.domain, personClass).addProperty(RDFS.range, RDFS.Literal);
        worksFor.addProperty(RDFS.domain, employeeClass).addProperty(RDFS.range, companyClass);
        hasSalary.addProperty(RDFS.domain, employeeClass).addProperty(RDFS.range, RDFS.Literal);

        // Sauvegarde du modèle en fichier RDF
        try (FileOutputStream out = new FileOutputStream("ontology.rdf")) {
            model.write(out, "RDF/XML"); // Format RDF avec OWL
            System.out.println("Fichier RDF contenant des classes OWL, relations de sous-classes et propriétés généré avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Affichage du modèle RDF généré
        model.write(System.out, "RDF/XML");
    }
}
