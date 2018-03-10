package br.com.sbrw.mp.protocol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MpSession {

	private HashMap<Integer, MpTalker> mpTalkers = new HashMap<>();

	private Integer sessionId;
	private Integer maxUsers;
	private byte[] syncPacket;
	private byte[] keepAlivePacket;
	private long cliTimeStart;

	public MpSession(MpTalker mpTalker, Integer maxUsers) {
		this.maxUsers = maxUsers;
		sessionId = mpTalker.getSessionId();
		put(mpTalker);
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public void put(MpTalker mpTalker) {
		mpTalkers.put(mpTalker.getPort(), mpTalker);
	}

	public Integer getMaxUsers() {
		return maxUsers;
	}

	public boolean isFull() {
		return this.mpTalkers.size() == maxUsers;
	}

	public Map<Integer, MpTalker> getMpTalkers() {
		return mpTalkers;
	}

	public boolean isAllSyncHelloOk() {
		Map<Integer, MpTalker> mpTalkersTmp = getMpTalkers();
		Iterator<Entry<Integer, MpTalker>> iterator = mpTalkersTmp.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, MpTalker> next = iterator.next();
			MpTalker value = next.getValue();
			if (!value.isSyncHelloOk()) {
				return false;
			}
		}
		return true;
	}

	public boolean isAllPlayerInfoBeforeOk() {
		Map<Integer, MpTalker> mpTalkersTmp = getMpTalkers();
		Iterator<Entry<Integer, MpTalker>> iterator = mpTalkersTmp.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, MpTalker> next = iterator.next();
			MpTalker value = next.getValue();
			if (!value.isPlayerInfoBeforeOk()) {
				return false;
			}
		}
		return true;
	}

	public boolean isAllCarStateBeforeOk() {
		Map<Integer, MpTalker> mpTalkersTmp = getMpTalkers();
		Iterator<Entry<Integer, MpTalker>> iterator = mpTalkersTmp.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, MpTalker> next = iterator.next();
			MpTalker value = next.getValue();
			if (!value.isCarStateInfoBeforeOk()) {
				return false;
			}
		}
		return true;
	}

	public boolean isAllPlayerInfoAfterOk() {
		Map<Integer, MpTalker> mpTalkersTmp = getMpTalkers();
		Iterator<Entry<Integer, MpTalker>> iterator = mpTalkersTmp.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, MpTalker> next = iterator.next();
			MpTalker value = next.getValue();
			if (!value.isPlayerInfoAfterOk()) {
				return false;
			}
		}
		return true;
	}

	public boolean isAllSyncOk() {
		Map<Integer, MpTalker> mpTalkersTmp = getMpTalkers();
		Iterator<Entry<Integer, MpTalker>> iterator = mpTalkersTmp.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, MpTalker> next = iterator.next();
			MpTalker value = next.getValue();
			if (!value.isSyncOk()) {
				return false;
			}
		}
		return true;
	}

	public byte[] getSyncPacket() {
		return syncPacket;
	}

	public void setSyncPacket(byte[] syncPacket) {
		this.syncPacket = syncPacket;
	}

	public long getCliTimeStart() {
		return cliTimeStart;
	}

	public void setCliTimeStart(long cliTimeStart) {
		this.cliTimeStart = cliTimeStart;
	}

	public byte[] getKeepAlivePacket() {
		return keepAlivePacket;
	}

	public void setKeepAlivePacket(byte[] keepAlivePacket) {
		this.keepAlivePacket = keepAlivePacket;
	}

}
