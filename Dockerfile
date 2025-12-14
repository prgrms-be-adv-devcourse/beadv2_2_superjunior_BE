FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.8

RUN elasticsearch-plugin install analysis-nori
