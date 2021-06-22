package yanry.lib.java.model.schedule;

import yanry.lib.java.model.watch.BooleanHolder;
import yanry.lib.java.model.watch.BooleanHolderImpl;
import yanry.lib.java.model.watch.ValueHolder;
import yanry.lib.java.model.watch.ValueHolderImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 本类适用的场景为：需要为不同的数据弹出不同的界面，同一时刻最多只显示一个界面，比如显示推送通知。
 * 当前有数据正在显示的情况下，新来的数据可以采取替换当前数据界面或进入等待队列等策略，而被替换的数据也可以相应采取接受或拒绝等策略。
 */
public class Scheduler {
    SchedulerManager manager;
    private Object tag;
    ShowingDataHolder showingData = new ShowingDataHolder();
    BooleanHolderImpl visibility = new BooleanHolderImpl();
    private HashMap<Class<? extends Display>, Display> displays = new HashMap<>();

    Scheduler(SchedulerManager manager, Object tag) {
        this.manager = manager;
        this.tag = tag;
    }

    public BooleanHolder getVisibility() {
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
                        next.setState(ShowData.STATE_DEQUEUE);
                        iterator.remove();
                    }
                }
                if (dismissCurrent) {
                    HashSet<Display> displaysToDismisses = new HashSet<>();
                    dismissCurrent(displaysToDismisses);
                    manager.rebalance(null, displaysToDismisses);
                }
            }
        }.start('[', tag, "] cancel: ", dismissCurrent);
    }

    public ValueHolder<ShowData> getShowingData() {
        return showingData;
    }

    public void addDisplay(Display<?> display) {
        display.setScheduler(this);
        displays.put(display.getClass(), display);
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

    public <T extends Display> T removeDisplay(Class<T> displayType) {
        return (T) displays.remove(displayType);
    }

    public Collection<Display> getDisplays() {
        return displays.values();
    }

    public <D extends ShowData> void show(D data, Class<? extends Display<? extends D>> displayType) {
        new ScheduleRunnable(manager) {
            @Override
            protected void doRun() {
                if (data.getState().getValue() == ShowData.STATE_SHOWING) {
                    if (manager.logger != null) {
                        manager.logger.ww("data is already showing: ", data);
                    }
                    return;
                }
                if (data.getState().getValue() == ShowData.STATE_ENQUEUE && manager.queue.remove(data) && manager.logger != null) {
                    manager.logger.dd("remove data from queue to schedule show: ", data);
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
                        next.setState(ShowData.STATE_DEQUEUE);
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
                        manager.runner.cancel(showingData);
                        showingData.scheduler.showingData.setValue(null);
                        // 结束当前正在显示的关联任务
                        if (manager.logger != null) {
                            manager.logger.vv("dismiss by expelled: ", showingData);
                        }
                        showingData.setState(ShowData.STATE_DISMISS);
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
                            data.setState(ShowData.STATE_ENQUEUE);
                            break;
                        case ShowData.STRATEGY_APPEND_TAIL:
                            if (manager.logger != null) {
                                manager.logger.vv("append tail: ", data);
                            }
                            manager.queue.addLast(data);
                            data.setState(ShowData.STATE_ENQUEUE);
                            break;
                    }
                }
            }
        }.start('[', tag, "] show: ", data);
    }

    HashSet<ShowData> getConcernedShowingData() {
        HashSet<ShowData> result = new HashSet<>();
        HashSet<Scheduler> schedulers = manager.conflictedSchedulers.get(this);
        if (schedulers != null) {
            for (Scheduler scheduler : schedulers) {
                ShowData showData = scheduler.showingData.getValue();
                if (showData != null) {
                    result.add(showData);
                }
            }
        }
        return result;
    }

    void dismissCurrent(HashSet<Display> displaysToDismisses) {
        ShowData currentData = this.showingData.setValue(null);
        if (currentData != null) {
            manager.runner.cancel(currentData);
            if (manager.logger != null) {
                manager.logger.vv("dismiss by cancel: ", currentData);
            }
            currentData.setState(ShowData.STATE_DISMISS);
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

    class ShowingDataHolder extends ValueHolderImpl<ShowData> {
        boolean isBoundTo(Display<?> display) {
            ShowData showingData = getValue();
            return showingData != null && showingData.display == display;
        }
    }
}
