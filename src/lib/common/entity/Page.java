/**
 * 
 */
package lib.common.entity;


/**
 * Applicable for paging display, typically used to query database, take mysql for example: select ... from ... limit [item_offset], [page_size].
 * @author yanry
 *
 * 2014年12月25日 下午3:55:38
 */
public class Page {
	private int pageSize;
	private int currentPage;
	private int pageCount;
	private int itemCount;
	
	/**
	 * 
	 * @param currentPage current page number, begin with 1.
	 * @param pageSize max number of item shown in one page.
	 */
	public Page(int currentPage, int pageSize) {
		this.currentPage = currentPage > 0 ? currentPage : 1;
		this.pageSize = pageSize;
	}
	
	/**
	 * Set this to calculate page count.
	 * @param itemCount total number of item.
	 */
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
		pageCount = itemCount / pageSize + (itemCount % pageSize > 0 ? 1 : 0);
		if (pageCount > 0 && currentPage > pageCount) {
			currentPage = pageCount;
		}
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * 
	 * @return total page number.
	 */
	public int getPageCount() {
		return pageCount;
	}

	public int getItemCount() {
		return itemCount;
	}

	/**
	 * 
	 * @return item offset, used to query database.
	 */
	public int getItemOffset() {
		return (currentPage - 1) * pageSize;
	}
}
