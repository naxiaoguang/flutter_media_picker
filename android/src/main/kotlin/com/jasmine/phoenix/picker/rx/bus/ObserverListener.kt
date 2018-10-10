package com.jasmine.phoenix.picker.rx.bus

import com.jasmine.phoenix.core.model.MediaEntity
import com.jasmine.phoenix.picker.model.MediaFolder

interface ObserverListener {
    fun observerUpFoldersData(folders: List<MediaFolder>)

    fun observerUpSelectsData(selectMediaEntities: List<MediaEntity>)
}
