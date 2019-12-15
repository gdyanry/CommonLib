package yanry.lib.java.model.schedule;

import yanry.lib.java.model.log.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 本类适用的场景为：需要为不同的数据弹出不同的界面，同一时刻最多只显示一个界面，比如显示推送通知。
 * 当前有数据正在显示的情况下，新来的数据可以采取替换当前数据界面或进入等待队列等策略，而被替换的数据也可以相应采取接受或拒绝等策略。
 */
public class Scheduler {
    SchedulerManager manager;
    ShowData current;
    HashMap<Class<? extends Display>, Display> displays;

    Scheduler(SchedulerManager manager) {
        this.manager = manager;
        displays = new HashMap<>();
    }

    public void addLink(Scheduler... schedulers) {
        HashSet<Scheduler> linkedSchedulers = manager.conflictedSchedulers.get(this);
        for (Scheduler scheduler : schedulers) {
            if (scheduler.manager == manager) {
                linkedSchedulers.add(scheduler);
            } else {
                Logger.getDefault().ww("can't link schedulers from different managers.");
            }
        }
    }

    public void cancel(boolean dismissCurrent) {
        manager.runner.run(() -> {
            Iterator<ShowData> iterator = manager.queue.iterator();
            while (iterator.hasNext()) {
                ShowData next = iterator.next();
                if (next.scheduler == this) {
                    next.dispatchRelease(ShowData.DEQUEUE_CANCELLED);
                    iterator.remove();
                }
            }
            if (dismissCurrent) {
                HashSet<Display> displaysToDismisses = new HashSet<>();
                dismissCurrent(displaysToDismisses);
                manager.rebalance(null, displaysToDismisses);
            }
        });
    }

    public <T extends Display> T getDisplay(Class<T> displayType) {
        T display = (T) displays.get(displayType);
        if (display == null) {
            try {
                display = displayType.newInstance();
                display.setScheduler(this);
                displays.put(displayType, display);
            } catch (Exception e) {
                Logger.getDefault().catches(e);
            }
        }
        return display;
    }

    public <D extends ShowData> void show(D data, Class<? extends Display<D, ?>> displayType) {
        manager.runner.run(() -> {
            data.scheduler = this;
            data.display = getDisplay(displayType);
            // 根据request的需要清理队列
            Iterator<ShowData> it = manager.queue.iterator();
            while (it.hasNext()) {
                ShowData next = it.next();
                if (next.scheduler == this && next.priority <= data.priority && data.expelWaitingData(next) && !next.rejectExpelled()) {
                    next.dispatchRelease(ShowData.DEQUEUE_EXPELLED);
                    it.remove();
                }
            }
            // 处理当前正在显示的关联数据
            HashSet<ShowData> concernedShowingData = getConcernedShowingData();
            boolean showNow = true;
            for (ShowData showingData : concernedShowingData) {
                if (data.strategy != ShowData.STRATEGY_SHOW_IMMEDIATELY ||
                        data.priority < showingData.priority ||
                        data.priority == showingData.priority && showingData.rejectDismissed()) {
                    showNow = false;
                    break;
                }
            }
            if (showNow) {
                HashSet<Display> displaysToDismisses = new HashSet<>();
                for (ShowData showingData : concernedShowingData) {
                    manager.runner.cancelPendingTimeout(showingData);
                    showingData.scheduler.current = null;
                    // 结束当前正在显示的关联任务
                    showingData.dispatchRelease(ShowData.DISMISS_EXPELLED);
                    if (data.display != showingData.display) {
                        displaysToDismisses.add(showingData.display);
                    }
                }
                Logger.getDefault().vv("show directly: ", data);
                // 显示及取消显示使得调度器处于非稳态，需要重新平衡到次稳态
                manager.rebalance(data, displaysToDismisses);
            } else {
                switch (data.strategy) {
                    case ShowData.STRATEGY_SHOW_IMMEDIATELY:
                    case ShowData.STRATEGY_INSERT_HEAD:
                        Logger.getDefault().vv("insert head: ", data);
                        manager.queue.addFirst(data);
                        break;
                    case ShowData.STRATEGY_APPEND_TAIL:
                        Logger.getDefault().vv("append tail: ", data);
                        manager.queue.addLast(data);
                        break;
                }
            }
        });
    }

    HashSet<ShowData> getConcernedShowingData() {
        HashSet<ShowData> result = new HashSet<>();
        HashSet<Scheduler> schedulers = manager.conflictedSchedulers.get(this);
        for (Scheduler scheduler : schedulers) {
            if (scheduler.current != null) {
                result.add(scheduler.current);
            }
        }
        return result;
    }

    void dismissCurrent(HashSet<Display> displaysToDismisses) {
        if (current != null) {
            ShowData currentTask = this.current;
            current = null;
            manager.runner.cancelPendingTimeout(currentTask);
            currentTask.dispatchRelease(ShowData.DISMISS_CANCELLED);
            if (displaysToDismisses == null) {
                currentTask.display.internalDismiss();
            } else {
                displaysToDismisses.add(currentTask.display);
            }
        }
    }
}
