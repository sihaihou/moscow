package com.reyco.moscow.core.smart;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.SortedBag;
import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.reyco.moscow.commons.net.HttpClient;
import com.reyco.moscow.commons.net.HttpClient.HttpResult;
import com.reyco.moscow.commons.utils.InetUtils;
import com.reyco.moscow.core.cluster.Server;
import com.reyco.moscow.core.cluster.ServerListChangeListener;
import com.reyco.moscow.core.cluster.ServerListManager;
import com.reyco.moscow.core.config.RunningConfig;
import com.reyco.moscow.core.constans.Constant;

/**
 * @author reyco
 * @date 2022.04.15
 * @version v1.0.1
 */
@Service
public class SmartService implements ServerListChangeListener {

	public static final Logger logger = LoggerFactory.getLogger(SmartService.class);

	public final static String VOTE_API = "/v1/moscow/smart/vote";

	public final static String BEAT_API = "/v1/moscow/smart/beat";

	public final static String BILL_API = "/v1/moscow/smart/bill";

	private volatile boolean loaded = false;

	public AtomicLong localTerm = new AtomicLong(0L);
	@Autowired
	private RunningConfig runningConfig;
	@Autowired
	private ServerListManager serverListManager;

	// master
	private SmartBill master = null;
	/**
	 * key：server=host+":"+port
	 */
	private Map<String, SmartBill> bills = new HashMap<String, SmartBill>();

	private ScheduledExecutorService masterVoteExecutorService = Executors.newScheduledThreadPool(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("com.reyco.moscow.smart.masterVote");
					return thread;
				}
			});
	private ScheduledExecutorService beatCheckExecutorService = Executors.newScheduledThreadPool(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("com.reyco.moscow.smart.beat");
					return thread;
				}
			});
	private ScheduledExecutorService printBillsExecutorService = Executors.newScheduledThreadPool(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("com.reyco.moscow.smart.printBills");
					return thread;
				}
			});

	@PostConstruct
	public void initMethod() throws Exception {
		serverListManager.addListen(this);
		masterVoteExecutorService.scheduleWithFixedDelay(new MasterVote(), 0, Constant.SUBTRACT_TIME,
				TimeUnit.MILLISECONDS);
		beatCheckExecutorService.scheduleWithFixedDelay(new BeatCheck(), 0, Constant.SUBTRACT_TIME,
				TimeUnit.MILLISECONDS);
		printBillsExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				System.err.println(getLocalServer());
				StringBuilder sb = new StringBuilder();
				for (SmartBill bill : bills.values()) {
					sb.append("【");
					sb.append("port:").append(bill.host.split(":")[1]);
					sb.append(",state:").append(bill.state);
					sb.append("】");
				}
				System.err.println(sb.toString().intern());
			}
		}, 0, 5000, TimeUnit.MILLISECONDS);
	}

	/**
	 * master选举
	 * 
	 * @author reyco
	 * @date 2022.04.17
	 * @version v1.0.1
	 */
	private class MasterVote implements Runnable {
		@Override
		public void run() {
			if (!loaded) {
				return;
			}
			SmartBill local = getLocal();
			local.leaderExpire -= Constant.SUBTRACT_TIME;
			if (local.leaderExpire > 0) {
				return;
			}
			local.resetLeaderExpire();
			local.resetBeatExpireTime();
			local.resetBeatTimeout();
			// 开始选举
			startVote();
		}

		/**
		 * 
		 */
		private void startVote() {

			resetBills();

			SmartBill local = bills.get(getLocalServer());
			local.state = SmartBill.State.CANDIDATE;
			local.vote = local.host;
			local.term.incrementAndGet();

			Map<String, String> params = new HashMap<>();
			params.put("vote", JSON.toJSONString(local));
			for (String server : getAllServerWithoutMySelf()) {
				try {
					String url = buildUrl(server, VOTE_API);
					HttpResult httpResult = HttpClient.httpPost(url, params);
					if (httpResult.getCode() != HttpURLConnection.HTTP_OK) {
						logger.warn("发起选举调用失败：{},url：{}", httpResult.getContent(), url);
						continue;
					}
					logger.debug("获取赞成的票：" + httpResult.getContent());
					SmartBill bill = JSON.parseObject(httpResult.getContent(), SmartBill.class);
					decisionMaster(bill);
				} catch (Exception e) {
					logger.error("error while sending vote to server: {}", server);
				}
			}
		}
	}

	/**
	 * 收到选举
	 * 
	 * @param remote
	 * @return
	 */
	public SmartBill receivedVote(SmartBill remote) {
		if (!bills.containsKey(remote.host)) {
			logger.warn("收到[{}]发过来的选举,本机节点中不存在对方", remote.host);
			throw new IllegalStateException("收到[" + remote.host + "]发过来的选举,本机节点中不存在对方");
		}

		SmartBill local = bills.get(getLocalServer());
		
		local.resetLeaderExpire();
		
		if (remote.term.get() <= local.term.get()) {
			logger.debug("收到[{}]发过来的选举,票比自己的少,localTerm:{},remoteTerm:{}", remote.host, local.term.get(),
					remote.term.get());
			if (StringUtils.isEmpty(local.vote)) {
				local.vote = local.host;
			}
			return local;
		}
		 
		local.state = SmartBill.State.SLAVE;
		local.vote = remote.host;
		local.term.set(remote.term.get());
		logger.info("{}投票给{},当前term：{}", local.host, remote.host, remote.term.get());
		return local;
	}

	/**
	 * 决定master
	 * 
	 * @param smartBill
	 * @return
	 */
	private SmartBill decisionMaster(SmartBill bill) {
		bills.put(bill.host, bill);
		// 选出票最多的host和票
		SortedBag sortedBag = new TreeBag();
		int maxVote = 0;
		String maxBill = null;
		int tempMaxVote = 0;
		String tempMaxBill = null;
		for (SmartBill billTemp : bills.values()) {
			if (StringUtils.isBlank(billTemp.vote)) {
				continue;
			}
			sortedBag.add(tempMaxBill = billTemp.vote);
			if ((tempMaxVote = sortedBag.getCount(tempMaxBill)) > maxVote) {
				maxVote = tempMaxVote;
				maxBill = tempMaxBill;
			}
		}
		if (maxVote >= getMajorityCount()) {
			SmartBill master = bills.get(maxBill);
			master.state = SmartBill.State.MASTER;
			if (!Objects.equals(this.master, master)) {
				this.master = master;
				logger.debug("恭喜：{} 竞选为Master.", master.host);
			}
		}
		return master;
	}

	/**
	 * 心跳检查任务
	 * 
	 * @author reyco
	 * @date 2022.04.15
	 * @version v1.0.1
	 */
	private class BeatCheck implements Runnable {
		@Override
		public void run() {
			if (!loaded) {
				return;
			}

			SmartBill local = getLocal();
			local.beatExpireTime -= Constant.SUBTRACT_TIME;
			if (local.beatExpireTime > 0) {
				return;
			}
			
			local.resetBeatExpireTime();
			local.resetBeatTimeout();

			startBeat();
		}

		/**
		 * 开始心跳
		 */
		private void startBeat() {
			SmartBill local = getLocal();
			if (local.state != SmartBill.State.MASTER) {
				return;
			}
			local.resetLeaderExpire();
			// build data
			Map<String, String> params = new HashMap<String, String>(1);
			params.put("beat", JSON.toJSONString(local));
			for (String server : getAllServerWithoutMySelf()) {
				try {
					String url = buildUrl(server, BEAT_API);
					HttpResult httpResult = HttpClient.httpPost(url, params);
					if (httpResult.getCode() != HttpURLConnection.HTTP_OK) {
						logger.warn("发起心跳调用失败,{},url：{}", httpResult.getContent(), url);
						continue;
					}
					logger.debug("获取心跳响应：" + httpResult.getContent());
					SmartBill bill = JSON.parseObject(httpResult.getContent(), SmartBill.class);
					bills.put(bill.host, bill);
				} catch (Exception e) {
					logger.error("向此[{}]服务发送心跳出错,e:{}", server, e);
				}
			}
			int count = 0;
			for(SmartBill bill : bills.values()) {
				if(System.currentTimeMillis()-bill.beatTimeout>Constant.BEAT_TIMEOUT) {
					if((count += 1)>=getMajorityCount()) {
						resetBills();
						break;
					}
				}
			}
		}
	}

	/**
	 * 接收心跳
	 * 
	 * @param remote
	 * @return
	 * @throws Exception
	 */
	public SmartBill receivedBeat(SmartBill remote) throws Exception {
		if (remote.state != SmartBill.State.MASTER) {
			logger.warn("来自非master的心跳,remote：{}", remote);
			throw new IllegalArgumentException("来自非master的心跳:" + remote);
		}
		SmartBill local = getLocal();
		if (local.term.get() > remote.term.get()) {
			logger.warn("来自票比自己少的心跳,local:{},remote:{}", local, remote);
			throw new IllegalArgumentException("来自票数比自己少的心跳,local:" + local + ",remote:" + remote);
		}
		// 修改slave
		if (local.state != SmartBill.State.SLAVE) {
			logger.info("收到master的心跳,给自己设置为slave节点, remote：{}", remote);
			local.state = SmartBill.State.SLAVE;
			local.vote = remote.host;
		}
		local.resetLeaderExpire();
		local.resetBeatExpireTime();
		local.resetBeatTimeout();
		// 修改master
		makeMaster(remote);
		return local;
	}

	/**
	 * 修改为master
	 * 
	 * @param remote
	 * @return
	 */
	private SmartBill makeMaster(SmartBill remote) {
		if (!Objects.equals(master, remote)) {
			master = remote;
			logger.debug("{}节点成为master,local:{},remote:{}", master.host, getLocal(), remote);
		}

		for (final SmartBill bill : bills.values()) {
			Map<String, String> params = new HashMap<>(1);
			if (!Objects.equals(bill, remote) && bill.state != SmartBill.State.SLAVE) {
				try {
					String url = buildUrl(bill.host, BILL_API);
					HttpResult httpResult = HttpClient.httpGet(url, params);
					if (httpResult.getCode() != HttpURLConnection.HTTP_OK) {
						logger.error("获取其它节点失败,msg:{},url:{}", httpResult.getContent(), url);
						bill.state = SmartBill.State.SLAVE;
						continue;
					}
					SmartBill remoteBill = JSON.parseObject(httpResult.getContent(), SmartBill.class);
					bills.put(remoteBill.host, remoteBill);
				} catch (Exception e) {
					bill.state = SmartBill.State.SLAVE;
					logger.error("");
				}
			}
		}
		return bills.put(remote.host, remote);
	}

	public List<SmartBill> getBills() {
		return new ArrayList<>(bills.values());
	}

	/**
	 * 重置所有的bill
	 */
	private void resetBills() {
		master = null;
		for (SmartBill bill : bills.values()) {
			bill.vote = null;
			bill.setState(SmartBill.State.SLAVE);
			bill.beatTimeout = System.currentTimeMillis();
		}
	}
	/**
	 * 获取绝大多数值
	 * 
	 * @return
	 */
	private Integer getMajorityCount() {
		return bills.size() / 2 + 1;
	}

	/**
	 * 构建Url
	 * 
	 * @param server
	 * @param api
	 * @return
	 */
	private String buildUrl(String server, String api) {
		return HttpClient.HTTP + server + runningConfig.getContextPath() + api;
	}

	/**
	 * 获取除了自己所有的server
	 * 
	 * @return
	 */
	private Set<String> getAllServerWithoutMySelf() {
		Set<String> servers = new HashSet<String>(bills.keySet());
		servers.remove(getLocalServer());
		return servers;
	}

	/**
	 * 获取本地的bill
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private SmartBill getLocal() {
		SmartBill local = bills.get(getLocalServer());
		if (local == null) {
			local = new SmartBill();
			local.host = getLocalServer();
			local.term.set(localTerm.get());
			bills.put(local.host, local);
		}
		if (local == null) {
			throw new IllegalArgumentException("没有发现本地bill");
		}
		return local;
	}

	/**
	 * 获取本地Server: host+":"+port
	 * 
	 * @return
	 */
	public String getLocalServer() {
		return InetUtils.getSelfIp() + Constant.COLON + runningConfig.getPort();
	}

	@Override
	public void onChangeServerList(List<Server> servers) {
		Map<String, SmartBill> tempBills = new HashMap<>(8);
		for (Server member : servers) {
			if (bills.containsKey(member.getKey())) {
				tempBills.put(member.getKey(), bills.get(member.getKey()));
				continue;
			}
			SmartBill bill = new SmartBill();
			bill.host = member.getKey();
			if (getLocalServer().equals(member.getKey())) {
				bill.term.set(localTerm.get());
			}
			tempBills.put(member.getKey(), bill);
		}
		bills = tempBills;
		if(runningConfig.getPort()>0) {
			loaded = true;
		}
	}
}
