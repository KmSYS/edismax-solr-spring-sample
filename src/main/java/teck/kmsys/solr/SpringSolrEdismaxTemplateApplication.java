package teck.kmsys.solr;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import tech.kmsys.solr.EdismaxQuery;
import tech.kmsys.solr.SimpleEdismaxQuery;
import teck.kmsys.solr.model.Product;

import java.util.List;

@SpringBootApplication
@ComponentScan
@EntityScan
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
        //reindex 3 documents
        reIndex();

        /********************************************************************************************/
        /* get document1 & document3 but ignore document2 by controlling minimumMatchPercent on searchText */
        Page<Product> productPage = findWithCustomEdismaxCriteria(
                "term1 term2 term6 term10 term11",
                "field", 50, 2, PageRequest.of(0, 10));

        System.out.println("\n** get document1 & document3 but ignore document2 by controlling minimumMatchPercent on" +
                " searchText\n");
        print(productPage.getContent());

        /********************************************************************************************/
        /* get document2 & document3 but ignore document1 by controlling minimumMatchPercent on searchText */
        productPage = findWithCustomEdismaxCriteria("term1 term5 term6 term10 term11",
                "field", 50, 2, PageRequest.of(0, 10));

        System.out.println("** get document2 & document3 but ignore document1 by controlling minimumMatchPercent on " +
                "searchText\n");
        print(productPage.getContent());

        /********************************************************************************************/
        /* make document3 show first by using boost=2.5 on its terms */
        productPage = findWithCustomEdismaxCriteria("term1 term2 term6 term10^2.5 term11",
                "field", 50, 2, PageRequest.of(0, 10));

        System.out.println("** make document3 show first by using boost=2.5 on its terms\n");
        print(productPage.getContent());
    }

    void print(List<Product> documents) {
        for (Product document : documents)
            System.out.println("id: " + document.id + "\t field: " + document.field);

        System.out.println("=================================================");
    }

    public void reIndex() {
        solrTemplate.delete(solrCoreName, new SimpleQuery("*:*"));
        solrTemplate.commit(solrCoreName);

        SolrInputDocument document1 = new SolrInputDocument();
        document1.addField("id", "document1");
        document1.addField("field", "term1 term2 term3 term4");
        SolrInputDocument document2 = new SolrInputDocument();
        document2.addField("id", "document2");
        document2.addField("field", "term5 term6 term7 term8");
        SolrInputDocument document3 = new SolrInputDocument();
        document3.addField("id", "document3");
        document3.addField("field", "term9 term10 term11 term12");

        solrTemplate.saveDocument(solrCoreName, document1);
        solrTemplate.saveDocument(solrCoreName, document2);
        solrTemplate.saveDocument(solrCoreName, document3);
        solrTemplate.commit(solrCoreName);
    }

    Page<Product> findWithCustomEdismaxCriteria(String searchText,
                                                String fieldName,
                                                int minimumMatchPercent,
                                                int boost,
                                                Pageable pageable) {

        //create instance of edismaxQuery
        EdismaxQuery edismaxQuery = new SimpleEdismaxQuery();
        //add criteria
        if (searchText != null)
            edismaxQuery.addCriteria(new SimpleStringCriteria(searchText));
        //set pageable
        if (pageable != null)
            edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(minimumMatchPercent);
        //add filter query
        //add query filed with boost
        if (fieldName != null)
            edismaxQuery.addQueryField(fieldName, boost);

        //get result from Solr Core
        Page<Product> solrDocuments = solrTemplate.query(solrCoreName, edismaxQuery, Product.class);
        return solrDocuments;
    }

    private Page<Product> findWithCustomEdismaxCriteria(String searchText,
                                                        String lang,
                                                        String fieldName,
                                                        Criteria criteria,
                                                        Pageable pageable) {

        //create instance of edismaxQuery
        EdismaxQuery edismaxQuery = new SimpleEdismaxQuery();
        //add criteria
        if (searchText != null)
            edismaxQuery.addCriteria(new SimpleStringCriteria(searchText));
        //set pageable
        if (pageable != null)
            edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(50);
        //add filter query
        if (criteria != null)
            edismaxQuery.addFilterQuery(new SimpleFilterQuery(criteria));
        //add query filed with boost
        if (fieldName != null)
            edismaxQuery.addQueryField(fieldName, 2.0);
        //search at another field with language to apply language analyzer on it
        if (lang != null && fieldName != null)
            edismaxQuery.addQueryField(fieldName + "_" + lang, 2.0);

        //get result from Solr Core
        Page<Product> solrDocuments = solrTemplate.query(solrCoreName, edismaxQuery, Product.class);
        return solrDocuments;
    }

    class Product {
        String id;
        String field;
    }
}