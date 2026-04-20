package com.xatruch.pos.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.BusinessData
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val businessDao = AppDatabase.getDatabase(application).businessDao()
    val businessData: LiveData<BusinessData?> = businessDao.getBusinessData().asLiveData()

    fun saveBusinessData(businessData: BusinessData) {
        viewModelScope.launch {
            businessDao.insertOrUpdate(businessData)
        }
    }
}