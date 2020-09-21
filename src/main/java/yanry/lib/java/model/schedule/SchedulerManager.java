package yanry.lib.java.model.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import yanry.lib.java.interfaces.Filter;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.runner.Runner;

public class SchedulerManager {
    Runner runner;
    Logger logger;
    AtomicBoolean isRunning;
    ConcurrentLinkedQueue<ScheduleRunnable> pendingRunnable;
    LinkedList<ShowData> queue;
    HashMap<Scheduler, HashSet<Scheduler>> conflictedSchedulers;
    HashMap<Object, Scheduler> schedulers;

    public SchedulerManager(Runner runner, Logger logger) {
        this.runner = runner;
        this.logger = logger;
        isRunning = new AtomicBoolean();
        pendingRunnable = new ConcurrentLinkedQueue<>();
        queue = new LinkedList<>();
        conflictedSchedulers = new HashMap<>();
        schedulers = new HashMap<>();
    }

    public Scheduler get(Object tag) {
        Scheduler scheduler = schedulers.get(tag);
        if (scheduler == null) {
            scheduler = new Scheduler(this, tag);
            schedulers.put(tag, scheduler);
            HashSet<Scheduler> set = new HashSet<>();
            set.add(scheduler);
            conflictedSchedulers.put(scheduler, set);
        }
        return scheduler;
    }

    public Scheduler peek(Object tag) {
        return schedulers.get(tag);
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
                    if (logger != null) {
                        logger.vv("dequeue by all cancel: ", data);
                    }
                    data.setState(ShowData.STATE_DEQUEUE);
                }
                queue.clear();
                if (dismissCurrent && schedulers.size() > 0) {
                    ArrayList<Scheduler> schedulers = new ArrayList<>(SchedulerManager.this.schedulers.values());
                    for (Scheduler scheduler : schedulers) {
                        scheduler.dismissCurrent(null);
                    }
                }
            }
        }.start(this, " cancel all: ", dismissCurrent);
    }

    public void cancelScheduler(boolean dismissCurrent, Filter<Scheduler> schedulerFilter) {
        ArrayList<Scheduler> schedulers = new ArrayList<>(this.schedulers.values());
        for (Scheduler scheduler : schedulers) {
            if (schedulerFilter.accept(scheduler)) {
                scheduler.cancel(dismissCurrent);
            }
        }
    }

    public boolean hasScheduler(Object tag) {
        return schedulers.get(tag) != null;
    }

    public void cancelByTag(Object tag) {
        new ScheduleRunnable(this) {
            @Override
            protected void doRun() {
                // 清理队列
                Iterator<ShowData> it = queue.iterator();
                while (it.hasNext()) {
                    ShowData data = it.next();
                    if (Objects.equals(data.tag, tag)) {
                        if (logger != null) {
                            logger.vv("dequeue by tag cancel: ", data);
                        }
                        data.setState(ShowData.STATE_DEQUEUE);
                        it.remove();
                    }
                }
                // 清理当前显示的窗口
                HashSet<Display> displaysToDismisses = new HashSet<>();
                if (schedulers.size() > 0) {
                    ArrayList<Scheduler> schedulers = new ArrayList<>(SchedulerManager.this.schedulers.values());
                    for (Scheduler scheduler : schedulers) {
                        ShowData showData = scheduler.showingData.getValue();
                        if (showData != null && Objects.equals(showData.tag, tag)) {
                            scheduler.dismissCurrent(displaysToDismisses);
                        }
                    }
                    rebalance(null, displaysToDismisses);
                }
            }
        }.start(this, " cancel by tag: ", tag);
    }

    void rebalance(ShowData showData, HashSet<Display> displaysToDismisses) {
        HashSet<ShowData> dataToShow = new HashSet<>();
        if (showData != null) {
            // 此处赋值是为了后面getConcernedShowingTasks()能得到正确的结果
            showData.scheduler.showingData.setValue(showData);
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
                    data.scheduler.showingData.setValue(data);
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
                            showingData.scheduler.showingData.setValue(null);
                            data.scheduler.showingData.setValue(data);
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
                if (logger != null) {
                    logger.vv("dequeue by invalid: ", next);
                }
                next.setState(ShowData.STATE_DEQUEUE);
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
            if (data != showData && logger != null) {
                logger.vv("loop and show: ", data);
            }
            data.display.show(data);
            data.setState(ShowData.STATE_SHOWING);
            if (data.duration > 0) {
                runner.schedule(data, data.duration);
            }
        }
    }
}
