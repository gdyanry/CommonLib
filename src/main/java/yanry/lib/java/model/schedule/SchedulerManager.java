package yanry.lib.java.model.schedule;

import yanry.lib.java.model.log.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class SchedulerManager {
    ScheduleRunner runner;
    boolean isRunning;
    LinkedList<ShowData> queue;
    HashMap<Scheduler, HashSet<Scheduler>> conflictedSchedulers;
    HashMap<Object, Scheduler> instances;

    public SchedulerManager(ScheduleRunner runner) {
        this.runner = runner;
        queue = new LinkedList<>();
        conflictedSchedulers = new HashMap<>();
        instances = new HashMap<>();
    }

    public Scheduler get(Object tag) {
        Scheduler scheduler = instances.get(tag);
        if (scheduler == null) {
            scheduler = new Scheduler(this);
            instances.put(tag, scheduler);
            HashSet<Scheduler> set = new HashSet<>();
            set.add(scheduler);
            conflictedSchedulers.put(scheduler, set);
        }
        return scheduler;
    }

    public void link(Scheduler a, Scheduler b) {
        a.addLink(b);
        b.addLink(a);
    }

    /**
     * 撤消显示所有的数据。
     *
     * @param dismissCurrent 是否关闭当前正在显示的界面。
     */
    public void cancelAll(boolean dismissCurrent) {
        new ScheduleRunnable(this) {
            @Override
            protected void doRun() {
                for (ShowData data : queue) {
                    Logger.getDefault().vv("dequeue by all cancel: ", data);
                    data.dispatchState(ShowData.STATE_DEQUEUE);
                }
                queue.clear();
                if (dismissCurrent) {
                    for (Scheduler scheduler : instances.values()) {
                        scheduler.dismissCurrent(null);
                    }
                }
            }
        }.start();
    }

    public boolean hasScheduler(Object tag) {
        return instances.get(tag) != null;
    }

    public void cancelByTag(Object tag) {
        new ScheduleRunnable(this) {
            @Override
            protected void doRun() {
                // 清理队列
                Iterator<ShowData> it = queue.iterator();
                while (it.hasNext()) {
                    ShowData data = it.next();
                    if (data.tag == tag) {
                        Logger.getDefault().vv("dequeue by tag cancel: ", data);
                        data.dispatchState(ShowData.STATE_DEQUEUE);
                        it.remove();
                    }
                }
                // 清理当前显示的窗口
                HashSet<Display> displaysToDismisses = new HashSet<>();
                for (Scheduler scheduler : instances.values()) {
                    if (scheduler.current.tag == tag) {
                        scheduler.dismissCurrent(displaysToDismisses);
                    }
                }
                rebalance(null, displaysToDismisses);
            }
        }.start();
    }


    void rebalance(ShowData showData, HashSet<Display> displaysToDismisses) {
        HashSet<ShowData> dataToShow = new HashSet<>();
        if (showData != null) {
            // 此处调用是为了后面getConcernedShowingTasks()能得到正确的结果
            showData.scheduler.current = showData;
            dataToShow.add(showData);
        }
        HashSet<ShowData> invalidData = new HashSet<>();
        // 先收集需要显示的数据
        for (ShowData data : queue) {
            HashSet<ShowData> concernedShowingData = data.scheduler.getConcernedShowingData();
            if (concernedShowingData.size() == 0) {
                if (data.hasFlag(ShowData.FLAG_INVALID_ON_DELAYED_SHOW)) {
                    invalidData.add(data);
                } else {
                    data.scheduler.current = data;
                    dataToShow.add(data);
                }
            } else {
                for (ShowData showingData : concernedShowingData) {
                    if (dataToShow.contains(showingData) && data.priority > showingData.priority) {
                        if (data.hasFlag(ShowData.FLAG_INVALID_ON_DELAYED_SHOW)) {
                            invalidData.add(data);
                        } else {
                            // 替换优先级较低的待显示数据
                            dataToShow.remove(showingData);
                            showingData.scheduler.current = null;
                            data.scheduler.current = data;
                            dataToShow.add(data);
                        }
                    }
                }
            }
        }
        Iterator<ShowData> iterator = queue.iterator();
        while (iterator.hasNext()) {
            ShowData next = iterator.next();
            if (invalidData.contains(next)) {
                // 从队列中清除无效的数据
                Logger.getDefault().vv("dequeue by invalid: ", next);
                next.dispatchState(ShowData.STATE_DEQUEUE);
                iterator.remove();
            } else if (dataToShow.contains(next)) {
                // 从队列中清除即将显示的数据
                iterator.remove();
                // 将要显示的数据对应的display不需要关闭
                if (displaysToDismisses != null) {
                    displaysToDismisses.remove(next.display);
                }
            }
        }
        // 先关闭旧的再显示新的
        if (displaysToDismisses != null) {
            for (Display display : displaysToDismisses) {
                display.internalDismiss();
            }
        }
        for (ShowData data : dataToShow) {
            if (data != showData) {
                Logger.getDefault().vv("loop and show: ", data);
            }
            data.display.show(data);
            data.dispatchState(ShowData.STATE_SHOWING);
            runner.cancelPendingTimeout(data);
            if (data.duration > 0) {
                runner.scheduleTimeout(data, data.duration);
            }
        }
    }
}
