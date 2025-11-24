package org.example.handler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 处理 JSON 数组与 List<Integer> 的转换，确保字符串元素转为整数
 */
@MappedJdbcTypes (JdbcType.VARCHAR)
@MappedTypes (List.class)
public class JsonListTypeHandler extends BaseTypeHandler<List<Integer>> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Integer> parameter, JdbcType jdbcType) throws SQLException {
        try {
// 写入数据库时，将 List<Integer> 转为 JSON 字符串
            String json = objectMapper.writeValueAsString (parameter);
            ps.setString (i, json);
        } catch (Exception e) {
            throw new SQLException ("Failed to convert List<Integer> to JSON string", e);
        }
    }
    @Override
    public List<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }
    @Override
    public List<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }
    @Override
    public List<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }
    /**
     解析 JSON 字符串为 List<Integer>，处理字符串类型的数字（如 "1" 转为 1）
     */
    private List<Integer> parseJson (String json) {
        if (json == null || json.trim ().isEmpty ()) {
            return new ArrayList<>();
        }
        try {
// 先尝试直接解析为 List<Integer>
            List<Integer> integerList = objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
            return integerList;
        } catch (Exception e) {
// 如果解析失败（可能是字符串类型的数字），尝试转换
            try {
// 先解析为 List<String>，再转为 List<Integer>
                List<String> stringList = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                List<Integer> integerList = new ArrayList<>();
                for (String s : stringList) {
                    integerList.add (Integer.parseInt (s.trim ())); // 字符串转整数
                }
                return integerList;
            } catch (Exception ex) {
                throw new RuntimeException ("Failed to parse JSON to List<Integer>: " + json, ex);
            }
        }
    }
}