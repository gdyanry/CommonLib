/**
 * 
 */
package lib.common.entity;

import lib.common.util.ConsoleUtil;

/**
 * @author yanry
 *
 * 2015年9月20日
 */
public class SimpleInfoHandler implements InfoHandler {
	private int level;

	@Override
	public void handleException(Exception e) {
		if (level <= LEVEL_EXCEPTION) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleThrowable(Throwable e) {
		if (level <= LEVEL_EXCEPTION) {
			e.printStackTrace();
		}
	}

	@Override
	public void showError(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMessage(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debug(Class<?> tag, String msg) {
		if (level <= LEVEL_DEBUG) {
			ConsoleUtil.debug(tag, msg);
		}
	}

	@Override
	public void error(Class<?> tag, String msg) {
		if (level <= LEVEL_ERROR) {
			ConsoleUtil.error(tag, msg);
		}
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

}
