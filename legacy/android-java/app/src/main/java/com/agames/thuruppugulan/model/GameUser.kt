package com.agames.thuruppugulan.model

import io.realm.RealmObject

class GameUser : RealmObject() {
    var userName: String? = null
    var profilePicUrl: String? = null
    var coins: Long? = null
}