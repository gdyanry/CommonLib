package yanry.lib.java.util;

public class GeometryUtil {

    /**
     * @param fromX
     * @param fromY
     * @param radian     弧度
     * @param distance
     * @param out        存放目标点x、y坐标的数组
     * @param startIndex out中存放结果的起始索引
     */
    public static void getPoint(int fromX, int fromY, double radian, double distance, int[] out, int startIndex) {
        out[startIndex] = (int) (fromX + distance * Math.cos(radian));
        out[startIndex + 1] = (int) (fromY + distance * Math.sin(radian));
    }

    /**
     * 求两点形成的向量在笛卡尔坐标系中的弧度值。
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return 返回-pi到pi之间的值。
     */
    public static double getRadian(int x1, int y1, int x2, int y2) {
        if (x1 == x2) {
            return y1 > y2 ? Math.PI / 2 : Math.PI * 3 / 2;
        }
        return Math.atan2(y2 - y1, x2 - x1);
    }

    /**
     * 求点到圆画切线生成的切点坐标。
     *
     * @param centerX    圆心x坐标值
     * @param centerY    圆心y坐标值
     * @param radius     圆半径
     * @param x
     * @param y
     * @param out        存放切点坐标的数组，按逆时针排序，0、1为第一个切点的坐标，2、3为第二个切点的坐标
     * @param startIndex out中存放结果的起始索引号
     * @return 返回false说明点不在圆外，此时out中没有结果。
     */
    public static boolean getPointOfContact(int centerX, int centerY, double radius, int x, int y, int[] out, int startIndex) {
        int dx = centerX - x;
        int dy = centerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance <= radius) {
            return false;
        }
        double radianOffset = getRadian(centerX, centerY, x, y);
        double halfRadian = Math.acos(1d * radius / distance);
        getPoint(centerX, centerY, halfRadian - radianOffset, radius, out, startIndex + 2);
        getPoint(centerX, centerY, halfRadian + radianOffset, radius, out, startIndex);
        return true;
    }

}
