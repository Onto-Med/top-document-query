# TOP Document Query


## Introduction

TOP Document Query is Java project that is responsible for the document search/clustering logic in the TOP Framework
(see [top-deployment](https://github.com/Onto-Med/top-deployment) for a documentation of the whole framework).
Its two main features are as follows:

* provide interfaces for developers to implement adapters to generate queries in a specific query language
  (right now an `Elasticsearch` adapter is provided)
* provides ``care.smith.top.top-document-query.concept_cluster.ConceptPipelineManager`` that connects to
  and provides functionality of [Concept Graphs](https://github.com/Onto-Med/concept-graphs)


## Adapter Configuration

An adapter that is implemented can be referenced and configured by a ``yaml`` file (exemplified below).
````yaml
# distinct name of the adapter
id: Adapter ID
# fully specified, dot notated `Adapter` class that implements `care.[...].adapter.TextAdapter`
adapter: care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchAdapter

connection:
  url: http://localhost
  port: 9201
index:
  - test_documents
field:
  - text
batchSize: 30
replaceFields: {'text': 'content'}
labelKey: label
conceptGraph:
  connection:
    url: http://localhost
    port: 5000

````
authentication details can be overwritten on a adapter level but are declared in the first place at the framework level