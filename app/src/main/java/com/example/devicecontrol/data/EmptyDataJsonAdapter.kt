package com.example.devicecontrol.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

class EmptyDataJsonAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): EmptyData {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Unit>()
        } else {
            reader.skipValue()
        }
        return EmptyData()
    }
}
