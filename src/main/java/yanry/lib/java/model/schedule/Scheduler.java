package yanry.lib.java.model.schedule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import yanry.lib.java.model.watch.BooleanHolder;
import yanry.lib.java.model.watch.BooleanReader;

/**
 * 本类适用的场景为：需要为不同的数据弹出不同的界面，同一时刻最多只显示一个界面，比如显示推送通知。
 * 当前有数据正在显示的情况下，新来的数据可以采取替换当前数据界面或进入等待队列等策略，而被替换的数据也可以相应采取接受或拒绝等策略。
 */
public class Scheduler {
    SchedulerManager manager;
    private Object tag;
    ShowData current;
    BooleanHolder visibility;
    private HashMap<Class<? extends Display>, Display> displays;

    Scheduler(SchedulerManager manager, Object tag) {
        this.manager = manager;
        this.tag = tag;
        visibility = new BooleanHolder();
        displays = new HashMap<>();
    }

    public BooleanReader getVisibility() {
        return visibility;
    }

    public void addLink(Scheduler... schedulers) {
        HashSet<Scheduler> linkedSchedulers = manager.conflictedSchedulers.get(this);
        for (Scheduler scheduler : schedulers) {
            if (scheduler.manager == manager) {
                linkedSchedulers.add(scheduler);
            } else if (manager.logger != null) {
                manager.logger.ee("can't link scheduler ", scheduler.tag, " to ", tag);
            }
        }
    }

    public void cancel(boolean dismissCurrent) {
        new ScheduleRunnable(manager) {
            @Override
            protected void doRun() {
                Iterator<ShowData> iterator = manager.queue.iterator();
                while (iterator.hasNext()) {
                    ShowData next = iterator.next();
                    if (next.scheduler == Scheduler.this) {
                        if (manager.logger != null) {
                            manager.logger.vv("dequeue by scheduler cancel: ", next);
                        }
                        next.state.setValue(ShowData.STATE_DEQUEUE);
                        iterator.remove();
                    }
                }
                if (dismissCurrent) {
                    HashSet<Display> displaysToDismisses = new HashSet<>();
                    dismissCurrent(displaysToDismisses);
                    manager.rebalance(null, displaysToDismisses);
                }
            }
        }.start(tag, " cancel: ", dismissCurrent);
    }

    public ShowData getShowingData() {
        return current;
    }

    public <T extends Display> T getDisplay(Class<T> displayType) {
        T display = (T) displays.get(displayType);
        if (display == null) {
            try {
                display = displayType.getDeclaredConstructor().newInstance();
                display.setScheduler(this);
                displays.put(displayType, display);
            } catch (Exception e) {
                if (manager.logger != null) {
                    manager.logger.catches(e);
                } else {
                    e.printStackTrace();
                }
            }
        }
        return display;
    }

    public <T extends Display> void setDisplay(Class<T> displayType, T display) {
        display.setScheduler(this);
        displays.put(displayType, display);
    }

    public <D extends ShowData> void show(D data, Class<? extends Display<D>> displayType) {
        new ScheduleRunnable(manager) {
            @Override
            protected void doRun() {
                if (data.state.getValue() == ShowData.STATE_SHOWING || data.state.getValue() == ShowData.STATE_ENQUEUE) {
                    if (manager.logger != null) {
                        manager.logger.ww("deny showing for invalid state: ", data, ' ', data.state);
                    }
                    return;
                }
                data.scheduler = Scheduler.this;
                data.display = getDisplay(displayType);
                // 根据request的需要清理队列
                Iterator<ShowData> it = manager.queue.iterator();
                while (it.hasNext()) {
                    ShowData next = it.next();
                    if (next.scheduler == Scheduler.this && next.priority <= data.priority && data.expelWaitingData(next) && !next.hasFlag(ShowData.FLAG_REJECT_EXPELLED)) {
                        if (manager.logger != null) {
                            manager.logger.vv("dequeue by expelled: ", next);
                        }
                        next.state.setValue(ShowData.STATE_DEQUEUE);
                        it.remove();
                    }
                }
                // 处理当前正在显示的关联数据
                HashSet<ShowData> concernedShowingData = getConcernedShowingData();
                boolean showNow = true;
                for (ShowData showingData : concernedShowingData) {
                    if (data.strategy != ShowData.STRATEGY_SHOW_IMMEDIATELY ||
                            data.priority < showingData.priority ||
                            data.priority == showingData.priority && showingData.hasFlag(ShowData.FLAG_REJECT_DISMISSED)) {
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
                        if (manager.logger != null) {
                            manager.logger.vv("dismiss by expelled: ", showingData);
                        }
                        showingData.state.setValue(ShowData.STATE_DISMISS);
                        if (data.display != showingData.display) {
                            displaysToDismisses.add(showingData.display);
                        }
                    }
                    if (manager.logger != null) {
                        manager.logger.vv("show directly: ", data);
                    }
                    // 显示及取消显示使得调度器处于非稳态，需要重新平衡到次稳态
                    manager.rebalance(data, displaysToDismisses);
                } else {
                    switch (data.strategy) {
                        case ShowData.STRATEGY_SHOW_IMMEDIATELY:
                        case ShowData.STRATEGY_INSERT_HEAD:
                            if (manager.logger != null) {
                                manager.logger.vv("insert head: ", data);
                            }
                            manager.queue.addFirst(data);
                            data.state.setValue(ShowData.STATE_ENQUEUE);
                            break;
                        case ShowData.STRATEGY_APPEND_TAIL:
                            if (manager.logger != null) {
                                manager.logger.vv("append tail: ", data);
                            }
                            manager.queue.addLast(data);
                            data.state.setValue(ShowData.STATE_ENQUEUE);
                            break;
                    }
                }
            }
        }.start(tag, " show: ", data);
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
            ShowData currentData = this.current;
            current = null;
            manager.runner.cancelPendingTimeout(currentData);
            if (manager.logger != null) {
                manager.logger.vv("dismiss by cancel: ", currentData);
            }
            currentData.state.setValue(ShowData.STATE_DISMISS);
            if (displaysToDismisses == null) {
                currentData.display.internalDismiss();
            } else {
                displaysToDismisses.add(currentData.display);
            }
        }
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}
