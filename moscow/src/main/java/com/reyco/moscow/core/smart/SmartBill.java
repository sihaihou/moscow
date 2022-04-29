package com.reyco.moscow.core.smart;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.reyco.moscow.commons.ToString;
import com.reyco.moscow.core.constans.Constant;

/**
 * @author reyco
 * @date 2022.04.15
 * @version v1.0.1
 */
public class SmartBill extends ToString{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8232427601861904351L;
	/**
	 * 投票者
	 */
	public String host;
	/**
	 * 被投票者
	 */
	public String vote;
	/**
	 * 票数
	 */
	public AtomicLong term = new AtomicLong(0L);
	/**
	 * 角色：启动模式为slave
	 */
	public volatile State state = State.SLAVE;
	/**
	 * lead
	 */
	public volatile Long leaderExpire = RandomUtils.nextLong(0, Constant.LEAD_TIMEOUT);

	public volatile Long beatExpireTime = RandomUtils.nextLong(0, Constant.BEAT_INTERVAL_TIME);

	public volatile Long beatTimeout = System.currentTimeMillis();
	
	public void resetLeaderExpire() {
		leaderExpire = Constant.LEAD_TIMEOUT + RandomUtils.nextLong(0, Constant.RESET_TIMEOUT);
	}

	public void resetBeatExpireTime() {
		beatExpireTime = Constant.BEAT_INTERVAL_TIME;
	}
	public void resetBeatTimeout() {
		beatTimeout = System.currentTimeMillis();
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getVote() {
		return vote;
	}
	public void setVote(String vote) {
		this.vote = vote;
	}
	public Long getTerm() {
		return term.get();
	}
	public void setTerm(Long term) {
		this.term.set(term);
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Long getLeaderExpire() {
		return leaderExpire;
	}
	public void setLeaderExpire(Long leaderExpire) {
		this.leaderExpire = leaderExpire;
	}
	public Long getBeatExpireTime() {
		return beatExpireTime;
	}
	public void setBeatExpireTime(Long beatExpireTime) {
		this.beatExpireTime = beatExpireTime;
	}
	public void setTerm(AtomicLong term) {
		this.term = term;
	}
	@Override
	public int hashCode() {
		return Objects.hash(host);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SmartBill)) {
			return false;
		}
		SmartBill otherSmartBill = (SmartBill) obj;
		return StringUtils.equals(host, otherSmartBill.host);
	}
	public enum State {
		MASTER, SLAVE, CANDIDATE;
	}
	
}
