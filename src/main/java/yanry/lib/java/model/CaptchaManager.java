package yanry.lib.java.model;

import yanry.lib.java.util.StringUtil;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is used to send captcha in server application.
 * 
 * @param <T>
 *            type of target of the captcha.
 * @author yanry
 * 
 *         2014年7月1日
 */
public abstract class CaptchaManager<T> {
	private HashMap<Object, String> map;
	private Timer timer;
	private HashMap<Object, TimerTask> tts;
	private Random r;
	private long timeout;
	private boolean overrideOnSameTargetThroughDifferentTypes;

	/**
	 * 
	 * @param timeoutMinutes
	 *            timeout of captchas in minute.
	 * @param timer
	 *            used to maintain timeout mechanism.
	 * @param overrideOnSameTargetThroughDifferentTypes
	 *            whether the captcha should override the existing one with
	 *            different type to the same target.
	 */
	public CaptchaManager(int timeoutMinutes, Timer timer, boolean overrideOnSameTargetThroughDifferentTypes) {
		map = new HashMap<Object, String>();
		this.timer = timer;
		tts = new HashMap<Object, TimerTask>();
		timeout = timeoutMinutes * 60000;
		this.overrideOnSameTargetThroughDifferentTypes = overrideOnSameTargetThroughDifferentTypes;
	}

	/**
	 * Generate a new captcha with given type and target. If you want to "send"
	 * a new captcha, use {@link #sendCaptcha(int, Object)} instead.
	 * 
	 * @param type
	 *            type of captcha.
	 * @param target
	 * @return the generated captcha.
	 */
	public String getCaptcha(int type, T target) {
		final Object key = getKey(type, target);
		TimerTask tt = tts.remove(key);
		if (tt != null) {
			tt.cancel();
		}
		if (r == null) {
			r = new Random();
		}
		String captcha = generateCaptcha(type, r);
		tt = new TimerTask() {
			@Override
			public void run() {
				map.remove(key);
				tts.remove(key);
			}
		};
		tts.put(key, tt);
		map.put(key, captcha);
		timer.schedule(tt, timeout);
		return captcha;
	}

	/**
	 * Send a new captcha to target.
	 * 
	 * @param type
	 *            type of captcha.
	 * @param target
	 * @return success or not.
	 */
	public boolean sendCaptcha(int type, T target) {
		return send(type, target, getSendText(type, getCaptcha(type, target)));
	}

	private Object getKey(int type, T target) {
		return overrideOnSameTargetThroughDifferentTypes ? target : new Key(type, target);
	}

	/**
	 * 
	 * @param type
	 *            type of captcha.
	 * @param target
	 * @param captcha
	 * @return whether the captcha is valid.
	 */
	public boolean verifyCaptcha(int type, T target, String captcha) {
		Object key = getKey(type, target);
		String c = map.get(key);
		if (c != null && c.equals(captcha)) {
			map.remove(key);
			return true;
		}
		return false;
	}

	/**
	 * Customize the rule of generating captcha. You can simply use
	 * {@link StringUtil#generateFixedLengthNumber(Random, int)} to get a number
	 * string.
	 * 
	 * @param type
	 *            captcha type.
	 * @param r
	 * @return
	 */
	protected abstract String generateCaptcha(int type, Random r);

	/**
	 * Generate the final text to send.
	 * 
	 * @param type
	 *            type of captcha.
	 * @param captcha
	 * @return
	 */
	protected abstract String getSendText(int type, String captcha);

	/**
	 * Send text to target.
	 * 
	 * @param type
	 *            type of captcha.
	 * @param target
	 * @param text
	 *            text to send.
	 * @return
	 */
	protected abstract boolean send(int type, T target, String text);

	private class Key {
		int type;
		T target;

		Key(int type, T target) {
			this.type = type;
			this.target = target;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + CaptchaManager.this.hashCode();
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			result = prime * result + type;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Key other = (Key) obj;
			if (!target.equals(other.target)) {
				return false;
			}
			return type == other.type;
		}
	}
}
