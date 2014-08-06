/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andrew.apollo.widgets;

/**
 * An interface that defines the breadcrumb operations.
 */
public interface Breadcrumb {

    /**
     * Method that changes the path of the breadcrumb.
     *
     * @param newPath The new path
     * @param isChrooted Using of chrooted environment
     */
    void changeBreadcrumbPath(final String newPath, boolean isChrooted);

    /**
     * Method that adds a new breadcrumb listener.
     *
     * @param listener The breadcrumb listener to add
     */
    void addBreadcrumbListener(BreadcrumbView.BreadcrumbListener listener);

    /**
     * Method that adds an active breadcrumb listener.
     *
     * @param listener The breadcrumb listener to remove
     */
    void removeBreadcrumbListener(BreadcrumbView.BreadcrumbListener listener);
}
