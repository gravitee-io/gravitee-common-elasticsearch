<#ftl output_format="JSON">
<#macro stringOrNull data="">
    <#if data != "">
      "${data}"
    <#else>
      null
</#if>
</#macro>
{ "index" : { "_index" : "${index}", "_type" : "${documentType}", "_id" : "${metrics.getRequestId()}"<#if pipeline??>, "pipeline" : "${pipeline}"</#if>} }
<@compress single_line=true>
{
  "gateway":"${gateway}"
  ,"@timestamp":"${@timestamp}"
  ,"transaction":"${metrics.getTransactionId()}"
  ,"method":${metrics.getHttpMethod().code()?c}
  ,"uri":"${metrics.getUri()}"
  ,"status":${metrics.getStatus()}
  ,"response-time":${metrics.getProxyResponseTimeMs()}
  <#if apiResponseTime??>
    ,"api-response-time":${apiResponseTime}
  </#if>
  <#if proxyLatency??>
  ,"proxy-latency":${proxyLatency}
  </#if>
  <#if requestContentLength??>
  ,"request-content-length":${requestContentLength}
  </#if>
  <#if responseContentLength??>
  ,"response-content-length":${responseContentLength}
  </#if>
  <#if metrics.getPlan()??>
  ,"plan":"${metrics.getPlan()}"
  </#if>
  <#if metrics.getApi()??>
  ,"api":"${metrics.getApi()}"
  </#if>
  <#if metrics.getApplication()??>
  ,"application":"${metrics.getApplication()}"
  </#if>
  ,"local-address":"${metrics.getLocalAddress()}"
  ,"remote-address":"${metrics.getRemoteAddress()}"
  <#if metrics.getEndpoint()??>
  ,"endpoint":"${metrics.getEndpoint()}"
  </#if>
  <#if metrics.getTenant()??>
  ,"tenant":"${metrics.getTenant()}"
  </#if>
  <#if metrics.getMessage()??>
  ,"message":"${metrics.getMessage()?j_string}"
  </#if>
  ,"path":"${metrics.getPath()}"
  <#if metrics.getMappedPath()??>
  ,"mapped-path":"${metrics.getMappedPath()}"
  </#if>
  <#if metrics.getUserAgent()??> || pipeline?has_content>
  ,"user-agent":"${metrics.getUserAgent()!""}"
  </#if>
  <#if metrics.getUser()??>
  ,"user":"${metrics.getUser()}"
  </#if>
  <#if metrics.getSecurityType()??>
  ,"security-type":"${metrics.getSecurityType()}"
  </#if>
  <#if metrics.getSecurityToken()??>
  ,"security-token":"${metrics.getSecurityToken()}"
  </#if>
  <#if metrics.getErrorKey()??>
  ,"error-key":"${metrics.getErrorKey()}"
  </#if>
  <#if metrics.getSubscription()??>
  ,"subscription":"${metrics.getSubscription()}"
  </#if>
}</@compress>
