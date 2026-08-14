package com.ourexists.omes.device.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.device.model.ProductAttrConfig;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL jsonb 列与 {@link ProductAttrConfig} 映射；写入使用 {@link PGobject}，避免被当作 varchar。
 */
public class PgJsonbProductAttrConfigTypeHandler extends BaseTypeHandler<ProductAttrConfig> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductAttrConfig parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject pg = new PGobject();
        pg.setType("jsonb");
        try {
            pg.setValue(MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("serialize product attr_config to jsonb failed", e);
        }
        ps.setObject(i, pg, Types.OTHER);
    }

    @Override
    public ProductAttrConfig getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public ProductAttrConfig getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public ProductAttrConfig getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private static ProductAttrConfig parse(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, ProductAttrConfig.class);
        } catch (Exception e) {
            throw new SQLException("parse jsonb to product attr_config failed", e);
        }
    }
}
