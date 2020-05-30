package teck.kmsys.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import tech.kmsys.solr.EdismaxQuery;
import tech.kmsys.solr.SimpleEdismaxQuery;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EdismaxQueryMockitoTest {

    EdismaxQuery edismaxQuery;
    SolrTemplate solrTemplate;
    final String SOLR_CORE_NAME = "my_core";
    final SampleDocument document1 = new SampleDocument("P0001", "term term1 term2 term3 term4");
    final SampleDocument document2 = new SampleDocument("P0002", "term term5 term6 term7 term8");
    final SampleDocument document3 = new SampleDocument("P0003", "term term9 term10 term11 term12");

    @Before
    public void setUp() {
        edismaxQuery = new SimpleEdismaxQuery();
        solrTemplate = mock(SolrTemplate.class);
    }

    @Test
    public void testSolrTemplateQueryWithEdismaxQuery1() {
        final SampleDocument document1 = new SampleDocument("P0001", "term term1 term2 term3 term4");
        final SampleDocument document2 = new SampleDocument("P0002", "term term5 term6 term7 term8");
        final SampleDocument document3 = new SampleDocument("P0003", "term term9 term10 term11 term12");

        /* get document1 & document3 but ignore document2 by controlling minimumMatchPercent on searchText */
        edismaxQuery.addCriteria(new SimpleStringCriteria("term1 term2 term6 term10 term11"));
        //set pageable
        Pageable pageable = PageRequest.of(0, 10);
        edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(50);
        edismaxQuery.addQueryField("field", 2.5);

        given(solrTemplate.query(SOLR_CORE_NAME, edismaxQuery, SampleDocument.class))
                .willReturn(new PageImpl<>(Arrays.asList(document1, document3)));

        List<SampleDocument> solrDocuments = solrTemplate.query(SOLR_CORE_NAME, edismaxQuery, SampleDocument.class).getContent();

        assertEquals(2, solrDocuments.size());
        assertThat(solrDocuments.get(0).field).isEqualTo(document1.field);
        assertThat(solrDocuments.get(1).field).isEqualTo(document3.field);
    }

    @Test
    public void testSolrTemplateQueryWithEdismaxQuery2() {
        /* get document2 & document3 but ignore document1 by controlling minimumMatchPercent on searchText */
        edismaxQuery.addCriteria(new SimpleStringCriteria("term1 term5 term6 term10 term11"));
        //set pageable
        Pageable pageable = PageRequest.of(0, 10);
        edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(50);
        edismaxQuery.addQueryField("field", 2.5);

        given(solrTemplate.query(SOLR_CORE_NAME, edismaxQuery, SampleDocument.class))
                .willReturn(new PageImpl<>(Arrays.asList(document2, document3)));

        List<SampleDocument> solrDocuments = solrTemplate.query(SOLR_CORE_NAME, edismaxQuery, SampleDocument.class).getContent();

        assertEquals(2, solrDocuments.size());
        assertThat(solrDocuments.get(0).field).isEqualTo(document2.field);
        assertThat(solrDocuments.get(1).field).isEqualTo(document3.field);
    }

    @Test
    public void testSolrTemplateQueryWithEdismaxQuery3() {
        /* make document3 show first by using boost=2.5 on its terms */
        edismaxQuery.addCriteria(new SimpleStringCriteria("term1 term2 term6 term10^2.5 term11"));
        //set pageable
        Pageable pageable = PageRequest.of(0, 10);
        edismaxQuery.setPageRequest(pageable);
        //set minimum match percent(not applied if criteria contains boolean operator(AND, OR, NOT) )
        edismaxQuery.setMinimumMatchPercent(50);
        edismaxQuery.addQueryField("field", 2.5);

        given(solrTemplate.query(SOLR_CORE_NAME, edismaxQuery, SampleDocument.class))
                .willReturn(new PageImpl<>(Arrays.asList(document3, document2)));

        List<SampleDocument> solrDocuments = solrTemplate.query(SOLR_CORE_NAME, edismaxQuery, SampleDocument.class).getContent();

        assertEquals(2, solrDocuments.size());
        //here is the difference document3 showed first
        assertThat(solrDocuments.get(0).field).isEqualTo(document3.field);
        assertThat(solrDocuments.get(1).field).isEqualTo(document2.field);
    }

    @SolrDocument
    static class SampleDocument {
        @Id
        @Field
        String id;
        @Field
        String field;

        SampleDocument(String id, String field) {
            this.id = id;
            this.field = field;
        }
    }
}
