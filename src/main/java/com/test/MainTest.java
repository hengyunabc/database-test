package com.test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MainTest {
	static final Logger logger = LoggerFactory.getLogger(MainTest.class);

	@Autowired
	ConsumerDao consumerDao;

	@Autowired
	MessageDao messageDao;
	@Autowired
	MoneyDao moneyDao;

	@Autowired
	DataSourceTransactionManager transactionManager;

	@Autowired
	TransactionTemplate transactionTemplate;

	// 测试单表的，insert update效率
	// 测试单表的，insert update + 消息队列的效率
	// 测试双表的，insert update效率
	// 测试双表的，insert update + 消息队列的效率

	int count = 10000;
	int threadCount = 10;

	/**
	 * 单表插入时，messageSize是1024,要6秒，设置为10,要2到3秒， 无message要1到2秒。
	 */
	int messageSize = 10;

	Executor executor = Executors.newCachedThreadPool();
	Random random = new Random();

	@PostConstruct
	public void init() throws InterruptedException {
		for (int countI = 100; countI < 1000000; countI = countI * 10) {
			for (int threadCountI = 1; threadCountI < 50; threadCountI = threadCountI + 10) {
				for (int messageSizeI = 10; messageSizeI < 10000; messageSizeI = messageSizeI * 10) {
					threadCount = threadCountI;
					messageSize = messageSizeI;
					count = countI;
					System.err.println("threadCount:" + threadCountI + ", count:" + countI + ", messageSize:"
							+ messageSizeI);
					testInsert2();
					testInsert2();
					testUpdate();
					testTwoTableInsert();
					testTwoUpdate();
					testTwoSelectForUpdate();
				}
			}
		}
		// testInsert2();
		// testInsert2();
		// testUpdate();
		// testTwoTableInsert();
		// testTwoUpdate();
		// testTwoSelectForUpdate();
		System.err.println("test done");
	}

	public void testInsert() {
		dropAndCreateAllTable();
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		insertConsumer();
		System.err.println("1111:");
		System.err.println(stopWatch);

		dropAndCreateAllTable();
		stopWatch.reset();
		stopWatch.start();
		insertConsumerAndMessage();

		System.err.println("2222:");
		System.err.println(stopWatch);
	}

	public void testInsert2() throws InterruptedException {
		dropAndCreateAllTable();
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				consumerDao.insert(new Consumer("xxxx", 18));
			}
		});
		System.err.println("单表insert:");
		System.err.println(stopWatch);

		dropAndCreateAllTable();
		stopWatch.reset();
		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							consumerDao.insert(new Consumer("xxxx", 18));
							// int xxx = threadCount / (threadCount - 10);
							messageDao.insertMessage(new byte[messageSize]);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});

		System.err.println("单表insert+message:");
		System.err.println(stopWatch);
	}

	public void testTwoTableInsert() throws InterruptedException {
		dropAndCreateAllTable();
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							long consumerId = consumerDao.insert(new Consumer("xxxx", 18));
							Money money = new Money(consumerId, 100);
							money.setId(consumerId);
							moneyDao.insert(money);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});
		System.err.println("双表insert:");
		System.err.println(stopWatch);

		dropAndCreateAllTable();
		stopWatch.reset();
		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							long consumerId = consumerDao.insert(new Consumer("xxxx", 18));
							Money money = new Money(consumerId, 100);
							money.setId(consumerId);
							moneyDao.insert(money);
							messageDao.insertMessage(new byte[messageSize]);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});

		System.err.println("双表insert+message:");
		System.err.println(stopWatch);
	}

	public void testUpdate() throws InterruptedException {
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				int nextInt = random.nextInt(count) + 1;
				Consumer consumer = new Consumer("xxxx", nextInt);
				consumer.setId(nextInt);
				consumerDao.update(consumer);
			}
		});
		System.err.println("单表update:");
		System.err.println(stopWatch);

		stopWatch.reset();
		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							int nextInt = random.nextInt(count) + 1;
							Consumer consumer = new Consumer("xxxx", nextInt);
							consumer.setId(nextInt);
							consumerDao.update(consumer);
							messageDao.insertMessage(new byte[messageSize]);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});

		System.err.println("单表update+message:");
		System.err.println(stopWatch);
	}

	public void testTwoUpdate() throws InterruptedException {
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							int nextInt = random.nextInt(count) + 1;
							Consumer consumer = new Consumer("xxxx", nextInt);
							consumer.setId(nextInt);
							consumerDao.update(consumer);
							Money money = new Money(nextInt, 120);
							money.setId(nextInt);
							moneyDao.update(money);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});
		System.err.println("双表update:");
		System.err.println(stopWatch);

		stopWatch.reset();
		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							int nextInt = random.nextInt(count) + 1;
							Consumer consumer = new Consumer("xxxx", nextInt);
							consumer.setId(nextInt);
							consumerDao.update(consumer);
							Money money = new Money(nextInt, 120);
							money.setId(nextInt);
							moneyDao.update(money);
							messageDao.insertMessage(new byte[messageSize]);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});

		System.err.println("双表update+message:");
		System.err.println(stopWatch);
	}

	public void testTwoSelectForUpdate() throws InterruptedException {
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						int nextInt = 0;
						try {
							nextInt = random.nextInt(count) + 1;
							consumerDao.selectForUpdate(nextInt);
							moneyDao.selectForUpdate(nextInt);
							Consumer consumer = new Consumer("xxxx", nextInt);
							consumer.setId(nextInt);
							consumerDao.update(consumer);
							Money money = new Money(nextInt, 120);
							money.setId(nextInt);
							moneyDao.update(money);
						} catch (Exception ex) {
							logger.error("nextInt:" + nextInt, ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});
		System.err.println("双表selectForUpdate:");
		System.err.println(stopWatch);

		stopWatch.reset();
		stopWatch.start();
		multiThreadTask(count, threadCount, new Runnable() {
			@Override
			public void run() {
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							int nextInt = random.nextInt(count) + 1;
							consumerDao.selectForUpdate(nextInt);
							moneyDao.selectForUpdate(nextInt);
							Consumer consumer = new Consumer("xxxx", nextInt);
							consumer.setId(nextInt);
							consumerDao.update(consumer);
							Money money = new Money(nextInt, 120);
							money.setId(nextInt);
							moneyDao.update(money);
							messageDao.insertMessage(new byte[messageSize]);
						} catch (Exception ex) {
							logger.error("", ex);
							status.setRollbackOnly();
						}
					}
				});
			}
		});

		System.err.println("双表selectForUpdate+message:");
		System.err.println(stopWatch);
	}

	public void testSelectForUpdate() {

	}

	public void multiThreadTask(int taskCount, int threadCount, final Runnable runnable) throws InterruptedException {
		final AtomicInteger countInteger = new AtomicInteger(taskCount);
		final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; ++i) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					while (countInteger.getAndDecrement() > 0) {
						runnable.run();
					}
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await();
	}

	interface Task {
		public void run(int taksNumber);
	}

	public void dropAndCreateAllTable() {
		consumerDao.dropTable();
		consumerDao.createTable();
		messageDao.dropTable();
		messageDao.createTable();
		moneyDao.dropTable();
		moneyDao.createTable();
	}

	public void insertConsumer() {
		for (int i = 0; i < count; ++i) {
			consumerDao.insert(new Consumer("" + i, i));
		}
	}

	public void insertConsumerAndMessage() {
		for (int i = 0; i < count; ++i) {
			final int ii = i;
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						consumerDao.insert(new Consumer("" + ii, ii));
						messageDao.insertMessage(new byte[1024]);
					} catch (Exception ex) {
						logger.error("", ex);
						status.setRollbackOnly();
					}
				}
			});
		}
	}

	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
		// context.refresh();
		// context.getBean("mainTest");
		System.in.read();

	}
}
