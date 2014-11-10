package com.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Service;

@Service
public class MoneyDao extends JdbcDaoSupport {

	SimpleJdbcInsert insert;

	@Autowired
	public void setDataSource2(DataSource dataSource) {
		super.setDataSource(dataSource);
		insert = new SimpleJdbcInsert(getJdbcTemplate()).withTableName("money").usingColumns("id",
				"consumerId", "number");
	}

	public void createTable() {
		String sql = "CREATE TABLE `money` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `consumerId` int(11) NOT NULL,  `number` int(11) NOT NULL DEFAULT '0',  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
		getJdbcTemplate().execute(sql);
	}

	public void dropTable() {
		String sql = "drop table if exists money";
		getJdbcTemplate().execute(sql);
	}

	public void insert(Money money) {
		Map<String, Object> parameters = new HashMap<String, Object>(4);
		if (money.getId() > 0) {
			parameters.put("id", money.getId());
		}
		parameters.put("consumerId", money.getConsumerId());
		parameters.put("number", money.getNumber());

		insert.execute(parameters);
	}

	public void update(Money money) {
		String sql = "update money set consumerId=?, number=? where id=?";
		getJdbcTemplate().update(sql, money.getConsumerId(), money.getNumber(), money.getId());
	}

	public Money selectForUpdate(long consumerId) {
		String sql = "select * from money where consumerId = ? for update";
		Money money = getJdbcTemplate().queryForObject(sql, new Object[] { consumerId }, new RowMapper<Money>() {
			@Override
			public Money mapRow(ResultSet rs, int rowNum) throws SQLException {
				long number = rs.getInt("number");
				long consumerId = rs.getInt("consumerId");
				long id = rs.getLong("id");
				Money money = new Money(consumerId, number);
				money.setId(id);
				return money;
			}

		});
		return money;
	}
}
