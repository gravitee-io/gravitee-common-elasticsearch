<#ftl output_format="JSON">
<#macro stringOrNull data="">
  <#if data != "">
  "${data}"
  <#else>
  null
  </#if>
</#macro>
{ "index" : { "_index" : "${index}", "_type" : "${type}", "_id" : "${log.getRequestId()}" } }
<@compress single_line=true>
{
  "@timestamp":"${@timestamp}",
  "api":"${log.getApi()}"
  <#if log.getClientRequest()??>
  ,"client-request": {
  "method":"${log.getClientRequest().getMethod()}",
  "uri":"${log.getClientRequest().getUri()}"
    <#if log.getClientRequest().getBody()??>
    ,"body":"${log.getClientRequest().getBody()?j_string}"
    </#if>
    <#if log.getClientRequest().getHeaders()??>
    ,"headers":{
      <#list log.getClientRequest().getHeaders() as headerKey, headerValue>
      "${headerKey}": [
        <#list headerValue as value>
          <#if value??>
            "${value?j_string}"
            <#sep>,</#sep>
          </#if>
        </#list>
      ]
        <#sep>,</#sep>
      </#list>
    }
    </#if>
    <#if log.getClientRequest().getSslInfo()??>
      ,"ssl-info": {
        "local-principal":"${log.getClientRequest().getSslInfo().getLocalPrincipal()}",
        "peer-principal":"${log.getClientRequest().getSslInfo().getPeerPrincipal()}",
        "cipher-suite":"${log.getClientRequest().getSslInfo().getCipherSuite()}",
        "protocol":"${log.getClientRequest().getSslInfo().getProtocol()}"
      <#if log.getClientRequest().getSslInfo().getLocalCertificates()??>
        ,"local-certificates":[
        <#list log.getClientRequest().getSslInfo().getLocalCertificates() as cert>
          {"version": "${cert.getVersion()}",
           "serial-number": "${cert.getSerialNumber()}",
           "algorithm": "${cert.getAlgorithm()}",
           "issuer": "${cert.getIssuer()}",
           "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      <#if log.getClientRequest().getSslInfo().getPeerCertificates()??>
        ,"peer-certificates":[
        <#list log.getClientRequest().getSslInfo().getPeerCertificates() as cert>
          {"version": "${cert.getVersion()}",
           "serial-number": "${cert.getSerialNumber()}",
           "algorithm": "${cert.getAlgorithm()}",
           "issuer": "${cert.getIssuer()}",
           "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      }
    </#if>
  }
  ,"client-response": {
  "status":${log.getClientResponse().getStatus()}
    <#if log.getClientResponse().getBody()??>
    ,"body":"${log.getClientResponse().getBody()?j_string}"
    </#if>
    <#if log.getClientResponse().getHeaders()??>
    ,"headers":{
      <#list log.getClientResponse().getHeaders() as headerKey, headerValue>
      "${headerKey}": [
        <#list headerValue as value>
          <#if value??>
            "${value?j_string}"
            <#sep>,</#sep>
          </#if>
        </#list>
      ]
        <#sep>,</#sep>
      </#list>
    }
    </#if>
    <#if log.getClientResponse().getSslInfo()??>
      ,"ssl-info": {
      "local-principal":"${log.getClientResponse().getSslInfo().getLocalPrincipal()}",
      "peer-principal":"${log.getClientResponse().getSslInfo().getPeerPrincipal()}",
      "cipher-suite":"${log.getClientResponse().getSslInfo().getCipherSuite()}",
      "protocol":"${log.getClientResponse().getSslInfo().getProtocol()}"
      <#if log.getClientResponse().getSslInfo().getLocalCertificates()??>
        ,"local-certificates":[
        <#list log.getClientResponse().getSslInfo().getLocalCertificates() as cert>
          {"version": "${cert.getVersion()}",
          "serial-number": "${cert.getSerialNumber()}",
          "algorithm": "${cert.getAlgorithm()}",
          "issuer": "${cert.getIssuer()}",
          "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      <#if log.getClientResponse().getSslInfo().getPeerCertificates()??>
        ,"peer-certificates":[
        <#list log.getClientResponse().getSslInfo().getPeerCertificates() as cert>
          {"version": "${cert.getVersion()}",
          "serial-number": "${cert.getSerialNumber()}",
          "algorithm": "${cert.getAlgorithm()}",
          "issuer": "${cert.getIssuer()}",
          "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      }
    </#if>
  }
  </#if>
  <#if log.getProxyRequest()??>
    <#if log.getClientRequest()??>,</#if>
  "proxy-request": {
  "method":"${log.getProxyRequest().getMethod()}",
  "uri":"${log.getProxyRequest().getUri()}"
    <#if log.getProxyRequest().getBody()??>
    ,"body":"${log.getProxyRequest().getBody()?j_string}"
    </#if>
    <#if log.getProxyRequest().getHeaders()??>
    ,"headers":{
      <#list log.getProxyRequest().getHeaders() as headerKey, headerValue>
      "${headerKey}": [
        <#list headerValue as value>
          <#if value??>
          "${value?j_string}"
            <#sep>,</#sep>
          </#if>
        </#list>
      ]
        <#sep>,</#sep>
      </#list>
    }
    </#if>
    <#if log.getProxyRequest().getSslInfo()??>
      ,"ssl-info": {
      "local-principal":"${log.getProxyRequest().getSslInfo().getLocalPrincipal()}",
      "peer-principal":"${log.getProxyRequest().getSslInfo().getPeerPrincipal()}",
      "cipher-suite":"${log.getProxyRequest().getSslInfo().getCipherSuite()}",
      "protocol":"${log.getProxyRequest().getSslInfo().getProtocol()}"
      <#if log.getProxyRequest().getSslInfo().getLocalCertificates()??>
        ,"local-certificates":[
        <#list log.getProxyRequest().getSslInfo().getLocalCertificates() as cert>
          {"version": "${cert.getVersion()}",
          "serial-number": "${cert.getSerialNumber()}",
          "algorithm": "${cert.getAlgorithm()}",
          "issuer": "${cert.getIssuer()}",
          "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      <#if log.getProxyRequest().getSslInfo().getPeerCertificates()??>
        ,"peer-certificates":[
        <#list log.getProxyRequest().getSslInfo().getPeerCertificates() as cert>
          {"version": "${cert.getVersion()}",
          "serial-number": "${cert.getSerialNumber()}",
          "algorithm": "${cert.getAlgorithm()}",
          "issuer": "${cert.getIssuer()}",
          "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      }
    </#if>
  }
  </#if>
  <#if log.getProxyResponse()??>
  ,"proxy-response": {
  "status":${log.getProxyResponse().getStatus()}
    <#if log.getProxyResponse().getBody()??>
    ,"body":"${log.getProxyResponse().getBody()?j_string}"
    </#if>
    <#if log.getProxyResponse().getHeaders()??>
    ,"headers":{
      <#list log.getProxyResponse().getHeaders() as headerKey, headerValue>
      "${headerKey}": [
        <#list headerValue as value>
          <#if value??>
          "${value?j_string}"
            <#sep>,</#sep>
          </#if>
        </#list>
      ]
        <#sep>,</#sep>
      </#list>
    }
    </#if>
    <#if log.getProxyResponse().getSslInfo()??>
      ,"ssl-info": {
      "local-principal":"${log.getProxyResponse().getSslInfo().getLocalPrincipal()}",
      "peer-principal":"${log.getProxyResponse().getSslInfo().getPeerPrincipal()}",
      "cipher-suite":"${log.getProxyResponse().getSslInfo().getCipherSuite()}",
      "protocol":"${log.getProxyResponse().getSslInfo().getProtocol()}"
      <#if log.getProxyResponse().getSslInfo().getLocalCertificates()??>
        ,"local-certificates":[
        <#list log.getProxyResponse().getSslInfo().getLocalCertificates() as cert>
          {"version": "${cert.getVersion()}",
          "serial-number": "${cert.getSerialNumber()}",
          "algorithm": "${cert.getAlgorithm()}",
          "issuer": "${cert.getIssuer()}",
          "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      <#if log.getProxyResponse().getSslInfo().getPeerCertificates()??>
        ,"peer-certificates":[
        <#list log.getProxyResponse().getSslInfo().getPeerCertificates() as cert>
          {"version": "${cert.getVersion()}",
          "serial-number": "${cert.getSerialNumber()}",
          "algorithm": "${cert.getAlgorithm()}",
          "issuer": "${cert.getIssuer()}",
          "subject": "${cert.getSubject()}"}
          <#sep>,</#sep>
        </#list>
        ]
      </#if>
      }
    </#if>
  }
  </#if>
}</@compress>
