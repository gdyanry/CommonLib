package lib.common.model.revert;

import lib.common.model.log.Logger;
import lib.common.util.object.ObjectUtil;

import java.util.HashMap;
import java.util.Objects;

public class RevertibleMap<K, V> {
    private RevertManager manager;
    private HashMap<K, V> map;
    private boolean dirty;
    private String snapShoot;

    public RevertibleMap(RevertManager manager) {
        this.manager = manager;
        map = new HashMap<>();
        dirty = true;
    }

    public boolean put(K key, V value) {
        if (map.containsKey(key)) {
            V oldValue = map.put(key, value);
            if (!Objects.equals(value, oldValue)) {
                manager.proceed(new Revertible() {
                    @Override
                    public void proceed() {
                    }

                    @Override
                    public void recover() {
                        map.put(key, oldValue);
                    }
                });
                dirty = true;
                return true;
            }
            return false;
        } else {
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    map.put(key, value);
                }

                @Override
                public void recover() {
                    map.remove(key);
                }
            });
            dirty = true;
            return true;
        }
    }

    public boolean remove(K key) {
        if (map.containsKey(key)) {
            V removedValue = map.remove(key);
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                }

                @Override
                public void recover() {
                    map.put(key, removedValue);
                }
            });
            dirty = true;
            return true;
        }
        return false;
    }

    public void clear() {
        if (map.size() > 0) {
            HashMap<K, V> copy = new HashMap<>(map);
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    map.clear();
                }

                @Override
                public void recover() {
                    map.putAll(copy);
                }
            });
            dirty = true;
        }
    }

    public String getSnapShootMD5() {
        if (dirty) {
            try {
                snapShoot = ObjectUtil.getSnapshotMd5(map);
            } catch (Exception e) {
                Logger.getDefault().catches(e);
            }
            dirty = false;
        }
        return snapShoot;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public V get(K key) {
        return map.get(key);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
