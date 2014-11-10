package com.test;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Service;
@Service
public class MessageDao extends JdbcDaoSupport {
	@Autowired
	public void setDataSource2(DataSource dataSource) {
		super.setDataSource(dataSource);
	}
	public void createTable() {
		String sql = "CREATE TABLE `message` (`id` int(64) NOT NULL AUTO_INCREMENT, "
				+ " `data` mediumblob NOT NULL, PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

		getJdbcTemplate().execute(sql);
	}

	public void dropTable() {
		String sql = "drop table if exists message";
		getJdbcTemplate().execute(sql);
	}

	public void insertMessage(byte[] data) {
		String sql = "insert into message(data) values(?)";
		getJdbcTemplate().update(sql, data);
	}
}
