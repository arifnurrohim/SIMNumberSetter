package app.arifnurrohim.simsetter.ui.screens.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.arifnurrohim.simsetter.R
import app.arifnurrohim.simsetter.repositories.PermissionRepository
import app.arifnurrohim.simsetter.repositories.RootRepository
import app.arifnurrohim.simsetter.repositories.ServiceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class MainViewModel : ViewModel() {

    abstract val state: StateFlow<State>
    abstract val number: StateFlow<String?>
    abstract fun onNumberChanged(newNumber: String)
    abstract fun onPhoneSelected(phoneId: Int)
    abstract fun onSaveClicked()
    abstract fun onMenuItemClicked(context: Context, id: Int)

    sealed class State {
        data class Loading(val loadType: LoadType) : State()
        data class Loaded(
            internal val number: String,
            val phoneId: Int,
            val phoneCount: Int
        ) : State()
        data class Error(val errorType: ErrorType) : State()
    }

    enum class ErrorType(@StringRes val messageRes: Int) {
        NO_ROOT(R.string.error_no_root),
        NO_PERMISSION(R.string.error_no_permission),
        NO_XPOSED(R.string.error_no_xposed),
        NO_SIM(R.string.error_no_sim),
        SAVE_FAILED(R.string.error_save_failed)
    }

    enum class LoadType(@StringRes val contentRes: Int) {
        LOADING(R.string.loading_loading),
        SAVING(R.string.loading_saving)
    }

}

class MainViewModelImpl(
    private val rootRepository: RootRepository,
    private val permissionRepository: PermissionRepository,
    private val serviceRepository: ServiceRepository
) : MainViewModel() {

    companion object {
        private const val URL_GITHUB = "https://github.com/KieronQuinn/SIMNumberSetter"
        private const val URL_DONATE = "https://kieronquinn.co.uk/redirect/SNS/donate"
    }

    private val customNumber = MutableStateFlow<String?>(null)
    private val selectedPhoneId = MutableStateFlow(0)

    override val state = MutableStateFlow<State>(State.Loading(LoadType.LOADING))
    override val number = combine(customNumber, state) { n, s ->
        when {
            n != null -> n
            s is State.Loaded -> s.number
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override fun onNumberChanged(newNumber: String) {
        viewModelScope.launch {
            customNumber.emit(newNumber)
        }
    }

    override fun onPhoneSelected(phoneId: Int) {
        if (selectedPhoneId.value == phoneId) return
        viewModelScope.launch {
            selectedPhoneId.emit(phoneId)
            customNumber.emit(null)
            load()
        }
    }

    override fun onSaveClicked() {
        save(number.value ?: "")
    }

    override fun onMenuItemClicked(context: Context, id: Int) {
        val url = when(id) {
            R.id.menu_github -> URL_GITHUB
            R.id.menu_donate -> URL_DONATE
            else -> return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }catch (e: ActivityNotFoundException) {
            //Nothing can be done
        }
    }

    private fun load() = viewModelScope.launch {
        state.emit(State.Loading(LoadType.LOADING))
        if (!rootRepository.isRooted()) {
            state.emit(State.Error(ErrorType.NO_ROOT))
            return@launch
        }
        if (!permissionRepository.grantDumpPermission()) {
            state.emit(State.Error(ErrorType.NO_PERMISSION))
            return@launch
        }
        val phoneCount = try {
            serviceRepository.runWithService {
                it.phoneCount
            }
        } catch (e: Exception) {
            null
        } ?: run {
            state.emit(State.Error(ErrorType.NO_XPOSED))
            return@launch
        }
        if (phoneCount <= 0) {
            state.emit(State.Error(ErrorType.NO_SIM))
            return@launch
        }
        val phoneId = selectedPhoneId.value.coerceIn(0, phoneCount - 1)
        selectedPhoneId.emit(phoneId)
        val number = try {
            serviceRepository.runWithService {
                it.getLine1Number(phoneId)
            }
        } catch (e: Exception) {
            null
        } ?: run {
            state.emit(State.Error(ErrorType.NO_XPOSED))
            return@launch
        }
        state.emit(State.Loaded(number, phoneId, phoneCount))
    }

    private fun save(number: String) = viewModelScope.launch {
        state.emit(State.Loading(LoadType.SAVING))
        val phoneId = selectedPhoneId.value
        val result = try {
            serviceRepository.runWithService {
                it.setLine1Number(phoneId, number, null)
            }
        } catch (e: Exception) {
            null
        } ?: run {
            state.emit(State.Error(ErrorType.NO_XPOSED))
            return@launch
        }
        customNumber.emit(null)
        delay(1000L)
        if (result) {
            load()
        } else {
            state.emit(State.Error(ErrorType.SAVE_FAILED))
        }
    }

    init {
        load()
    }

    override fun onCleared() {
        serviceRepository.unbindServiceIfNeeded()
        super.onCleared()
    }

}
