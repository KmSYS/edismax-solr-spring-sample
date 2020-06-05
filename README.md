![Java CI with Maven](https://github.com/KmSYS/spring-solr-edismax-template/workflows/Java%20CI%20with%20Maven/badge.svg)

# spring-solr-edismax-template

## Prerequisites

* Install Apache Solr `http://lucene.apache.org/solr/downloads.html`
* create new core with name content `bin/solr create -c product`
* have a basic knowledge about Apache Solr 

## Running the project 
* Go to `SpringSolrEdismaxTemplateApplication` class then run it

## Explanation for  `searchOnSampleProducts` method

* create instance from EdismaxQuery interface
* write your criteria 
* configure edismax parameters 
* finally print the result 

```

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


```
