
import info.aduna.iteration.CloseableIteration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.repository.RepositoryException;
import org.protege.owl.rdf.Utilities;
import org.protege.owl.rdf.api.OwlTripleStore;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Converter {
    /*
     * To change this license header, choose License Headers in Project Properties.
     * To change this template file, choose Tools | Templates
     * and open the template in the editor.
     */

    /**
     *
     * @author cebiy
     */
    ArrayList<String> alldata = new ArrayList<>();
    ArrayList<String> team = new ArrayList<>();
    ArrayList<String> fifaname = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> birthdate = new ArrayList<>();
    ArrayList<String> position = new ArrayList<>();
    ArrayList<String> shirtname = new ArrayList<>();
    ArrayList<String> clubs = new ArrayList<>();
    ArrayList<String> height = new ArrayList<>();
    ArrayList<String> weight = new ArrayList<>();
    ArrayList<String> number = new ArrayList<>();
public static Logger LOGGER = LoggerFactory.getLogger(Converter.class);
	public static String NS = "http://ex.org/ontologies/soccer/fifaplayer.owl";

	public static final OWLClass Club         = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(NS + "#Club"));
	public static final OWLClass Player  = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(NS + "#Player"));
	public static final OWLClass Position = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(NS + "#Position"));
	public static final OWLClass Team = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(NS + "#Team"));
	public static final OWLObjectProperty Plays_for = OWLManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS + "#playsFor"));
	public static final OWLObjectProperty HAS_Position = OWLManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS + "#hasPosition"));
	
	
	
	private OWLOntology ontology;
	private OwlTripleStore ots;
	
	@BeforeMethod
	public void setup() throws OWLOntologyCreationException, RepositoryException {
	    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    ontology = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\cebiy\\Desktop\\Assignment_Football_Ontology.owl"));
	    ots = Utilities.getOwlTripleStore(ontology, false);
	}

	@Test
	public void testHasAxiom() throws RepositoryException, OWLOntologyCreationException {
		long startTime = System.currentTimeMillis();
		for (OWLAxiom axiom : ontology.getAxioms()) {
			Assert.assertTrue(ots.hasAxiom(ontology.getOntologyID(), axiom));
		}
		LOGGER.info("Parsing all the axioms from the triple store took " + (System.currentTimeMillis() - startTime) + "ms.");
	}
	
	@Test
	public void testListAxioms() throws RepositoryException, OWLOntologyCreationException {
		          CloseableIteration<OWLAxiom, RepositoryException> axiomIt = ots.listAxioms(ontology.getOntologyID());
		Set<OWLAxiom> collected = new HashSet<OWLAxiom>();
		try {
			while (axiomIt.hasNext()) {
				OWLAxiom axiom = axiomIt.next();
				Assert.assertTrue(ontology.containsAxiom(axiom));
				collected.add(axiom);
			}
		}
		finally {
			axiomIt.close();
		}
		Assert.assertEquals(collected.size(), ontology.getAxiomCount());
	}
	
	@Test
	public void testRemove() throws RepositoryException {
		OWLAxiom axiom = selectInterestingAxiom();
		Assert.assertTrue(ots.hasAxiom(ontology.getOntologyID(), axiom));
		ots.removeAxiom(ontology.getOntologyID(), axiom);
		Assert.assertFalse(ots.hasAxiom(ontology.getOntologyID(), axiom));
		ots.removeAxiom(ontology.getOntologyID(), axiom);
        Assert.assertFalse(ots.hasAxiom(ontology.getOntologyID(), axiom));
	}
	
	@Test
	public void testRemoveNotPresent() throws RepositoryException {
        OWLAxiom present = selectInterestingAxiom();	    
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLClassExpression someValuesFrom = factory.getOWLObjectSomeValuesFrom(Plays_for,Club);
        OWLClassExpression definition = factory.getOWLObjectIntersectionOf(Player, someValuesFrom);
        OWLAxiom notPresent = factory.getOWLEquivalentClassesAxiom(Player, definition);

        Assert.assertFalse(ontology.containsAxiom(notPresent));
        Assert.assertTrue(ontology.containsAxiom(present));
        Assert.assertFalse(ots.hasAxiom(ontology.getOntologyID(), notPresent));
        Assert.assertTrue(ots.hasAxiom(ontology.getOntologyID(), present));
        
        ots.removeAxiom(ontology.getOntologyID(), notPresent); 
        
        Assert.assertFalse(ots.hasAxiom(ontology.getOntologyID(), notPresent));
        Assert.assertTrue(ots.hasAxiom(ontology.getOntologyID(), present));
	}
	
	@Test
	public void testAdd() throws RepositoryException {
	    OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	    OWLClassExpression someValuesFrom = factory.getOWLObjectSomeValuesFrom(HAS_Position,Position);
	    OWLClassExpression definition = factory.getOWLObjectIntersectionOf(Player, someValuesFrom);
	    OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(Player, definition);
	    Assert.assertFalse(ontology.containsAxiom(axiom));
	    Assert.assertFalse(ots.hasAxiom(ontology.getOntologyID(), axiom));
	    ots.addAxiom(ontology.getOntologyID(), axiom);
	    Assert.assertTrue(ots.hasAxiom(ontology.getOntologyID(), axiom));
	}
	
	protected OWLAxiom selectInterestingAxiom() {
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLClass Player = factory.getOWLClass(IRI.create(NS + "#Player"));
		return ontology.getEquivalentClassesAxioms(Player).iterator().next();
	}

    public void bringdata() {

        String path = "C://Users//cebiy//Desktop//fifa.csv";
        File excelfile = new File(path);
        int rowindex = 0;
        try {
            FileReader fr = new FileReader(excelfile);
           
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {

                alldata.add(line);
                
                String array[] = alldata.get(rowindex).split(","); //reference:https://stackoverflow.com/questions/34866496/splitting-cell-into-strings-in-excel-apache-poi-java
                for (int i = 0; i < array.length; i++) {
                    if (i == 0) {
                        team.add(array[i]);
                        
                    }
                    
                    if (i == 2) {
                        position.add(array[i]);

                    }
                    if (i == 1) {

                        number.add((array[i]));

                    }

                    if (i == 3) {
                        fifaname.add(array[i]);

                    }
                    if (i == 4) {
                        birthdate.add(array[i]);

                    }
                    if (i == 5) {
                        shirtname.add(array[i]);

                    }

                    if (i == 6) {
                        clubs.add(array[i]);

                    }
                    if (i == 7) {

                        height.add((array[i]));

                    }
                    if (i == 8) {

                        weight.add((array[i]));

                    }

                }

                rowindex++;
            }
           
          
//            for (String onedata : fullnamees) {
//                System.out.println("" + onedata);
//
//            }
//            System.out.println("names are finished second step");
//            for (String onedata : movies) {
//                System.out.println("" + onedata);
//
//            }
//            System.out.println("movies are finished second step");
//            for (String onedata : countries) {
//                System.out.println("" + onedata);
//
//            }
//            System.out.println("COUNTRIES ARE FINISHED ");
        } catch (Exception e) {
            System.out.println("We have Error" + e);
        }
    }
    

    private void Csv2rdf() {
        Model model1 = ModelFactory.createDefaultModel();
        Property team1 = model1.createProperty("http://ex.org/property/team");
        Property number1 = model1.createProperty("http://ex.org/property/number");
        Property position1= model1.createProperty("http://ex.org/property/position");
        Property fifaname1= model1.createProperty("http://ex.org/property/fifaname");
        Property birthdate1 = model1.createProperty("http://ex.org/property/birthdate");
        Property shortname1 = model1.createProperty("http://ex.org/property/shortname");
        Property clubs1 = model1.createProperty("http://ex.org/property/club");
        Property height1 = model1.createProperty("http://ex.org/property/height");
        Property weight1= model1.createProperty("http://ex.org/property/weight");
      
        for (int i = 1; i < alldata.size(); i++) {
            Resource players = model1.createResource("http://ex.org/property/players" + fifaname.get(i));
            players.addProperty(team1, team.get(i));
            players.addProperty(number1,number.get(i)+"");
            players.addProperty(position1, position.get(i));
            players.addProperty(fifaname1, fifaname.get(i));
            players.addProperty(birthdate1, birthdate.get(i));
            players.addProperty(shortname1, shirtname.get(i));
            players.addProperty(clubs1, clubs.get(i));
            players.addProperty(height1, height.get(i)+"");
            players.addProperty(weight1, weight.get(i)+"");
           
        }
        model1.write(System.out, "TURTLE");
        try {
            Writer w = new FileWriter("C://Users//cebiy//Desktop//fifaplayers.ttl");
            model1.write(w, "TURTLE");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Converter obj = new Converter();
        obj.bringdata();
        obj.Csv2rdf();
        System.out.println("Succesfull");
    }
}
