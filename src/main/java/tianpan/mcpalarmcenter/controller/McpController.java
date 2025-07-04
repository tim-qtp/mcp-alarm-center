package tianpan.mcpalarmcenter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tianpan.mcpalarmcenter.mcp.tools.AlarmTools;
import tianpan.mcpalarmcenter.service.AlarmService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class McpController {
    
    private final AlarmService alarmService;
    private final AlarmTools alarmTools;
    
    // 存储SSE连接
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    /**
     * MCP SSE端点 - 用于Cursor等客户端连接
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String clientId = "client_" + System.currentTimeMillis();
        emitters.put(clientId, emitter);
        
        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError((ex) -> emitters.remove(clientId));
        
        try {
            // 发送初始连接消息
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"connected\",\"clientId\":\"" + clientId + "\"}"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    /**
     * MCP 消息端点
     */
    @PostMapping(value = "/mcp/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handleMcpMessage(@RequestBody Map<String, Object> message) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String method = (String) message.get("method");
            Map<String, Object> params = (Map<String, Object>) message.get("params");
            
            switch (method) {
                case "tools/list":
                    response.put("result", Map.of("tools", alarmTools));
                    break;
                case "tools/call":
                    // 处理工具调用
                    String toolName = (String) params.get("name");
                    Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                    response.put("result", handleToolCall(toolName, arguments));
                    break;
                case "resources/list":
                    response.put("result", Map.of("resources", new HashMap<>()));
                    break;
                case "prompts/list":
                    response.put("result", Map.of("prompts", new HashMap<>()));
                    break;
                default:
                    response.put("error", Map.of("message", "Unknown method: " + method));
            }
        } catch (Exception e) {
            response.put("error", Map.of("message", e.getMessage()));
        }
        
        return response;
    }
    
    /**
     * 处理工具调用
     */
    private Object handleToolCall(String toolName, Map<String, Object> arguments) {
        switch (toolName) {
            case "get_latest_alarms":
                int limit = arguments.containsKey("limit") ? (Integer) arguments.get("limit") : 5;
                return alarmService.getLatestAlarms(limit);
            case "get_alarm_count":
                String timeRange = (String) arguments.get("timeRange");
                return alarmService.getAlarmCount(timeRange);
            case "ignore_alarm":
                String alertId = (String) arguments.get("alertId");
                return alarmService.ignoreAlarm(alertId);
            case "get_alarm_count_between":
                String start = (String) arguments.get("start");
                String end = (String) arguments.get("end");
                return alarmService.getAlarmCountBetween(
                    java.time.LocalDateTime.parse(start),
                    java.time.LocalDateTime.parse(end)
                );
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }
    
    /**
     * MCP 工具列表端点
     */
    @GetMapping(value = "/mcp/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getTools() {
        Map<String, Object> response = new HashMap<>();
        response.put("tools", alarmTools);
        return response;
    }
    
    /**
     * MCP 资源列表端点
     */
    @GetMapping(value = "/mcp/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getResources() {
        Map<String, Object> response = new HashMap<>();
        response.put("resources", new HashMap<>());
        return response;
    }
    
    /**
     * MCP 提示词端点
     */
    @GetMapping(value = "/mcp/prompts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getPrompts() {
        Map<String, Object> response = new HashMap<>();
        response.put("prompts", new HashMap<>());
        return response;
    }
    
    /**
     * MCP 健康检查端点
     */
    @GetMapping(value = "/mcp/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "alarm-center-mcp");
        return response;
    }
} 