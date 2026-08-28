package com.lifelab.core.session

import com.tencent.mmkv.MMKV

class AuthTokenStore {
    private val mmkv: MMKV = requireNotNull(MMKV.defaultMMKV()){
        "MMKV没初始化"
    }
    fun saveSession(
        token: String,
        userId: String,
        account: String,
    ){
        mmkv.encode(KEY_ACCESS_TOKEN, token)
        mmkv.encode(KEY_USER_ID, userId)
        mmkv.encode(KEY_ACCOUNT, account)
    }
    fun getAccessToken() : String?{
        return mmkv.decodeString(KEY_ACCESS_TOKEN)
    }

    fun clearAccessToken() {
        mmkv.removeValueForKey(KEY_ACCESS_TOKEN)
        mmkv.removeValueForKey(KEY_USER_ID)
        mmkv.removeValueForKey(KEY_ACCOUNT)
    }

    fun hasAccessToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    fun getUserId(): String? = mmkv.decodeString(KEY_USER_ID)

    fun getAccount(): String? = mmkv.decodeString(KEY_ACCOUNT)

    fun getLocalDataOwnerId(): String? =
        mmkv.decodeString(KEY_LOCAL_DATA_OWNER_ID)

    fun saveLocalDataOwnerId(userId: String) {
        mmkv.encode(KEY_LOCAL_DATA_OWNER_ID, userId)
    }

    companion object{
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_LOCAL_DATA_OWNER_ID = "local_data_owner_id"
    }

}
