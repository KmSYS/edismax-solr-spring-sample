package teck.kmsys.solr;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import tech.kmsys.solr.EdismaxQuery;
import tech.kmsys.solr.SimpleEdismaxQuery;
import teck.kmsys.solr.model.Product;

@SpringBootApplication
public class SpringSolrEdismaxTemplateApplication implements CommandLineRunner {

    @Autowired
    private SolrTemplate solrTemplate;
    @Value("${solr.core.name}")
    private String solrCoreName;

    public static void main(String[] args) {
        SpringApplication.run(SpringSolrEdismaxTemplateApplication.class, args);
    }

    @Override
    public void run(String... args) {
        //insert 3 products
        insertSampleProduct();
        //search on 3 products
        searchOnSampleProducts();
    }

    private void insertSampleProduct() {
        //clear data
        solrTemplate.delete(solrCoreName, new SimpleQuery("*:*"));
        solrTemplate.commit(solrCoreName);

        //insert 3 products
        SolrInputDocument phone = new SolrInputDocument();
        phone.addField("id", "p0001");
        phone.addField("name", "phone");
        phone.addField("description", "term1 term2 term3 term4");
        solrTemplate.saveDocument(solrCoreName, phone);
        solrTemplate.commit(solrCoreName);

        SolrInputDocument phoneCover = new SolrInputDocument();
        phoneCover.addField("id", "p0002");
        phoneCover.addField("name", "phone cover");
        phoneCover.addField("description", "term5 term6 term7 term8");
        solrTemplate.saveDocument(solrCoreName, phoneCover);
        solrTemplate.commit(solrCoreName);

        SolrInputDocument wirelessCharger = new SolrInputDocument();
        wirelessCharger.addField("id", "p0003");
        wirelessCharger.addField("name", "wireless charger");
        wirelessCharger.addField("description", "term9 term10 term11 term12");
        solrTemplate.saveDocument(solrCoreName, wirelessCharger);
        solrTemplate.commit(solrCoreName);
    }

    private void searchOnSampleProducts() {

        //create instance of edismaxQuery
        EdismaxQuery edismaxQuery = new SimpleEdismaxQuery();
        //add criteria
        edismaxQuery.addCriteria(new SimpleStringCriteria("term1 term2 term6 term10 term11"));
        //set pageable
        edismaxQuery.setPageRequest(PageRequest.of(0, 10));
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(50);
        //add query filed with boost
        edismaxQuery.addQueryField("description", 2);
        //get result from Solr Core
        Page<Product> results = solrTemplate.query(solrCoreName, edismaxQuery, Product.class);
        //print results
        for (Product product : results) {
            System.out.println("=================================================");
            System.out.println(product);
        }

    }

}