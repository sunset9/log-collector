package com.mobigen.fileio.dao;

import java.sql.Connection;
import java.util.List;

public interface FileioDao {
    Boolean executeQuery(Connection conn, List<String> sqls);

}