package no.cantara.service.loadtest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class FutureSelector<RESULT> {

    List<Future<RESULT>> futures = new ArrayList<>(1000);

    public void add(Future<RESULT> future) {
        synchronized (future) {
            futures.add(future);
        }
    }

    public Collection<Future<RESULT>> selectAllDone() {
        List<Future<RESULT>> list = new ArrayList<>();
        synchronized (futures) {
            Iterator<Future<RESULT>> i = futures.iterator();
            while (i.hasNext()) {
                Future<RESULT> future = i.next();
                if (future.isDone()) {
                    list.add(future);
                    i.remove();
                }
            }
        }
        return list;
    }

    public Collection<Future<RESULT>> selectAllNotDone() {
        List<Future<RESULT>> list = new ArrayList<>();
        synchronized (futures) {
            Iterator<Future<RESULT>> i = futures.iterator();
            while (i.hasNext()) {
                Future<RESULT> future = i.next();
                if (!future.isDone()) {
                    list.add(future);
                    i.remove();
                }
            }
        }
        return list;
    }
}
