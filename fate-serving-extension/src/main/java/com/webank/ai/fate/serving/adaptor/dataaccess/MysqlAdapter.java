package com.webank.ai.fate.serving.adaptor.dataaccess;

import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MysqlAdapter extends AbstractSingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(MysqlAdapter.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String tableName;
    private final String idColumn;

    public MysqlAdapter(String serviceId) throws Exception {
        Environment environment = getEnvironment();
        String property = environment.getProperty(serviceId);
        JSONObject dbPropertyByServiceId = JsonUtil.json2Object(property, JSONObject.class);

        if (dbPropertyByServiceId == null) {
            throw new Exception("not found db property about serviceId");
        }

        this.dbUrl = dbPropertyByServiceId.getString("dbUrl");
        this.dbUser = dbPropertyByServiceId.getString("dbUser");
        this.dbPassword = dbPropertyByServiceId.getString("dbPassword");
        this.tableName = dbPropertyByServiceId.getString("tableName");
        this.idColumn = dbPropertyByServiceId.getString("idColumn");
    }

    @Override
    public void init() {

    }

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // 获取数据库连接
            connection = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword);

            // 构建SQL查询语句
            String sql = "SELECT * FROM " + this.tableName + " WHERE " + this.idColumn + " = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, (String) featureIds.get("id"));

            // 执行查询
            resultSet = preparedStatement.executeQuery();

            // 处理查询结果
            if (resultSet.next()) {
                Map<String, Object> data = new HashMap<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    data.put(columnName, columnValue);
                }

                returnResult.setRetcode(StatusCode.SUCCESS);
                returnResult.setData(data);
            } else {
                returnResult.setRetcode(StatusCode.FEATURE_DATA_ADAPTOR_ERROR);
                returnResult.setRetmsg("Data not found for id: " + featureIds.get("id"));
            }

            logger.info("MysqlAdapter result, {}", JsonUtil.object2Json(returnResult));

        } catch (SQLException e) {
            logger.error("SQL error", e);
            returnResult.setRetcode(StatusCode.SYSTEM_ERROR);
            returnResult.setRetmsg(e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Failed to close database resources", e);
            }
        }

        return returnResult;
    }

    public static void main(String[] args) throws Exception {
        MysqlAdapter mysqlAdapter = new MysqlAdapter("dasdassadas454563d2");
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("id", "1691708343659307010");
        ReturnResult data = mysqlAdapter.getData(null, mapD);
        System.out.println(data.getData());
    }
}