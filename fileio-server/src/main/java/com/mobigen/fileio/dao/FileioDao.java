package com.mobigen.fileio.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface FileioDao {
    Boolean executeQuery(Connection conn, List<String> sqls);

}