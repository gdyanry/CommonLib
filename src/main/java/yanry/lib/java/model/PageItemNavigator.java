package yanry.lib.java.model;

/**
 * rongyu.yan
 * 2019/5/10
 **/
public abstract class PageItemNavigator {
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_RIGHT = 4;

    private int pageColumn;
    private int pageRow;
    private int pageSize;
    private int itemCount;
    private int globalIndex;

    public PageItemNavigator(int pageColumn, int pageRow) {
        this.pageColumn = pageColumn;
        this.pageRow = pageRow;
        pageSize = pageColumn * pageRow;
    }

    public void setGlobalIndex(int globalIndex) {
        updateGlobalIndex(globalIndex);
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
        if (globalIndex >= itemCount) {
            globalIndex = itemCount - 1;
        }
    }

    public boolean move(int direction) {
        switch (direction) {
            case DIRECTION_UP:
                return updateGlobalIndex(globalIndex - pageColumn);
            case DIRECTION_DOWN:
                if (updateGlobalIndex(globalIndex + pageColumn)) {
                    return true;
                }
                int newGlobalIndex = itemCount - 1;
                if (newGlobalIndex % pageSize % pageColumn < globalIndex % pageSize % pageColumn) {
                    int pageIndex = globalIndex / pageSize;
                    globalIndex = newGlobalIndex;
                    int newPageIndex = globalIndex / pageSize;
                    if (newPageIndex != pageIndex) {
                        onPageChange(pageIndex, newPageIndex);
                    }
                    return true;
                }
                return false;
            case DIRECTION_LEFT:
                return updateGlobalIndex(globalIndex - 1);
            case DIRECTION_RIGHT:
                return updateGlobalIndex(globalIndex + 1);
        }
        return false;
    }

    private boolean updateGlobalIndex(int newGlobalIndex) {
        if (newGlobalIndex >= 0 && newGlobalIndex < itemCount) {
            if (newGlobalIndex != globalIndex) {
                int oldGlobalIndex = globalIndex;
                int oldPageIndex = globalIndex / pageSize;
                globalIndex = newGlobalIndex;
                int newPageIndex = globalIndex / pageSize;
                if (oldPageIndex != newPageIndex) {
                    onPageChange(oldPageIndex, newPageIndex);
                }
                onGlobalIndexChange(oldGlobalIndex, newGlobalIndex);
                return true;
            }
        }
        return false;
    }

    public boolean setPageIndex(int index) {
        return updateGlobalIndex(index * pageSize);
    }

    public boolean setIndexInPage(int index) {
        return updateGlobalIndex(pageSize * getPageIndex() + index);
    }

    public boolean setCoordinateInPage(int rowIndex, int columnIndex) {
        return updateGlobalIndex(pageSize * getPageIndex() + pageColumn * rowIndex + columnIndex);
    }

    public boolean increasePageIndex(int increment) {
        if (updateGlobalIndex(globalIndex + pageSize * increment)) {
            return true;
        }
        return setPageIndex(getPageIndex() + increment);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageColumn() {
        return pageColumn;
    }

    public int getPageRow() {
        return pageRow;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getIndexInPage() {
        return globalIndex % pageSize;
    }

    public int getColumnIndexInPage() {
        return globalIndex % pageColumn;
    }

    public int getRowIndexInPage() {
        return globalIndex % pageSize / pageColumn;
    }

    public int getPageIndex() {
        return globalIndex / pageSize;
    }

    public int getPageCount() {
        return itemCount / pageSize + (itemCount % pageSize > 0 ? 1 : 0);
    }

    public int getPageIndexForItem(int itemIndex) {
        return itemIndex / pageSize;
    }

    protected abstract void onPageChange(int oldIndex, int newIndex);

    protected abstract void onGlobalIndexChange(int oldIndex, int newIndex);
}
