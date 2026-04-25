package com.xatruch.pos.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.xatruch.pos.data.AppDatabase
import com.xatruch.pos.data.entity.BusinessData
import com.xatruch.pos.data.repository.FirestoreRepository
import com.xatruch.pos.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val businessDao = AppDatabase.getDatabase(application).businessDao()
    private val firestoreRepository = FirestoreRepository()
    val businessData: LiveData<BusinessData?> = businessDao.getBusinessData().asLiveData()

    private val _saveStatus = MutableLiveData<Result<Unit>>()
    val saveStatus: LiveData<Result<Unit>> = _saveStatus

    fun saveBusinessData(businessData: BusinessData, newLogoUri: Uri? = null) {
        viewModelScope.launch {
            try {
                var updatedData = businessData
                
                // Si hay un nuevo logo local, lo convertimos a Base64
                if (newLogoUri != null) {
                    val base64Image = withContext(Dispatchers.IO) {
                        ImageUtils.uriToBase64(getApplication(), newLogoUri)
                    }
                    if (base64Image != null) {
                        updatedData = businessData.copy(logoUri = base64Image)
                    }
                }

                businessDao.insertOrUpdate(updatedData)
                firestoreRepository.saveBusinessData(updatedData)
                _saveStatus.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _saveStatus.postValue(Result.failure(e))
                android.util.Log.e("SettingsViewModel", "Error saving business data", e)
            }
        }
    }
}
