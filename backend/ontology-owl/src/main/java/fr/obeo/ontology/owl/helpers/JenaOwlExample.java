package fr.obeo.ontology.owl.helpers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

class Person {
    private final String name;

    private final String worksFor;

    public Person(String name, String worksFor) {
        this.name = name;
        this.worksFor = worksFor;
    }

    public String getName() {
        return name;
    }

    public String getWorksFor() {
        return worksFor;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', worksFor='" + worksFor + "'}";
    }
}

public class JenaOwlExample {
    public static void main(String[] args) {
        // Création d'un modèle Jena en mémoire
        Model model = ModelFactory.createDefaultModel();

        // Définition de l'espace de noms
        String baseURI = "http://example.org/ontology#";
        model.setNsPrefix("ex", baseURI);

        // Création des classes OWL
        Resource personClass = model.createResource(baseURI + "Person")
                .addProperty(RDF.type, OWL.Class);
        Resource companyClass = model.createResource(baseURI + "Company")
                .addProperty(RDF.type, OWL.Class);

        // Création de propriétés OWL
        Property hasName = model.createProperty(baseURI + "hasName");
        Property worksFor = model.createProperty(baseURI + "worksFor");
        worksFor.addProperty(RDF.type, OWL.ObjectProperty);

        // Création d'instances
        Resource johnDoe = model.createResource(baseURI + "JohnDoe")
                .addProperty(RDF.type, personClass)
                .addProperty(hasName, "John Doe");

        Resource acmeCorp = model.createResource(baseURI + "AcmeCorp")
                .addProperty(RDF.type, companyClass)
                .addProperty(hasName, "Acme Corporation");

        // Définition de la relation entre John Doe et Acme Corp
        johnDoe.addProperty(worksFor, acmeCorp);

        // Sauvegarde du modèle en fichier OWL
        try (FileOutputStream out = new FileOutputStream("ontology.owl")) {
            model.write(out, "RDF/XML"); // Format OWL (RDF/XML)
            System.out.println("Fichier OWL généré avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Chargement d'un modèle OWL à partir d'un fichier
        Model loadedModel = ModelFactory.createDefaultModel();
        try (FileInputStream in = new FileInputStream("ontology.owl")) {
            loadedModel.read(in, null, "RDF/XML");
            System.out.println("Fichier OWL chargé avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Transformation du modèle en POJO
        StmtIterator iter = loadedModel.listStatements(null, RDF.type, personClass);
        while (iter.hasNext()) {
            Resource personResource = iter.next().getSubject();
            String name = personResource.getProperty(hasName).getString();
            String worksForCompany = personResource.hasProperty(worksFor) ?
                    personResource.getProperty(worksFor).getObject().asResource().getLocalName() : "Unknown";

            Person person = new Person(name, worksForCompany);
            System.out.println("Personne extraite : " + person);
        }

        // Affichage du modèle chargé
        loadedModel.write(System.out, "RDF/XML"); // Affichage en format Turtle pour la lisibilité
    }
}
