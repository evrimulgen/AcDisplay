/*
 * Copyright (C) 2014 AChep@xda <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.achep.acdisplay.notifications;

import java.util.ArrayList;

/**
 * Is a list of {@link OpenNotification notifications} with
 * an ability to easily add / replace / remove item from the list.
 *
 * @author Artem Chepurnoy
 * @see NotificationUtils#equals(OpenNotification, OpenNotification)
 */
final class NotificationList {

    /**
     * Default return value of {@link #push(OpenNotification)}
     * or {@link #remove(OpenNotification)} methods.
     */
    public static final int RESULT_DEFAULT = 0;

    private static final int EVENT_ADDED = 0;
    private static final int EVENT_CHANGED = 1;
    private static final int EVENT_REMOVED = 2;

    private ArrayList<OpenNotification> mList;
    private OnNotificationListChangedListener mListener;

    /**
     * Interface definition for a callback to be invoked
     * when a list of notifications has changed.
     */
    public interface OnNotificationListChangedListener {

        /**
         * Called when new notification was added to list.
         *
         * @param n newly added notification
         */
        public int onNotificationAdded(OpenNotification n);

        /**
         * Called when old notification was replaced with new one.
         *
         * @param n   newly added notification
         * @param old removed notification
         */
        public int onNotificationChanged(OpenNotification n, OpenNotification old);

        /**
         * Called when notification was removed from list.
         *
         * @param n removed notification
         */
        public int onNotificationRemoved(OpenNotification n);

    }

    public NotificationList(OnNotificationListChangedListener callback) {
        mListener = callback;
        mList = new ArrayList<>(10);
    }

    public int pushOrRemove(OpenNotification n, boolean push, boolean silently) {
        if (silently) {
            // Hide listener.
            OnNotificationListChangedListener l = mListener;
            mListener = null;

            // Perform action.
            pushOrRemove(n, push);

            // Restore listener.
            mListener = l;
            return RESULT_DEFAULT;
        }
        return pushOrRemove(n, push);
    }

    public int pushOrRemove(OpenNotification n, boolean push) {
        return push ? push(n) : remove(n);
    }

    /**
     * Adds or replaces existent notification to/of the list.
     *
     * @return {@link NotificationList.OnNotificationListChangedListener#onNotificationAdded(OpenNotification n)} or
     * {@link NotificationList.OnNotificationListChangedListener#onNotificationChanged(OpenNotification n, OpenNotification old)}
     */
    public int push(OpenNotification n) {
        final int index = indexOf(n);
        if (index == -1) {
            // Add new notification to the list.
            mList.add(n);
            return notifyListener(EVENT_ADDED, n, null);
        } else {
            // Replace old notification with new one.
            OpenNotification old = mList.get(index);
            mList.remove(index);
            mList.add(index, n);
            return notifyListener(EVENT_CHANGED, n, old);
        }
    }

    /**
     * Removes notification from the list.
     *
     * @return {@link NotificationList.OnNotificationListChangedListener#onNotificationRemoved(OpenNotification n)}
     * @see #push(OpenNotification n)
     */
    public int remove(OpenNotification n) {
        final int index = indexOf(n);
        if (index != -1) {
            mList.get(index).getNotificationData().stopLoading();
            mList.remove(index);
            return notifyListener(EVENT_REMOVED, n, null);
        }
        return RESULT_DEFAULT;
    }

    /**
     * <b>Do not operate on this list!</b>
     * Use this only for searching and getting notifications.
     *
     * @return link to primitive list of notifications.
     */
    public ArrayList<OpenNotification> list() {
        return mList;
    }

    public int indexOf(OpenNotification n) {
        final int size = mList.size();
        for (int i = 0; i < size; i++) {
            if (NotificationUtils.equals(n, mList.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Notifies listener about this event.
     *
     * @see #EVENT_ADDED
     * @see #EVENT_CHANGED
     * @see #EVENT_REMOVED
     */
    private int notifyListener(int event, OpenNotification n, OpenNotification old) {
        if (mListener == null) return RESULT_DEFAULT;
        switch (event) {
            case EVENT_ADDED:
                return mListener.onNotificationAdded(n);
            case EVENT_CHANGED:
                return mListener.onNotificationChanged(n, old);
            case EVENT_REMOVED:
                return mListener.onNotificationRemoved(n);
            default:
                throw new IllegalArgumentException();
        }
    }
}