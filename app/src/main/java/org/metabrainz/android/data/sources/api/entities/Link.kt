package org.metabrainz.android.data.sources.api.entities

class Link {
    var type: String? = null
    var url: Url? = null
    var isEnded = false
    val pageTitle: String
        get() {
            val resource = url!!.resource!!.trim { it <= ' ' }
            return resource.substring(resource.lastIndexOf("/") + 1)
        }
}