package ro.cluj.totemz.firebase

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by mihai on 7/2/2017.
 */
val firebaseModule = Kodein.Module {
    bind <FirebaseDatabase>() with singleton { FirebaseDatabase.getInstance() }
    bind<FirebaseAuth>() with singleton { FirebaseAuth.getInstance() }
}