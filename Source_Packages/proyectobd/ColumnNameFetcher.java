package Source_Packages.proyectobd;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnNameFetcher {
    public static String getColumnNames(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            StringBuilder columnNames = new StringBuilder();

            while (columns.next()) {
                if (columnNames.length() > 0) {
                    columnNames.append(", ");
                }
                columnNames.append(columns.getString("COLUMN_NAME"));
            }

            return columnNames.toString();
        }
    }
}