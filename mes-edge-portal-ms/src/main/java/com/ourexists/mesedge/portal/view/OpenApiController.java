package com.ourexists.mesedge.portal.view;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Tag(name = "开放接口")
@RestController
@RequestMapping("/open/proxyScada")
public class OpenApiController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping
    public ResponseEntity<byte[]> proxy(
            @RequestParam String url,
            HttpServletRequest request
    ) throws Exception {

        log.info("proxy -> {}", url);

        // ===== 复制请求头 =====
        HttpHeaders headers = new HttpHeaders();
        var headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();

            // host 不能带过去
            if (!"host".equalsIgnoreCase(name)
                    && !"accept-encoding".equalsIgnoreCase(name)) {
                headers.add(name, request.getHeader(name));
            }
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);

        // ===== 请求目标 =====
        ResponseEntity<byte[]> resp =
                restTemplate.exchange(
                        url,
                        HttpMethod.valueOf(request.getMethod()),
                        entity,
                        byte[].class
                );

        MediaType contentType = resp.getHeaders().getContentType();

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.putAll(resp.getHeaders());

// ===== 透传cookie =====
        if (respHeaders.containsKey(HttpHeaders.SET_COOKIE)) {

            List<String> cookies =
                    respHeaders.get(HttpHeaders.SET_COOKIE);

            List<String> newCookies = cookies.stream()
                    .map(c -> c.replaceAll("Domain=[^;]+;", ""))
                    .toList();

            respHeaders.put(HttpHeaders.SET_COOKIE, newCookies);
        }

// ===== 清理压缩 =====
        respHeaders.remove(HttpHeaders.CONTENT_ENCODING);
        respHeaders.remove(HttpHeaders.CONTENT_LENGTH);

        // ===== 如果是 HTML → 注入 base =====
        if (contentType != null &&
                contentType.includes(MediaType.TEXT_HTML)) {

            String html =
                    new String(resp.getBody(), StandardCharsets.UTF_8);

            URI uri = URI.create(url);

            String base =
                    uri.getScheme() + "://" +
                            uri.getHost() +
                            (uri.getPort() > 0 ? ":" + uri.getPort() : "") +
                            "/";

            html = injectBase(html, base);


            return ResponseEntity.status(resp.getStatusCode())
                    .headers(respHeaders)
                    .body(html.getBytes(StandardCharsets.UTF_8));
        }

        // ===== 非HTML直接返回 =====
        return ResponseEntity.status(resp.getStatusCode())
                .headers(respHeaders)
                .body(resp.getBody());
    }

    private String injectBase(String html, String base) {

        String tag =
                "<base href=\"/open/proxyScada?url=" + base + "\">";

        if (html.contains("<head>")) {
            return html.replace("<head>", "<head>" + tag);
        }

        return tag + html;
    }
}
