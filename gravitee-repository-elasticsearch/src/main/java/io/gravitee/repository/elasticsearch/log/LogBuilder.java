/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.elasticsearch.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.ssl.CertificateInfo;
import io.gravitee.common.ssl.SSLInfo;
import io.gravitee.elasticsearch.model.SearchHit;
import io.gravitee.repository.log.model.ExtendedLog;
import io.gravitee.repository.log.model.Log;
import io.gravitee.repository.log.model.Request;
import io.gravitee.repository.log.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * Builder for log request.
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 * @author Guillaume Waignier (Zenika)
 * @author Sebastien Devaux (Zenika)
 */
final class LogBuilder {
	
	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(LogBuilder.class);
	
	/** Document simple date format **/
	private static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final static String FIELD_TRANSACTION_ID = "transaction";
    private final static String FIELD_TIMESTAMP = "@timestamp";
    private final static String FIELD_GATEWAY = "gateway";

    private final static String FIELD_METHOD = "method";
    private final static String FIELD_URI = "uri";
    private final static String FIELD_ENDPOINT = "endpoint";
    private final static String FIELD_REQUEST_CONTENT_LENGTH = "request-content-length";
    private final static String FIELD_RESPONSE_CONTENT_LENGTH = "response-content-length";
    private final static String FIELD_CLIENT_REQUEST = "client-request";
    private final static String FIELD_PROXY_REQUEST = "proxy-request";
    private final static String FIELD_CLIENT_RESPONSE = "client-response";
    private final static String FIELD_PROXY_RESPONSE = "proxy-response";
    private final static String FIELD_BODY = "body";
    private final static String FIELD_HEADERS = "headers";
    private final static String FIELD_STATUS = "status";
    private final static String FIELD_RESPONSE_TIME = "response-time";
    private final static String FIELD_API_RESPONSE_TIME = "api-response-time";

    private final static String FIELD_REQUEST_SSL_LOCAL_PRINCIPAL = "ssl-local-principal";
    private final static String FIELD_REQUEST_SSL_PEER_PRINCIPAL = "ssl-peer-principal";
    private final static String FIELD_REQUEST_SSL_PROTOCOL = "ssl-protocol";

    private final static String FIELD_SSL_INFO = "ssl-info";
    private final static String FIELD_SSL_LOCAL_PRINCIPAL = "local-principal";
    private final static String FIELD_SSL_PEER_PRINCIPAL = "peer-principal";
    private final static String FIELD_SSL_CIPHER_SUITE = "cipher-suite";
    private final static String FIELD_SSL_LOCAL_CERTS = "local-certificates";
    private final static String FIELD_SSL_PEER_CERTS = "peer-certificates";

    private final static String FIELD_SSL_CERT_VERSION = "version";
    private final static String FIELD_SSL_CERT_SERIAL_NUMBER = "serial-number";
    private final static String FIELD_SSL_CERT_ALGORITHM = "algorithm";
    private final static String FIELD_SSL_CERT_ISSUER = "issuer";
    private final static String FIELD_SSL_CERT_SUBJECT = "subject";

    private final static String FIELD_LOCAL_ADDRESS = "local-address";
    private final static String FIELD_REMOTE_ADDRESS = "remote-address";

    private final static String FIELD_TENANT = "tenant";
    private final static String FIELD_APPLICATION = "application";
    private final static String FIELD_API = "api";
    private final static String FIELD_PLAN = "plan";
    private final static String FIELD_HOST = "host";

    private final static String FIELD_MESSAGE = "message";
    private final static String FIELD_USER = "user";

    private final static String FIELD_SECURITY_TYPE = "security-type";
    private final static String FIELD_SECURITY_TOKEN = "security-token";

    private final static String FIELD_ERROR_KEY = "error-key";

    static Log createLog(final SearchHit hit) {
        return createLog(hit, new Log());
    }

    static ExtendedLog createExtendedLog(final SearchHit hit, final JsonNode log) {
        ExtendedLog extentedLog = createLog(hit, new ExtendedLog());

        // Add client and proxy requests / responses
        if (log != null) {
            extentedLog.setClientRequest(createRequest(log.get(FIELD_CLIENT_REQUEST)));
            extentedLog.setProxyRequest(createRequest(log.get(FIELD_PROXY_REQUEST)));
            extentedLog.setClientResponse(createResponse(log.get(FIELD_CLIENT_RESPONSE)));
            extentedLog.setProxyResponse(createResponse(log.get(FIELD_PROXY_RESPONSE)));
        }

        return extentedLog;
    }

    private static void handleTextChild(JsonNode node, String fieldName, Consumer<String> ifDefinedHandler) {
        final JsonNode fieldNode = node.get(fieldName);
        if (fieldNode != null && ! fieldNode.isNull()) {
            ifDefinedHandler.accept(fieldNode.asText());
        }
    }

    private static <T extends Log> T  createLog(final SearchHit hit, final T log) {
        final JsonNode source = hit.getSource();
        log.setId(hit.getId());
        log.setTransactionId(source.get(FIELD_TRANSACTION_ID).asText());
        log.setGateway( source.get(FIELD_GATEWAY).asText());

        try {
            log.setTimestamp(dtf.parse((source.get(FIELD_TIMESTAMP).asText())).getTime());
        } catch (final ParseException e) {
            logger.error("Impossible to parse date", e);
            throw new IllegalArgumentException("Impossible to parse timestamp field", e);
        }

        log.setUri(source.get(FIELD_URI).asText());

        JsonNode methodNode = source.get(FIELD_METHOD);

        if (methodNode.canConvertToInt()) {
            log.setMethod(HttpMethod.get(methodNode.asInt()));
        } else {
            log.setMethod(HttpMethod.valueOf(methodNode.asText()));
        }

        log.setStatus(source.get(FIELD_STATUS).asInt());
        log.setResponseTime(source.get(FIELD_RESPONSE_TIME).asLong());
        log.setApiResponseTime(source.path(FIELD_API_RESPONSE_TIME).asLong());
        log.setRequestContentLength(source.path(FIELD_REQUEST_CONTENT_LENGTH).asLong());
        log.setResponseContentLength(source.path(FIELD_RESPONSE_CONTENT_LENGTH).asLong());
        log.setLocalAddress(source.get(FIELD_LOCAL_ADDRESS).asText());
        log.setRemoteAddress(source.get(FIELD_REMOTE_ADDRESS).asText());

        handleTextChild(source, FIELD_TENANT, log::setTenant);
        handleTextChild(source, FIELD_APPLICATION, log::setApplication);
        handleTextChild(source, FIELD_API, log::setApi);
        handleTextChild(source, FIELD_PLAN, log::setPlan);
        handleTextChild(source, FIELD_ENDPOINT, log::setEndpoint);
        handleTextChild(source, FIELD_MESSAGE, log::setMessage);
        handleTextChild(source, FIELD_HOST, log::setHost);
        handleTextChild(source, FIELD_USER, log::setUser);
        handleTextChild(source, FIELD_SECURITY_TYPE, log::setSecurityType);
        handleTextChild(source, FIELD_SECURITY_TOKEN, log::setSecurityToken);
        handleTextChild(source, FIELD_ERROR_KEY, log::setErrorKey);
        handleTextChild(source, FIELD_REQUEST_SSL_PEER_PRINCIPAL, log::setSslPeerPrincipal);
        handleTextChild(source, FIELD_REQUEST_SSL_LOCAL_PRINCIPAL, log::setSslLocalPrincipal);
        handleTextChild(source, FIELD_REQUEST_SSL_PROTOCOL, log::setSslProtocol);

        return log;
    }

    private static CertificateInfo createCertificateInfo(final JsonNode node) {
        if (node == null) {
            return null;
        }

        CertificateInfo cert = new CertificateInfo();
        if (node.get(FIELD_SSL_CERT_VERSION) != null) {
            cert.setVersion(node.get(FIELD_SSL_CERT_VERSION).asInt());
        }
        if (node.get(FIELD_SSL_CERT_SERIAL_NUMBER) != null) {
            cert.setSerialNumber(BigInteger.valueOf(node.get(FIELD_SSL_CERT_SERIAL_NUMBER).asLong()));
        }
        handleTextChild(node, FIELD_SSL_CERT_ALGORITHM, cert::setAlgorithm);
        handleTextChild(node, FIELD_SSL_CERT_ISSUER, cert::setIssuer);
        handleTextChild(node, FIELD_SSL_CERT_SUBJECT, cert::setSubject);
        return cert;
    }

    private static SSLInfo createSslInfo(final JsonNode node) {
        if (node == null) {
            return null;
        }

        SSLInfo sslInfo = new SSLInfo();

        handleTextChild(node, FIELD_SSL_LOCAL_PRINCIPAL, sslInfo::setLocalPrincipal);
        handleTextChild(node, FIELD_SSL_PEER_PRINCIPAL, sslInfo::setPeerPrincipal);
        handleTextChild(node, FIELD_SSL_CIPHER_SUITE, sslInfo::setCipherSuite);

        if (node.get(FIELD_SSL_LOCAL_CERTS) != null) {
            sslInfo.setLocalCertificates(StreamSupport.stream(node.get(FIELD_SSL_LOCAL_CERTS).spliterator(), false)
                    .map(LogBuilder::createCertificateInfo)
                    .toArray(CertificateInfo[]::new));
        }
        if (node.get(FIELD_SSL_PEER_CERTS) != null) {
            sslInfo.setPeerCertificates(StreamSupport.stream(node.get(FIELD_SSL_PEER_CERTS).spliterator(), false)
                    .map(LogBuilder::createCertificateInfo)
                    .toArray(CertificateInfo[]::new));
        }
        return sslInfo;
    }

    private static Request createRequest(final JsonNode node) {
        if (node == null) {
            return null;
        }

        final Request request = new Request();
        request.setUri(node.path(FIELD_URI).asText());

        handleTextChild(node, FIELD_METHOD, methodName -> request.setMethod(HttpMethod.valueOf(node.get(FIELD_METHOD).asText())));
        handleTextChild(node, FIELD_BODY, request::setBody);

        if (node.get(FIELD_SSL_INFO) != null) {
            request.setSslInfo(createSslInfo(node.get(FIELD_SSL_INFO)));
        }

        request.setHeaders(createHttpHeaders(node.get(FIELD_HEADERS)));
        return request;
    }

    private static Response createResponse(final JsonNode node) {
        if (node == null) {
            return null;
        }

        Response response = new Response();
        response.setStatus(node.path(FIELD_STATUS).asInt());
        handleTextChild(node, FIELD_BODY, response::setBody);

        if (node.get(FIELD_SSL_INFO) != null) {
            response.setSslInfo(createSslInfo(node.get(FIELD_SSL_INFO)));
        }
        response.setHeaders(createHttpHeaders(node.get(FIELD_HEADERS)));
        return response;
    }

    private static HttpHeaders createHttpHeaders(final JsonNode node) {
        if (node == null) {
            return null;
        }

        HttpHeaders httpHeaders = new HttpHeaders();

        final Iterator<String> iterator = node.fieldNames();
        while (iterator.hasNext()) {
            final String name = iterator.next();
            final ArrayNode values = (ArrayNode) node.get(name);
            httpHeaders.put(name, convertToList(values));
        }

        return httpHeaders;
    }

    private static List<String> convertToList(ArrayNode values) {
        final List<String> result = new ArrayList<>(values.size());
        values.forEach(jsonNode -> result.add(jsonNode.asText()));
        return result;
    }
}
