<#ftl output_format="JSON">
{
    "template": "${indexName}-*",
    "settings": {
        "index.number_of_shards":${numberOfShards},
        "index.number_of_replicas":${numberOfReplicas},
        "index.refresh_interval": "${refreshInterval}"
    },
    "mappings": {
        "log": {
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "api": {
                    "type": "keyword"
                },
                "client-request": {
                    "type": "object",
                    "properties": {
                        "body":{
                            "type": "string"
                        },
                        "headers": {
                            "enabled":  false,
                            "type": "object"
                        }
                    }
                },
                "client-response": {
                    "type": "object",
                    "properties": {
                        "body":{
                            "type": "string"
                        },
                        "headers": {
                            "enabled":  false,
                            "type": "object"
                        }
                    }
                },
                "proxy-request": {
                    "type": "object",
                    "properties": {
                        "body":{
                            "type": "string"
                        },
                        "headers": {
                            "enabled":  false,
                            "type": "object"
                        }
                    }
                },
                "proxy-response": {
                    "type": "object",
                    "properties": {
                        "body":{
                            "type": "string"
                        },
                        "headers": {
                            "enabled":  false,
                            "type": "object"
                        }
                    }
                }
            }
        }
    }
}