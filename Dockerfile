FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

RUN elasticsearch-plugin install analysis-nori
