/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.register.common;

import com.webank.ai.fate.register.interfaces.NotifyListener;
import com.webank.ai.fate.register.task.*;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.webank.ai.fate.register.common.Constants.*;


public abstract class FailbackRegistry extends AbstractRegistry {

    private static final Logger logger = LogManager.getLogger(FailbackRegistry.class);


    private final ConcurrentMap<URL, FailedRegisteredTask> failedRegistered = new ConcurrentHashMap<URL, FailedRegisteredTask>();

    private final ConcurrentMap<URL, FailedUnregisteredTask> failedUnregistered = new ConcurrentHashMap<URL, FailedUnregisteredTask>();

    private final ConcurrentMap<Holder, FailedSubscribedTask> failedSubscribed = new ConcurrentHashMap<Holder, FailedSubscribedTask>();

    private final ConcurrentMap<Holder, FailedUnsubscribedTask> failedUnsubscribed = new ConcurrentHashMap<Holder, FailedUnsubscribedTask>();

    private final ConcurrentMap<Holder, FailedNotifiedTask> failedNotified = new ConcurrentHashMap<Holder, FailedNotifiedTask>();

    private final ConcurrentMap<String, FailedSubProjectTask> failedSubProject = new ConcurrentHashMap<>();


    private final int retryPeriod;

    private final HashedWheelTimer retryTimer;


    public FailbackRegistry(URL url) {
        super(url);
        this.retryPeriod = url.getParameter(REGISTRY_RETRY_PERIOD_KEY, 5000);

        // since the retry task will not be very much. 128 ticks is enough.
        retryTimer = new HashedWheelTimer(new NamedThreadFactory(REFIX_FATE_REGISTRY_RETRY_TIMER, true), retryPeriod, TimeUnit.MILLISECONDS, 128);
    }

    @Override
    public void subProject(String project) {

        logger.info("try to subProject: {}", project);
        super.subProject(project);
        failedSubProject.remove(project);
        try {
            doSubProject(project);
        } catch (Exception e) {
            addFailedSubscribedProjectTask(project);
        }
    }

    public void removeFailedRegisteredTask(URL url) {
        failedRegistered.remove(url);
    }

    public void removeFailedUnregisteredTask(URL url) {
        failedUnregistered.remove(url);
    }

    public void removeFailedSubscribedProjectTask(String project) {

        failedSubProject.remove(project);
    }


    public void removeFailedSubscribedTask(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        failedSubscribed.remove(h);
    }

    public void removeFailedUnsubscribedTask(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        failedUnsubscribed.remove(h);
    }

    public void removeFailedNotifiedTask(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        failedNotified.remove(h);
    }

    public void addFailedSubscribedProjectTask(String project) {

        logger.info("try to add failed subscribed project {}",project);

        FailedSubProjectTask oldOne = failedSubProject.get(project);
        if (oldOne != null) {
            return;
        }
        URL url = new URL();

        URL newUrl = url.setProject(project);

        FailedSubProjectTask newTask = new FailedSubProjectTask(newUrl, this);
        oldOne = failedSubProject.putIfAbsent(project, newTask);
        if (oldOne == null) {
            // never has a retry task. then start a new task for retry.
            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }


    private void addFailedRegistered(URL url) {
        FailedRegisteredTask oldOne = failedRegistered.get(url);
        if (oldOne != null) {
            return;
        }
        FailedRegisteredTask newTask = new FailedRegisteredTask(url, this);
        oldOne = failedRegistered.putIfAbsent(url, newTask);
        if (oldOne == null) {
            // never has a retry task. then start a new task for retry.
            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }

//    private void addFailedSubProject(String url) {
//        FailedSubProjectTask oldOne = failedSubProject.get(url);
//        if (oldOne != null) {
//            return;
//        }
//        FailedSubProjectTask newTask = new FailedSubProjectTask(url, this);
//        oldOne = failedRegistered.putIfAbsent(url, newTask);
//        if (oldOne == null) {
//            // never has a retry task. then start a new task for retry.
//            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
//        }
//    }


    private void removeFailedRegistered(URL url) {
        FailedRegisteredTask f = failedRegistered.remove(url);
        if (f != null) {
            f.cancel();
        }
    }

    private void addFailedUnregistered(URL url) {
        FailedUnregisteredTask oldOne = failedUnregistered.get(url);
        if (oldOne != null) {
            return;
        }
        FailedUnregisteredTask newTask = new FailedUnregisteredTask(url, this);
        oldOne = failedUnregistered.putIfAbsent(url, newTask);
        if (oldOne == null) {
            // never has a retry task. then start a new task for retry.
            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }

    private void removeFailedUnregistered(URL url) {
        FailedUnregisteredTask f = failedUnregistered.remove(url);
        if (f != null) {
            f.cancel();
        }
    }

    private void addFailedSubscribed(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        FailedSubscribedTask oldOne = failedSubscribed.get(h);
        if (oldOne != null) {
            return;
        }
        FailedSubscribedTask newTask = new FailedSubscribedTask(url, this, listener);
        oldOne = failedSubscribed.putIfAbsent(h, newTask);
        if (oldOne == null) {
            // never has a retry task. then start a new task for retry.
            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }

    private void removeFailedSubscribed(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        FailedSubscribedTask f = failedSubscribed.remove(h);
        if (f != null) {
            f.cancel();
        }
        removeFailedUnsubscribed(url, listener);
        removeFailedNotified(url, listener);
    }

    private void addFailedUnsubscribed(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        FailedUnsubscribedTask oldOne = failedUnsubscribed.get(h);
        if (oldOne != null) {
            return;
        }
        FailedUnsubscribedTask newTask = new FailedUnsubscribedTask(url, this, listener);
        oldOne = failedUnsubscribed.putIfAbsent(h, newTask);
        if (oldOne == null) {
            // never has a retry task. then start a new task for retry.
            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }

    private void removeFailedUnsubscribed(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        FailedUnsubscribedTask f = failedUnsubscribed.remove(h);
        if (f != null) {
            f.cancel();
        }
    }

    private void addFailedNotified(URL url, NotifyListener listener, List<URL> urls) {
        Holder h = new Holder(url, listener);
        FailedNotifiedTask newTask = new FailedNotifiedTask(url, listener);
        FailedNotifiedTask f = failedNotified.putIfAbsent(h, newTask);
        if (f == null) {
            // never has a retry task. then start a new task for retry.
            newTask.addUrlToRetry(urls);
            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        } else {
            // just add urls which needs retry.
            newTask.addUrlToRetry(urls);
        }
    }

    private void removeFailedNotified(URL url, NotifyListener listener) {
        Holder h = new Holder(url, listener);
        FailedNotifiedTask f = failedNotified.remove(h);
        if (f != null) {
            f.cancel();
        }
    }

    ConcurrentMap<URL, FailedRegisteredTask> getFailedRegistered() {
        return failedRegistered;
    }

    ConcurrentMap<URL, FailedUnregisteredTask> getFailedUnregistered() {
        return failedUnregistered;
    }

    ConcurrentMap<Holder, FailedSubscribedTask> getFailedSubscribed() {
        return failedSubscribed;
    }

    ConcurrentMap<Holder, FailedUnsubscribedTask> getFailedUnsubscribed() {
        return failedUnsubscribed;
    }

    ConcurrentMap<Holder, FailedNotifiedTask> getFailedNotified() {
        return failedNotified;
    }

    @Override
    public void register(URL url) {
        super.register(url);
        removeFailedRegistered(url);
        removeFailedUnregistered(url);
        try {
            // Sending a registration request to the server side
            doRegister(url);
        } catch (Exception e) {
            Throwable t = e;
            addFailedRegistered(url);
        }
    }

    @Override
    public void unregister(URL url) {
        super.unregister(url);
        removeFailedRegistered(url);
        removeFailedUnregistered(url);
        try {
            // Sending a cancellation request to the server side
            doUnregister(url);
        } catch (Exception e) {
            Throwable t = e;

            // If the startup detection is opened, the Exception is thrown directly.
            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
//            boolean skipFailback = t instanceof SkipFailbackWrapperException;
//            if (check || skipFailback) {
//                if (skipFailback) {
//                    t = t.getCause();
//                }
//                throw new IllegalStateException("Failed to unregister " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
//            } else {
//                logger.error("Failed to unregister " + url + ", waiting for retry, cause: " + t.getMessage(), t);
//            }

            // Record a failed registration request to a failed list, retry regularly
            addFailedUnregistered(url);
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        logger.info("prepare to subscribe " + url);
        super.subscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // Sending a subscription request to the server side
            doSubscribe(url, listener);
        } catch (Exception e) {
            Throwable t = e;

            List<URL> urls = getCacheUrls(url);
            if (CollectionUtils.isNotEmpty(urls)) {
                notify(url, listener, urls);
                logger.error("Failed to subscribe " + url + ", Using cached list: " + urls + " from cache file: " + getUrl().getParameter(FILE_KEY, System.getProperty(USER_HOME) + "/fate-registry-" + url.getHost() + ".cache") + ", cause: " + t.getMessage(), t);
            } else {
                // If the startup detection is opened, the Exception is thrown directly.
                boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                        && url.getParameter(Constants.CHECK_KEY, true);
//                boolean skipFailback = t instanceof SkipFailbackWrapperException;
//                if (check || skipFailback) {
//                    if (skipFailback) {
//                        t = t.getCause();
//                    }
//                    throw new IllegalStateException("Failed to subscribe " + url + ", cause: " + t.getMessage(), t);
//                } else {
//                    logger.error("Failed to subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
//                }
            }

            // Record a failed registration request to a failed list, retry regularly
            addFailedSubscribed(url, listener);
        }
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // Sending a canceling subscription request to the server side
            doUnsubscribe(url, listener);
        } catch (Exception e) {
            Throwable t = e;

            // If the startup detection is opened, the Exception is thrown directly.
            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true);
//            boolean skipFailback = t instanceof SkipFailbackWrapperException;
//            if (check || skipFailback) {
//                if (skipFailback) {
//                    t = t.getCause();
//                }
//                throw new IllegalStateException("Failed to unsubscribe " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
//            } else {
//                logger.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
//            }

            // Record a failed registration request to a failed list, retry regularly
            addFailedUnsubscribed(url, listener);
        }
    }

    @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            doNotify(url, listener, urls);
        } catch (Exception t) {
            // Record a failed registration request to a failed list, retry regularly
            addFailedNotified(url, listener, urls);
            logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    protected void doNotify(URL url, NotifyListener listener, List<URL> urls) {
        super.notify(url, listener, urls);
    }

    @Override
    protected void recover() throws Exception {
        // register
        logger.info("prepare to recover registed......{}", getRegistered());

        Set<URL> recoverRegistered = new HashSet<URL>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (URL url : recoverRegistered) {
                addFailedRegistered(url);
            }
        }

        logger.info("prepare to recover registed.project.....{}", projectSets);

        Set<String>  subjectSets = new HashSet(this.projectSets);
        if(!subjectSets.isEmpty()) {
            subjectSets.forEach(project -> {
                try{
                this.addFailedSubscribedProjectTask(project);
            }catch(Exception e){
                    logger.error("recover addFailedSubscribedProjectTask error",e);

            }

            });
        }

        logger.info("prepare to recover subscribed......{}", getSubscribed());
        // subscribe
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<URL, Set<NotifyListener>>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    addFailedSubscribed(url, listener);
                }
            }
        }
        logger.info("recover over !!!!!!");
    }

    @Override
    public void destroy() {
        super.destroy();
        retryTimer.stop();
    }

    public abstract void doRegister(URL url);

    public abstract void doUnregister(URL url);

    public abstract void doSubscribe(URL url, NotifyListener listener);

    public abstract void doUnsubscribe(URL url, NotifyListener listener);

    static class Holder {

        private final URL url;

        private final NotifyListener notifyListener;

        Holder(URL url, NotifyListener notifyListener) {
            if (url == null || notifyListener == null) {
                throw new IllegalArgumentException();
            }
            this.url = url;
            this.notifyListener = notifyListener;
        }

        @Override
        public int hashCode() {
            return url.hashCode() + notifyListener.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Holder) {
                Holder h = (Holder) obj;
                return this.url.equals(h.url) && this.notifyListener.equals(h.notifyListener);
            } else {
                return false;
            }
        }
    }
}
