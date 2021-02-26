package yanry.lib.java.interfaces;

/**
 * 可根据顺序值大小进行比较从而方便排序的接口
 */
public interface SimpleComparable extends Comparable<SimpleComparable> {
    /**
     * 获取当前元素的顺序值，按从小到大排序。
     *
     * @return
     */
    int getCompareValue();

    @Override
    default int compareTo(SimpleComparable o) {
        return getCompareValue() - o.getCompareValue();
    }
}
