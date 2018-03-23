<#ftl output_format="JSON">
{
    "index_patterns": ["${indexName}-*"],
    "settings": {
        "index.number_of_shards":${numberOfShards},
        "index.number_of_replicas":${numberOfReplicas},
        "refresh_interval": "1s"
    },
    "mappings": {
        "log": {
            "properties": {
                "client-request": {
                    "type": "object",
                    "enabled": false
                },
                "client-response": {
                    "type": "object",
                    "enabled": false
                },
                "proxy-request": {
                    "type": "object",
                    "enabled": false
                },
                "proxy-response": {
                    "type": "object",
                    "enabled": false
                }
            }
        }
    }
}