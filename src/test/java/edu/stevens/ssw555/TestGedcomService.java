package edu.stevens.ssw555;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


public class TestGedcomService {

    private HashMap<String, Individual> individuals;
    private HashMap<String, Family> families;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @BeforeEach
    public void setUp() throws IOException {
        // Create the file if it does not exist
        Path filePath = Paths.get("GedcomService_output.txt");
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clear the contents of the file after each test
        Path filePath = Paths.get("GedcomService_output.txt");
        if (Files.exists(filePath)) {
            new PrintWriter(filePath.toFile()).close();
        }
    }

    @Test
    public void testBirthBeforeDeath() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();

        Individual ind1 = new Individual("I1");
        ind1.setBirth("01/01/1990");
        ind1.setDeath("01/01/2000");

        Individual ind2 = new Individual("I2");
        ind2.setBirth("01/01/2000");
        ind2.setDeath("01/01/1990"); // Error

        individuals.put(ind1.getId(), ind1);
        individuals.put(ind2.getId(), ind2);

        Gedcom_Service.birthBeforeDeath(individuals);

        // Add assertions or check output file for the error
        Assert.assertEquals(
                Files.readString(Paths.get("GedcomService_output.txt")),
                "ERROR:INDIVIDUAL: User Story US03: Birth Before Death \n" +
                        "Individual: I2 - null was born after death\n" +
                        "DOB: 01/01/2000 DOD: 01/01/1990\n" +
                        "\n"
        );
    }

    @Test
    public void testMarriageBeforeDivorce() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();
        families = new HashMap<>();

        Individual husb = new Individual("I1");
        Individual wife = new Individual("I2");

        Family fam = new Family("F1");
        fam.setHusb(husb.getId());
        fam.setWife(wife.getId());
        fam.setMarriage("01/01/2000");
        fam.setDivorce("01/01/1990");

        families.put(fam.getId(), fam);
        individuals.put(husb.getId(), husb);
        individuals.put(wife.getId(), wife);

        Gedcom_Service.Marriagebeforedivorce(individuals, families);

        // Add assertions or check output file for the error
        Assert.assertEquals(
                Files.readString(Paths.get("GedcomService_output.txt")),
                "ERROR:FAMILY: User Story US04: Marriage Before Divorce \n" +
                        "Family: F1\n" +
                        "Individual: I1: nullI2: null marriage date is before divorce date.\n" +
                        "Marriage Date: 01/01/2000 Divorce Date: 01/01/1990\n" +
                        "\n"
        );
    }

    @Test
    public void testBirthBeforeMarriageOfParent() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();
        families = new HashMap<>();

        Individual husb = new Individual("I1");
        Individual wife = new Individual("I2");
        Individual child = new Individual("I3");
        child.setBirth("01/01/1980"); // Error, before parents' marriage

        Family fam = new Family("F1");
        fam.setHusb(husb.getId());
        fam.setWife(wife.getId());
        fam.setMarriage("01/01/1990");
        fam.setChild(new ArrayList<>(Arrays.asList(child.getId())));

        families.put(fam.getId(), fam);
        individuals.put(husb.getId(), husb);
        individuals.put(wife.getId(), wife);
        individuals.put(child.getId(), child);

        Gedcom_Service.birthbeforemarriageofparent(individuals, families);

        // Add assertions or check output file for the error
        Assert.assertEquals(Files.readString(
                        Paths.get("GedcomService_output.txt")),
                "ERROR: User Story US08: Birth Before Marriage Date \n" +
                        "Family ID: F1\n" +
                        "Individual: I3: null Has been born before parents' marriage\n" +
                        "DOB: 01/01/1980 Parents Marriage Date: 01/01/1990\n" +
                        "\n" +
                        "\n"
        );
    }

    @Test
    public void testMaleLastName() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();
        families = new HashMap<>();

        Individual father = new Individual("I1");
        father.setName("John Doe");
        father.setSex("male");

        Individual son = new Individual("I2");
        son.setName("Jake Doe");
        son.setSex("male");

        Individual daughter = new Individual("I3");
        daughter.setName("Jane Doe");
        daughter.setSex("female");

        Individual son2 = new Individual("I4");
        son2.setName("Drake Doie");
        son2.setSex("male");

        Family fam = new Family("F1");
        fam.setHusb(father.getId());
//        fam.setWife("I4"); // Assuming mother exists
        fam.setChild(new ArrayList<>(Arrays.asList(son.getId(), daughter.getId(), son2.getId())));

        families.put(fam.getId(), fam);
        individuals.put(father.getId(), father);
        individuals.put(son.getId(), son);
        individuals.put(son2.getId(), son2);
        individuals.put(daughter.getId(), daughter);

        Gedcom_Service.individuals = individuals;
        Gedcom_Service.families = families;
//        Gedcom_Service.printMaps();

        Gedcom_Service.Malelastname(families);

        // Add assertions or check output file for the error
        Assert.assertEquals(Files.readString(
                        Paths.get("GedcomService_output.txt")),
                "ERROR: User Story US16:Male last name \n" +
                        "Family ID: F1   family members don't have same last name \n" +
                        "\n" +
                        "\n" +
                        "ERROR: User Story US16:Male last name \n" +
                        "Family ID: F1   family members don't have same last name \n" +
                        "\n" +
                        "\n"
        );
    }

    @Test
    public void testMaleLastNameV2() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();
        families = new HashMap<>();

        Individual father = new Individual("I1");
        father.setName("John Doe");
        father.setSex("male");

        Individual son = new Individual("I2");
        son.setName("Jake Doe");
        son.setSex("male");

        Individual son2 = new Individual("I3");
        son2.setName("Mike Deo");
        son2.setSex("male");

        Individual son3 = new Individual("I4");
        son3.setName("Drake Doie");
        son3.setSex("male");

        Family fam = new Family("F1");
        fam.setHusb(father.getId());
//        fam.setWife("I4"); // Assuming mother exists
        fam.setChild(new ArrayList<>(Arrays.asList(son.getId(), son2.getId(), son3.getId())));

        families.put(fam.getId(), fam);
        individuals.put(father.getId(), father);
        individuals.put(son.getId(), son);
        individuals.put(son2.getId(), son2);
        individuals.put(son3.getId(), son3);

        Gedcom_Service.individuals = individuals;
        Gedcom_Service.families = families;
//        Gedcom_Service.printMaps();

        Gedcom_Service.Malelastname(families);

        // Add assertions or check output file for the error
        Assert.assertEquals(Files.readString(
                        Paths.get("GedcomService_output.txt")),
                "ERROR: User Story US16:Male last name \n" +
                        "Family ID: F1   family members don't have same last name \n" +
                        "\n" +
                        "\n" +
                        "ERROR: User Story US16:Male last name \n" +
                        "Family ID: F1   family members don't have same last name \n" +
                        "\n" +
                        "\n"
        );
    }

    @Test
    public void testMaleLastName2() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();
        families = new HashMap<>();

        Individual father = new Individual("I1");
        father.setName("John Doie");
        father.setSex("male");

        Individual son = new Individual("I2");
        son.setName("Jake Doe");
        son.setSex("male");

        Individual daughter = new Individual("I3");
        daughter.setName("Jane Doe");
        daughter.setSex("female");

        Family fam = new Family("F1");
        fam.setHusb(father.getId());
//        fam.setWife("I4"); // Assuming mother exists
        fam.setChild(new ArrayList<>(Arrays.asList(son.getId(), daughter.getId())));

        families.put(fam.getId(), fam);
        individuals.put(father.getId(), father);
        individuals.put(son.getId(), son);
        individuals.put(daughter.getId(), daughter);

        Gedcom_Service.individuals = individuals;
        Gedcom_Service.families = families;
//        Gedcom_Service.printMaps();

        Gedcom_Service.Malelastname(families);

        // Add assertions or check output file for the error
        Assert.assertEquals(Files.readString(
                        Paths.get("GedcomService_output.txt")),
                "ERROR: User Story US16:Male last name \n" +
                        "Family ID: F1   family members don't have same last name \n" +
                        "\n" +
                        "\n"
        );
    }

    @Test
    public void testUniqueFamilyNameBySpouses() throws Exception {
        String simulatedInput = "GedcomService_output.txt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Gedcom_Service.createOutputFile();

        individuals = new HashMap<>();
        families = new HashMap<>();

        Individual husb = new Individual("I1");
        Individual wife = new Individual("I2");

        Family fam1 = new Family("F1");
        fam1.setHusb(husb.getId());
        fam1.setWife(wife.getId());
        fam1.setMarriage("01/01/1990");

        Family fam2 = new Family("F2");
        fam2.setHusb(husb.getId());
        fam2.setWife(wife.getId());
        fam2.setMarriage("01/01/1990"); // Same as fam1

        families.put(fam1.getId(), fam1);
        families.put(fam2.getId(), fam2);
        individuals.put(husb.getId(), husb);
        individuals.put(wife.getId(), wife);

        Gedcom_Service.uniqueFamilynameBySpouses(individuals, families);

        // Add assertions or check output file for the error
        Assert.assertEquals(Files.readString(
                        Paths.get("GedcomService_output.txt")),
                "ERROR: User Story US24: Unique Families By Spouse :\n" +
                        "F2: Husbund Name: null,Wife Name: null and F1: Husbund Name: null,Wife Name: null\n" +
                        " have same spouses and marriage dates :01/01/1990\n" +
                        "\n" +
                        "ERROR: User Story US24: Unique Families By Spouse :\n" +
                        "F1: Husbund Name: null,Wife Name: null and F2: Husbund Name: null,Wife Name: null\n" +
                        " have same spouses and marriage dates :01/01/1990\n" +
                        "\n"
        );
    }
}
