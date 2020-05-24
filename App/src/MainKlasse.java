import org.sparx.Element;
import org.sparx.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Versuchs- und Testklasse
 */
public class MainKlasse {

    public static void main(String[] args) {

        //DBConnection.getInstance().connectToDatabase();
        //DBConnection.getInstance().clearDB();

        //new EAvsEssarelBsp().createBSP();

        //new CFTRekonstruktion(866).readCFT().printCFT();
        /*
        StringMatcher sm = new StringMatcher();

        NameIDQueryResult a1 = new NameIDQueryResult("Manfred_Hallo_yo",5);
        NameIDQueryResult a2 = new NameIDQueryResult("Hans_Ha",17);
        NameIDQueryResult b1 = new NameIDQueryResult("Ute_hallo_yo",6);
        NameIDQueryResult b2 = new NameIDQueryResult("Manfred",7);
        NameIDQueryResult b3 = new NameIDQueryResult("Hans_Ha",8);
        List<NameIDQueryResult> list = new ArrayList<>();
        List<NameIDQueryResult> list2 = new ArrayList<>();
        list2.add(a1);
        list2.add(a2);
        list.add(b1);
        list.add(b2);
        list.add(b3);
        List<Match> m = sm.matchEverythingOnce(list,list2);
        m.forEach(match -> {
            System.out.println(match.getA()+"   "+match.getB());
        });*/

/*
        String input = "D:\\BA\\Beispiele\\EABeispiel\\EABsp.eap";
        new SecondEAReader(input).readData();

        //new Integrator2().integrateModels();


        String in = "D:\\BA\\Beispiele\\EssarelBsp\\ESSaRelBeispiel";
        //String in = "D:\\BA_SVN\\ESSaRel\\eclipse-essarel\\runtime-New_configuration\\RavonScenario";
        new EssarelReader(in).readData();
*/


        Repository repo = new org.sparx.Repository();
        repo.OpenFile("D:\\BA_SVN\\Generic Infusion Pump\\Enterprise Architect\\GIP.eap");
        String moon = repo.SQLQuery("SELECT Object_ID FROM t_object WHERE Stereotype = 'FTM/N'");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        Element moonElement = null;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        //componentsXML bearbeiten
        Document componentDoc = null;
        InputSource inputComponentsXML = new InputSource(new StringReader(moon));
        try {
            assert builder != null;
            componentDoc = builder.parse(inputComponentsXML);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert componentDoc != null;
        NodeList nListComponents = componentDoc.getElementsByTagName("Object_ID");

        for(int i = 0;i<nListComponents.getLength();i++){
            moonElement = repo.GetElementByID(Integer.parseInt(nListComponents.item(i).getFirstChild().getNodeValue()));
        }

        moonElement.GetTaggedValues().forEach(property -> {
            if(property.GetName().equals("m"))System.out.println(property.GetName()+" "+property.GetValue());
        });

    }
}
