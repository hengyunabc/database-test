package com.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

@Service
public class ConsumerDao extends JdbcDaoSupport {
	static final Logger logger = LoggerFactory.getLogger(ConsumerDao.class);
	@Autowired
	public void setDataSource2(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	public void createTable() {
		String sql = "CREATE TABLE `consumer` (  `id` int(11) NOT NULL AUTO_INCREMENT, `name` char(50) NOT NULL, `age` int(11) DEFAULT NULL,  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
		getJdbcTemplate().execute(sql);
	}

	public void dropTable() {
		String sql = "drop table if exists consumer";
		getJdbcTemplate().execute(sql);
	}

	public long insert(final Consumer consumer) {
		final String sql = "insert into consumer(name, age) values(?, ?)";
		KeyHolder holder = new GeneratedKeyHolder();
//		getJdbcTemplate().update(sql, consumer.getName(), consumer.getAge());
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); 
				ps.setString(1, consumer.getName());
				ps.setInt(2, consumer.getAge());
				return ps;
			}
		}, holder);
		long newId = holder.getKey().longValue();
		return newId;
	}
	
	public Consumer selectForUpdate(long id) {
		String sql = "select * from consumer where id = ? for update";
		Consumer consumer = getJdbcTemplate().queryForObject(sql, new Object[] { id }, new RowMapper<Consumer>() {
			@Override
			public Consumer mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = rs.getString("name");
				int age = rs.getInt("age");
				long id = rs.getLong("id");
				Consumer consumer= new Consumer(name, age);
				consumer.setId(id);
				return consumer;
			}
			
		});
		return consumer;
	}

	public void insertAndCreateMoney(Consumer consumer) {
		String sql = "insert into consumer(name, age) values(?, ?)";
		getJdbcTemplate().update(sql, consumer.getName(), consumer.getAge());
	}
	
	public void update(Consumer consumer) {
		String sql = "update consumer set name=?, age=? where id=?";
		getJdbcTemplate().update(sql, consumer.getName(), consumer.getAge(), consumer.getId());
	}
}
