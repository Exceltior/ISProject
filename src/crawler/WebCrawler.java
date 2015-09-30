package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Tiago on 28/09/2015.
 */
public class WebCrawler {

    public static StringWriter xmltext;

    public static void main(String[] args) throws IOException{

        processWebPage("http://www.pixmania.pt/telefones/telemovel/smartphone-19883-s.html");
    }

    private static void processWebPage(String link) throws IOException{

        System.out.println("Parsing Site");
        ListOfThings capsula = new ListOfThings();

        Document dom = Jsoup.connect(link).timeout(0).get();

        /*Elements links = dom.select("http://www.pixmania.pt/telefones/telemovel/smartphone-19883-s.html");

        for(Element e: links){
            System.out.println(e.text());
            if (e.text().equals("Smartphone")){
                System.out.print("Encontrei   -  ");
                System.out.println(e.text());
            }
        }*/
        Elements links = dom.select("a[class=imgC]");

        for(Element e: links){
            capsula.getData().add(extractInformation(e.attr("href")));
        }

        try {
            JAXBContext jc = JAXBContext.newInstance(ListOfThings.class);
            Marshaller ms = jc.createMarshaller();
            ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            xmltext = new StringWriter();
            ms.marshal(capsula, xmltext);
            //System.out.print(xmltext);

            try {
                System.out.println("Enviar para jmstopic");
                //SE NAO HOUVER CONNECT HA QUE GRAVAR UM FICHEIRO
                Sender teste = new Sender();
                teste.send(xmltext.toString());
            } catch (NamingException e) {
                e.printStackTrace();
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    private static ListOfThings.Info extractInformation(String url) throws IOException{

        System.out.println("Popular xml");
        ListOfThings.Info item = new ListOfThings.Info();

        Document dompagina = Jsoup.connect(url).timeout(0).get();

        item.setBrand(dompagina.getElementsByClass("pageTitle")
                .get(0).getElementsByAttributeValue("itemprop", "brand").text());

        item.setName(dompagina.getElementsByClass("pageTitle")
                .get(0).getElementsByAttributeValue("itemprop", "name").text());

        item.setPrice(dompagina.getElementsByClass("currentPrice")
                .get(0).getElementsByAttributeValue("itemprop", "price").text());

        dompagina.getElementsByClass("descTxt").text();

        dompagina.getElementsByClass("customList").get(0).getElementsByAttributeValue("itemprop", "description").text();

        //System.out.println(dompagina);

        return item;
    }
}
