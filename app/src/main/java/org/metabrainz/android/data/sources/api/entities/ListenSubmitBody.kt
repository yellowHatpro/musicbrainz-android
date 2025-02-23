package org.metabrainz.android.data.sources.api.entities

import com.google.gson.annotations.SerializedName
import java.util.*

class ListenSubmitBody {
    @SerializedName("listen_type")
    var listenType: String? = null
    @JvmField
    var payload: MutableList<Payload> = ArrayList()
    fun getPayload(): List<Payload> {
        return payload
    }

    fun setPayload(payload: MutableList<Payload>) {
        this.payload = payload
    }

    fun addListen(payload: Payload) {
        this.payload.add(payload)
    }

    fun addListen(timestamp: Long, metadata: ListenTrackMetadata) {
        payload.add(Payload(timestamp, metadata))
    }

    override fun toString(): String {
        return "ListenSubmitBody{" +
                "listenType='" + listenType + '\'' +
                ", payload=" + payload +
                '}'
    }

    class Payload(@field:SerializedName("listened_at") var timestamp: Long, @field:SerializedName("track_metadata") var metadata: ListenTrackMetadata) {

        override fun toString(): String {
            return "Payload{" +
                    "timestamp=" + timestamp +
                    ", metadata=" + metadata +
                    '}'
        }
    }
}