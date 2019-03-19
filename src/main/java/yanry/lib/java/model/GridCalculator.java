package yanry.lib.java.model;

/**
 * @author yanry
 *
 * 2016年4月3日
 */
public abstract class GridCalculator {

	private int availableWidth;
	private int columnNum;
	private int itemWidth;
	private ItemWidthType type;
	private int itemSpace;
	
	public GridCalculator availableWidth(int val) {
		this.availableWidth = val;
		return this;
	}
	
	public GridCalculator columnNumber(int val) {
		this.columnNum = val;
		return this;
	}
	
	public GridCalculator itemWidth(int val) {
		this.itemWidth = val;
		return this;
	}
	
	public GridCalculator itemWidthType(ItemWidthType val) {
		this.type = val;
		return this;
	}
	
	public GridCalculator itemSpace(int val) {
		this.itemSpace = val;
		return this;
	}
	
	public void calculate() {
		if (availableWidth <= 0) {
			throw new IllegalArgumentException("available width is invalid.");
		}
		// column number is unspecified
		if (columnNum <= 0) {
			if (type == ItemWidthType.Suggest) {
				columnNum = (availableWidth + itemSpace) / (itemWidth + itemSpace);
				if ((availableWidth - (columnNum - 1) * itemSpace)
						* (availableWidth - columnNum * itemSpace) > itemWidth * itemWidth * columnNum
								* (columnNum + 1)) {
					columnNum++;
				}
			} else {
				columnNum = (availableWidth + itemSpace) / (itemWidth + itemSpace);
			}
			if (columnNum == 0) {
				columnNum = 1;
			}
			// column number is confirmed now
			onColumnNumberCalculated(columnNum);
		}
		// if item width is unfixed, calculate it by column number
		if (type != ItemWidthType.Fixed) {
			itemWidth = (availableWidth - (columnNum - 1) * itemSpace) / columnNum;
			onItemWidthCalculated(itemWidth);
		}
	}
	
	public int getItemWidth() {
		return itemWidth;
	}
	
	public int getColumnNumber() {
		return columnNum;
	}
	
	protected abstract void onColumnNumberCalculated(int val);
	
	protected abstract void onItemWidthCalculated(int val);
	
	public enum ItemWidthType {
		Fixed, Suggest, Min
	}
}
