# 错误返回样式（统一响应结构）

所有接口错误统一返回 `ApiResponse`，字段：
- `code`：HTTP 对应的业务码（如 401/403/400/404/500）。
- `message`：人类可读的错误信息。
- `errorCode`：规范化错误码字符串（UNAUTHORIZED/ FORBIDDEN/ BAD_REQUEST/ NOT_FOUND/ INTERNAL_ERROR）。
- `data`：错误时为 `null`。

示例：

1) 未认证（401）：
```json
{"code":401,"message":"未认证","errorCode":"UNAUTHORIZED","data":null}
```

2) 无权限（403）：
```json
{"code":403,"message":"无权限","errorCode":"FORBIDDEN","data":null}
```

3) 参数错误（400）：
```json
{"code":400,"message":"请求参数错误","errorCode":"BAD_REQUEST","data":null}
```

4) 资源不存在（404）：
```json
{"code":404,"message":"资源不存在","errorCode":"NOT_FOUND","data":null}
```

5) 服务器错误（500）：
```json
{"code":500,"message":"服务器错误","errorCode":"INTERNAL_ERROR","data":null}
```

说明：
- 401/403 由 `SecurityConfig` 的自定义 EntryPoint/AccessDeniedHandler 输出统一 JSON。
- 400/404/500 由 `GlobalExceptionHandler` 统一映射。
