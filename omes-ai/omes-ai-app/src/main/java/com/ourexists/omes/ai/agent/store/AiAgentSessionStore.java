package com.ourexists.omes.ai.agent.store;

import com.ourexists.omes.ai.model.agent.AiAgentMessageDto;
import com.ourexists.omes.ai.model.agent.AiAgentSessionDto;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class AiAgentSessionStore {

    private final JdbcTemplate jdbcTemplate;

    public AiAgentSessionStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ai_agent_chat_session (" +
                    "session_id VARCHAR(64) PRIMARY KEY," +
                    "title VARCHAR(255)," +
                    "operator_id VARCHAR(128)," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ai_agent_chat_message (" +
                    "id VARCHAR(64) PRIMARY KEY," +
                    "session_id VARCHAR(64)," +
                    "role VARCHAR(64)," +
                    "content TEXT," +
                    "created_at TIMESTAMP)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ai_agent_audit_log (" +
                    "id VARCHAR(64) PRIMARY KEY," +
                    "session_id VARCHAR(64)," +
                    "operator_id VARCHAR(128)," +
                    "action VARCHAR(64)," +
                    "success_flag INTEGER," +
                    "message TEXT," +
                    "created_at TIMESTAMP)");
        } catch (DataAccessException ignored) {
            // Some DBs may not support IF NOT EXISTS. In that case, rely on pre-created schema.
        }
    }

    public String createSession(String title, String operatorId) {
        String sessionId = UUID.randomUUID().toString();
        Date now = new Date();
        jdbcTemplate.update("INSERT INTO ai_agent_chat_session(session_id,title,operator_id,created_at,updated_at) VALUES (?,?,?,?,?)",
                sessionId, title, operatorId, now, now);
        return sessionId;
    }

    public void appendMessage(String sessionId, String role, String content) {
        Date now = new Date();
        jdbcTemplate.update("INSERT INTO ai_agent_chat_message(id,session_id,role,content,created_at) VALUES (?,?,?,?,?)",
                UUID.randomUUID().toString(), sessionId, role, content, now);
        jdbcTemplate.update("UPDATE ai_agent_chat_session SET updated_at=? WHERE session_id=?", now, sessionId);
    }

    public List<AiAgentSessionDto> listSessions() {
        return jdbcTemplate.query("SELECT session_id,title,operator_id,created_at,updated_at FROM ai_agent_chat_session ORDER BY updated_at DESC",
                (rs, rowNum) -> mapSession(rs));
    }

    public List<AiAgentMessageDto> listMessages(String sessionId) {
        return jdbcTemplate.query("SELECT id,session_id,role,content,created_at FROM ai_agent_chat_message WHERE session_id=? ORDER BY created_at ASC",
                (rs, rowNum) -> mapMessage(rs), sessionId);
    }

    public void saveAudit(String sessionId, String operatorId, String action, boolean success, String message) {
        jdbcTemplate.update("INSERT INTO ai_agent_audit_log(id,session_id,operator_id,action,success_flag,message,created_at) VALUES (?,?,?,?,?,?,?)",
                UUID.randomUUID().toString(), sessionId, operatorId, action, success ? 1 : 0, message, new Date());
    }

    private AiAgentSessionDto mapSession(ResultSet rs) throws SQLException {
        AiAgentSessionDto dto = new AiAgentSessionDto();
        dto.setSessionId(rs.getString("session_id"));
        dto.setTitle(rs.getString("title"));
        dto.setOperatorId(rs.getString("operator_id"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));
        return dto;
    }

    private AiAgentMessageDto mapMessage(ResultSet rs) throws SQLException {
        AiAgentMessageDto dto = new AiAgentMessageDto();
        dto.setId(rs.getString("id"));
        dto.setSessionId(rs.getString("session_id"));
        dto.setRole(rs.getString("role"));
        dto.setContent(rs.getString("content"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        return dto;
    }
}
