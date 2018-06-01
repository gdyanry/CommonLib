/**
 * 
 */
package lib.common.entity;

import lib.common.interfaces.InfoHandler;
import lib.common.util.ConsoleUtil;

/**
 * @author yanry
 *
 * 2015年9月20日
 */
public class SimpleInfoHandler implements InfoHandler {
	private int level;

	@Override
	public void handleException(Throwable e) {
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
	public void debug(String msg) {
		if (level <= LEVEL_DEBUG) {
			ConsoleUtil.debug(getClass(), msg);
		}
	}

	@Override
	public void error(String msg) {
		if (level <= LEVEL_ERROR) {
			ConsoleUtil.error(getClass(), msg);
		}
	}

	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}
}
