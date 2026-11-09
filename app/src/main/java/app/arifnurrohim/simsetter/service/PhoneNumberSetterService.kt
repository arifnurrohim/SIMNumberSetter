package app.arifnurrohim.simsetter.service

import android.os.Message
import com.android.internal.telephony.Phone
import com.android.internal.telephony.PhoneFactory
import app.arifnurrohim.simsetter.IPhoneNumberSetter

/**
 *  Proxy Service for calls to [Phone] methods to be called via the [IPhoneNumberSetter] service.
 *
 *  Dual SIM support: every call takes a `phoneId` (the physical SIM slot index) so the number can
 *  be read/written on any slot, instead of only the default phone.
 */
class PhoneNumberSetterService: IPhoneNumberSetter.Stub() {

    private fun getPhone(phoneId: Int): Phone? {
        val phone = try {
            PhoneFactory.getPhone(phoneId)
        } catch (e: Throwable) {
            null
        }
        if (phone != null) return phone
        val phones = try {
            PhoneFactory.getPhones()
        } catch (e: Throwable) {
            null
        }
        phones?.getOrNull(phoneId)?.let { return it }
        if (phoneId == 0) {
            return try {
                PhoneFactory.getDefaultPhone()
            } catch (e: Throwable) {
                null
            }
        }
        return null
    }

    override fun getPhoneCount(): Int {
        return try {
            PhoneFactory.getPhones()?.size ?: getDefaultPhoneCount()
        } catch (e: Throwable) {
            getDefaultPhoneCount()
        }
    }

    private fun getDefaultPhoneCount(): Int {
        return try {
            if (PhoneFactory.getDefaultPhone() != null) 1 else 0
        } catch (e: Throwable) {
            0
        }
    }

    override fun getLine1Number(phoneId: Int): String {
        val phone = getPhone(phoneId) ?: return ""
        return phone.line1Number ?: ""
    }

    override fun setLine1Number(phoneId: Int, number: String, onComplete: Message?): Boolean {
        val phone = getPhone(phoneId) ?: return false
        var tag = phone.line1AlphaTag
        if (tag.isNullOrEmpty()) {
            tag = "Voice Line ${phoneId + 1}"
        }
        return phone.setLine1Number(tag, number, onComplete)
    }

}
