package com.czbix.v2ex.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
        private val creators: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val found = creators[modelClass] ?: creators.entries.find {
            modelClass.isAssignableFrom(it.key)
        }?.value

        requireNotNull(found) {
            "Unknown model class $modelClass"
        }

        try {
            @Suppress("UNCHECKED_CAST")
            return found.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}