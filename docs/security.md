# 安全说明（MVP）

本项目采用 JWT 无状态认证，前后端分离；当前版本为最小可运行版本（MVP），以下为关键安全注意事项及配置。

## CORS（跨域资源共享）

- 目的：允许前端在不同域名/端口调用后端 API。
- 配置位置：`SecurityConfig` 中启用 `http.cors()` 并提供 `CorsConfigurationSource`。
- 测试环境策略：允许所有来源（`*`）进行 `GET/POST/PATCH/OPTIONS`，允许请求头 `Authorization`、`Content-Type`，暴露 `Content-Disposition`（用于 XLSX 下载）。
- 生产环境建议：
  - 将 `AllowedOriginPatterns`（或 `AllowedOrigins`）限制为实际前端域名列表，例如：`https://portal.example.com`。
  - 若使用 Cookie（跨域携带凭证），需要设置 `allowCredentials(true)`，同时不可使用 `*`；应配合 `SameSite=None; Secure` 与 HTTPS。

### 代码片段（测试环境）

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    config.setExposedHeaders(List.of("Content-Disposition"));
    config.setAllowCredentials(false); // '*' 时不可携带凭证
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### 代码片段（生产建议，显式域名 + Cookie）

```java
@Bean
public CorsConfigurationSource corsConfigurationSourceProd() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://portal.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true); // 允许 Cookie
    config.setExposedHeaders(List.of("Content-Disposition"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## CSRF（跨站请求伪造）

- 当前版本为前后端分离 + JWT，无状态会话，后端已关闭 CSRF 防护（`http.csrf().disable()`）。
- 使用 `Authorization: Bearer <token>` 时，浏览器不会自动附带该头，天然避免传统 CSRF 攻击路径。
- 若改用 Cookie 携带会话/凭证，则必须：
  - 开启服务端 CSRF 保护（例如 Spring Security 的 CSRF Token）；
  - 或采用双重提交 Cookie、同源策略强化、严格的 CORS（同源 + 白名单来源 + allowCredentials）、`SameSite` 设置（一般 `Lax` 或 `Strict`）。

## XSS（跨站脚本）

- 前端页面为静态页面 + 简单交互；避免将未转义的用户输入直接注入 HTML。
- 建议：
  - 对输入进行校验与编码（HTML entity 编码）。
  - 后端返回文本数据时，避免直接拼接 HTML，保持结构化 JSON。
  - 配置 Content Security Policy（CSP）限制脚本来源。

## JWT 使用期限

- 登录响应包含 `accessToken` 与 `expiresIn`（秒）。建议设置较短的访问令牌有效期，例如 15–60 分钟。
- 当前实现：仅 Access Token，未实现 Refresh Token。

## Refresh Token（是否实现）

- 现阶段：未实现。
- 建议方案（后续迭代）：
  - 颁发长期 Refresh Token（仅通过安全 Cookie，`HttpOnly`、`Secure`、`SameSite=None`），访问令牌短期有效。
  - 提供刷新端点（如 `POST /api/auth/refresh`）验证 Refresh Token 并发新 Access Token。
  - 支持刷新令牌旋转（每次刷新使旧刷新令牌失效），并在服务端维护令牌黑名单或状态。

## Token 存储策略建议

- `localStorage`（当前方案）：
  - 优点：实现简单，易于前端管理；不会被浏览器自动携带，降低 CSRF 风险。
  - 缺点：可能被 XSS 读取。需要加强前端 XSS 防护与 CSP。
- `HttpOnly Cookie`：
  - 优点：脚本不可读取，降低被盗风险。
  - 缺点：浏览器会自动携带，存在 CSRF 风险；需启用严格 CORS、CSRF 保护与 `SameSite=None; Secure`（跨站）且必须 HTTPS。

### 后端设置 HttpOnly Cookie 示例

```java
// 在登录成功后设置 HttpOnly Cookie（示例）
@PostMapping("/api/auth/login-cookie")
public ResponseEntity<Map<String, Object>> loginWithCookie(@RequestBody Map<String, String> body, HttpServletResponse response) {
    // ... 认证逻辑，生成 accessToken ...
    String jwt = generatedToken;
    ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
            .httpOnly(true)
            .secure(true) // 生产环境必须 HTTPS
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofMinutes(30))
            .build();
    response.addHeader("Set-Cookie", cookie.toString());
    return ResponseEntity.ok(Map.of("message", "登录成功"));
}
```

> 若采用 Cookie 方案，务必将 CORS 改为白名单来源 + `allowCredentials(true)` 并开启 CSRF 防护。

