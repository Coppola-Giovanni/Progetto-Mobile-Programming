package com.sudokuMaster.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


internal val Context.userPreferencesDataStore : DataStore<UserPreferences>  by dataStore (
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer
)

private object UserPreferencesSerializer : Serializer<UserPreferences>{

    override val defaultValue: UserPreferences
        get() = UserPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return UserPreferences.parseFrom(input)
        }catch (exception: InvalidProtocolBufferException){
            throw CorruptionException("Cannot read user preferences proto.", exception)
        }
    }
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) = t.writeTo(output)
}
