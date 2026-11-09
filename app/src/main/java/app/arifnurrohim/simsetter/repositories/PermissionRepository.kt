package app.arifnurrohim.simsetter.repositories

import android.content.Context
import android.content.pm.PackageManager
import app.arifnurrohim.simsetter.BuildConfig
import app.arifnurrohim.simsetter.repositories.PermissionRepository.Companion.PERMISSION_DUMP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PermissionRepository {

    companion object {
        internal const val PERMISSION_DUMP = "android.permission.DUMP"
    }

    suspend fun grantDumpPermission(): Boolean

}

class PermissionRepositoryImpl(
    private val context: Context,
    private val rootRepository: RootRepository
): PermissionRepository {

    private fun hasDumpPermission(): Boolean {
        return context.checkCallingOrSelfPermission(PERMISSION_DUMP) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun runGrantCommand() {
        rootRepository.runRootCommand(
            "pm grant ${BuildConfig.APPLICATION_ID} $PERMISSION_DUMP"
        )
    }

    override suspend fun grantDumpPermission(): Boolean {
        return withContext(Dispatchers.IO) {
            if(!hasDumpPermission()){
                runGrantCommand()
                hasDumpPermission()
            }else true
        }
    }

}